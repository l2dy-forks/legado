package io.legado.app.ui.about

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import io.legado.app.R
import io.legado.app.constant.EventBus
import io.legado.app.data.appDb
import io.legado.app.lib.theme.ThemeStore
import io.legado.app.ui.book.readRecord.ReadRecordEffect
import io.legado.app.ui.book.readRecord.ReadRecordScreen
import io.legado.app.ui.book.readRecord.ReadRecordViewModel
import io.legado.app.ui.book.search.SearchActivity
import io.legado.app.ui.common.compose.LegadoTheme
import io.legado.app.utils.observeEvent
import io.legado.app.utils.startActivity
import io.legado.app.utils.startActivityForBook
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 阅读记录页 — Compose 版（UI 样式参考 MD3 的 ReadRecordScreen）
 *
 * 数据层复用现有 ReadRecordDao 和 DailyReadRecordDao，
 * 无需新增数据库表。
 */
class ReadRecordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = ThemeStore.primaryColor(this)

        observeEvent<String>(EventBus.RECREATE) {
            recreate()
        }

        setContent {
            LegadoTheme {
                val viewModel = remember { ReadRecordViewModel() }
                val state by viewModel.uiState.collectAsState()

                ReadRecordScreen(
                    state = state,
                    onIntent = viewModel::onIntent,
                    effects = viewModel.effects,
                    onBack = { finish() },
                    onOverviewClick = { startActivity<ReadRecordOverviewActivity>() },
                    onNavigateToBook = { bookName, author ->
                        lifecycleScope.launch {
                            val book = withContext(IO) {
                                appDb.bookDao.findByName(bookName).firstOrNull()
                            }
                            if (book != null) {
                                startActivityForBook(book)
                            } else {
                                SearchActivity.start(this@ReadRecordActivity, bookName)
                            }
                        }
                    },
                    onNavigateToSearch = { bookName ->
                        SearchActivity.start(this@ReadRecordActivity, bookName)
                    },
                )
            }
        }
    }
}
