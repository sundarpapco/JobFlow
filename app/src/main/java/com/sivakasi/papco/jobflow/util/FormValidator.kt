package com.sivakasi.papco.jobflow.util

/*A simple utility class which maintains a boolean. Once we assign false,
then we cannot assign true to it again. Used to validate many boolean return functions in sequence.
If any one of the function returned false, then the form validator will return false after validating
all the fields

Example:

val validator=FormValidator()
    .validate(someNameValidationFunctionReturningTrue())
    .validate(someAgeValidatingFunctionReturningFalse())
    .validate(someHeightValidatingFunctionReturningTrue())

validator.isValid() // Will return false
*/

class FormValidator {

    private var validity:Boolean=true
    set(value) {
        field = if(field)
            value
        else
            false
    }

    fun validate(boolean:Boolean):FormValidator{
        validity=boolean
        return this
    }

    fun isValid()=validity

}