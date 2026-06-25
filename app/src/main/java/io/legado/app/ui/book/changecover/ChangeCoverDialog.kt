package io.legado.app.ui.book.changecover

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.filletBackground
import io.legado.app.ui.common.compose.LegadoTheme
import io.legado.app.utils.setLayout

/**
 * 换封面弹窗 — Compose 版。
 */
class ChangeCoverDialog() : DialogFragment() {

    constructor(name: String, author: String) : this() {
        arguments = Bundle().apply {
            putString("name", name)
            putString("author", author)
        }
    }

    private val viewModel by viewModels<ChangeCoverViewModel>()

    override fun onStart() {
        super.onStart()
        setLayout(0.95f, 0.9f)
        if (!AppConfig.isEInkMode) {
            dialog?.window?.setBackgroundDrawable(requireContext().filletBackground)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val name = arguments?.getString("name").orEmpty()
        val author = arguments?.getString("author").orEmpty()
        viewModel.onIntent(ChangeCoverIntent.Initialize(name, author))

        return ComposeView(requireContext()).apply {
            setContent {
                LegadoTheme {
                    ChangeCoverScreen(
                        viewModel = viewModel,
                        onDismiss = { dismissAllowingStateLoss() },
                        onCoverSelected = { coverUrl ->
                            (activity as? CallBack)?.coverChangeTo(coverUrl)
                            dismissAllowingStateLoss()
                        },
                    )
                }
            }
        }
    }

    interface CallBack {
        fun coverChangeTo(coverUrl: String)
    }

    companion object {
        fun create(name: String, author: String): ChangeCoverDialog {
            return ChangeCoverDialog().apply {
                arguments = Bundle().apply {
                    putString("name", name)
                    putString("author", author)
                }
            }
        }
    }
}
