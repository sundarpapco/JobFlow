package com.sivakasi.papco.jobflow.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.RelocationRequester
import androidx.compose.ui.layout.relocationRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
@Composable
fun JobFlowTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
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
    singleLine: Boolean = true,
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
@Composable
fun SelectableTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
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

@Composable
fun JobFlowTopBar(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.surface.copy(0.99f),
    contentColor: Color = MaterialTheme.colors.onSurface,
    elevation: Dp = AppBarDefaults.TopAppBarElevation
) {
    TopAppBar(
        backgroundColor = backgroundColor,
        modifier = modifier,
        navigationIcon = navigationIcon,
        title = {
            Column {
                Text(
                    text = title,
                    style = LocalTextStyle.current
                )

                if (subtitle != null)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.subtitle2
                    )
            }
        },
        contentColor = contentColor,
        elevation = elevation,
        actions = actions
    )
}

@Composable
fun JobFlowRadioButton(
    isSelected:Boolean,
    title:String,
    onClick:()->Unit
){
    Row(
        verticalAlignment = Alignment.CenterVertically
    ){
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colors.primary,
                unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text= title,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.clickable {
                onClick()
            }
        )
    }
}

@Preview
@Composable
fun RadioButtonPreview(){

    JobFlowTheme {
        Surface{
            JobFlowRadioButton(isSelected = true, title = "PVC") {

            }
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