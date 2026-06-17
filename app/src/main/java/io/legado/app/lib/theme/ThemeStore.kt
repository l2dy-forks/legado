package io.legado.app.lib.theme

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.LogUtils
import splitties.init.appCtx

/**
 * @author Aidan Follestad (afollestad), Karim Abou Zeid (kabouzeid)
 */
@Suppress("unused")
class ThemeStore @SuppressLint("CommitPrefEdits")
private constructor(private val mContext: Context) : ThemeStoreInterface {

    private val mEditor = prefs(mContext).edit()

    override fun primaryColor(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_PRIMARY_COLOR, color)
        if (autoGeneratePrimaryDark(mContext))
            primaryColorDark(ColorUtils.darkenColor(color))
        return this
    }

    override fun primaryColorRes(@ColorRes colorRes: Int): ThemeStore {
        return primaryColor(ContextCompat.getColor(mContext, colorRes))
    }

    override fun primaryColorAttr(@AttrRes colorAttr: Int): ThemeStore {
        return primaryColor(ThemeUtils.resolveColor(mContext, colorAttr))
    }

    override fun primaryColorDark(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_PRIMARY_COLOR_DARK, color)
        return this
    }

    override fun primaryColorDarkRes(@ColorRes colorRes: Int): ThemeStore {
        return primaryColorDark(ContextCompat.getColor(mContext, colorRes))
    }

    override fun primaryColorDarkAttr(@AttrRes colorAttr: Int): ThemeStore {
        return primaryColorDark(ThemeUtils.resolveColor(mContext, colorAttr))
    }

    override fun accentColor(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_ACCENT_COLOR, color)
        return this
    }

    override fun accentColorRes(@ColorRes colorRes: Int): ThemeStore {
        return accentColor(ContextCompat.getColor(mContext, colorRes))
    }

    override fun accentColorAttr(@AttrRes colorAttr: Int): ThemeStore {
        return accentColor(ThemeUtils.resolveColor(mContext, colorAttr))
    }

    override fun statusBarColor(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_STATUS_BAR_COLOR, color)
        return this
    }

    override fun statusBarColorRes(@ColorRes colorRes: Int): ThemeStore {
        return statusBarColor(ContextCompat.getColor(mContext, colorRes))
    }

    override fun statusBarColorAttr(@AttrRes colorAttr: Int): ThemeStore {
        return statusBarColor(ThemeUtils.resolveColor(mContext, colorAttr))
    }

    override fun navigationBarColor(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_NAVIGATION_BAR_COLOR, color)
        return this
    }

    override fun navigationBarColorRes(@ColorRes colorRes: Int): ThemeStore {
        return navigationBarColor(ContextCompat.getColor(mContext, colorRes))
    }

    override fun navigationBarColorAttr(@AttrRes colorAttr: Int): ThemeStore {
        return navigationBarColor(ThemeUtils.resolveColor(mContext, colorAttr))
    }

    override fun textColorPrimary(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_TEXT_COLOR_PRIMARY, color)
        return this
    }

    override fun textColorPrimaryRes(@ColorRes colorRes: Int): ThemeStore {
        return textColorPrimary(ContextCompat.getColor(mContext, colorRes))
    }

    override fun textColorPrimaryAttr(@AttrRes colorAttr: Int): ThemeStore {
        return textColorPrimary(ThemeUtils.resolveColor(mContext, colorAttr))
    }

    override fun textColorPrimaryInverse(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_TEXT_COLOR_PRIMARY_INVERSE, color)
        return this
    }

    override fun textColorPrimaryInverseRes(@ColorRes colorRes: Int): ThemeStore {
        return textColorPrimaryInverse(ContextCompat.getColor(mContext, colorRes))
    }

    override fun textColorPrimaryInverseAttr(@AttrRes colorAttr: Int): ThemeStore {
        return textColorPrimaryInverse(ThemeUtils.resolveColor(mContext, colorAttr))
    }

    override fun textColorSecondary(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_TEXT_COLOR_SECONDARY, color)
        return this
    }

    override fun textColorSecondaryRes(@ColorRes colorRes: Int): ThemeStore {
        return textColorSecondary(ContextCompat.getColor(mContext, colorRes))
    }

    override fun textColorSecondaryAttr(@AttrRes colorAttr: Int): ThemeStore {
        return textColorSecondary(ThemeUtils.resolveColor(mContext, colorAttr))
    }

    override fun textColorSecondaryInverse(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_TEXT_COLOR_SECONDARY_INVERSE, color)
        return this
    }

    override fun textColorSecondaryInverseRes(@ColorRes colorRes: Int): ThemeStore {
        return textColorSecondaryInverse(ContextCompat.getColor(mContext, colorRes))
    }

    override fun textColorSecondaryInverseAttr(@AttrRes colorAttr: Int): ThemeStore {
        return textColorSecondaryInverse(ThemeUtils.resolveColor(mContext, colorAttr))
    }

    override fun backgroundColor(color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_BACKGROUND_COLOR, color)
        return this
    }

    override fun bottomBackground(color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_BOTTOM_BACKGROUND, color)
        return this
    }

    override fun colorOnPrimary(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_COLOR_ON_PRIMARY, color)
        return this
    }

    override fun colorPrimaryContainer(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_COLOR_PRIMARY_CONTAINER, color)
        return this
    }

    override fun colorOnPrimaryContainer(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_COLOR_ON_PRIMARY_CONTAINER, color)
        return this
    }

    override fun colorSecondaryContainer(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_COLOR_SECONDARY_CONTAINER, color)
        return this
    }

    override fun colorOnSecondaryContainer(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_COLOR_ON_SECONDARY_CONTAINER, color)
        return this
    }

    override fun colorSurface(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_COLOR_SURFACE, color)
        return this
    }

    override fun colorOnSurface(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_COLOR_ON_SURFACE, color)
        return this
    }

    override fun colorSurfaceVariant(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_COLOR_SURFACE_VARIANT, color)
        return this
    }

    override fun colorOnSurfaceVariant(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_COLOR_ON_SURFACE_VARIANT, color)
        return this
    }

    override fun colorSurfaceContainer(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_COLOR_SURFACE_CONTAINER, color)
        return this
    }

    override fun autoGeneratePrimaryDark(autoGenerate: Boolean): ThemeStore {
        mEditor.putBoolean(ThemeStorePrefKeys.KEY_AUTO_GENERATE_PRIMARYDARK, autoGenerate)
        return this
    }

    // Commit method

    override fun apply() {
        mEditor.putLong(ThemeStorePrefKeys.VALUES_CHANGED, System.currentTimeMillis())
            .putBoolean(ThemeStorePrefKeys.IS_CONFIGURED_KEY, true)
            .apply()
        invalidateColors()
    }

    companion object {

        @Volatile
        private var cachedColors: ThemeColors? = null

        fun editTheme(context: Context): ThemeStore {
            return ThemeStore(context)
        }

        @CheckResult
        internal fun prefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(
                ThemeStorePrefKeys.CONFIG_PREFS_KEY_DEFAULT,
                Context.MODE_PRIVATE
            )
        }

        fun markChanged(context: Context) {
            ThemeStore(context).apply()
        }

        // ---- cache management ----

        fun invalidateColors() {
            cachedColors = null
        }

        @CheckResult
        fun readAllColors(context: Context): ThemeColors {
            val p = prefs(context)
            // Layer 1: basic colors (no dependencies)
            val primary = p.getInt(
                ThemeStorePrefKeys.KEY_PRIMARY_COLOR,
                ThemeUtils.resolveColor(
                    context,
                    androidx.appcompat.R.attr.colorPrimary,
                    Color.parseColor("#455A64")
                )
            )
            val primaryDark = p.getInt(
                ThemeStorePrefKeys.KEY_PRIMARY_COLOR_DARK,
                ThemeUtils.resolveColor(
                    context,
                    androidx.appcompat.R.attr.colorPrimaryDark,
                    Color.parseColor("#37474F")
                )
            )
            val accent = p.getInt(
                ThemeStorePrefKeys.KEY_ACCENT_COLOR,
                ThemeUtils.resolveColor(
                    context,
                    androidx.appcompat.R.attr.colorAccent,
                    Color.parseColor("#263238")
                )
            )
            val background = p.getInt(
                ThemeStorePrefKeys.KEY_BACKGROUND_COLOR,
                ThemeUtils.resolveColor(context, android.R.attr.colorBackground)
            )
            val bottomBg = p.getInt(
                ThemeStorePrefKeys.KEY_BOTTOM_BACKGROUND,
                ThemeUtils.resolveColor(context, android.R.attr.colorBackground)
            )
            // Layer 2: text colors
            val textPrimary = p.getInt(
                ThemeStorePrefKeys.KEY_TEXT_COLOR_PRIMARY,
                ThemeUtils.resolveColor(context, android.R.attr.textColorPrimary)
            )
            val textPrimaryInv = p.getInt(
                ThemeStorePrefKeys.KEY_TEXT_COLOR_PRIMARY_INVERSE,
                ThemeUtils.resolveColor(context, android.R.attr.textColorPrimaryInverse)
            )
            val textSecondary = p.getInt(
                ThemeStorePrefKeys.KEY_TEXT_COLOR_SECONDARY,
                ThemeUtils.resolveColor(context, android.R.attr.textColorSecondary)
            )
            val textSecondaryInv = p.getInt(
                ThemeStorePrefKeys.KEY_TEXT_COLOR_SECONDARY_INVERSE,
                ThemeUtils.resolveColor(context, android.R.attr.textColorSecondaryInverse)
            )
            // Layer 3: M3 tokens (fallbacks may reference layer 1 values)
            val onPrimary = p.getInt(
                ThemeStorePrefKeys.KEY_COLOR_ON_PRIMARY, Color.WHITE
            )
            val primaryContainer = p.getInt(
                ThemeStorePrefKeys.KEY_COLOR_PRIMARY_CONTAINER,
                ColorUtils.blendColors(primary, Color.WHITE, 0.6f)
            )
            val onPrimaryContainer = p.getInt(
                ThemeStorePrefKeys.KEY_COLOR_ON_PRIMARY_CONTAINER,
                ColorUtils.blendColors(primary, Color.BLACK, 0.7f)
            )
            val secondaryContainer = p.getInt(
                ThemeStorePrefKeys.KEY_COLOR_SECONDARY_CONTAINER,
                ColorUtils.blendColors(accent, Color.WHITE, 0.6f)
            )
            val onSecondaryContainer = p.getInt(
                ThemeStorePrefKeys.KEY_COLOR_ON_SECONDARY_CONTAINER,
                ColorUtils.blendColors(accent, Color.BLACK, 0.7f)
            )
            val surface = p.getInt(
                ThemeStorePrefKeys.KEY_COLOR_SURFACE, background
            )
            val onSurface = p.getInt(
                ThemeStorePrefKeys.KEY_COLOR_ON_SURFACE, textPrimary
            )
            val surfaceVariant = p.getInt(
                ThemeStorePrefKeys.KEY_COLOR_SURFACE_VARIANT,
                ColorUtils.blendColors(background, primary, 0.05f)
            )
            val onSurfaceVariant = p.getInt(
                ThemeStorePrefKeys.KEY_COLOR_ON_SURFACE_VARIANT, textSecondary
            )
            val surfaceContainer = p.getInt(
                ThemeStorePrefKeys.KEY_COLOR_SURFACE_CONTAINER,
                ColorUtils.blendColors(background, primary, 0.08f)
            )
            // Layer 4: derived values
            val statusBar = p.getInt(
                ThemeStorePrefKeys.KEY_STATUS_BAR_COLOR, primary
            )
            val navBar = p.getInt(
                ThemeStorePrefKeys.KEY_NAVIGATION_BAR_COLOR, bottomBg
            )
            // Booleans
            val coloredSb = p.getBoolean(ThemeStorePrefKeys.KEY_APPLY_PRIMARYDARK_STATUSBAR, true)
            val coloredNb = p.getBoolean(ThemeStorePrefKeys.KEY_APPLY_PRIMARY_NAVBAR, false)
            val autoGenPd = p.getBoolean(ThemeStorePrefKeys.KEY_AUTO_GENERATE_PRIMARYDARK, true)

            return ThemeColors(
                primaryColor = primary,
                primaryColorDark = primaryDark,
                accentColor = accent,
                statusBarColor = statusBar,
                navigationBarColor = navBar,
                textColorPrimary = textPrimary,
                textColorPrimaryInverse = textPrimaryInv,
                textColorSecondary = textSecondary,
                textColorSecondaryInverse = textSecondaryInv,
                backgroundColor = background,
                bottomBackground = bottomBg,
                colorOnPrimary = onPrimary,
                colorPrimaryContainer = primaryContainer,
                colorOnPrimaryContainer = onPrimaryContainer,
                colorSecondaryContainer = secondaryContainer,
                colorOnSecondaryContainer = onSecondaryContainer,
                colorSurface = surface,
                colorOnSurface = onSurface,
                colorSurfaceVariant = surfaceVariant,
                colorOnSurfaceVariant = onSurfaceVariant,
                colorSurfaceContainer = surfaceContainer,
                coloredStatusBar = coloredSb,
                coloredNavigationBar = coloredNb,
                autoGeneratePrimaryDark = autoGenPd,
            )
        }

        @CheckResult
        fun getColors(context: Context): ThemeColors {
            cachedColors?.let { return it }
            val colors = readAllColors(context)
            cachedColors = colors
            return colors
        }

        // ---- cached companion property (replaces old _accentColor volatile) ----

        var accentColor: Int
            get() = getColors(appCtx).accentColor
            set(@Suppress("UNUSED_PARAMETER") _) {
                // no-op: cache invalidation happens in ThemeStore.apply()
            }

        // ---- cached getters (each delegates to getColors) ----

        @CheckResult
        @ColorInt
        fun primaryColor(context: Context = appCtx): Int = getColors(context).primaryColor

        @CheckResult
        @ColorInt
        fun primaryColorDark(context: Context): Int = getColors(context).primaryColorDark

        @CheckResult
        @ColorInt
        fun accentColor(context: Context = appCtx): Int = getColors(context).accentColor

        @CheckResult
        @ColorInt
        fun statusBarColor(context: Context, transparent: Boolean): Int {
            val c = getColors(context)
            val spValue = prefs(context).getInt(
                ThemeStorePrefKeys.KEY_STATUS_BAR_COLOR, Int.MIN_VALUE
            )
            if (spValue != Int.MIN_VALUE) return spValue
            return if (transparent) c.primaryColor else c.primaryColorDark
        }

        @CheckResult
        @ColorInt
        fun navigationBarColor(context: Context): Int = getColors(context).navigationBarColor

        @CheckResult
        @ColorInt
        fun textColorPrimary(context: Context): Int = getColors(context).textColorPrimary

        @CheckResult
        @ColorInt
        fun textColorPrimaryInverse(context: Context): Int = getColors(context).textColorPrimaryInverse

        @CheckResult
        @ColorInt
        fun textColorSecondary(context: Context): Int = getColors(context).textColorSecondary

        @CheckResult
        @ColorInt
        fun textColorSecondaryInverse(context: Context): Int = getColors(context).textColorSecondaryInverse

        @CheckResult
        @ColorInt
        fun backgroundColor(context: Context = appCtx): Int = getColors(context).backgroundColor

        @CheckResult
        @ColorInt
        fun bottomBackground(context: Context = appCtx): Int = getColors(context).bottomBackground

        @CheckResult
        @ColorInt
        fun colorOnPrimary(context: Context = appCtx): Int = getColors(context).colorOnPrimary

        @CheckResult
        @ColorInt
        fun colorPrimaryContainer(context: Context = appCtx): Int = getColors(context).colorPrimaryContainer

        @CheckResult
        @ColorInt
        fun colorOnPrimaryContainer(context: Context = appCtx): Int = getColors(context).colorOnPrimaryContainer

        @CheckResult
        @ColorInt
        fun colorSecondaryContainer(context: Context = appCtx): Int = getColors(context).colorSecondaryContainer

        @CheckResult
        @ColorInt
        fun colorOnSecondaryContainer(context: Context = appCtx): Int = getColors(context).colorOnSecondaryContainer

        @CheckResult
        @ColorInt
        fun colorSurface(context: Context = appCtx): Int = getColors(context).colorSurface

        @CheckResult
        @ColorInt
        fun colorOnSurface(context: Context = appCtx): Int = getColors(context).colorOnSurface

        @CheckResult
        @ColorInt
        fun colorSurfaceVariant(context: Context = appCtx): Int = getColors(context).colorSurfaceVariant

        @CheckResult
        @ColorInt
        fun colorOnSurfaceVariant(context: Context = appCtx): Int = getColors(context).colorOnSurfaceVariant

        @CheckResult
        @ColorInt
        fun colorSurfaceContainer(context: Context = appCtx): Int = getColors(context).colorSurfaceContainer

        @CheckResult
        fun coloredStatusBar(context: Context): Boolean = getColors(context).coloredStatusBar

        @CheckResult
        fun coloredNavigationBar(context: Context): Boolean = getColors(context).coloredNavigationBar

        @CheckResult
        fun autoGeneratePrimaryDark(context: Context): Boolean = getColors(context).autoGeneratePrimaryDark

        @CheckResult
        fun isConfigured(context: Context): Boolean {
            return prefs(context).getBoolean(ThemeStorePrefKeys.IS_CONFIGURED_KEY, false)
        }

        @SuppressLint("CommitPrefEdits")
        fun isConfigured(context: Context, version: Int): Boolean {
            val prefs = prefs(context)
            val lastVersion = prefs.getInt(ThemeStorePrefKeys.IS_CONFIGURED_VERSION_KEY, -1)
            if (version > lastVersion) {
                prefs.edit().putInt(ThemeStorePrefKeys.IS_CONFIGURED_VERSION_KEY, version).apply()
                return false
            }
            return true
        }
    }
}