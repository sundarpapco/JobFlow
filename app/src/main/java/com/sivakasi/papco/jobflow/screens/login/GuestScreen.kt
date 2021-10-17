package com.sivakasi.papco.jobflow.screens.login

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.ui.JobFlowTheme

@Composable
fun GuestScreen(
    onSignOut: () -> Unit
) {
    JobFlowTheme {
        Surface {

            val configuration = LocalConfiguration.current
            val buttonFocus = remember { FocusRequester() }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 32.dp, end = 32.dp)
            ) {

                //Guest Greeting text
                Text(
                    modifier = Modifier.padding(top = 30.dp),
                    text = stringResource(id = R.string.guest_greeting),
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
                    text = stringResource(id = R.string.activation_description),
                    color = MaterialTheme.colors.onSurface,
                    style = MaterialTheme.typography.caption
                )

                Spacer(Modifier.height(30.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(buttonFocus)
                        .focusable(true),
                    onClick = { onSignOut() }
                ) {
                    Text(text = stringResource(id = R.string.sign_out_caps))
                }

                Spacer(Modifier.height(24.dp))

            }


        }
    }
}

@Preview
@Composable
private fun GuestScreenPreview() {

    GuestScreen{

    }
}