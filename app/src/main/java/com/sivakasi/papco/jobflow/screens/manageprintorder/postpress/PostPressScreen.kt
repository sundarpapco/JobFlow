package com.sivakasi.papco.jobflow.screens.manageprintorder.postpress

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Binding
import com.sivakasi.papco.jobflow.data.Lamination
import com.sivakasi.papco.jobflow.screens.manageprintorder.ManagePrintOrderVM
import com.sivakasi.papco.jobflow.ui.TextInputDialog
import com.sivakasi.papco.jobflow.ui.WaitDialog
import com.sivakasi.papco.jobflow.util.LoadingStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
@Composable
fun PostPressScreen(
    viewModel:ManagePrintOrderVM,
    navigationController:NavController
) {

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val state = viewModel.postPressScreenState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Surface(
            color = MaterialTheme.colors.background,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            PostPressItemsList(postPressScreenState = state)
        }

        BottomButton(
            text = if (viewModel.isEditMode)
                stringResource(id = R.string.save_print_order).toUpperCase(Locale.current)
            else
                stringResource(id = R.string.create_print_order).toUpperCase(Locale.current)
        ) {
            if(viewModel.isEditMode)
                viewModel.updatePrintOrder()
            else
                viewModel.savePrintOrder()
        }
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
                state.dismissLaminationDialog()
            },
            onNegativeClick = {
                state.dismissBindingDialog()
            }
        )
    }

    viewModel.saveUpdateStatus?.let{

        when(it){

            is LoadingStatus.Error ->{
                //Toast the message here
                LaunchedEffect(Unit) {
                    Toast.makeText(
                        context,
                        it.exception.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    //Remove the error state so that toast will not show up on screen rotation
                    //Safe to reset the state since we are in a side effect
                    viewModel.saveUpdateStatus = null
                }
            }

            else ->{
                WaitDialog()
                if(it is LoadingStatus.Success<*>){
                    LaunchedEffect(Unit){
                        navigationController.popBackStack(R.id.fragmentJobDetails, true)
                    }
                }
            }

        }

    }

}

@Composable
fun PostPressItemsList(postPressScreenState: PostPressScreenState) {

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item("top spacing") {
            Spacer(
                Modifier
                    .height(24.dp)
                    .fillMaxWidth()
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

@Composable
private fun BottomButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 12.dp, 12.dp, 16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Button(onClick = onClick) {
            Text(
                text = text
            )
        }
    }
}

/*
@ExperimentalComposeUiApi
@Preview
@Composable
fun PreviewPostPressScreen() {


    val viewModel = ManagePrintOrderVM(
        Repository(application.applicationContext)
    )

    JobFlowTheme {
        PostPressScreen(
            postPressScreenState = state,
            isEditMode = false,
            onUpdatePrintOrder = {},
            onCreatePrintOrder = {}
        )
    }

}*/
