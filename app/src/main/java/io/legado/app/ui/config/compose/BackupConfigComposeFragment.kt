package io.legado.app.ui.config.compose

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import io.legado.app.R
import io.legado.app.constant.AppLog
import io.legado.app.constant.PreferKey
import io.legado.app.exception.NoStackTraceException
import io.legado.app.help.AppWebDav
import io.legado.app.help.config.AppConfig
import io.legado.app.help.config.LocalConfig
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.help.storage.Backup
import io.legado.app.help.storage.BackupConfig
import io.legado.app.help.storage.ImportOldData
import io.legado.app.help.storage.Restore
import io.legado.app.lib.dialogs.alert
import io.legado.app.lib.dialogs.selector
import io.legado.app.ui.config.ConfigViewModel
import io.legado.app.ui.file.HandleFileContract
import io.legado.app.ui.widget.dialog.WaitDialog
import io.legado.app.utils.getPrefString
import io.legado.app.utils.isContentScheme
import io.legado.app.utils.showHelp
import io.legado.app.utils.showLogSheet
import io.legado.app.utils.toastOnUi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import splitties.init.appCtx

class BackupConfigComposeFragment : ConfigComposeFragment() {
    private val viewModel by activityViewModels<ConfigViewModel>()
    private val waitDialog by lazy { WaitDialog(requireContext()) }
    private var backupPath by mutableStateOf("")
    private var restoreJob: Job? = null

    private val selectBackupPath = registerForActivityResult(HandleFileContract()) {
        it.uri?.let { uri ->
            val path = if (uri.isContentScheme()) uri.toString() else uri.path ?: return@registerForActivityResult
            AppConfig.backupPath = path
            backupPath = path
        }
    }

    private val restoreDoc = registerForActivityResult(HandleFileContract()) {
        it.uri?.let { uri ->
            waitDialog.setText("恢复中…")
            waitDialog.show()
            val task = Coroutine.async {
                Restore.restore(appCtx, uri)
            }.onFinally {
                waitDialog.dismiss()
            }
            waitDialog.setOnCancelListener {
                task.cancel()
            }
        }
    }

    private val restoreOld = registerForActivityResult(HandleFileContract()) {
        it.uri?.let { uri ->
            ImportOldData.importUri(appCtx, uri)
        }
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        super.onFragmentCreated(view, savedInstanceState)
        backupPath = getPrefString(PreferKey.backupPath) ?: ""
        if (!LocalConfig.backupHelpVersionIsLast) {
            showHelp("webDavHelp")
        }
    }

    @Composable
    override fun ConfigContent() {
        BackupConfigScreen(
            onBackClick = { activity?.finish() },
            viewModel = viewModel,
            onBackupPathClick = { selectBackupPath.launch {} },
            onRestoreIgnoreClick = { backupIgnore() },
            onImportOldClick = { restoreOld.launch {} },
            onLocalRestoreClick = {
                restoreDoc.launch {
                    title = getString(R.string.select_restore_file)
                    mode = HandleFileContract.FILE
                    allowExtensions = arrayOf("zip")
                }
            },
            onWebDavRestoreClick = { webDavRestore() },
            onHelpClick = { showHelp("webDavHelp") },
            onLogClick = { showLogSheet() },
        )
    }

    private fun backupIgnore() {
        val checkedItems = BooleanArray(BackupConfig.ignoreKeys.size) {
            BackupConfig.ignoreConfig[BackupConfig.ignoreKeys[it]] ?: false
        }
        alert(R.string.restore_ignore) {
            multiChoiceItems(BackupConfig.ignoreTitle, checkedItems) { _, which, isChecked ->
                BackupConfig.ignoreConfig[BackupConfig.ignoreKeys[which]] = isChecked
            }
            onDismiss {
                BackupConfig.saveIgnoreConfig()
            }
        }
    }

    private fun webDavRestore() {
        waitDialog.setText(R.string.loading)
        waitDialog.setOnCancelListener {
            restoreJob?.cancel()
        }
        waitDialog.show()
        Coroutine.async {
            restoreJob = coroutineContext[Job]
            showRestoreDialog(requireContext())
        }.onError {
            AppLog.put("WebDav恢复出错\n${it.localizedMessage}", it)
            if (context == null) return@onError
            appCtx.toastOnUi("WebDav恢复出错\n${it.localizedMessage}")
        }.onFinally {
            waitDialog.dismiss()
        }
    }

    private suspend fun showRestoreDialog(context: android.content.Context) {
        val names = withContext(IO) { AppWebDav.getBackupNames() }
        if (AppWebDav.isJianGuoYun && names.size > 700) {
            context.toastOnUi("由于坚果云限制列出文件数量，部分备份可能未显示，请及时清理旧备份")
        }
        if (names.isNotEmpty()) {
            currentCoroutineContext().ensureActive()
            withContext(Main) {
                context.selector(
                    title = context.getString(R.string.select_restore_file),
                    items = names
                ) { _, index ->
                    if (index in 0 until names.size) {
                        view?.post { restoreWebDav(names[index]) }
                    }
                }
            }
        } else {
            throw NoStackTraceException("Web dav no back up file")
        }
    }

    private fun restoreWebDav(name: String) {
        waitDialog.setText("恢复中…")
        waitDialog.show()
        val task = Coroutine.async {
            AppWebDav.restoreWebDav(name)
        }.onError {
            AppLog.put("WebDav恢复出错\n${it.localizedMessage}", it)
            appCtx.toastOnUi("WebDav恢复出错\n${it.localizedMessage}")
        }.onFinally {
            waitDialog.dismiss()
        }
        waitDialog.setOnCancelListener {
            task.cancel()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        waitDialog.dismiss()
    }
}
