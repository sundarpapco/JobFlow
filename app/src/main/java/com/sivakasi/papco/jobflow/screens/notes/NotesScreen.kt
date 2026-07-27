package com.sivakasi.papco.jobflow.screens.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.extensions.toastError
import com.sivakasi.papco.jobflow.nav3.graph.AppGraph
import com.sivakasi.papco.jobflow.screens.viewprintorder.NotesFragmentVM
import com.sivakasi.papco.jobflow.ui.JobFlowAlertDialog
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import com.sivakasi.papco.jobflow.ui.MenuAction
import com.sivakasi.papco.jobflow.ui.OptionsMenu
import com.sivakasi.papco.jobflow.ui.WaitDialog
import com.sivakasi.papco.jobflow.util.LoadingStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
fun EntryProviderScope<NavKey>.notesScreenEntry(
    backStack: NavBackStack<NavKey>
){
    entry<AppGraph.Notes> {key->

        val viewModel: NotesFragmentVM = hiltViewModel()

        NotesScreen(
            screenState = viewModel.screenState,
            title = stringResource(R.string.notes_title,key.poNumber.toString()),
            onSave = {viewModel.saveNotes()},
            onClose = {backStack.removeLastOrNull()}
        )

        LaunchedEffect(Unit) {
            viewModel.observePrintOrderForRemoval(key.poNumber,key.initialNotes)
        }

    }
}


@Composable
fun NotesScreen(
    screenState: NotesScreenState,
    title: String,
    onSave: () -> Unit,
    onClose: () -> Unit
) {

    val context = LocalContext.current

    BackHandler {
        if (screenState.initialNotes != screenState.notes)
            screenState.showExitConfirmationDialog()
        else
            onClose()
    }

    Scaffold(
        topBar = {
            JobFlowTopBar(
                title = title,
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (screenState.initialNotes != screenState.notes)
                                screenState.showExitConfirmationDialog()
                            else
                                onClose()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    OptionsMenu(
                        menuItems = listOf(
                            MenuAction(
                                null,
                                Icons.Filled.Save,
                                stringResource(R.string.save)
                            )
                        ),
                        onItemClick = {
                            onSave()
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            TextField(
                modifier = Modifier.fillMaxSize(),
                value = screenState.notes,
                onValueChange = {
                    screenState.notes = it
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text(
                        text = stringResource(R.string.notes)
                    )
                }

            )
        }
    }

    when (screenState.dialog) {
        is NotesScreenDialogs.None -> {

        }

        is NotesScreenDialogs.WaitDialog -> {
            WaitDialog()
        }

        is NotesScreenDialogs.ExitConfirmation -> {
            JobFlowAlertDialog(
                title = stringResource(R.string.unsaved_changes_title),
                message = stringResource(R.string.unsaved_changes_confirmation),
                positiveButtonText = stringResource(R.string.exit),
                onPositiveClick = onClose,
                cancellable = true,
                onDismissListener = { screenState.hideDialog() }
            )
        }

        is NotesScreenDialogs.PONotFound -> {
            JobFlowAlertDialog(
                title = stringResource(R.string.po_not_found),
                message = stringResource(R.string.po_not_found_desc),
                positiveButtonText = stringResource(R.string.exit),
                onPositiveClick = onClose,
                cancellable = false,
                onDismissListener = onClose
            )
        }
    }

    LaunchedEffect(Unit) {
        screenState.saveStatus.collect {
            when (it) {
                is LoadingStatus.Loading -> {
                    screenState.startLoading()
                }

                is LoadingStatus.Success<*> -> {
                    onClose()
                }

                is LoadingStatus.Error -> {
                    screenState.stopLoading()
                    context.toastError(it.exception)
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewNotesScreen() {

    val screenState = remember {
        NotesScreenState()
    }

    JobFlowTheme {
        NotesScreen(
            screenState = screenState,
            title = "Notes - po28412",
            onSave = {},
            onClose = {}
        )
    }
}