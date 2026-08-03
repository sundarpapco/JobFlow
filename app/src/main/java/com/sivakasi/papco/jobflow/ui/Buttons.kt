package com.sivakasi.papco.jobflow.ui

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable

@Composable
fun JobFlowFloatingActionButton(
    onClick:()->Unit
){
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = "Add New Machine"
        )
    }
}