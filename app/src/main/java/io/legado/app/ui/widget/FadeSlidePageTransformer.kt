package io.legado.app.ui.widget

import android.view.View
import androidx.viewpager.widget.ViewPager
import io.legado.app.help.config.AppConfig
import kotlin.math.abs

/**
 * ViewPager 页面切换过渡动画：Fade + Scale
 * - 当前页：alpha 1→0 + scale 1.0→0.85
 * - 新页：alpha 0→1 + scale 0.85→1.0
 * - E-Ink 模式下禁用动画
 */
class FadeSlidePageTransformer : ViewPager.PageTransformer {

    companion object {
        private const val MIN_SCALE = 0.85f
        private const val MIN_ALPHA = 0.5f
    }

    override fun transformPage(page: View, position: Float) {
        // E-Ink 模式禁用动画
        if (AppConfig.isEInkMode) return

        val pageWidth = page.width
        val pageHeight = page.height

        when {
            position < -1 -> {
                // 页面在屏幕左侧之外 [-] 不可见
                page.alpha = 0f
            }
            position <= 1 -> {
                // 修改默认滑动过渡为 Fade + Scale 效果
                val scaleFactor = MIN_SCALE.coerceAtLeast(1 - abs(position))
                val verticalMargin = pageHeight * (1 - scaleFactor) / 2
                val horizontalMargin = pageWidth * (1 - scaleFactor) / 2

                when {
                    position < 0 -> {
                        page.translationX = horizontalMargin - verticalMargin / 2
                    }
                    else -> {
                        page.translationX = -horizontalMargin + verticalMargin / 2
                    }
                }

                // Scale the page down
                page.scaleX = scaleFactor
                page.scaleY = scaleFactor

                // Fade the page
                page.alpha = MIN_ALPHA + (1 - MIN_ALPHA) * (1 - abs(position))
            }
            else -> {
                // 页面在屏幕右侧之外 [+] 不可见
                page.alpha = 0f
            }
        }
    }
}
