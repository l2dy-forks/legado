package io.legado.app.lib.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the reactive [StateFlow] of [ThemeColors] for Compose consumption.
 * View-layer callers continue to use [ThemeStore] companion getters (now cached).
 */
object ThemeManager {

    private val _colors = MutableStateFlow(ThemeColors.DEFAULT)
    val colors: StateFlow<ThemeColors> = _colors.asStateFlow()

    /**
     * Called once at app startup ([ThemeConfig.applyDayNightInit])
     * and after every theme change ([ThemeConfig.applyTheme]).
     */
    fun refresh(context: Context) {
        ThemeStore.invalidateColors()
        _colors.value = ThemeStore.getColors(context)
    }
}
