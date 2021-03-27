package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.clearErrorOnTextChange
import com.sivakasi.papco.jobflow.databinding.DialogRunningTimeBinding
import com.sivakasi.papco.jobflow.number
import com.sivakasi.papco.jobflow.util.Duration
import com.sivakasi.papco.jobflow.util.FormValidator
import com.sivakasi.papco.jobflow.util.toast
import com.wajahatkarim3.easyvalidation.core.rules.GreaterThanOrEqualRule
import com.wajahatkarim3.easyvalidation.core.rules.LessThanOrEqualRule
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator

class DialogRunningTime : DialogFragment() {

    companion object {
        const val TAG = "papco.jobFlow.screens.managePrintOrder.dialogRunningTime"
        private const val KEY_HOURS = "running:time:hours"
        private const val KEY_MINUTES = "running:time:minutes"
        private const val KEY_FIRST_RUN = "first:run"
        private const val KEY_HAS_SPOT_COLOURS="has:spot:colours"


        fun getInstance(duration: Duration? = null,hasSpotColours: Boolean=false): DialogRunningTime {

            val args = Bundle()

            if (duration != null) {
                args.putBoolean(KEY_HAS_SPOT_COLOURS,hasSpotColours)
                args.putInt(KEY_HOURS, duration.hours)
                args.putInt(KEY_MINUTES, duration.minutes)
            }
            return DialogRunningTime().also {
                it.arguments = args
            }

        }
    }

    private var _viewBinding: DialogRunningTimeBinding? = null
    private val viewBinding: DialogRunningTimeBinding
        get() = _viewBinding!!

    private val callback: DialogRunningTimeListener? by lazy {
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
        _viewBinding = DialogRunningTimeBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        loadDurationIfEditMode()
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
            txtHours.clearErrorOnTextChange()
            txtMinutes.clearErrorOnTextChange()
            txtExpression.clearErrorOnTextChange()
        }

        viewBinding.btnSave.setOnClickListener {
            onSaveClicked()
        }

    }

    private fun loadDurationIfEditMode() {

        val duration = getDuration()
        if (!isFirstRun || duration == null)
            return

        with(viewBinding) {
            chkboxSpotColours.isChecked=hasSpotColours()
            txtHours.setText(duration.hours.toString())
            txtMinutes.setText(duration.minutes.toString())
            txtExpression.setText("")
        }

    }


    private fun validateForm(): Boolean {

        val validator = FormValidator()

        validator.validate(validateHours())
        validator.validate(validateMinutes())
        validator.validate(validateExpressionField())

        return validator.isValid()
    }

    private fun validateHours(): Boolean {

        val isValid = if (viewBinding.txtExpression.text.toString().isNotBlank())
            true
        else
            viewBinding.txtHours.validator()
                .addRule(GreaterThanOrEqualRule(0))
                .addRule(LessThanOrEqualRule(1000))
                .check()

        if (!isValid)
            viewBinding.txtHours.error = getString(R.string.error_invalid_hours)

        return isValid
    }

    private fun validateMinutes(): Boolean {

        val isValid = if (viewBinding.txtMinutes.text.toString().isNotBlank())
            true
        else
            viewBinding.txtMinutes.validator()
                .addRule(GreaterThanOrEqualRule(0))
                .addRule(LessThanOrEqualRule(59))
                .check()

        if (!isValid)
            viewBinding.txtMinutes.error = getString(R.string.error_invalid_minutes)

        return isValid
    }

    private fun validateExpressionField(): Boolean {

        val expression = viewBinding.txtExpression.text.toString()
        val isValid = if (expression.isBlank()) {
            val hours = viewBinding.txtHours.number(0)
            val minutes = viewBinding.txtMinutes.number(0)
            hours + minutes != 0
        } else
            true

        if (!isValid)
            viewBinding.txtExpression.error = getString(R.string.error_invalid_expression)

        return isValid
    }


    private fun onSaveClicked() {
        if (validateForm()) {
            if (tryToDispatchResult())
                dismiss()
        }
    }


    private fun tryToDispatchResult(): Boolean {

        val expression=viewBinding.txtExpression.text.toString().trim()
        val duration:Duration
        val spotColours:Boolean

        if(expression.isBlank()) {
            duration = readDurationFromFields()
            spotColours=viewBinding.chkboxSpotColours.isChecked
        }else{
            val patternChecker=PatternChecker(expression)
            if(!patternChecker.isValid) {
                viewBinding.txtExpression.error=getString(R.string.error_invalid_expression)
                return false
            }
            duration=patternChecker.totalTime()
            spotColours=patternChecker.hasExtraColour
        }

        return if (callback == null)
            false
        else {
            callback?.onSubmitRunningTime(duration,spotColours)
            true
        }
    }

    private fun readDurationFromFields():Duration{
        val hours=viewBinding.txtHours.number(0)
        val minutes=viewBinding.txtMinutes.number(0)
        return Duration(hours,minutes)
    }

    private fun getTheCallBack(): DialogRunningTimeListener? {

        var callback: DialogRunningTimeListener? = null

        try {
            callback = when {
                parentFragment != null -> {
                    parentFragment as DialogRunningTimeListener
                }
                activity != null -> {
                    activity as DialogRunningTimeListener
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

    private fun getDuration(): Duration? {

        return arguments?.let {
            val duration = Duration()
            duration.hours = it.getInt(KEY_HOURS, 0)
            duration.minutes = it.getInt(KEY_MINUTES, 0)
            duration
        }

    }

    private fun hasSpotColours():Boolean=
        arguments?.getBoolean(KEY_HAS_SPOT_COLOURS,false) ?: false

    interface DialogRunningTimeListener {
        fun onSubmitRunningTime(runningTime: Duration, hasSpotColours: Boolean)
    }

}