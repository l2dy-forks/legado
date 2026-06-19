package io.legado.app.ui.common.compose.settingItem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ClickableSettingItem(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    option: String? = null,
    painter: Painter? = null,
    imageVector: ImageVector? = null,
    onLongClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    SettingItem(
        modifier = modifier,
        title = title,
        description = description,
        option = option,
        painter = painter,
        imageVector = imageVector,
        trailingContent = trailingContent ?: {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        onClick = onClick,
        onLongClick = onLongClick,
    )
}
