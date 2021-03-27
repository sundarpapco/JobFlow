package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.clearErrorOnTextChange
import com.sivakasi.papco.jobflow.common.ResultDialogFragment
import com.sivakasi.papco.jobflow.data.Lamination
import com.sivakasi.papco.jobflow.databinding.DialogLaminationBinding
import com.sivakasi.papco.jobflow.number
import com.wajahatkarim3.easyvalidation.core.rules.GreaterThanRule
import com.wajahatkarim3.easyvalidation.core.rules.ValidNumberRule
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator

class DialogLamination : ResultDialogFragment() {

    companion object {

        const val TAG="DialogLamination:TAG"
        private const val KEY_FIRST_RUN = "first:run"
        private const val KEY_LAMINATION_TYPE = "lamination:type"
        private const val KEY_MICRON = "micron"
        private const val KEY_REMARKS = "remarks"
        private const val KEY_CODE = "key:code"

        fun getInstance(lamination: Lamination? = null, code: Int = -1): DialogLamination {

            val args = Bundle()
            args.putInt(KEY_CODE, code)
            if (lamination != null) {
                args.putInt(KEY_LAMINATION_TYPE, lamination.material)
                args.putInt(KEY_MICRON, lamination.micron)
                args.putString(KEY_REMARKS, lamination.remarks)
            } else {
                args.putInt(KEY_LAMINATION_TYPE, Lamination.MATERIAL_PVC)
                args.putInt(KEY_MICRON, 7)
                args.putString(KEY_REMARKS, "Hello")
            }
            return DialogLamination().also {
                it.arguments = args
            }

        }


    }

    private var _viewBinding: DialogLaminationBinding? = null
    private val viewBinding: DialogLaminationBinding
        get() = _viewBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            firstRun = it.getBoolean(KEY_FIRST_RUN, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = DialogLaminationBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_FIRST_RUN, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun initViews() {
        if (firstRun) {
            loadLamination(getLamination())
            firstRun = false
        }

        viewBinding.txtMicron.clearErrorOnTextChange()

        viewBinding.btnSave.setOnClickListener {
            if(validateMicron())
                if(dispatchResult(createLamination(),getCode()))
                    dismiss()
        }
    }

    private fun loadLamination(lamination: Lamination) {

        if(!firstRun)
            return


        when (lamination.material) {

            Lamination.MATERIAL_PVC -> {
                viewBinding.radioPvc.isChecked = true
            }

            Lamination.MATERIAL_BOPP -> {
                viewBinding.radioBopp.isChecked = true
            }

            Lamination.MATERIAL_MATT -> {
                viewBinding.radioMatt.isChecked = true
            }

            else -> {
                throw IllegalStateException("Invalid lamination type found")
            }
        }

        viewBinding.txtMicron.setText(lamination.micron.toString())
        viewBinding.txtRemarks.setText(lamination.remarks)

        firstRun=false
    }


    private fun getLamination(): Lamination {

        val lamination = Lamination()

        arguments?.let { args ->
            lamination.material = args.getInt(KEY_LAMINATION_TYPE)
            lamination.micron = args.getInt(KEY_MICRON)
            lamination.remarks = args.getString(KEY_REMARKS, "")
        } ?: throw IllegalArgumentException("Lamination argument not found in the bundle")

        return lamination

    }

    private fun createLamination(): Lamination {

        val result = Lamination()
        when (viewBinding.radioGroupFilm.checkedRadioButtonId) {

            viewBinding.radioPvc.id -> {
                result.material = Lamination.MATERIAL_PVC
            }

            viewBinding.radioBopp.id -> {
                result.material = Lamination.MATERIAL_BOPP
            }

            viewBinding.radioMatt.id -> {
                result.material = Lamination.MATERIAL_MATT
            }

            else -> {
                throw IllegalStateException("Invalid lamination type in UI")
            }

        }

        result.micron = viewBinding.txtMicron.number()
        result.remarks = viewBinding.txtRemarks.text.toString()
        return result
    }


    private fun validateMicron(): Boolean {

        return viewBinding.txtMicron.validator()
            .addRule(ValidNumberRule())
            .addRule(GreaterThanRule(0))
            .addErrorCallback {
                viewBinding.txtMicron.error = getString(R.string.error_invalid_micron)
            }
            .check()
    }


    private fun getCode(): Int =
        arguments?.getInt(KEY_CODE, -1) ?: -1


}