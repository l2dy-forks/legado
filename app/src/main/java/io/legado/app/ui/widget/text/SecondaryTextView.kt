package io.legado.app.ui.widget.text

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import io.legado.app.R
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.colorOnSurfaceVariant

/**
 * @author Aidan Follestad (afollestad)
 */
@Suppress("unused")
class SecondaryTextView(context: Context, attrs: AttributeSet) :
    AppCompatTextView(context, attrs) {

    init {
        setTextColor(
            if (AppConfig.isEInkMode) context.colorOnSurfaceVariant
            else ContextCompat.getColor(context, R.color.secondaryText)
        )
    }
}
