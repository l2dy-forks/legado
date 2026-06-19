package io.legado.app.ui.config

import android.app.Application
import android.content.Context
import io.legado.app.R
import io.legado.app.base.BaseViewModel
import io.legado.app.data.appDb
import io.legado.app.help.AppWebDav
import io.legado.app.help.book.BookHelp
import io.legado.app.help.storage.Backup
import io.legado.app.utils.FileUtils
import io.legado.app.utils.restart
import io.legado.app.utils.toastOnUi
import kotlinx.coroutines.delay
import splitties.init.appCtx

class ConfigViewModel(application: Application) : BaseViewModel(application) {

    fun upWebDavConfig() {
        execute {
            AppWebDav.upConfig()
        }
    }

    fun testWebDav() {
        execute {
            AppWebDav.testConnection()
                .onSuccess { context.toastOnUi(R.string.success) }
                .onFailure { context.toastOnUi("WebDAV test failed: ${it.localizedMessage}") }
        }
    }

    fun backupWebDav() {
        execute {
            if (!AppWebDav.isOk) {
                context.toastOnUi(R.string.web_dav_not_configured)
                return@execute
            }
            Backup.backupWebDavLocked(context)
            context.toastOnUi(R.string.success)
        }
    }

    fun restoreWebDav() {
        execute {
            context.toastOnUi(R.string.restore_summary)
        }
    }

    fun backupLocal() {
        execute {
            Backup.backupLocalLocked(context, null)
            context.toastOnUi(R.string.success)
        }
    }

    fun restoreLocal() {
        execute {
            context.toastOnUi(R.string.restore_summary)
        }
    }

    fun clearCache() {
        execute {
            BookHelp.clearCache()
            FileUtils.delete(context.cacheDir.absolutePath)
        }.onSuccess {
            context.toastOnUi(R.string.clear_cache_success)
        }
    }

    fun clearWebViewData() {
        execute {
            FileUtils.delete(context.getDir("webview", Context.MODE_PRIVATE))
            FileUtils.delete(context.getDir("hws_webview", Context.MODE_PRIVATE), true)
            context.toastOnUi(R.string.clear_webview_data_success)
            delay(3000)
            appCtx.restart()
        }
    }

    fun shrinkDatabase() {
        execute {
            appDb.openHelper.writableDatabase.execSQL("VACUUM")
        }.onSuccess {
            context.toastOnUi(R.string.success)
        }
    }

}
