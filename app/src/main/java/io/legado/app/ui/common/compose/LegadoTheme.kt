package io.legado.app.ui.common.compose

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.M3ColorHelper
import io.legado.app.lib.theme.ThemeManager
import io.legado.app.lib.theme.cardBackgroundColor
import io.legado.app.lib.theme.popupBackgroundColor
import io.legado.app.utils.ColorUtils

/**
 * 从 ThemeManager StateFlow 读取当前主题颜色，构建 M3 ColorScheme。
 * 缓存失效后 Compose 自动重组拿到新颜色。
 */
@Composable
fun rememberLegadoColorScheme(): ColorScheme {
    val c by ThemeManager.colors.collectAsState()

    return if (AppConfig.isEInkMode) {
        val tokens = M3ColorHelper.computeEInkTokens()
        lightColorScheme(
            primary = Color(c.primaryColor),
            onPrimary = Color(tokens.onPrimary),
            primaryContainer = Color(tokens.primaryContainer),
            onPrimaryContainer = Color(tokens.onPrimaryContainer),
            secondary = Color(c.accentColor),
            secondaryContainer = Color(tokens.secondaryContainer),
            onSecondaryContainer = Color(tokens.onSecondaryContainer),
            surface = Color(tokens.surface),
            onSurface = Color(tokens.onSurface),
            surfaceVariant = Color(tokens.surfaceVariant),
            onSurfaceVariant = Color(tokens.onSurfaceVariant),
            background = Color(tokens.surface),
            surfaceContainerLow = Color(tokens.surfaceContainer),
        )
    } else if (AppConfig.isNightTheme) {
        darkColorScheme(
            primary = Color(c.primaryColor),
            onPrimary = Color(c.colorOnPrimary),
            primaryContainer = Color(c.colorPrimaryContainer),
            onPrimaryContainer = Color(c.colorOnPrimaryContainer),
            secondary = Color(c.accentColor),
            secondaryContainer = Color(c.colorSecondaryContainer),
            onSecondaryContainer = Color(c.colorOnSecondaryContainer),
            surface = Color(c.colorSurface),
            onSurface = Color(c.colorOnSurface),
            surfaceVariant = Color(c.colorSurfaceVariant),
            onSurfaceVariant = Color(c.colorOnSurfaceVariant),
            background = Color(c.backgroundColor),
            surfaceContainerLow = Color(c.colorSurfaceContainer),
        )
    } else {
        lightColorScheme(
            primary = Color(c.primaryColor),
            onPrimary = Color(c.colorOnPrimary),
            primaryContainer = Color(c.colorPrimaryContainer),
            onPrimaryContainer = Color(c.colorOnPrimaryContainer),
            secondary = Color(c.accentColor),
            secondaryContainer = Color(c.colorSecondaryContainer),
            onSecondaryContainer = Color(c.colorOnSecondaryContainer),
            surface = Color(c.colorSurface),
            onSurface = Color(c.colorOnSurface),
            surfaceVariant = Color(c.colorSurfaceVariant),
            onSurfaceVariant = Color(c.colorOnSurfaceVariant),
            background = Color(c.backgroundColor),
            surfaceContainerLow = Color(c.colorSurfaceContainer),
        )
    }
}

/**
 * 用户自定义卡片背景色，优先读 PreferKey.cCardBg/cNCardBg，回退 surfaceContainerLow
 */
@Composable
fun legadoCardBackgroundColor(): Color {
    val context = LocalContext.current
    return Color(context.cardBackgroundColor)
}

/**
 * 用户自定义弹窗背景色，优先读 PreferKey.cPopupBg/cNPopupBg，回退 cardBackground
 */
@Composable
fun legadoPopupBackgroundColor(): Color {
    val context = LocalContext.current
    return Color(context.popupBackgroundColor)
}

/**
 * 浮窗文字主色，根据 popupBackgroundColor 明暗自动选择。
 * 浅色背景 → 深色文字，深色背景 → 白色文字。
 */
@Composable
fun legadoPopupPrimaryTextColor(): Color {
    val context = LocalContext.current
    val isLight = ColorUtils.isColorLight(context.popupBackgroundColor)
    return if (isLight) Color(0xDE000000) else Color(0xFFFFFFFF)
}

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraSmall = RoundedCornerShape(12.dp),  // DropdownMenu 圆角 12dp
    extraLarge = RoundedCornerShape(24.dp),
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LegadoTheme(content: @Composable () -> Unit) {
    val colorScheme = rememberLegadoColorScheme()
    val motionScheme = if (AppConfig.isEInkMode) MotionScheme.standard() else MotionScheme.expressive()
    CompositionLocalProvider(
        LocalAnimationsEnabled provides !AppConfig.isEInkMode
    ) {
        // 使用 MaterialExpressiveTheme 而非 MaterialTheme，
        // 设置内部 LocalUsingExpressiveTheme 标志位，
        // 使 ModalBottomSheet 使用 expressive spring 动画（收窄下滑退场）
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            motionScheme = motionScheme,
            content = content,
        )
    }
}
