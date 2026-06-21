package io.legado.app.ui.main.my

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import io.legado.app.ui.config.ConfigViewModel
import io.legado.app.ui.config.compose.BackupConfigScreen
import io.legado.app.ui.config.compose.OtherConfigScreen
import io.legado.app.ui.config.compose.ThemeConfigScreen

/** 其他设置页回调集合。宿主为 MyFragment。 */
data class OtherConfigActions(
    val onCheckSource: () -> Unit,
    val onUploadRule: () -> Unit,
)

/** 备份页回调集合。宿主为 MyFragment（含 HandleFileContract launcher、WaitDialog、WebDav 恢复）。 */
data class BackupConfigActions(
    val onBackupPath: () -> Unit,
    val onRestoreIgnore: () -> Unit,
    val onImportOld: () -> Unit,
    val onLocalRestore: () -> Unit,
    val onWebDavRestore: () -> Unit,
    val onHelp: () -> Unit,
    val onLog: () -> Unit,
)

/** 主题设置页回调集合。宿主为 MyFragment（含颜色选择器、背景图 launcher）。 */
data class ThemeConfigActions(
    val onRequestColorPicker: (String, Color, (Color) -> Unit) -> Unit,
    val onThemeList: () -> Unit,
    val onBgImage: (Boolean) -> Unit,
    val onThemeModeToggle: () -> Unit,
)

/** 其他设置路由内容。ConfigViewModel 借 Fragment 的 ViewModelStore。 */
@Composable
fun MyOtherConfigRoute(
    fragment: Fragment,
    onBack: () -> Unit,
    actions: OtherConfigActions,
) {
    val viewModel: ConfigViewModel = viewModel(viewModelStoreOwner = fragment)
    OtherConfigScreen(
        onBackClick = onBack,
        viewModel = viewModel,
        onCheckSourceClick = actions.onCheckSource,
        onUploadRuleClick = actions.onUploadRule,
    )
}

/** 备份路由内容。ConfigViewModel 借 Fragment 的 ViewModelStore。 */
@Composable
fun MyBackupConfigRoute(
    fragment: Fragment,
    onBack: () -> Unit,
    actions: BackupConfigActions,
) {
    val viewModel: ConfigViewModel = viewModel(viewModelStoreOwner = fragment)
    BackupConfigScreen(
        onBackClick = onBack,
        viewModel = viewModel,
        onBackupPathClick = actions.onBackupPath,
        onRestoreIgnoreClick = actions.onRestoreIgnore,
        onImportOldClick = actions.onImportOld,
        onLocalRestoreClick = actions.onLocalRestore,
        onWebDavRestoreClick = actions.onWebDavRestore,
        onHelpClick = actions.onHelp,
        onLogClick = actions.onLog,
    )
}

/** 主题设置路由内容。无 ViewModel。 */
@Composable
fun MyThemeConfigRoute(
    onBack: () -> Unit,
    actions: ThemeConfigActions,
) {
    ThemeConfigScreen(
        onBackClick = onBack,
        onRequestColorPicker = actions.onRequestColorPicker,
        onThemeListClick = actions.onThemeList,
        onBgImageClick = actions.onBgImage,
        onThemeModeToggle = actions.onThemeModeToggle,
    )
}
