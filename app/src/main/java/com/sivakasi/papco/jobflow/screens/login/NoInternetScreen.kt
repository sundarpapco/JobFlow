package com.sivakasi.papco.jobflow.screens.login

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.screens.clients.ui.LoadingScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTheme


@Composable
fun NoInternetScreen(
    isRefreshing:MutableState<Boolean>,
    onRefresh:()->Unit
){
    val config = LocalConfiguration.current

    JobFlowTheme{
        if(config.orientation==Configuration.ORIENTATION_PORTRAIT)
            NoInternetPortraitScreen(isRefreshing.value,onRefresh)
        else
            NoInternetLandscapeScreen(isRefreshing.value,onRefresh)
    }

}

@Composable
private fun NoInternetPortraitScreen(
    isRefreshing: Boolean,
    onRefresh:()->Unit
) {
    Surface(
        color = MaterialTheme.colors.background,
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
                tint = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.disabled),
                painter = painterResource(id = R.drawable.wifi_off),

                contentDescription = "No connection Icon"
            )

           Column {
               Text(
                   text = stringResource(id = R.string.oops_no_internet),
                   color = MaterialTheme.colors.secondary,
                   style = MaterialTheme.typography.h5,
                   fontWeight = FontWeight.Bold
               )
               Text(
                   modifier = Modifier.padding(top = 8.dp),
                   text = stringResource(id = R.string.check_internet_connection),
                   style = MaterialTheme.typography.subtitle1
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
        color = MaterialTheme.colors.background,
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
                tint = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.disabled),
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
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = stringResource(id = R.string.check_internet_connection),
                    style = MaterialTheme.typography.subtitle1
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


    JobFlowTheme {
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

    JobFlowTheme {
        NoInternetLandscapeScreen(false){

        }
    }

}