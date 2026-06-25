package io.legado.app.ui.book.explore

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.legado.app.ui.book.info.compose.BookInfoComposeActivity
import io.legado.app.ui.common.compose.LegadoTheme
import io.legado.app.utils.startActivity

class ExploreShowActivity : AppCompatActivity() {

    private val viewModel by viewModels<ExploreShowViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.run {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = android.graphics.Color.TRANSPARENT
        }

        val sourceUrl = intent.getStringExtra("sourceUrl").orEmpty()
        val exploreUrl = intent.getStringExtra("exploreUrl").orEmpty()
        val title = intent.getStringExtra("exploreName").orEmpty()
        viewModel.onIntent(ExploreShowIntent.Initialize(sourceUrl, exploreUrl, title))

        setContent {
            LegadoTheme {
                ExploreShowScreen(
                    viewModel = viewModel,
                    onBack = { finish() },
                    onOpenBookInfo = { name, author, bookUrl ->
                        startActivity<BookInfoComposeActivity> {
                            putExtra("name", name)
                            putExtra("author", author)
                            putExtra("bookUrl", bookUrl)
                        }
                    },
                )
            }
        }
    }
}
