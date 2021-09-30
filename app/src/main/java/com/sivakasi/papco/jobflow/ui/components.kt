package com.sivakasi.papco.jobflow.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.RelocationRequester
import androidx.compose.ui.layout.relocationRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

@ExperimentalComposeUiApi
@Composable
fun JobFlowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onTabPressed: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: String? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    error: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors()
) {
    Column {

        val relocationRequester = remember { RelocationRequester() }
        val coroutineScope = rememberCoroutineScope()

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .relocationRequester(relocationRequester)
                .onFocusChanged {
                    if (it.isFocused)
                        coroutineScope.launch {
                            relocationRequester.bringIntoView()
                        }
                }
                .onKeyEvent {
                    if (it.key.keyCode == Key.Tab.keyCode) {
                        onTabPressed()
                        true
                    } else
                        false
                },
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            label = if (label != null) {
                { Text(label) }
            } else null,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = error != null,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            interactionSource = interactionSource,
            shape = shape,
            colors = colors
        )

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption
            )
        }
    }
}

@ExperimentalComposeUiApi
@Preview
@Composable
private fun JobFlowTextFieldPreview() {
    JobFlowTheme {
        Surface {
            JobFlowTextField(
                value = "m.sundaravel@gmail.com",
                onValueChange = {},
                singleLine = true,
                label = "Email",
                error = "*Invalid email",
                onTabPressed = {}
            )
        }
    }
}