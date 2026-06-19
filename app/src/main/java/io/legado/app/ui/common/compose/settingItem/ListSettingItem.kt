package io.legado.app.ui.common.compose.settingItem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.legado.app.ui.common.compose.RoundDropdownMenuItem

@Composable
fun ListSettingItem(
    title: String,
    selectedValue: String,
    displayEntries: Array<String>,
    entryValues: Array<String>,
    description: String? = null,
    painter: Painter? = null,
    imageVector: ImageVector? = null,
    onValueChange: (String) -> Unit,
) {
    val currentEntry =
        displayEntries.getOrNull(entryValues.indexOf(selectedValue)) ?: selectedValue

    SettingItem(
        title = title,
        description = description,
        painter = painter,
        imageVector = imageVector,
        trailingContent = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = currentEntry,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 13.sp,
                )
            }
        },
        dropdownMenu = { onDismiss ->
            displayEntries.forEachIndexed { index, display ->
                RoundDropdownMenuItem(
                    text = display,
                    onClick = {
                        onValueChange(entryValues[index])
                        onDismiss()
                    },
                    trailingIcon = if (selectedValue == entryValues[index]) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null
                )
            }
        }
    )
}
