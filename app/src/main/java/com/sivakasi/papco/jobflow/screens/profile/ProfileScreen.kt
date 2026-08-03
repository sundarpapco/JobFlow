package com.sivakasi.papco.jobflow.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.ui.JobFlowMaterial3Theme

@Composable
fun ProfileScreen(
    name: String, email: String, role: String
) {

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(
                modifier = Modifier.height(16.dp)
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
                    contentDescription = "Profile Icon",
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(
                    modifier = Modifier.width(16.dp)
                )
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = email,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = role,
                        style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                }
            }
            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}

@Preview
@Composable
private fun BottomSheetPreview() {
    JobFlowMaterial3Theme {
        ProfileScreen("Madhana", "madhanasundar@gmail.com", "admin")
    }

}