package io.legado.app.ui.main.my

import androidx.compose.runtime.Composable
import io.legado.app.ui.about.AboutScreen

/**
 * 关于页回调集合。宿主为 MyFragment。onBack 由 NavDisplay backStack pop 处理，不在此处。
 */
data class AboutActions(
    val onShare: () -> Unit,
    val onScoring: () -> Unit,
    val onContributors: () -> Unit,
    val onUpdateLog: () -> Unit,
    val onCheckUpdate: () -> Unit,
    val onCrashLog: () -> Unit,
    val onSaveLog: () -> Unit,
    val onCreateHeapDump: () -> Unit,
    val onPrivacyPolicy: () -> Unit,
    val onLicense: () -> Unit,
    val onDisclaimer: () -> Unit,
)

/**
 * 关于页路由内容。纯回调、无 ViewModel。
 * 回调实现宿主为 MyFragment（能 share/openUrl/showMarkdownSheet/showCrashLogSheet/checkUpdate 等）。
 */
@Composable
fun MyAboutRoute(
    onBack: () -> Unit,
    actions: AboutActions,
) {
    AboutScreen(
        onBackClick = onBack,
        onShareClick = actions.onShare,
        onScoringClick = actions.onScoring,
        onContributorsClick = actions.onContributors,
        onUpdateLogClick = actions.onUpdateLog,
        onCheckUpdateClick = actions.onCheckUpdate,
        onCrashLogClick = actions.onCrashLog,
        onSaveLogClick = actions.onSaveLog,
        onCreateHeapDumpClick = actions.onCreateHeapDump,
        onPrivacyPolicyClick = actions.onPrivacyPolicy,
        onLicenseClick = actions.onLicense,
        onDisclaimerClick = actions.onDisclaimer,
    )
}

