package com.sivakasi.papco.jobflow.screens.manageprintorder.postpress

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Binding
import com.sivakasi.papco.jobflow.data.Lamination
import com.sivakasi.papco.jobflow.ui.JobFlowTheme


@Composable
fun BindingPostPressItem(
    binding: Binding?,
    onCheckedChange: (Boolean) -> Unit
) {

    PostPressItem(
        name = stringResource(id = R.string.binding),
        remarks = binding?.getBindingName(LocalContext.current),
        isSelected = binding != null,
        onCheckedChange = onCheckedChange,
        remarks2 = if (binding != null && binding.remarks.isNotBlank())
            binding.remarks
        else
            null
    )
}


@Composable
fun LaminationPostPressItem(
    lamination: Lamination?,
    onCheckedChange: (Boolean) -> Unit
) {

    PostPressItem(
        name = stringResource(id = R.string.lamination),
        remarks = lamination?.toString(),
        isSelected = lamination != null,
        onCheckedChange = onCheckedChange,
        remarks2 = if (lamination != null && lamination.remarks.isNotBlank())
            lamination.remarks
        else
            null
    )
}


@Composable
fun PostPressItem(
    name: String,
    remarks: String?,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    remarks2: String? = null
) {

    Surface(
        color = MaterialTheme.colors.background,
        modifier = modifier
    ) {

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colors.primary,
                    uncheckedColor = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                    checkmarkColor = MaterialTheme.colors.background,
                )

            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.fillMaxWidth()
                )
                remarks?.let {
                    if (remarks.isNotBlank())
                        Text(
                            text = it,
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
                        )
                }

                remarks2?.let {
                    if (remarks2.isNotBlank())
                        Text(
                            text = it,
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
                        )
                }
            }

        }


    }


}

@Preview
@Composable
private fun PreviewPostPressItem() {

    JobFlowTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                LaminationPostPressItem(
                    lamination = Lamination(
                        Lamination.MATERIAL_MATT,
                        15,
                        remarks = "@Arun Soundari Lamination"
                    ),
                    onCheckedChange = {}
                )

                PostPressItem(
                    name = "Binding",
                    remarks = "Saddle Stitch",
                    isSelected = false,
                    onCheckedChange = {}
                )
            }

        }

    }

}