package com.sivakasi.papco.jobflow

import android.text.Editable
import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

private class ClearErrorTextWatcher(private val textInputLayout:TextInputLayout) : AbstractTextWatcher() {

    init {
        textInputLayout.errorIconDrawable=null
    }

    override fun afterTextChanged(p0: Editable?) {
        textInputLayout.error=null
    }
}


fun TextInputEditText.clearErrorOnTextChange(){

    addTextChangedListener(ClearErrorTextWatcher(this.parent.parent as TextInputLayout))

}