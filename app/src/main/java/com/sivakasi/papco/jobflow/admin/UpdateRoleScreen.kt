package com.sivakasi.papco.jobflow.admin

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.screens.login.AuthError
import com.sivakasi.papco.jobflow.ui.JobFlowTextField
import com.sivakasi.papco.jobflow.ui.JobFlowTheme

@ExperimentalComposeUiApi
@Composable
fun UpdateRoleScreen(
    updateRoleState: UpdateRoleState,
    onSubmit: () -> Unit,
    onUserChange:()->Unit
) {
    JobFlowTheme {
        Surface {

            val configuration = LocalConfiguration.current
            val focusManager = LocalFocusManager.current
            val buttonFocus = remember { FocusRequester() }
            var menuExpanded by remember {
                mutableStateOf(false)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 32.dp, end = 32.dp)
            ) {

                //Heading text
                Text(
                    modifier = Modifier.padding(top = 30.dp),
                    text = stringResource(id = R.string.update_role),
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.h4
                )

                if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    Spacer(Modifier.height(50.dp))
                else
                    Spacer(Modifier.height(25.dp))

                //Description for update role
                Text(
                    modifier = Modifier.padding(bottom = 18.dp),
                    text = stringResource(id = R.string.update_role_desc),
                    color = MaterialTheme.colors.onSurface,
                    style = MaterialTheme.typography.caption
                )

                //Email Field
                JobFlowTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onUserChange()
                        },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = LocalContentColor.current,
                        disabledBorderColor = MaterialTheme.colors.primary.copy(alpha = ContentAlpha.high),
                        disabledLeadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                        disabledTrailingIconColor = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                        disabledLabelColor = MaterialTheme.colors.primary.copy(alpha = ContentAlpha.high)
                    ),
                    onTabPressed = { },
                    value = updateRoleState.selectedUser?.displayName
                        ?: stringResource(id = R.string.tap_to_select_user),
                    label = stringResource(id = R.string.user),
                    onValueChange = {updateRoleState.error=null},
                    readOnly = true,
                    singleLine = true,
                    enabled = false,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "User Icon"
                        )
                    }
                )

                /*//Email Field
                JobFlowTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onTabPressed = { buttonFocus.requestFocus() },
                    error = updateRoleState.emailError,
                    value = updateRoleState.email,
                    label = stringResource(id = R.string.email_to_reset),
                    onValueChange = {
                        updateRoleState.emailError = null
                        updateRoleState.error = null
                        if (!it.contains("\t"))
                            updateRoleState.email = it
                    },
                    singleLine = true,
                    enabled = !updateRoleState.isLoading,
                    keyboardActions = KeyboardActions { focusManager.clearFocus() },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = "Email to reset"
                        )
                    })*/

                //Roles Field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {

                    var dropDownWidth by remember { mutableStateOf(0) }

                    JobFlowTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged { dropDownWidth = it.width }
                            .clickable {
                                if (!updateRoleState.isLoading)
                                    menuExpanded = !menuExpanded
                            },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            disabledTextColor = LocalContentColor.current,
                            disabledBorderColor = MaterialTheme.colors.primary.copy(alpha = ContentAlpha.high),
                            disabledLeadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                            disabledTrailingIconColor = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                            disabledLabelColor = MaterialTheme.colors.primary.copy(alpha = ContentAlpha.high)
                        ),
                        onTabPressed = { },
                        value = updateRoleState.roles[updateRoleState.selectedRoleIndex],
                        label = stringResource(id = R.string.user_role),
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.user_role),
                                contentDescription = "User Role to assign"
                            )
                        },
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

                        updateRoleState.roles.forEachIndexed { index, s ->
                            MenuItem(text = s) {
                                updateRoleState.error=null
                                updateRoleState.selectedRoleIndex = index
                                menuExpanded = !menuExpanded
                            }
                        }
                    }
                }

                Spacer(Modifier.height(30.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(buttonFocus)
                        .focusable(true),
                    onClick = {
                        focusManager.clearFocus()
                        onSubmit()
                    },
                    enabled = !updateRoleState.isLoading
                ) {
                    Text(text = stringResource(id = R.string.update_role_caps))
                }

                Spacer(Modifier.height(28.dp))

                if (updateRoleState.isLoading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }

                updateRoleState.error?.let {
                    AuthError(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        error = it
                    )
                }

            }


        }
    }
}

@Composable
fun MenuItem(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(48.dp)
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface
        )
    }
}

@ExperimentalComposeUiApi
@Preview
@Composable
private fun UpdateRoleScreenPreview() {
    UpdateRoleScreen(
        UpdateRoleState(),{}
    ) {

    }
}