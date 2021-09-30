package com.sivakasi.papco.jobflow.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.ui.JobFlowTextField
import com.sivakasi.papco.jobflow.ui.JobFlowTheme

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun LoginScreen(
    authState: AuthenticationState,
    onFormSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
    onModeChange: (AuthenticationMode) -> Unit
) {
    JobFlowTheme {
        Surface {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 32.dp, end = 32.dp)
            ) {

                Heading(mode = authState.mode)

                Spacer(Modifier.height(50.dp))

                LoginFields(
                    authState = authState,
                    onForgotPassword = onForgotPassword,
                    onFormSubmit = onFormSubmit
                )

                Spacer(Modifier.height(24.dp))

                RegisterOrLogin(
                    authState.mode,
                    onModeChange
                )

                Spacer(Modifier.height(16.dp))

            }
        }
    }
}

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
private fun LoginFields(
    authState: AuthenticationState,
    onForgotPassword: () -> Unit,
    onFormSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {

    val focusManager = LocalFocusManager.current
    val (nameFocus, emailFocus, passwordFocus, confirmPasswordFocus, submitButtonFocus) = FocusRequester.createRefs()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        //Name Text Box
        AnimatedVisibility(authState.mode == AuthenticationMode.SIGNUP) {
            JobFlowTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .focusRequester(nameFocus),
                onTabPressed = {
                    emailFocus.requestFocus()
                },
                error = authState.nameError,
                value = authState.name,
                onValueChange = {
                    authState.clearErrors()
                    if (!it.contains("\t"))
                        authState.name = it
                },
                label = stringResource(id = R.string.name),
                enabled = !authState.isLoading,
                keyboardActions = KeyboardActions {
                    emailFocus.requestFocus()
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Name"
                    )
                })
        }


        //Email Text Box
        JobFlowTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top=16.dp)
                .focusRequester(emailFocus),
            onTabPressed = { passwordFocus.requestFocus() },
            error = authState.emailError,
            value = authState.email,
            label = stringResource(id = R.string.email),
            onValueChange = {
                authState.emailError = null
                authState.authError = null
                if (!it.contains("\t"))
                    authState.email = it
            },
            singleLine = true,
            enabled = !authState.isLoading,
            keyboardActions = KeyboardActions { passwordFocus.requestFocus() },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = "Password"
                )
            })

        //Password Text Box
        JobFlowTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .focusRequester(passwordFocus),
            onTabPressed = {
                if (authState.mode == AuthenticationMode.LOGIN)
                    emailFocus.requestFocus()
                else
                    confirmPasswordFocus.requestFocus()
            },
            error = authState.passwordError,
            value = authState.password,
            onValueChange = {
                authState.authError = null
                authState.passwordError = null
                authState.confirmPasswordError = null
                if (!it.contains("\t"))
                    authState.password = it
            },
            label = stringResource(id = R.string.password),
            visualTransformation = PasswordVisualTransformation(),
            enabled = !authState.isLoading,
            keyboardActions = KeyboardActions {
                if (authState.mode == AuthenticationMode.LOGIN) {
                    focusManager.clearFocus() //Hides the softkeyboard
                    onFormSubmit()
                } else
                    confirmPasswordFocus.requestFocus()
            },
            keyboardOptions = KeyboardOptions(
                imeAction = if (authState.mode == AuthenticationMode.LOGIN)
                    ImeAction.Done
                else
                    ImeAction.Next
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Password"
                )
            })

        //Confirmation password Field
        AnimatedVisibility(visible = authState.mode == AuthenticationMode.SIGNUP) {
            JobFlowTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .focusRequester(confirmPasswordFocus),
                onTabPressed = { submitButtonFocus.requestFocus() },
                error = authState.confirmPasswordError,
                value = authState.confirmPassword,
                onValueChange = {
                    authState.authError = null
                    authState.passwordError = null
                    authState.confirmPasswordError = null
                    if (!it.contains("\t"))
                        authState.confirmPassword = it
                },
                label = stringResource(id = R.string.confirm_password),
                visualTransformation = PasswordVisualTransformation(),
                enabled = !authState.isLoading,
                keyboardActions = KeyboardActions {
                    focusManager.clearFocus() //Hides the soft keyboard
                    onFormSubmit()
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Password"
                    )
                })
        }


        //Forgot password Text
        AnimatedVisibility(visible = authState.mode == AuthenticationMode.LOGIN) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    modifier = Modifier
                        .clickable {
                            if (!authState.isLoading)
                                onForgotPassword()
                        },
                    text = stringResource(id = R.string.forgot_password_question),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.secondary

                )
            }

        }

        Spacer(Modifier.height(30.dp))

        //Form Submit button
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(submitButtonFocus)
                .focusable(true),
            onClick = { onFormSubmit() },
            enabled = !authState.isLoading
        ) {
            if (authState.mode == AuthenticationMode.LOGIN)
                Text(text = stringResource(id = R.string.sign_in_caps))
            else
                Text(text = stringResource(id = R.string.register_caps))
        }

        Spacer(Modifier.height(28.dp))

        if (authState.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        authState.authError?.let {
            AuthError(error = it)
        }
    }
}

@Composable
private fun AuthError(error: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
    ) {
        Icon(
            modifier = Modifier
                .padding(end = 8.dp)
                .align(Alignment.CenterVertically),
            imageVector = Icons.Filled.Info,
            contentDescription = "Password",
            tint = MaterialTheme.colors.error
        )

        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = error,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.error
        )
    }
}

@Composable
private fun RegisterOrLogin(
    mode: AuthenticationMode,
    onModeChange: (AuthenticationMode) -> Unit
) {

    Crossfade(targetState = mode) { authMode ->
        when (authMode) {

            AuthenticationMode.LOGIN -> {
                Register(
                    onRegister = { onModeChange(AuthenticationMode.SIGNUP) }
                )
            }

            AuthenticationMode.SIGNUP -> {
                Login(
                    onSelect = { onModeChange(AuthenticationMode.LOGIN) }
                )
            }

        }
    }


}

@Composable
private fun Register(onRegister: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row {
            Text(
                text = stringResource(id = R.string.dont_have_an_account_ques),
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.caption
            )

            Spacer(Modifier.width(8.dp))

            Text(
                modifier = Modifier.clickable { onRegister() },
                text = stringResource(R.string.register),
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.caption
            )
        }
    }

}

@Composable
private fun Login(onSelect: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row {
            Text(
                text = stringResource(id = R.string.already_have_an_account),
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.caption
            )

            Spacer(Modifier.width(8.dp))

            Text(
                modifier = Modifier.clickable { onSelect() },
                text = stringResource(R.string.login),
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.caption
            )
        }

    }
}

@Composable
private fun Heading(mode: AuthenticationMode) {

    Crossfade(mode) { authMode ->
        when (authMode) {
            AuthenticationMode.LOGIN -> {
                Text(
                    modifier = Modifier.padding(top = 30.dp),
                    text = stringResource(id = R.string.login),
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.h3
                )
            }

            AuthenticationMode.SIGNUP -> {
                Text(
                    modifier = Modifier.padding(top = 30.dp),
                    text = stringResource(id = R.string.register),
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.h3
                )
            }
        }
    }
}

/*@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Preview
@Composable
private fun LoginCredentialsPreview() {

    val credentials = AuthenticationState(LocalContext.current)

    JobFlowTheme {
        Surface {
            LoginFields(authState = credentials, {}, {})
        }
    }
}

@Preview
@Composable
private fun AuthErrorPreview() {

    JobFlowTheme {
        Surface {
            AuthError(error = "This user does not exist or this user has a different password. Try again with different password")
        }
    }
}*/

/*@Preview
@Composable
private fun RegisterPreview() {

    JobFlowTheme {
        Surface {
            Register({})
        }
    }
}*/

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreen(authState = AuthenticationState(LocalContext.current), {}, {}, {})
}