package com.sivakasi.papco.jobflow.screens.manageprintorder.plateMakingDetails

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PlateMakingDetail
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.extensions.isPositiveNumber
import com.sivakasi.papco.jobflow.ui.JobFlowDropDown
import com.sivakasi.papco.jobflow.ui.JobFlowTextField
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar

@Composable
fun PlateMakingScreen(
    screenState: PlateMakingDetailsScreenState,
    onNext: () -> Unit,
    onClose: () -> Unit
) {

    Scaffold(
        topBar = {
            JobFlowTopBar(
                title = if (screenState.isEditMode)
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
                        if (screenState.validate())
                            onNext()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.next)
                    )
                }
            }
        }
    ) { paddingValues ->

        PlateMakingScreenContent(
            screenState,
            modifier = Modifier
                .padding(paddingValues)
                .padding(top = 24.dp, bottom = 0.dp, start = 24.dp, end = 24.dp)
        )

    }


}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun PlateMakingScreenContent(
    screenState: PlateMakingDetailsScreenState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val backsideOptions = remember {
        context.resources.getStringArray(R.array.backsideOptions).toList()
    }

    Column(
        modifier = modifier
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = stringResource(R.string.plate_making_details),
            style = MaterialTheme.typography.h4
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlateNumber(screenState)

            Spacer(Modifier.weight(1f))

            Checkbox(
                checked = screenState.dontCheckSize,
                onCheckedChange = {
                    screenState.dontCheckSize = it
                }
            )

            Text(
                modifier = Modifier.clickable {
                    screenState.dontCheckSize = !screenState.dontCheckSize
                },
                text = stringResource(R.string.do_not_check_size),
                style = MaterialTheme.typography.subtitle2
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            JobFlowTextField(
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (it.hasFocus) {
                            screenState.trimHeight = screenState.trimHeight.copy(
                                selection = TextRange(0, screenState.trimHeight.text.length)
                            )
                        }
                    },
                value = screenState.trimHeight,
                onValueChange = {
                    screenState.trimHeightError = null
                    if (it.text.isPositiveNumber())
                        screenState.trimHeight = it
                },
                label = stringResource(R.string.trim_height),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                error = screenState.trimHeightError,
                trailingIcon = {
                    Text(
                        text = "mm"
                    )
                }
            )

            JobFlowTextField(
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (it.hasFocus) {
                            screenState.trimWidth = screenState.trimWidth.copy(
                                selection = TextRange(0, screenState.trimWidth.text.length)
                            )
                        }
                    },
                value = screenState.trimWidth,
                onValueChange = {
                    screenState.trimWidthError = null
                    if (it.text.isPositiveNumber())
                        screenState.trimWidth = it
                },
                label = stringResource(R.string.trim_width),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                error = screenState.trimWidthError,
                trailingIcon = {
                    Text(
                        text = "mm"
                    )
                }
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            JobFlowTextField(
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (it.hasFocus) {
                            if (screenState.jobHeight.text.isBlank())
                                screenState.jobHeight = screenState.trimHeight.copy(
                                    selection = TextRange(0, screenState.trimHeight.text.length)
                                )
                            else
                                screenState.jobHeight = screenState.jobHeight.copy(
                                    selection = TextRange(0, screenState.jobHeight.text.length)
                                )
                        }
                    },
                value = screenState.jobHeight,
                onValueChange = {
                    if (it.text.isPositiveNumber()) {
                        screenState.jobHeight = it
                        screenState.autoSetGripperAndTail()
                    }
                },
                label = stringResource(R.string.job_height),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                enabled = screenState.enablePlateFields,
                trailingIcon = {
                    Text(
                        text = "mm"
                    )
                }
            )

            JobFlowTextField(
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (it.hasFocus) {
                            if (screenState.jobWidth.text.isBlank())
                                screenState.jobWidth = screenState.trimWidth.copy(
                                    selection = TextRange(0, screenState.trimWidth.text.length)
                                )
                            else
                                screenState.jobWidth = screenState.jobWidth.copy(
                                    selection = TextRange(0, screenState.jobWidth.text.length)
                                )
                        }
                    },
                value = screenState.jobWidth,
                onValueChange = {
                    if (it.text.isPositiveNumber())
                        screenState.jobWidth = it
                },
                label = stringResource(R.string.job_width),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                enabled = screenState.enablePlateFields,
                trailingIcon = {
                    Text(
                        text = "mm"
                    )
                }
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            JobFlowTextField(
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (it.hasFocus) {
                            screenState.gripper = screenState.gripper.copy(
                                selection = TextRange(0, screenState.gripper.text.length)
                            )
                        }
                    },
                value = screenState.gripper,
                onValueChange = {
                    if (it.text.isPositiveNumber())
                        screenState.gripper = it
                },
                label = stringResource(R.string.gripper),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                enabled = screenState.enablePlateFields,
                trailingIcon = {
                    Text(
                        text = "mm"
                    )
                }
            )

            JobFlowTextField(
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (it.hasFocus) {
                            screenState.tail = screenState.tail.copy(
                                selection = TextRange(0, screenState.tail.text.length)
                            )
                        }
                    },
                value = screenState.tail,
                onValueChange = {
                    if (it.text.isPositiveNumber())
                        screenState.tail = it
                },
                label = stringResource(R.string.tail),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                enabled = screenState.enablePlateFields,
                trailingIcon = {
                    Text(
                        text = "mm"
                    )
                }
            )
        }

        JobFlowTextField(
            modifier = Modifier
                .onFocusChanged {
                    if (it.hasFocus) {
                        screenState.machine = screenState.machine.copy(
                            selection = TextRange(0, screenState.machine.text.length)
                        )
                    }
                },
            value = screenState.machine,
            onValueChange = {
                screenState.machineError = null
                screenState.machine = it
            },
            label = stringResource(R.string.machine),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            error = screenState.machineError
        )

        JobFlowTextField(
            modifier = Modifier
                .onFocusChanged {
                    if (it.hasFocus) {
                        screenState.screen = screenState.screen.copy(
                            selection = TextRange(0, screenState.screen.text.length)
                        )
                    }
                },
            value = screenState.screen,
            onValueChange = {
                screenState.screenError = null
                screenState.screen = it
            },
            label = stringResource(R.string.screen),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            error = screenState.screenError,
            enabled = screenState.enablePlateFields
        )

        JobFlowDropDown(
            value = screenState.backside,
            dropDownItems = backsideOptions,
            label = stringResource(R.string.backside),
            onClick = { _, string ->
                screenState.backside = string
            }
        )

        Column {
            JobFlowTextField(
                modifier = Modifier
                    .onFocusChanged {
                        if (it.hasFocus) {
                            screenState.backsideMachine = screenState.backsideMachine.copy(
                                selection = TextRange(0, screenState.backsideMachine.text.length)
                            )
                        }
                    },
                value = screenState.backsideMachine,
                onValueChange = {
                    screenState.backsideMachine = it
                },
                label = stringResource(R.string.backside_machine),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.blank_if_same_as_front_machine),
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.secondaryVariant
            )
        }
    }
}


@Composable
private fun PlateNumber(screenState: PlateMakingDetailsScreenState) {
    if (screenState.isEditMode) {
        Text(
            text = if (screenState.plateNumber == PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE)
                stringResource(R.string.outside_plate)
            else
                stringResource(R.string.plate_number, screenState.plateNumber)
        )
    } else {
        if (screenState.jobType == PrintOrder.TYPE_REPEAT_JOB) {
            Text(
                text = if (screenState.plateNumber == PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE)
                    stringResource(R.string.outside_plate)
                else
                    stringResource(R.string.old_plate_number, screenState.plateNumber)
            )
        } else {
            Checkbox(
                checked = screenState.plateNumber == PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE,
                onCheckedChange = { checked ->
                    if (checked)
                        screenState.plateNumber =
                            PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE
                    else
                        screenState.plateNumber =
                            PlateMakingDetail.PLATE_NUMBER_NOT_YET_ALLOCATED
                }
            )

            Text(
                modifier = Modifier.clickable {
                    if (screenState.plateNumber == PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE)
                        screenState.plateNumber =
                            PlateMakingDetail.PLATE_NUMBER_NOT_YET_ALLOCATED
                    else
                        screenState.plateNumber =
                            PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE
                },
                text = stringResource(R.string.outside_plate),
                style = MaterialTheme.typography.subtitle2
            )
        }
    }
}


@Preview(name = "Screen")
@Composable
private fun PreviewContent() {

    val context = LocalContext.current

    val screenState = remember {
        val state = PlateMakingDetailsScreenState(context)
        val details = PlateMakingDetail().apply {
            plateNumber = 12345
            trimmingHeight = 585
            trimmingWidth = 910
            jobHeight = 585
            jobWidth = 910
            machine = "D3000S5"
            screen = "175# Final"
        }
        val printOrder = PrintOrder().apply {
            plateMakingDetail = details
        }
        state.loadPrintOrder(printOrder, false)
        state
    }

    JobFlowTheme {
        PlateMakingScreen(
            screenState = screenState,
            onNext = { screenState.validate() },
            onClose = {}
        )
    }

}

