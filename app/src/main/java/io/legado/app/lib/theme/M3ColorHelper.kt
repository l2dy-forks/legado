package io.legado.app.lib.theme

import android.graphics.Color
import androidx.annotation.ColorInt
import io.legado.app.utils.ColorUtils

/**
 * Computes Material 3 color tokens from user-selected primary/accent/background colors.
 */
object M3ColorHelper {

    data class M3Tokens(
        @ColorInt val onPrimary: Int,
        @ColorInt val primaryContainer: Int,
        @ColorInt val onPrimaryContainer: Int,
        @ColorInt val secondaryContainer: Int,
        @ColorInt val onSecondaryContainer: Int,
        @ColorInt val surface: Int,
        @ColorInt val onSurface: Int,
        @ColorInt val surfaceVariant: Int,
        @ColorInt val onSurfaceVariant: Int,
        @ColorInt val surfaceContainer: Int
    )

    fun computeTokens(
        @ColorInt primary: Int,
        @ColorInt accent: Int,
        @ColorInt background: Int,
        isDark: Boolean
    ): M3Tokens {
        val onPrimary = if (ColorUtils.isColorLight(primary)) Color.BLACK else Color.WHITE
        val primaryContainer = ColorUtils.blendColors(primary, if (isDark) Color.BLACK else Color.WHITE, 0.6f)
        val onPrimaryContainer = ColorUtils.blendColors(primary, if (isDark) Color.WHITE else Color.BLACK, 0.7f)

        val secondaryContainer = ColorUtils.blendColors(accent, if (isDark) Color.BLACK else Color.WHITE, 0.6f)
        val onSecondaryContainer = ColorUtils.blendColors(accent, if (isDark) Color.WHITE else Color.BLACK, 0.7f)

        val surface = background
        val onSurface = if (ColorUtils.isColorLight(background)) {
            ColorUtils.blendColors(Color.BLACK, background, 0.13f)
        } else {
            ColorUtils.blendColors(Color.WHITE, background, 0.13f)
        }
        val surfaceVariant = ColorUtils.blendColors(background, primary, 0.05f)
        val onSurfaceVariant = if (ColorUtils.isColorLight(background)) {
            ColorUtils.blendColors(Color.BLACK, background, 0.45f)
        } else {
            ColorUtils.blendColors(Color.WHITE, background, 0.45f)
        }
        val surfaceContainer = ColorUtils.blendColors(background, primary, 0.08f)

        return M3Tokens(
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainer = surfaceContainer
        )
    }

    fun computeEInkTokens(): M3Tokens {
        return M3Tokens(
            onPrimary = Color.WHITE,
            primaryContainer = Color.WHITE,
            onPrimaryContainer = Color.BLACK,
            secondaryContainer = Color.WHITE,
            onSecondaryContainer = Color.BLACK,
            surface = Color.WHITE,
            onSurface = Color.BLACK,
            surfaceVariant = Color.WHITE,
            onSurfaceVariant = Color.BLACK,
            surfaceContainer = Color.WHITE
        )
    }
}
