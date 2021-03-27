package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.common.ResultDialogFragment
import com.sivakasi.papco.jobflow.data.Binding
import com.sivakasi.papco.jobflow.databinding.DialogBindingBinding

class DialogBinding : ResultDialogFragment() {

    private var _viewBinding: DialogBindingBinding? = null
    private val viewBinding: DialogBindingBinding
        get() = _viewBinding!!

    companion object {

        const val TAG = "tag:dialog:binding"
        private const val KEY_BINDING_TYPE = "key:binding:type"
        private const val KEY_REMARKS = "key:binding:remarks"
        private const val KEY_CODE = "key:dialog:code"

        fun getInstance(
            bindingType: Int = Binding.TYPE_SADDLE_STITCH,
            remarks: String = "",
            code:Int=-1
        ): DialogBinding {

            val args = Bundle().apply {
                putInt(KEY_BINDING_TYPE, bindingType)
                putString(KEY_REMARKS, remarks)
                putInt(KEY_CODE,code)
            }

            return DialogBinding().apply {
                arguments = args
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding= DialogBindingBinding.inflate(inflater,container,false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        loadBindingIfNecessary()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding=null
    }

    private fun initViews(){
        viewBinding.btnSave.setOnClickListener {
            onSaveButtonClicked()
        }
    }

    private fun onSaveButtonClicked(){
        if(dispatchResult(createBinding(),getCode()))
            dismiss()
    }

    private fun loadBindingIfNecessary() {

        if(!firstRun)
            return

        val binding=getBinding()

        when(binding.type){

            Binding.TYPE_SADDLE_STITCH-> viewBinding.radioBtnSaddle.isChecked=true
            Binding.TYPE_PERFECT->viewBinding.radioBtnPerfect.isChecked=true
            Binding.TYPE_CASE->viewBinding.radioBtnCaseBinding.isChecked=true
            else->error("Invalid binding type found while initializing binding dialog")
        }

        viewBinding.txtRemarks.setText(binding.remarks)
    }

    private fun createBinding():Binding{

        val result=Binding()

        when(viewBinding.radioGroupBindingType.checkedRadioButtonId){
            R.id.radio_btn_saddle -> result.type=Binding.TYPE_SADDLE_STITCH
            R.id.radio_btn_perfect -> result.type=Binding.TYPE_PERFECT
            R.id.radio_btn_case_binding -> result.type=Binding.TYPE_CASE
        }
        result.remarks=viewBinding.txtRemarks.text.toString().trim()
        return result

    }

    private fun getBinding(): Binding {

        return Binding().apply {
            type = arguments?.getInt(KEY_BINDING_TYPE)
                ?: error("Binding type argument not found on dialog")
            remarks =
                arguments?.getString(KEY_REMARKS) ?: error("Remark argument not found on dialog")
        }
    }

    private fun getCode():Int=
        arguments?.getInt(KEY_CODE) ?: error("Dialog code not found in argument")
}