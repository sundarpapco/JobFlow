package com.sivakasi.papco.jobflow.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.util.LoadingStatus

@ExperimentalComposeUiApi
@Composable
fun LoginScreen(
    loginState: LoginState,
    onLogin: () -> Unit,
    onLoginSuccess: () -> Unit
) {

    val focusManager = LocalFocusManager.current

    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onKeyEvent {
                                if (it.key.keyCode == Key.Tab.keyCode) {
                                    focusManager.moveFocus(FocusDirection.Down)
                                    true
                                } else false
                            },
                        isError = loginState.emailError != null,
                        value = loginState.email,
                        label = { Text(stringResource(id = R.string.email)) },
                        onValueChange = {
                            loginState.emailError = null
                            loginState.email = it
                        },
                        singleLine = true,
                        enabled = !isLoggingIn(loginState.loggingStatus),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = "Password"
                            )
                        })

                    loginState.emailError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption
                        )
                    }
                }

                Column {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .onKeyEvent {
                                if (it.key.keyCode == Key.Tab.keyCode) {
                                    focusManager.moveFocus(FocusDirection.Down)
                                    true
                                } else false
                            },
                        isError = loginState.passwordError != null,
                        value = loginState.password,
                        onValueChange = {
                            loginState.passwordError = null
                            loginState.password = it
                        },
                        label = { Text(stringResource(id = R.string.password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !isLoggingIn(loginState.loggingStatus),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Password"
                            )
                        })

                    loginState.passwordError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption
                        )
                    }
                }

                ErrorIfAny(status = loginState.loggingStatus)

                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        focusManager.clearFocus()
                        onLogin()
                    },
                    enabled = !isLoggingIn(loginState.loggingStatus)
                ) {
                    Text(stringResource(R.string.login))
                }

                loginState.loggingStatus?.let {

                    when (it) {
                        is LoadingStatus.Loading -> LinearProgressIndicator()
                        is LoadingStatus.Success<*> -> onLoginSuccess()
                        is LoadingStatus.Error -> {
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ErrorIfAny(status: LoadingStatus?) {

    if(status !is LoadingStatus.Error){
      Spacer(
          modifier = Modifier
              .requiredHeight(48.dp)
              .fillMaxWidth()
      )
    }else{

        val message=when(status.exception){

            is FirebaseAuthInvalidUserException->{
                stringResource(id = R.string.user_not_found)
            }

            is FirebaseAuthInvalidCredentialsException->{
                stringResource(id = R.string.wrong_password)
            }

            else->{
                status.exception.message ?: stringResource(id = R.string.error_unknown_error)
            }

        }

        ErrorBox(errorMsg = message)
    }
}


@Composable
private fun ErrorBox(errorMsg: String) {
    Box(
        modifier = Modifier
            .requiredHeight(48.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Error message",
                tint = MaterialTheme.colors.error
            )

            Text(
                text = errorMsg,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@ExperimentalComposeUiApi
@Preview
@Composable
fun PreviewLoginScreen() {

    val loginState = LoginState().apply {
        email = "papcopvtltd@gmail.com"
        emailError = "Invalid email"
    }

    JobFlowTheme {
        LoginScreen(loginState = loginState,
            onLogin = {},
            onLoginSuccess = {})
    }

}

@Preview
@Composable
private fun PreviewErrorBox() {
    JobFlowTheme {
        Surface(
            color = MaterialTheme.colors.background
        ) {
            ErrorBox("Invalid username or login")
        }
    }
}

private fun isLoggingIn(loggingStatus: LoadingStatus?): Boolean {
    return loggingStatus?.let {
        it is LoadingStatus.Loading || it is LoadingStatus.Success<*>
    } ?: false
}