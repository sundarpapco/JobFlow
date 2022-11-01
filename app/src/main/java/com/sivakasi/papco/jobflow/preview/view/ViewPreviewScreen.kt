package com.sivakasi.papco.jobflow.preview.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import coil.load
import coil.size.Size
import com.jsibbold.zoomage.ZoomageView
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar

@Composable
fun ViewPreviewScreen(
    imageUrl: String,
    onBackPressed:()->Unit
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ){
        JobFlowTopBar(
            title = "Job Preview",
            navigationIcon = {
                IconButton(onClick = { onBackPressed() }) {
                    Icon(Icons.Filled.ArrowBack,null)
                }
            }
        )
        ZoomablePreviewImage(
            imageUrl = imageUrl,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )
    }
}


@Composable
private fun ZoomablePreviewImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    AndroidView(
        factory ={ ZoomageView(context) },
        update = {
            it.restrictBounds=true
            it.load(imageUrl){
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
