package io.legado.app.ui.main

import android.view.View
import androidx.core.view.ViewCompat

/**
 * 在 View 树中查找 transitionName 匹配的 View
 */
fun View.findViewByTransitionName(name: String): View? {
    if (ViewCompat.getTransitionName(this) == name) return this
    if (this is android.view.ViewGroup) {
        for (i in 0 until childCount) {
            val found = getChildAt(i).findViewByTransitionName(name)
            if (found != null) return found
        }
    }
    return null
}
