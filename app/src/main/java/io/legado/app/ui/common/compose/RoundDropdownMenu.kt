package io.legado.app.ui.common.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 带 expressive spring 回弹动画的圆角下拉菜单。
 *
 * 核心：在 DropdownMenu 内容 lambda 内嵌套 [MaterialExpressiveTheme]，
 * 确保 Popup 子窗口中正确应用 [MotionScheme.expressive] 动画参数，
 * 使菜单展开时产生弹跳、收起时自然回缩。
 *
 * 颜色使用 legado 现有主题系统的 [legadoPopupBackgroundColor]，
 * 形状默认 [MaterialTheme.shapes.medium]（12dp 圆角）。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RoundDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    shadowElevation: Dp = 4.dp,
    verticalSpacing: Dp = 4.dp,
    content: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit
) {
    val colorScheme = rememberLegadoColorScheme()
    val containerColor = legadoPopupBackgroundColor()

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = shape,
        shadowElevation = shadowElevation,
        containerColor = containerColor
    ) {
        // 嵌套 MaterialExpressiveTheme 确保 Popup 窗口内动画生效
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            motionScheme = MotionScheme.expressive(),
            shapes = Shapes()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(verticalSpacing)
            ) {
                content(onDismissRequest)
            }
        }
    }
}
