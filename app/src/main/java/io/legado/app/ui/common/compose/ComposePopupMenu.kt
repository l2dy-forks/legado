package io.legado.app.ui.common.compose

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp

/**
 * Shows a Compose-styled dropdown menu as a [PopupWindow] anchored below [anchor].
 *
 * Drop-in replacement for [androidx.appcompat.widget.PopupMenu] in View-based
 * RecyclerView adapters. Uses [AnimatedVisibility] with a spring scale+fade
 * for the "bounce on expand, spring back on collapse" effect.
 *
 * Usage in an adapter's showMenu():
 * ```
 * showComposeDropdownMenu(context, anchorView) { dismiss ->
 *     RoundDropdownMenuItem(text = "Edit", onClick = { dismiss(); callback.edit(item) })
 *     RoundDropdownMenuItem(text = "Delete", onClick = { dismiss(); callback.del(item) })
 * }
 * ```
 */
fun showComposeDropdownMenu(
    context: Context,
    anchor: View,
    content: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit
) {
    val composeView = ComposeView(context)

    val popup = PopupWindow(
        composeView,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        true
    ).apply {
        setBackgroundDrawable(ColorDrawable(0x00000000))
        isOutsideTouchable = true
        isFocusable = true
        isClippingEnabled = false
    }

    composeView.setContent {
        LegadoTheme {
            var visible by remember { mutableStateOf(false) }

            // Trigger enter animation on next frame
            androidx.compose.runtime.LaunchedEffect(Unit) {
                visible = true
            }

            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                    initialScale = 0.85f,
                ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
                exit = scaleOut(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    targetScale = 0.85f,
                ) + fadeOut(),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = legadoPopupBackgroundColor(),
                    shadowElevation = 4.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp),
                    ) {
                        val dismissFn: () -> Unit = {
                            visible = false
                            popup.dismiss()
                        }
                        content(dismissFn)
                    }
                }
            }
        }
    }

    // Measure first to get size, then show
    composeView.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    val location = IntArray(2)
    anchor.getLocationInWindow(location)
    popup.showAtLocation(
        anchor,
        Gravity.TOP or Gravity.START,
        location[0],
        location[1] + anchor.height
    )
}

