package com.sivakasi.papco.jobflow.preview.view

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.extensions.sharePreview
import com.sivakasi.papco.jobflow.nav3.graph.AppGraph
import com.sivakasi.papco.jobflow.nav3.util.Toaster
import com.sivakasi.papco.jobflow.preview.JobPreview
import com.sivakasi.papco.jobflow.preview.ZoomableImageView
import com.sivakasi.papco.jobflow.screens.clients.ui.LoadingScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import com.sivakasi.papco.jobflow.ui.MenuAction
import com.sivakasi.papco.jobflow.ui.OptionsMenu
import com.sivakasi.papco.jobflow.ui.WaitDialog
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
fun EntryProviderScope<NavKey>.viewPreviewScreenEntry(
    backStack: NavBackStack<NavKey>){

    entry<AppGraph.Preview> {key->

        val context = LocalContext.current
        val viewModel: ViewPreviewVM = hiltViewModel()

        ViewPreviewScreen(
            screenState = viewModel.screenState,
            onBack = {backStack.removeLastOrNull()},
            onShare = {viewModel.sharePreview(it)}
        )

        //Load the Preview in to the Screen
        LaunchedEffect(Unit) {
            val preview = JobPreview(
                context = context,
                previewId = key.previewId,
                fileName = key.fileName,
                displayUrl = key.imageUrl
            )

            viewModel.loadPreview(preview)
        }

        //Observe the ViewModel and Share the preview if VM Downloaded one
        LaunchedEffect(Unit) {
            viewModel.sharePreview.collect {
                context.sharePreview(it)
            }
        }

        Toaster(context,viewModel.screenState)
    }

}

@SuppressLint("LocalContextGetResourceValueCall")
@ExperimentalCoroutinesApi
@Composable
fun ViewPreviewScreen(
    screenState: ViewPreviewScreenState,
    onBack:()->Unit,
    onShare:(JobPreview)->Unit
) {

    val context = LocalContext.current

    Scaffold(
        topBar = {
            JobFlowTopBar(
                title = "Job Preview",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
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
                                    onShare(preview)
                                }
                            }
                        }
                    )
                }
            )
        }
    ) {paddingValues->
        screenState.preview?.let {
            ZoomableImageView(
                imageUrl = it.displayUrl,
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            )
        } ?:run{
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        }

        if(screenState.isWaiting)
            WaitDialog()
    }
}
