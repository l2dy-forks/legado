package io.legado.app.ui.widget

import android.view.View
import androidx.viewpager.widget.ViewPager
import io.legado.app.help.config.AppConfig
import kotlin.math.abs

/**
 * ViewPager 平滑滑动页面切换动画
 * 在 ViewPager 默认水平滑动基础上添加淡入淡出效果，不干扰原生滑动。
 * - E-Ink 模式下禁用
 */
class SmoothSlidePageTransformer : ViewPager.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        if (AppConfig.isEInkMode) return

        // 不修改 translationX，保留 ViewPager 原生滑动行为
        // 仅在页面接近屏幕边缘时添加轻微淡出
        val absPosition = abs(position)
        page.alpha = when {
            absPosition >= 1f -> 0.85f
            absPosition <= 0.5f -> 1f
            else -> 1f - (absPosition - 0.5f) * 0.3f
        }
    }
}
