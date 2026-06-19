package io.legado.app.ui.main.my

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import io.legado.app.R
import io.legado.app.base.BaseFragment
import io.legado.app.constant.EventBus
import io.legado.app.constant.PreferKey
import io.legado.app.databinding.FragmentMyConfigBinding
import io.legado.app.help.config.ThemeConfig
import io.legado.app.lib.dialogs.selector
import io.legado.app.lib.theme.primaryColor
import io.legado.app.service.WebService
import io.legado.app.ui.about.AboutActivity
import io.legado.app.ui.about.ReadRecordActivity
import io.legado.app.ui.book.bookmark.AllBookmarkActivity
import io.legado.app.ui.book.source.manage.BookSourceActivity
import io.legado.app.ui.book.toc.rule.TxtTocRuleActivity
import io.legado.app.ui.common.compose.LegadoTheme
import io.legado.app.ui.config.ConfigActivity
import io.legado.app.ui.config.ConfigTag
import io.legado.app.ui.dict.rule.DictRuleActivity
import io.legado.app.ui.file.FileManageActivity
import io.legado.app.ui.main.MainFragmentInterface
import io.legado.app.ui.replace.ReplaceRuleActivity
import io.legado.app.utils.applyNavigationBarPadding
import io.legado.app.utils.dpToPx
import io.legado.app.utils.getPrefBoolean
import io.legado.app.utils.getPrefString
import io.legado.app.utils.observeEventSticky
import io.legado.app.utils.openUrl
import io.legado.app.utils.putPrefBoolean
import io.legado.app.utils.putPrefString
import io.legado.app.utils.sendToClip
import io.legado.app.utils.setEdgeEffectColor
import io.legado.app.utils.showHelp
import io.legado.app.utils.startActivity
import io.legado.app.utils.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyFragment() : BaseFragment(R.layout.fragment_my_config), MainFragmentInterface {

    constructor(position: Int) : this() {
        val bundle = Bundle()
        bundle.putInt("position", position)
        arguments = bundle
    }

    override val position: Int? get() = arguments?.getInt("position")

    private val binding by viewBinding(FragmentMyConfigBinding::bind)

    private val _webServiceRunning = MutableStateFlow(WebService.isRun)
    private val _webServiceAddress = MutableStateFlow(WebService.hostAddress)

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        setSupportToolbar(binding.titleBar.toolbar)

        observeEventSticky<String>(EventBus.WEB_SERVICE) {
            _webServiceRunning.value = WebService.isRun
            _webServiceAddress.value = WebService.hostAddress
        }

        val composeView = ComposeView(requireContext()).apply {
            setContent {
                LegadoTheme {
                    MyContent(
                        webServiceRunning = _webServiceRunning,
                        webServiceAddress = _webServiceAddress,
                        lifecycleScope = lifecycleScope,
                    )
                }
            }
        }
        binding.preFragment.addView(composeView)
    }

    override fun onCompatCreateOptionsMenu(menu: Menu) {
        menuInflater.inflate(R.menu.main_my, menu)
    }

    override fun onCompatOptionsItemSelected(item: MenuItem) {
        when (item.itemId) {
            R.id.menu_help -> showHelp("appHelp")
        }
    }

    @Composable
    private fun MyContent(
        webServiceRunning: StateFlow<Boolean>,
        webServiceAddress: StateFlow<String>,
        lifecycleScope: kotlinx.coroutines.CoroutineScope,
    ) {
        val context = LocalContext.current
        var themeMode by mutableStateOf(context.getPrefString(PreferKey.themeMode, "0") ?: "0")

        val isRunning by webServiceRunning.collectAsState()
        val hostAddress by webServiceAddress.collectAsState()

        MyScreen(
            themeMode = themeMode,
            webServiceRunning = isRunning,
            webServiceAddress = hostAddress,
            onBookSourceManage = { startActivity<BookSourceActivity>() },
            onTxtTocRuleManage = { startActivity<TxtTocRuleActivity>() },
            onReplaceManage = { startActivity<ReplaceRuleActivity>() },
            onDictRuleManage = { startActivity<DictRuleActivity>() },
            onBookmark = { startActivity<AllBookmarkActivity>() },
            onReadRecord = { startActivity<ReadRecordActivity>() },
            onBackupRestore = {
                startActivity<ConfigActivity> { putExtra("configTag", ConfigTag.BACKUP_CONFIG) }
            },
            onThemeSetting = {
                startActivity<ConfigActivity> { putExtra("configTag", ConfigTag.THEME_CONFIG) }
            },
            onThemeModeChange = { newValue ->
                themeMode = newValue
                context.putPrefString(PreferKey.themeMode, newValue)
                ThemeConfig.applyDayNight(context)
            },
            onOtherSetting = {
                startActivity<ConfigActivity> { putExtra("configTag", ConfigTag.OTHER_CONFIG) }
            },
            onWebServiceChange = { checked ->
                if (checked) {
                    WebService.start(context)
                } else {
                    WebService.stop(context)
                }
                context.putPrefBoolean(PreferKey.webService, checked)
            },
            onWebServiceLongClick = {
                if (WebService.isRun) {
                    context.selector(arrayListOf("复制地址", "浏览器打开")) { _, i ->
                        when (i) {
                            0 -> context.sendToClip(WebService.hostAddress)
                            1 -> context.openUrl(WebService.hostAddress)
                        }
                    }
                }
            },
            onFileManage = { startActivity<FileManageActivity>() },
            onAbout = { startActivity<AboutActivity>() },
            onExit = { activity?.finish() },
        )
    }
}
