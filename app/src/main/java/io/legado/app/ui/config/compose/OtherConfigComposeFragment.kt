package io.legado.app.ui.config.compose

import androidx.compose.runtime.Composable
import androidx.fragment.app.activityViewModels
import io.legado.app.ui.config.CheckSourceConfig
import io.legado.app.ui.config.ConfigViewModel
import io.legado.app.ui.config.DirectLinkUploadConfig
import io.legado.app.utils.showDialogFragment

class OtherConfigComposeFragment : ConfigComposeFragment() {
    private val viewModel by activityViewModels<ConfigViewModel>()

    @Composable
    override fun ConfigContent() {
        OtherConfigScreen(
            onBackClick = { activity?.finish() },
            viewModel = viewModel,
            onCheckSourceClick = { showDialogFragment<CheckSourceConfig>() },
            onUploadRuleClick = { showDialogFragment<DirectLinkUploadConfig>() },
        )
    }
}