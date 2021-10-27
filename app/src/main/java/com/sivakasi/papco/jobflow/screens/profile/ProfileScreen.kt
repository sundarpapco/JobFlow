package com.sivakasi.papco.jobflow.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.ui.JobFlowTheme

@Composable
fun ProfileScreen(
    name:String,email:String,role:String
) {

    JobFlowTheme {
        Surface{
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_drag_line),
                    contentDescription = "drag handle",
                    tint= LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(
                        modifier = Modifier.width(24.dp)
                    )
                    Icon(
                        modifier = Modifier.size(65.dp, 65.dp),
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Profile Icon"
                    )
                    Spacer(
                        modifier = Modifier.width(16.dp)
                    )
                    Column{
                        Text(
                            text = name,
                            style = MaterialTheme.typography.h5,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = email,
                            style = MaterialTheme.typography.subtitle2,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = role,
                            style = MaterialTheme.typography.subtitle2.copy(fontStyle = FontStyle.Italic),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colors.secondary
                        )

                    }
                }
                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }

        }
    }

}

@Preview
@Composable
private fun BottomSheetPreview() {
    ProfileScreen("Madhana","madhanasundar@gmail.com","admin")
}