package com.sivakasi.papco.jobflow.admin

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.sivakasi.papco.jobflow.ui.JobFlowMaterial3Theme
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.User
import com.sivakasi.papco.jobflow.nav3.graph.AppGraph
import com.sivakasi.papco.jobflow.nav3.util.ResultEffect
import com.sivakasi.papco.jobflow.nav3.util.ResultEventBus
import com.sivakasi.papco.jobflow.screens.login.AuthError
import com.sivakasi.papco.jobflow.ui.JobFlowTextField
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(
    ExperimentalCoroutinesApi::class, ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class
)
fun EntryProviderScope<NavKey>.updateRoleScreenEntry(
    backStack: NavBackStack<NavKey>,
    resultBus: ResultEventBus
) {

    entry<AppGraph.UpdateRole> {

        val viewModel: UpdateRoleVM = hiltViewModel()

        Scaffold(
            topBar = {
                JobFlowTopBar(
                    title = stringResource(R.string.update_role),
                    navigationIcon = {
                        IconButton(
                            onClick = { backStack.removeLastOrNull() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back Arrow"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            UpdateRoleScreen(
                modifier = Modifier.padding(paddingValues),
                updateRoleState = viewModel.state,
                onSubmit = viewModel::onUpdateRole,
                onUserChange = { backStack.add(AppGraph.SelectUser) },
                onUserDelete = viewModel::deleteUser
            )
        }

        ResultEffect<User>(resultBus, AppGraph.SelectUser.SELECTION_KEY) {
            viewModel.selectUser(it)
        }
    }
}


@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun UpdateRoleScreen(
    updateRoleState: UpdateRoleState,
    onSubmit: () -> Unit,
    onUserChange: () -> Unit,
    onUserDelete: () -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(modifier = modifier) {

        val configuration = LocalConfiguration.current
        val focusManager = LocalFocusManager.current
        val buttonFocus = remember { FocusRequester() }
        var menuExpanded by remember {
            mutableStateOf(false)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 32.dp, end = 32.dp)
        ) {

            //Heading text
            Text(
                modifier = Modifier.padding(top = 30.dp),
                text = stringResource(id = R.string.update_role),
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.headlineLarge
            )

            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                Spacer(Modifier.height(50.dp))
            else
                Spacer(Modifier.height(25.dp))

            //Description for update role
            Text(
                modifier = Modifier.padding(bottom = 18.dp),
                text = stringResource(id = R.string.update_role_desc),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelSmall
            )

            //Email Field
            JobFlowTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onUserChange()
                    },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = LocalContentColor.current,
                    disabledBorderColor = MaterialTheme.colorScheme.primary,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.54f),
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.54f),
                    disabledLabelColor = MaterialTheme.colorScheme.primary
                ),
                value = updateRoleState.selectedUser?.displayName
                    ?: stringResource(id = R.string.tap_to_select_user),
                label = stringResource(id = R.string.user),
                onValueChange = { updateRoleState.error = null },
                readOnly = true,
                singleLine = true,
                enabled = false,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "User Icon"
                    )
                }
            )

            //Roles Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {

                var dropDownWidth by remember { mutableIntStateOf(0) }

                JobFlowTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { dropDownWidth = it.width }
                        .clickable {
                            if (!updateRoleState.isLoading)
                                menuExpanded = !menuExpanded
                        },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = LocalContentColor.current,
                        disabledBorderColor = MaterialTheme.colorScheme.primary,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.54f),
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.54f),
                        disabledLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    value = updateRoleState.roles[updateRoleState.selectedRoleIndex],
                    label = stringResource(id = R.string.user_role),
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    enabled = false,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.user_role),
                            contentDescription = "User Role to assign"
                        )
                    },
                    trailingIcon = {
                        if (menuExpanded)
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_drop_up),
                                contentDescription = "Close drop down menu"
                            )
                        else
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Open drop down menu"
                            )
                    })

                DropdownMenu(
                    modifier = Modifier
                        .width(with(LocalDensity.current) { dropDownWidth.toDp() }),
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }) {

                    updateRoleState.roles.forEachIndexed { index, s ->
                        MenuItem(text = s) {
                            updateRoleState.error = null
                            updateRoleState.selectedRoleIndex = index
                            menuExpanded = !menuExpanded
                        }
                    }
                }
            }

            Spacer(Modifier.height(30.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(buttonFocus)
                    .focusable(true),
                onClick = {
                    focusManager.clearFocus()
                    onSubmit()
                },
                enabled = !updateRoleState.isLoading
            ) {
                Text(text = stringResource(id = R.string.update_role_caps))
            }
            Spacer(Modifier.height(8.dp))
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(buttonFocus)
                    .focusable(true),
                onClick = {
                    focusManager.clearFocus()
                    onUserDelete()
                },
                enabled = !updateRoleState.isLoading
            ) {
                Text(text = stringResource(id = R.string.delete_user))
            }

            Spacer(Modifier.height(28.dp))

            if (updateRoleState.isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            updateRoleState.error?.let {
                AuthError(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    error = it
                )
            }

        }
    }

}

@Composable
fun MenuItem(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(48.dp)
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Preview
@Composable
private fun UpdateRoleScreenPreview() {

    JobFlowMaterial3Theme {
        UpdateRoleScreen(
            updateRoleState = UpdateRoleState(),
            onSubmit = {},
            onUserChange = {},
            onUserDelete = {},
            modifier = Modifier
        )
    }
}