package io.legado.app.ui.common.compose

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.legado.app.R

/**
 * 圆角下拉菜单项，与 [RoundDropdownMenu] 配套使用。
 *
 * 选中态显示 primary 色文字与 Check 图标，默认文字色使用 [legadoPopupPrimaryTextColor]。
 */
@Composable
fun RoundDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    isSelected: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource? = null,
) {
    val interaction = interactionSource ?: remember { MutableInteractionSource() }
    val hasCustomContentColor = color != Color.Unspecified

    val colorScheme = rememberLegadoColorScheme()
    val popupTextColor = legadoPopupPrimaryTextColor()
    val selectedContentColor = colorScheme.primary
    val defaultContentColor = popupTextColor

    val contentColor = if (enabled) {
        when {
            hasCustomContentColor -> color
            isSelected -> selectedContentColor
            else -> defaultContentColor
        }
    } else {
        when {
            hasCustomContentColor -> color.copy(alpha = 0.38f)
            isSelected -> selectedContentColor.copy(alpha = 0.38f)
            else -> defaultContentColor.copy(alpha = 0.38f)
        }
    }

    val containerColor = Color.Transparent

    Surface(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth(),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        contentColor = contentColor,
        interactionSource = interaction
    ) {
        Row(
            modifier = Modifier
                .padding(contentPadding)
                .heightIn(min = 48.dp)
                .widthIn(min = 120.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    leadingIcon()
                }
                Spacer(Modifier.width(12.dp))
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    modifier = Modifier.widthIn(max = 200.dp),
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = contentColor
                )
            }

            if (trailingIcon != null) {
                Spacer(Modifier.width(8.dp))
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    trailingIcon()
                }
            } else if (isSelected) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
            }
        }
    }
}
