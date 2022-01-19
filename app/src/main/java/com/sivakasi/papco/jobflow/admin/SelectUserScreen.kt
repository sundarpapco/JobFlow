package com.sivakasi.papco.jobflow.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.User
import com.sivakasi.papco.jobflow.screens.clients.ui.LoadingScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import com.sivakasi.papco.jobflow.util.LoadingStatus

@ExperimentalMaterialApi
@Suppress("UNCHECKED_CAST")
@Composable
fun UsersListScreen(
    users: LoadingStatus,
    onClick: (User) -> Unit,
    onBackPressed: () -> Unit
) {

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SelectUserTopBar(
            if(users is LoadingStatus.Success<*>)
                (users.data as List<User>).size
            else
                0,
            onBackPressed
        )

        when(users){

            is LoadingStatus.Loading ->{
                LoadingScreen()
            }

            is LoadingStatus.Error -> {
                val msg = users.exception.message ?: stringResource(id = R.string.error_unknown_error)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                onBackPressed()
            }

            is LoadingStatus.Success<*> -> {
                UsersList(
                    modifier = Modifier.fillMaxSize(),
                    users = users.data as List<User>,
                    onClick = onClick
                )
            }
        }


    }


}

@Composable
private fun SelectUserTopBar(
    loadedUserCount: Int = 0,
    onBackPressed: () -> Unit
) {
    JobFlowTopBar(
        title = stringResource(id = R.string.select_user),
        subtitle = if (loadedUserCount > 0)
            stringResource(id = R.string.xx_users, loadedUserCount)
        else
            null,
        navigationIcon = {
            IconButton(
                onClick = onBackPressed
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back Arrow"
                )
            }
        }
    )
}


@ExperimentalMaterialApi
@Composable
private fun UsersList(
    users: List<User>,
    modifier: Modifier = Modifier,
    onClick: (User) -> Unit
) {

    LazyColumn(
        modifier = modifier.background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        item(
            key = "top_space"
        ) {
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(
            items = users,
            key = { it.email }
        ) {
            UserListItem(user = it, onClick = onClick)
        }

        item(
            key = "bottom_space"
        ) {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

}

@ExperimentalMaterialApi
@Composable
private fun UserListItem(
    user: User,
    onClick: (User) -> Unit
) {
    Card(
        backgroundColor = MaterialTheme.colors.background,
        modifier = Modifier
            .fillMaxWidth(),
        onClick = { onClick(user) },
        shape = RectangleShape
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "User Icon",
                    tint = MaterialTheme.colors.secondary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onBackground
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.subtitle2,
                        color = MaterialTheme.colors.onBackground.copy(0.7f)
                    )
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Preview
@Composable
private fun UserListItemPreview() {

    val users = remember {
        listOf(
            User(
                displayName = "Sundaravel",
                email = "m.sundaravel@gmail.com"
            ),
            User(
                displayName = "Madhana",
                email = "madhanasundar@gmail.com"
            ),
            User(
                displayName = "Rithanya",
                email = "rithanyams@gmail.com"
            ),
            User(
                displayName = "Saatvik",
                email = "saatvik.sundaravel@gmail.com"
            )
        )

    }

    JobFlowTheme {
        UsersListScreen(users = LoadingStatus.Success(users), onClick = {}, onBackPressed = {})
    }

}