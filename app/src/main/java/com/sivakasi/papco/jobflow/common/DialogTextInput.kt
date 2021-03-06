package com.sivakasi.papco.jobflow.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.sivakasi.papco.jobflow.databinding.DialogTextInputBinding

class DialogTextInput : ResultDialogFragment() {

    companion object {
        const val TAG = "dialogTextInput"
        private const val KEY_HEADING = "key:heading"
        private const val KEY_DEFAULT_TEXT = "key:defaultText"
        private const val KEY_ACCEPT_BLANK = "key:accept:blank"
        private const val KEY_CODE = "key:code"

        fun getInstance(
            title: String,
            defaultText: String = "",
            code: Int = -1,
            acceptBlank: Boolean = true
        ): DialogTextInput {

            val args = Bundle()
            args.putString(KEY_HEADING, title)
            args.putString(KEY_DEFAULT_TEXT, defaultText)
            args.putInt(KEY_CODE, code)
            args.putBoolean(KEY_ACCEPT_BLANK, acceptBlank)
            return DialogTextInput().apply {
                arguments = args
            }

        }
    }

    private var _viewBinding: DialogTextInputBinding? = null
    private val viewBinding: DialogTextInputBinding
        get() = _viewBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = DialogTextInputBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        loadFromArgumentIfNecessary()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun initViews() {
        viewBinding.btnSave.setOnClickListener {
            if (dispatchResult(viewBinding.txtRemarks.text.toString(), getCode()))
                dismiss()
        }

        viewBinding.txtRemarks.addTextChangedListener {
            if (!isBlankAccepted())
                viewBinding.btnSave.isEnabled = it!!.isNotBlank()
        }

        if(!isBlankAccepted() && getDefaultText().isBlank())
            viewBinding.btnSave.isEnabled=false
    }

    private fun loadFromArgumentIfNecessary() {

        viewBinding.lblHeading.text = getTitle()

        if (!firstRun)
            return

        viewBinding.txtRemarks.setText(getDefaultText())

    }

    private fun getTitle(): String =
        arguments?.getString(KEY_HEADING) ?: ""

    private fun getDefaultText(): String =
        arguments?.getString(KEY_DEFAULT_TEXT) ?: ""

    private fun getCode(): Int =
        arguments?.getInt(KEY_CODE, -1) ?: -1

    private fun isBlankAccepted(): Boolean =
        arguments?.getBoolean(KEY_ACCEPT_BLANK) ?: true
}