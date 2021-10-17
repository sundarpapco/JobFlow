package com.sivakasi.papco.jobflow.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.sivakasi.papco.jobflow.admin.MenuItem

@Composable
fun OptionsMenu(
    menuItems: List<MenuAction>,
    onItemClick: (String) -> Unit
) {

    val actions = remember { menuItems.filter { it.iconId != null || it.imageVector != null } }
    val overFlowItems =
        remember { menuItems.filter { it.iconId == null && it.imageVector == null } }

    var expanded by rememberSaveable(Unit) { mutableStateOf(false) }

    actions.forEach {
        IconButton(onClick = {
            onItemClick(it.label)
            expanded = false
        }) {
            if (it.imageVector != null)
                Icon(
                    imageVector = it.imageVector!!,
                    contentDescription = it.label,
                    tint = MaterialTheme.colors.onSurface
                )
            else
                Icon(
                    painterResource(id = it.iconId!!),
                    contentDescription = it.label,
                    tint = MaterialTheme.colors.onSurface
                )
        }
    }

    if (overFlowItems.isNotEmpty()) {
        Box(
            modifier = Modifier.wrapContentSize(Alignment.TopEnd)
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Overflow menu",
                    tint = MaterialTheme.colors.onSurface
                )
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {

                overFlowItems.forEach {
                    MenuItem(text = it.label) {
                        expanded = false
                        onItemClick(it.label)
                    }
                }
            }
        }
    }
}

@Composable
fun<T> ContextMenu(
    identifier:T,
    menuItems: List<MenuAction>,
    onItemClick: (String,T) -> Unit
) {

    val actions = remember { menuItems.filter { it.iconId != null || it.imageVector != null } }
    val overFlowItems =
        remember { menuItems.filter { it.iconId == null && it.imageVector == null } }

    var expanded by rememberSaveable(Unit) { mutableStateOf(false) }

    actions.forEach {
        IconButton(onClick = {
            onItemClick(it.label,identifier)
            expanded = false
        }) {
            if (it.imageVector != null)
                Icon(
                    imageVector = it.imageVector!!,
                    contentDescription = it.label,
                    tint = MaterialTheme.colors.onSurface
                )
            else
                Icon(
                    painterResource(id = it.iconId!!),
                    contentDescription = it.label,
                    tint = MaterialTheme.colors.onSurface
                )
        }
    }

    if (overFlowItems.isNotEmpty()) {
        Box(
            modifier = Modifier.wrapContentSize(Alignment.TopEnd)
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Overflow menu",
                    tint = MaterialTheme.colors.onSurface
                )
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {

                overFlowItems.forEach {
                    MenuItem(text = it.label) {
                        expanded = false
                        onItemClick(it.label,identifier)
                    }
                }
            }
        }
    }

}