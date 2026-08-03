package com.sivakasi.papco.jobflow.screens.login

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.sivakasi.papco.jobflow.ui.JobFlowMaterial3Theme
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.nav3.graph.AppGraph
import com.sivakasi.papco.jobflow.util.JobFlowAuth

fun EntryProviderScope<NavKey>.guestScreenEntry(
    auth: JobFlowAuth
){
    entry<AppGraph.Guest>{
        GuestScreen { auth.logout() }
    }
}

@Composable
fun GuestScreen(
    onSignOut: () -> Unit
) {
    JobFlowMaterial3Theme {
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
                    text = stringResource(id = R.string.activation_description),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelSmall
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