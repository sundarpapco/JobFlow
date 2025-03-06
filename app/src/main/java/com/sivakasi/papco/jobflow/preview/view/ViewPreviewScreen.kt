package com.sivakasi.papco.jobflow.preview.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.load
import coil.size.Size
import com.jsibbold.zoomage.ZoomageView
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.screens.clients.ui.LoadingScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import com.sivakasi.papco.jobflow.ui.MenuAction
import com.sivakasi.papco.jobflow.ui.OptionsMenu
import com.sivakasi.papco.jobflow.ui.WaitDialog
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Composable
fun ViewPreviewScreen(
    viewModel: ViewPreviewVM,
    navController: NavController
) {

    val screenState = viewModel.screenState
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        JobFlowTopBar(
            title = "Job Preview",
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, null)
                }
            },
            actions = {
                OptionsMenu(
                    menuItems = listOf(
                        MenuAction(
                            imageVector = Icons.Filled.Share,
                            label = context.getString(R.string.share)
                        )
                    ),
                    onItemClick ={
                        if(it==context.getString(R.string.share)){
                            screenState.preview?.let{preview->
                                viewModel.sharePreview(preview)
                            }
                        }
                    }
                )
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ){
            screenState.preview?.let {
                ZoomablePreviewImage(
                    imageUrl = it.displayUrl,
                    modifier = Modifier
                        .fillMaxSize()
                )
            } ?:run{
                LoadingScreen()
            }

            if(screenState.isWaiting)
                WaitDialog()

        }
    }
}


@Composable
private fun ZoomablePreviewImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    AndroidView(
        factory = { ZoomageView(context) },
        update = {
            it.restrictBounds = true
            it.load(imageUrl) {
                crossfade(true)
                size(Size.ORIGINAL)
            }
        },
        modifier = modifier
    )
}


/*
@Preview(device = Devices.AUTOMOTIVE_1024p, widthDp = 720, heightDp = 360)
@Composable
private fun PreviewZoomImageLandscape() {
    JobFlowTheme {
        ViewPreviewScreen()
    }
}

@Preview
@Composable
private fun PreviewZoomImagePortrait() {
    JobFlowTheme {
        ViewPreviewScreen()
    }
}*/
