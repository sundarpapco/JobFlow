package com.sivakasi.papco.jobflow.screens.login

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.sivakasi.papco.jobflow.ui.JobFlowMaterial3Theme
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.nav3.graph.AppGraph

fun EntryProviderScope<NavKey>.noInternetEntry(
    isRefreshing: Boolean,
    onRefresh:()->Unit
){
    entry<AppGraph.NoInternet> {
        NoInternetScreen(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh
        )
    }
}


@Composable
fun NoInternetScreen(
    isRefreshing: Boolean,
    onRefresh:()->Unit
){
    val config = LocalConfiguration.current

    JobFlowMaterial3Theme{
        if(config.orientation==Configuration.ORIENTATION_PORTRAIT)
            NoInternetPortraitScreen(isRefreshing,onRefresh)
        else
            NoInternetLandscapeScreen(isRefreshing,onRefresh)
    }

}

@Composable
private fun NoInternetPortraitScreen(
    isRefreshing: Boolean,
    onRefresh:()->Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {

            Icon(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally)
                    .weight(1f),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f),
                painter = painterResource(id = R.drawable.wifi_off),

                contentDescription = "No connection Icon"
            )

           Column {
               Text(
                   text = stringResource(id = R.string.oops_no_internet),
                   color = MaterialTheme.colorScheme.tertiary,
                   style = MaterialTheme.typography.headlineMedium,
                   fontWeight = FontWeight.Bold
               )
               Text(
                   modifier = Modifier.padding(top = 8.dp),
                   text = stringResource(id = R.string.check_internet_connection),
                   style = MaterialTheme.typography.titleMedium
               )
               Spacer(Modifier.height(32.dp))
               Button(
                   modifier = Modifier.fillMaxWidth(),
                   onClick = onRefresh,
                   enabled = !isRefreshing
               ) {
                   Text(
                       text = stringResource(id = R.string.try_again)
                   )
               }
               if (isRefreshing) {
                   LinearProgressIndicator(
                       Modifier.fillMaxWidth()
                           .padding(top=12.dp)
                   )
               }
               Spacer(Modifier.height(150.dp))
           }

        }
    }
}

@Composable
private fun NoInternetLandscapeScreen(
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f),
                painter = painterResource(id = R.drawable.wifi_off),

                contentDescription = "No connection Icon"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = stringResource(id = R.string.oops_no_internet),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = stringResource(id = R.string.check_internet_connection),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRefresh,
                    enabled = !isRefreshing
                ) {
                    Text(
                        text = stringResource(id = R.string.try_again)
                    )
                }
                if (isRefreshing) {
                    LinearProgressIndicator(
                        Modifier.fillMaxWidth()
                            .padding(top=12.dp)
                    )
                }
            }
        }
    }
}


@Preview(name = "Portrait")
@Composable
private fun PreviewNoInternetScreenPortrait() {


    JobFlowMaterial3Theme {
        NoInternetPortraitScreen(false){

        }
    }

}

@Preview(
    name = "Landscape",
    device = Devices.AUTOMOTIVE_1024p,
    widthDp = 1080,
    heightDp = 526
)
@Composable
private fun PreviewNoInternetScreenLandscape() {

    JobFlowMaterial3Theme {
        NoInternetLandscapeScreen(false){

        }
    }

}