package com.sivakasi.papco.jobflow.screens.login

import android.content.res.Configuration
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.ui.JobFlowTextField
import com.sivakasi.papco.jobflow.ui.JobFlowTheme

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordState,
    onFormSubmit: () -> Unit
) {
    JobFlowTheme {
        Surface {

            val configuration = LocalConfiguration.current
            val focusManager = LocalFocusManager.current
            val buttonFocus = remember { FocusRequester() }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 32.dp, end = 32.dp)
            ) {

                //Heading text
                Text(
                    modifier = Modifier.padding(top = 30.dp),
                    text = stringResource(id = R.string.forgot_password_question),
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.h4
                )

                if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    Spacer(Modifier.height(50.dp))
                else
                    Spacer(Modifier.height(25.dp))

                //Description for forgot password
                Text(
                    modifier = Modifier.padding(bottom = 18.dp),
                    text = stringResource(id = R.string.forgot_password_desc),
                    color = MaterialTheme.colors.onSurface,
                    style = MaterialTheme.typography.caption
                )

                JobFlowTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onTabPressed = { buttonFocus.requestFocus() },
                    error = state.emailError,
                    value = state.email,
                    label = stringResource(id = R.string.email_to_reset),
                    onValueChange = {
                        state.emailError = null
                        state.authError = null
                        if (!it.contains("\t"))
                            state.email = it
                    },
                    singleLine = true,
                    enabled = !state.isLoading,
                    keyboardActions = KeyboardActions { onFormSubmit() },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = "Email to reset"
                        )
                    })

                Spacer(Modifier.height(30.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(buttonFocus)
                        .focusable(true),
                    onClick = {
                        focusManager.clearFocus()
                        onFormSubmit()
                    },
                    enabled = !state.isLoading
                ) {
                    Text(text = stringResource(id = R.string.send_email_caps))
                }

                Spacer(Modifier.height(28.dp))

                if (state.isLoading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }

                state.authError?.let {
                    AuthError(error = it)
                }

            }


        }
    }
}

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Preview
@Composable
private fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen(ForgotPasswordState(LocalContext.current)) {

    }
}