package com.sivakasi.papco.jobflow.screens.login

import android.content.res.Configuration
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.sivakasi.papco.jobflow.ui.JobFlowMaterial3Theme
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.nav3.graph.AppGraph
import com.sivakasi.papco.jobflow.ui.JobFlowTextField

@OptIn(
    ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class
)
fun EntryProviderScope<NavKey>.forgotPasswordEntry() {

    entry<AppGraph.ForgotPassword> {

        val viewModel: ForgotPasswordVM = hiltViewModel()

        ForgotPasswordScreen(
            state = viewModel.forgotPasswordState,
            onFormSubmit = viewModel::onFormSubmit
        )

    }
}


@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordState,
    onFormSubmit: () -> Unit
) {
    JobFlowMaterial3Theme {
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
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.headlineLarge
                )

                if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    Spacer(Modifier.height(50.dp))
                else
                    Spacer(Modifier.height(25.dp))

                //Description for forgot password
                Text(
                    modifier = Modifier.padding(bottom = 18.dp),
                    text = stringResource(id = R.string.forgot_password_desc),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelSmall
                )

                JobFlowTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
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

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Preview
@Composable
private fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen(ForgotPasswordState(LocalContext.current)) {

    }
}