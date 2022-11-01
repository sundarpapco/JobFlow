package com.sivakasi.papco.jobflow.preview

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.admin.MenuItem
import com.sivakasi.papco.jobflow.preview.view.ViewPreviewFragment
import com.sivakasi.papco.jobflow.ui.JobFlowAlertDialog
import com.sivakasi.papco.jobflow.ui.JobFlowFloatingActionButton
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PreviewManagementScreen(
    viewModel: PreviewManagementVM,
    navController: NavController,
    title:String
) {

    val screenState = viewModel.screenState

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.uploadFileToStorage(it)
        }
    }

    Scaffold(
        topBar = {
            JobFlowTopBar(
                title = title,
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            JobFlowFloatingActionButton {
                filePicker.launch("image/jpeg")
            }
        }
    ) {
        if(screenState.imageUris.isEmpty())
            NoPreviewsScreen()
        else
            ContentMain(
                screenState = screenState,
                navController = navController,
                viewModel = viewModel
            )
    }
}

@ExperimentalCoroutinesApi
@Composable
private fun ContentMain(
    screenState:PreviewManagementScreenState,
    navController: NavController,
    viewModel: PreviewManagementVM
){
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        PreviewGrid(
            previews = screenState.imageUris,
            navController = navController,
            modifier = Modifier.fillMaxSize(),
            onDelete = { screenState.showDeleteConfirmation(it) }
        )

        screenState.previewToDelete?.let {
            JobFlowAlertDialog(
                message = stringResource(id = R.string.delete_preview_confirmation),
                positiveButtonText = stringResource(id = R.string.menu_delete),
                negativeButtonText = stringResource(id = R.string.cancel),
                onPositiveClick = { viewModel.deletePreview(it) },
                onNegativeClick = { screenState.hideDeleteConfirmationDialog() },
                onDismissListener = { screenState.hideDeleteConfirmationDialog() }
            )
        }
    }
}


@ExperimentalCoroutinesApi
@Composable
fun PreviewGrid(
    previews: List<JobPreview>,
    navController: NavController,
    modifier: Modifier = Modifier,
    onDelete: (JobPreview) -> Unit
) {
    LazyVerticalGrid(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        columns = GridCells.Adaptive(150.dp),
        modifier = modifier
    ) {

        items(previews) { preview ->
            PreviewImage(
                preview,
                modifier = Modifier.size(150.dp),
                onDelete = onDelete
            ) {
                navigateToViewPreviewScreen(navController, preview.downloadUrl)
            }
        }

    }
}


@ExperimentalCoroutinesApi
@Composable
fun PreviewImage(
    preview: JobPreview,
    modifier: Modifier = Modifier,
    onDelete: (JobPreview) -> Unit,
    onClick: (JobPreview) -> Unit
) {
    val context = LocalContext.current

    var loadedImage: Drawable? by remember { mutableStateOf(null) }
    var contextMenuShowing by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = { onClick(preview) },
                    onLongClick = {
                        //Show the delete option context menu only when the image is from server
                        //If it starts with file, then it means that image is still uploading to the server
                        if (!preview.downloadUrl.startsWith("file"))
                            contextMenuShowing = true
                    }
                )
                .background(MaterialTheme.colors.surface),
            model = loadedImage,
            contentScale = ContentScale.Crop,
            contentDescription = null
        )

        DropdownMenu(
            expanded = contextMenuShowing,
            offset = DpOffset(50.dp, (-50).dp),
            onDismissRequest = { contextMenuShowing = false }
        ) {
            MenuItem(text = stringResource(id = R.string.menu_delete)) {
                onDelete(preview)
                contextMenuShowing = false
            }
        }
    }

    LaunchedEffect(key1 = preview.downloadUrl) {
        val request = ImageRequest.Builder(context)
            .data(preview.downloadUrl)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .target(
                onStart = {},
                onSuccess = { loadedImage = it },
                onError = {}
            )
            .build()
        context.imageLoader.enqueue(request)
    }
}

private fun navigateToViewPreviewScreen(
    navController: NavController,
    imageUrl: String
) {
    navController.navigate(
        R.id.action_previewTestFragment_to_viewPreviewFragment,
        ViewPreviewFragment.getArgument(imageUrl)
    )
}

@Composable
private fun NoPreviewsScreen(){
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Image(
                painterResource(id = R.drawable.ic_image),
                null,
                alpha=0.4f,
                modifier = Modifier.size(50.dp),
                contentScale = ContentScale.FillBounds
            )
            Text(
                text = stringResource(id = R.string.no_previews_available),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onBackground.copy(alpha=0.4f)
            )

        }
    }
}

@ExperimentalCoroutinesApi
@Preview
@Composable
private fun NoPreviewScreenPreview() {

    JobFlowTheme {
        NoPreviewsScreen()
    }

}