package io.legado.app.ui.config.compose

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import io.legado.app.R
import io.legado.app.base.BaseFragment
import io.legado.app.ui.common.compose.LegadoTheme

abstract class ConfigComposeFragment : BaseFragment(R.layout.fragment_config_compose) {

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<ComposeView>(R.id.composeView).setContent {
            LegadoTheme {
                ConfigContent()
            }
        }
    }

    @Composable
    abstract fun ConfigContent()
}
