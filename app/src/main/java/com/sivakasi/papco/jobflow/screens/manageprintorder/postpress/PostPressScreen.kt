package com.sivakasi.papco.jobflow.screens.manageprintorder.postpress

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Binding
import com.sivakasi.papco.jobflow.data.Lamination
import com.sivakasi.papco.jobflow.extensions.toastError
import com.sivakasi.papco.jobflow.nav3.graph.PrintOrderGraph
import com.sivakasi.papco.jobflow.screens.manageprintorder.ManagePrintOrderVM
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import com.sivakasi.papco.jobflow.ui.TextInputDialog
import com.sivakasi.papco.jobflow.ui.WaitDialog
import com.sivakasi.papco.jobflow.util.LoadingStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi


@OptIn(ExperimentalCoroutinesApi::class, ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class
)
fun EntryProviderScope<NavKey>.postPressScreenEntry(
    viewModel: ManagePrintOrderVM,
    onExitFlow:()->Unit
){
    entry<PrintOrderGraph.PostPressDetails> {

        val processDeath by viewModel.recoveringFromProcessDeath.collectAsStateWithLifecycle()

        PostPressScreen(
            state = viewModel.postPressScreenState,
            onClose = onExitFlow,
            onSavePrintOrder = {viewModel.createPrintOrder()},
            onUpdatePrintOrder = {viewModel.updatePrintOrder()}
        )

        LaunchedEffect(processDeath) {
            if (processDeath)
                onExitFlow()
        }
    }
}


@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
@Composable
fun PostPressScreen(
    state: PostPressScreenState,
    onSavePrintOrder: () -> Unit,
    onUpdatePrintOrder: () -> Unit,
    onClose: () -> Unit
) {

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            JobFlowTopBar(
                title = if (state.isEditMode)
                    stringResource(R.string.edit_job)
                else
                    stringResource(R.string.create_job),
                navigationIcon = {
                    IconButton(
                        onClick = onClose
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp, top = 0.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = {
                        if (state.isEditMode)
                            onUpdatePrintOrder()
                        else
                            onSavePrintOrder()
                    }
                ) {
                    Text(
                        text = if (state.isEditMode)
                            stringResource(id = R.string.save_print_order).toUpperCase(Locale.current)
                        else
                            stringResource(id = R.string.create_print_order).toUpperCase(Locale.current)
                    )
                }
            }
        }
    ) { paddingValues ->

        PostPressItemsList(
            state,
            modifier = Modifier
                .padding(paddingValues)
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 0.dp)
        )

    }

    state.remarksDialogState?.let {
        TextInputDialog(
            dialogState = it,
            onPositiveClick = {
                onRemarksDialogResult(context, state)
            },
            onNegativeClick = {
                keyboardController?.hide()
                state.hideRemarksDialog()
            },
            allowBlank = true
        )
    }

    state.laminationDialogState?.let {
        LaminationDialog(
            state = it,
            onPositiveClick = {
                state.lamination = it.toLamination()
                state.dismissLaminationDialog()
            },
            onNegativeClick = {
                state.dismissLaminationDialog()
            }
        )
    }

    state.bindingDialogState?.let {
        BindingDialog(
            state = it,
            onPositiveClick = {
                state.binding = it.toBinding()
                state.dismissBindingDialog()
            },
            onNegativeClick = {
                state.dismissBindingDialog()
            }
        )
    }

    if (state.isWaiting)
        WaitDialog()

    LaunchedEffect(Unit) {
        state.loadingStatus.collect { status ->

            when (status) {
                is LoadingStatus.Loading -> {
                    state.isWaiting = true
                }

                is LoadingStatus.Success<*> -> {
                    state.isWaiting = false
                    onClose()
                }

                is LoadingStatus.Error -> {
                    state.isWaiting = false
                    context.toastError(status.exception)
                }

            }

        }
    }
}


@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun PostPressItemsList(postPressScreenState: PostPressScreenState, modifier: Modifier = Modifier) {

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item("Screen heading") {
            Text(
                text = stringResource(R.string.post_press_details),
                style = MaterialTheme.typography.h4
            )
        }

        item("Lamination") {
            LaminationPostPressItem(
                lamination = postPressScreenState.lamination,
                onCheckedChange = { checked ->
                    if (checked) {
                        postPressScreenState.lamination = Lamination()
                        postPressScreenState.showLaminationDialog()
                    } else
                        postPressScreenState.lamination = null
                }
            )
        }

        item("Foils") {
            PostPressItem(
                name = stringResource(id = R.string.foils),
                remarks = postPressScreenState.foil,
                isSelected = postPressScreenState.foil != null,
                onCheckedChange = { checked ->
                    if (checked) {
                        postPressScreenState.foil = ""
                        postPressScreenState.showRemarksDialog(
                            dialogCode = context.getString(R.string.foils),
                            title = context.getString(R.string.foils)
                        )
                    } else
                        postPressScreenState.foil = null
                }
            )
        }

        item("Scoring") {
            PostPressItem(
                name = stringResource(id = R.string.scoring),
                remarks = postPressScreenState.scoring,
                isSelected = postPressScreenState.scoring != null,
                onCheckedChange = { checked ->
                    if (checked) {
                        postPressScreenState.scoring = ""
                        postPressScreenState.showRemarksDialog(
                            dialogCode = context.getString(R.string.scoring),
                            title = context.getString(R.string.scoring)
                        )
                    } else
                        postPressScreenState.scoring = null
                }
            )
        }

        item("Folding") {
            PostPressItem(
                name = stringResource(id = R.string.folding),
                remarks = postPressScreenState.folding,
                isSelected = postPressScreenState.folding != null,
                onCheckedChange = { checked ->
                    if (checked) {
                        postPressScreenState.folding = ""
                        postPressScreenState.showRemarksDialog(
                            dialogCode = context.getString(R.string.folding),
                            title = context.getString(R.string.folding)
                        )
                    } else
                        postPressScreenState.folding = null
                }
            )
        }

        item("Binding") {
            BindingPostPressItem(
                binding = postPressScreenState.binding,
                onCheckedChange = { checked ->
                    if (checked) {
                        postPressScreenState.binding = Binding()
                        postPressScreenState.showBindingDialog()
                    } else
                        postPressScreenState.binding = null
                }
            )
        }

        item("Spot UV") {
            PostPressItem(
                name = stringResource(id = R.string.spot_uv),
                remarks = postPressScreenState.spotUV,
                isSelected = postPressScreenState.spotUV != null,
                onCheckedChange = { checked ->
                    if (checked) {
                        postPressScreenState.spotUV = ""
                        postPressScreenState.showRemarksDialog(
                            dialogCode = context.getString(R.string.spot_uv),
                            title = context.getString(R.string.spot_uv)
                        )
                    } else
                        postPressScreenState.spotUV = null
                }
            )
        }

        item("Aqueous Coating") {
            PostPressItem(
                name = stringResource(id = R.string.aqueous_coating),
                remarks = postPressScreenState.aqueousCoating,
                isSelected = postPressScreenState.aqueousCoating != null,
                onCheckedChange = { checked ->
                    if (checked) {
                        postPressScreenState.aqueousCoating = ""
                        postPressScreenState.showRemarksDialog(
                            dialogCode = context.getString(R.string.aqueous_coating),
                            title = context.getString(R.string.aqueous_coating)
                        )
                    } else
                        postPressScreenState.aqueousCoating = null
                }
            )
        }

        item("Cutting") {
            PostPressItem(
                name = stringResource(id = R.string.cutting),
                remarks = postPressScreenState.cutting,
                isSelected = postPressScreenState.cutting != null,
                onCheckedChange = { checked ->
                    if (checked) {
                        postPressScreenState.cutting = ""
                        postPressScreenState.showRemarksDialog(
                            dialogCode = context.getString(R.string.cutting),
                            title = context.getString(R.string.cutting)
                        )
                    } else
                        postPressScreenState.cutting = null
                }
            )
        }

        item("Packing") {
            PostPressItem(
                name = stringResource(id = R.string.packing),
                remarks = postPressScreenState.packing,
                isSelected = postPressScreenState.packing != null,
                onCheckedChange = { checked ->
                    if (checked) {
                        postPressScreenState.packing = ""
                        postPressScreenState.showRemarksDialog(
                            dialogCode = context.getString(R.string.packing),
                            title = context.getString(R.string.packing)
                        )
                    } else
                        postPressScreenState.packing = null
                }
            )
        }
    }

}


private fun onRemarksDialogResult(
    context: Context,
    screenState: PostPressScreenState
) {

    screenState.remarksDialogState?.let { dialogState ->

        when (dialogState.data) {
            context.getString(R.string.foils) -> {
                screenState.foil = dialogState.text.text
            }

            context.getString(R.string.scoring) -> {
                screenState.scoring = dialogState.text.text
            }

            context.getString(R.string.folding) -> {
                screenState.folding = dialogState.text.text
            }

            context.getString(R.string.spot_uv) -> {
                screenState.spotUV = dialogState.text.text
            }

            context.getString(R.string.aqueous_coating) -> {
                screenState.aqueousCoating = dialogState.text.text
            }

            context.getString(R.string.cutting) -> {
                screenState.cutting = dialogState.text.text
            }

            context.getString(R.string.packing) -> {
                screenState.packing = dialogState.text.text
            }
        }
    }

    screenState.hideRemarksDialog()
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalCoroutinesApi::class)
@ExperimentalComposeUiApi
@Preview
@Composable
fun PreviewPostPressScreen() {

    val context = LocalContext.current
    val screenState = remember {
        PostPressScreenState(context).apply {
            folding="Special Book"
        }
    }

    JobFlowTheme {
        PostPressScreen(
            state = screenState,
            onSavePrintOrder = {},
            onUpdatePrintOrder = {},
            onClose = {}
        )
    }

}
