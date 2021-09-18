package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.sivakasi.papco.jobflow.*
import com.sivakasi.papco.jobflow.data.PaperDetail
import com.sivakasi.papco.jobflow.databinding.DialogPaperDetailBinding
import com.sivakasi.papco.jobflow.extensions.*
import com.sivakasi.papco.jobflow.util.FormValidator
import com.sivakasi.papco.jobflow.util.toast


class DialogPaperDetail : DialogFragment() {

    companion object {
        const val TAG = "papco.jobFlow.screens.managePrintOrder.dialogPaperDetail"
        private const val KEY_PAPER_DETAIL = "paper:detail:to:edit"
        private const val KEY_MODE_EDIT = "edit:mode"
        private const val KEY_FIRST_RUN = "first:run"
        private const val KEY_EDIT_INDEX = "edit:index"

        fun getInstance(
            editIndex: Int = -1,
            paperDetailToEdit: PaperDetail? = null
        ): DialogPaperDetail {

            val args = Bundle()
            args.putInt(KEY_EDIT_INDEX, editIndex)
            if (paperDetailToEdit != null) {
                args.putBoolean(KEY_MODE_EDIT, true)
                args.putParcelable(KEY_PAPER_DETAIL, paperDetailToEdit)
            }
            return DialogPaperDetail().also {
                it.arguments = args
            }

        }
    }

    private var _viewBinding: DialogPaperDetailBinding? = null
    private val viewBinding: DialogPaperDetailBinding
        get() = _viewBinding!!

    private val callback: DialogPaperDetailListener? by lazy {
        getTheCallBack()
    }

    private var isFirstRun = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFirstRun = savedInstanceState?.getBoolean(KEY_FIRST_RUN) ?: true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = DialogPaperDetailBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        loadPaperDetailIfEditMode()
    }

    override fun onResume() {
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        super.onResume()
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

        with(viewBinding) {
            txtHeight.clearErrorOnTextChange()
            txtWidth.clearErrorOnTextChange()
            txtGsm.clearErrorOnTextChange()
            txtSubstrate.clearErrorOnTextChange()
            txtSheets.clearErrorOnTextChange()
        }

        viewBinding.txtExpression.setOnFocusChangeListener { thisView, hasFocus ->

            if (!hasFocus) {
                val enteredExpression = (thisView as EditText).text.toString().trim()
                if (enteredExpression.isNotBlank()) {
                    /*
                    Try to evaluate the expression.
                    Surrounding inside the try..catch block to handle exception cases like user purposely
                    entered a very very long number string inside the expression field which will throw
                    Numeric exception like calculated total sheets exceeds Int or even long
                     */
                    try {
                        val expressionEvaluator = SheetsExpressionChecker(enteredExpression)
                        if (expressionEvaluator.isValid)
                            viewBinding.txtSheets.setText(
                                expressionEvaluator.totalSheets().toString()
                            )
                        else
                            viewBinding.txtSheets.setText("0")
                    } catch (e: Exception) {
                        viewBinding.txtSheets.setText("0")
                    }
                }
            }
        }

        viewBinding.btnSave.setOnClickListener {
            onSaveClicked()
        }

    }

    private fun loadPaperDetailIfEditMode() {

        if (!isFirstRun || !isEditMode())
            return

        val paperDetail = getPaperDetailFromParcel()
        with(viewBinding) {
            txtHeight.setText(paperDetail.height.toString())
            txtWidth.setText(paperDetail.width.toString())
            txtGsm.setText(paperDetail.gsm.toString())
            txtSheets.setText(paperDetail.sheets.toString())
            txtSubstrate.setText(paperDetail.name)
            if (paperDetail.partyPaper)
                radioButtonPartyOwn.isChecked = true
            else
                radioButtonOurOwn.isChecked = true
        }

    }


    private fun validateForm(): Boolean {

        val validator = FormValidator()
        val errorMsg = getString(R.string.required_field)

        with(viewBinding) {
            validator.validate(txtHeight.validateForGreaterThanZero(errorMsg))
            validator.validate(txtWidth.validateForGreaterThanZero(errorMsg))
            validator.validate(txtGsm.validateForGreaterThanZero(errorMsg))
            validator.validate(txtSubstrate.validateForNonBlank(errorMsg))
            validator.validate(txtSheets.validateForGreaterThanZero(errorMsg))
        }

        return validator.isValid()

    }

    private fun onSaveClicked() {
        if (validateForm()) {
            if (tryToDispatchResult(createNewPaperDetail()))
                dismiss()
        }
    }

    private fun createNewPaperDetail(): PaperDetail {
        val result = PaperDetail()
        with(viewBinding) {
            result.partyPaper = radioButtonPartyOwn.isChecked
            result.height = txtHeight.numberDecimal()
            result.width = txtWidth.numberDecimal()
            result.gsm = txtGsm.number()
            result.name = txtSubstrate.text.toString()
            result.sheets = txtSheets.number()
        }
        return result
    }


    private fun tryToDispatchResult(result: PaperDetail): Boolean {

        return if (callback == null)
            false
        else {
            callback?.onSubmitPaperDetail(getEditIndex(), result)
            true
        }
    }

    private fun getTheCallBack(): DialogPaperDetailListener? {

        var callback: DialogPaperDetailListener? = null

        try {
            callback = when {
                parentFragment != null -> {
                    parentFragment as DialogPaperDetailListener
                }
                activity != null -> {
                    activity as DialogPaperDetailListener
                }
                else -> {
                    toast("Should be called either from Activity or fragment")
                    null
                }
            }
        } catch (exception: Exception) {
            toast("Caller should implement Text Input Listener")
        }

        return callback
    }

    private fun isEditMode(): Boolean =
        arguments?.getBoolean(KEY_MODE_EDIT) ?: false

    private fun getEditIndex(): Int =
        arguments?.getInt(KEY_EDIT_INDEX) ?: -1

    private fun getPaperDetailFromParcel(): PaperDetail =
        arguments?.getParcelable(KEY_PAPER_DETAIL)
            ?: throw IllegalStateException("Cannot find the paper details in the parcel")

    interface DialogPaperDetailListener {
        fun onSubmitPaperDetail(editIndex: Int, paperDetail: PaperDetail)
    }

}