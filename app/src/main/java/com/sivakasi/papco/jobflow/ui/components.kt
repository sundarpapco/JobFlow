package com.sivakasi.papco.jobflow.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.admin.MenuItem
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun JobFlowTextField(
    value: String,
    onValueChange: (String) -> Unit,
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
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors()
) {

    Column(
        modifier = modifier
    ) {

        val relocationRequester = remember { BringIntoViewRequester() }
        val coroutineScope = rememberCoroutineScope()

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(relocationRequester)
                .onFocusChanged {
                    if (it.isFocused)
                        coroutineScope.launch {
                            relocationRequester.bringIntoView()
                        }
                }
            /* .onKeyEvent {
                 if(it.type== KeyEventType.KeyDown){
                     if (it.key.keyCode == Key.Tab.keyCode) {
                         onTabPressed()
                         true
                     } else
                         false
                 }else
                     false
             }*/,
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
            minLines = minLines,
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


@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun JobFlowTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
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
    Column(
        modifier = modifier
    ) {

        val relocationRequester = remember { BringIntoViewRequester() }
        val coroutineScope = rememberCoroutineScope()

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(relocationRequester)
                .onFocusChanged {
                    if (it.isFocused)
                        coroutineScope.launch {
                            relocationRequester.bringIntoView()
                        }
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

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun SelectableTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
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

        val relocationRequester = remember { BringIntoViewRequester() }
        val coroutineScope = rememberCoroutineScope()

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .bringIntoViewRequester(relocationRequester)
                .onFocusChanged {
                    if (it.isFocused)
                        coroutineScope.launch {
                            relocationRequester.bringIntoView()
                        }
                }
            /*.onKeyEvent {
                if (it.key.keyCode == Key.Tab.keyCode) {
                    onTabPressed()
                    true
                } else
                    false
            }*/,
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
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.onSurface,
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
    isSelected: Boolean,
    title: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    textColor: Color = MaterialTheme.colors.onSurface
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colors.primary,
                unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            ),
            enabled = enabled
        )
        //Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.clickable {
                onClick()
            },
            color = textColor
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun JobFlowDropDown(
    value: String,
    dropDownItems: List<String>,
    label: String,
    onClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {

        var dropDownWidth by remember { mutableIntStateOf(0) }
        var menuExpanded by remember { mutableStateOf(false) }

        JobFlowTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { dropDownWidth = it.width }
                .clickable {
                    menuExpanded = !menuExpanded
                },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                disabledTextColor = LocalContentColor.current,
                disabledBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.42f),
                disabledLeadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                disabledTrailingIconColor = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                disabledLabelColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
            ),
            value = value,
            label = label,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            enabled = false,
            leadingIcon = leadingIcon,
            trailingIcon = {
                if (menuExpanded)
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_drop_up),
                        contentDescription = "Close drop down menu"
                    )
                else
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Open drop down menu"
                    )
            })

        DropdownMenu(
            modifier = Modifier
                .width(with(LocalDensity.current) { dropDownWidth.toDp() }),
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }) {

            dropDownItems.forEachIndexed { index, s ->
                MenuItem(text = s) {
                    menuExpanded = !menuExpanded
                    onClick(index, s)
                }
            }
        }
    }
}

@Composable
fun JobFlowCircularProgressBar(
    modifier: Modifier = Modifier,
    text: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text(
            text = text,
            style = MaterialTheme.typography.body2
        )
    }
}

@Preview
@Composable
private fun PreviewProgressBar() {
    JobFlowTheme {
        JobFlowCircularProgressBar(
            text = "One moment please"
        )
    }
}


@ExperimentalFoundationApi
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
                error = "*Invalid email"
            )
        }
    }
}