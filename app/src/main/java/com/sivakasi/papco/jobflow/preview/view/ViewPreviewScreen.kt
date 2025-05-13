package com.sivakasi.papco.jobflow.preview.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.preview.ZoomableImageView
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

    Scaffold(
        topBar = {
            JobFlowTopBar(
                title = "Job Preview",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                                    viewModel.sharePreview(preview)
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
