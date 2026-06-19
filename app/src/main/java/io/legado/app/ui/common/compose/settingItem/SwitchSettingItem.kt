package io.legado.app.ui.common.compose.settingItem

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun SwitchSettingItem(
    title: String,
    checked: Boolean,
    description: String? = null,
    painter: Painter? = null,
    imageVector: ImageVector? = null,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingItem(
        title = title,
        description = description,
        painter = painter,
        imageVector = imageVector,
        onClick = { if (enabled) onCheckedChange(!checked) },
        trailingContent = {
            Switch(
                modifier = Modifier.scale(0.8f),
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
        }
    )
}
