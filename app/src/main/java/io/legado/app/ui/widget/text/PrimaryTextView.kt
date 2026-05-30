package io.legado.app.ui.widget.text

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.ThemeStore
import io.legado.app.lib.theme.colorOnSurface

/**
 * @author Aidan Follestad (afollestad)
 */
@Suppress("unused")
class PrimaryTextView(context: Context, attrs: AttributeSet) :
    AppCompatTextView(context, attrs) {

    init {
        setTextColor(
            if (AppConfig.isEInkMode) context.colorOnSurface
            else ThemeStore.textColorPrimary(context)
        )
    }
}
