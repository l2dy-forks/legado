package io.legado.app.ui.common.compose

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.legado.app.R
import io.legado.app.help.config.AppConfig

/**
 * Create a ComposeView that renders a toolbar MoreVert icon with
 * a [RoundDropdownMenu] for overflow items.
 *
 * Usage in an Activity's onCompatCreateOptionsMenu:
 * ```
 * val overflowView = createOverflowMenuView { dismiss ->
 *     RoundDropdownMenuItem(text = "Item 1", onClick = { dismiss(); doSomething() })
 *     RoundDropdownMenuItem(text = "Item 2", onClick = { dismiss(); doOther() })
 * }
 * menu.add(...)?.setActionView(overflowView)
 * ```
 */
fun Context.createOverflowMenuView(
    modifier: Modifier = Modifier,
    iconContentDescriptionId: Int = R.string.more_menu,
    content: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit
): View {
    return ComposeView(this).apply {
        // Defer setContent until attached to window to avoid crash when
        // the menu system measures this action view before it's in the view tree.
        var contentSet = false
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                if (!contentSet) {
                    contentSet = true
                    setContent {
                        LegadoTheme {
                            val iconTint = if (AppConfig.isEInkMode) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            }
                            var expanded by remember { mutableStateOf(false) }

                            Box(modifier = modifier) {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_more_vert),
                                        contentDescription = stringResource(iconContentDescriptionId),
                                        tint = iconTint,
                                    )
                                }
                                RoundDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    content = content,
                                )
                            }
                        }
                    }
                }
            }

            override fun onViewDetachedFromWindow(v: View) {}
        })
    }
}
