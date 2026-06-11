package io.legado.app.ui.book.info.compose

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.compose.viewModel
import io.legado.app.R
import io.legado.app.help.book.isWebFile
import io.legado.app.ui.book.info.BookInfoViewModel
import io.legado.app.utils.sendToClip
import io.legado.app.utils.toastOnUi
import io.legado.app.utils.StartActivityContract
import io.legado.app.utils.openFileUri
import io.legado.app.utils.sendToClip
import io.legado.app.utils.shareWithQr
import io.legado.app.utils.showDialogFragment
import io.legado.app.utils.startActivity
import io.legado.app.utils.toastOnUi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 书籍详情页路由包装器（Compose Route 版）。
 * 将原 [BookInfoComposeActivity] 的逻辑提取为纯 Composable，
 * 用于在 Navigation 3 NavDisplay 中渲染。
 * 所有 ActivityResult 回调通过 [rememberLauncherForActivityResult] 在 Composable 中处理。
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BookInfoRouteScreen(
    bookUrl: String?,
    name: String?,
    author: String?,
    onBack: () -> Unit,
    onReadBook: (String, Boolean, Boolean) -> Unit = { _, _, _ -> },
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedCoverKey: String? = null,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    // ViewModel 作用域绑定到父 Fragment/Activity
    val viewModel: BookInfoViewModel = viewModel(viewModelStoreOwner = context as androidx.lifecycle.ViewModelStoreOwner)

    // Init data from route params (instead of Intent)
    LaunchedEffect(bookUrl, name, author) {
        val intent = Intent().apply {
            name?.let { putExtra("name", it) }
            author?.let { putExtra("author", it) }
            bookUrl?.let { putExtra("bookUrl", it) }
        }
        viewModel.initData(intent)
    }

    // Observe LiveData via manual observer (LiveData doesn't support collectAsStateWithLifecycle directly)
    var book by remember { mutableStateOf(viewModel.bookData.value) }
    var chapterList by remember { mutableStateOf(viewModel.chapterListData.value) }
    var inBookshelfState by remember { mutableStateOf(viewModel.inBookshelf) }

    DisposableEffect(Unit) {
        val bookObserver = Observer<io.legado.app.data.entities.Book?> { book = it }
        val chObserver = Observer<List<io.legado.app.data.entities.BookChapter>?> { chapterList = it }
        viewModel.bookData.observeForever(bookObserver)
        viewModel.chapterListData.observeForever(chObserver)
        onDispose {
            viewModel.bookData.removeObserver(bookObserver)
            viewModel.chapterListData.removeObserver(chObserver)
        }
    }

    // Sync inBookshelf
    LaunchedEffect(Unit) {
        inBookshelfState = viewModel.inBookshelf
    }

    if (book != null) {
        val b = book!!
        var canUpdateState by remember(b) { mutableStateOf(b.canUpdate) }
        var splitLongChapterState by remember(b) { mutableStateOf(b.getSplitLongChapter()) }

        BookDetailScreen(
            book = b,
            latestChapterTitle = b.latestChapterTitle,
            totalChapterNum = b.totalChapterNum,
            onBack = onBack,
            onReadClick = {
                if (b.isWebFile) {
                    activity?.toastOnUi("暂不支持 Web 文件")
                } else {
                    onReadBook(b.bookUrl, viewModel.inBookshelf, false)
                }
            },
            onShelfClick = {
                if (!b.isWebFile) {
                    viewModel.addToBookshelf { inBookshelfState = true }
                }
            },
            inBookshelf = inBookshelfState,
            onTocClick = {
                if (chapterList.isNullOrEmpty()) {
                    activity?.toastOnUi(R.string.chapter_list_empty)
                } else {
                    onReadBook(b.bookUrl, viewModel.inBookshelf, false)
                }
            },
            onEditClick = { activity?.toastOnUi("编辑请使用完整详情页") },
            onMenuAction = { action ->
                when (action) {
                    MENU_CAN_UPDATE -> {
                        b.canUpdate = !b.canUpdate
                        canUpdateState = b.canUpdate
                        if (viewModel.inBookshelf) viewModel.saveBook(b)
                    }
                    MENU_SPLIT_LONG_CHAPTER -> {
                        b.setSplitLongChapter(!b.getSplitLongChapter())
                        splitLongChapterState = b.getSplitLongChapter()
                        viewModel.loadBookInfo(b, false)
                    }
                    MENU_REFRESH -> viewModel.refreshBook(b)
                    MENU_TOP -> viewModel.topBook()
                    MENU_CLEAR_CACHE -> viewModel.clearCache()
                    MENU_COPY_BOOK_URL -> context.sendToClip(b.bookUrl)
                    MENU_COPY_TOC_URL -> b.tocUrl?.let { context.sendToClip(it) }
                }
            },
            canUpdate = canUpdateState,
            splitLongChapter = splitLongChapterState,
            isLoginVisible = !viewModel.bookSource?.loginUrl.isNullOrBlank(),
            isSourceVariableVisible = viewModel.bookSource != null,
            isBookVariableVisible = viewModel.bookSource != null,
            coverTransitionName = sharedCoverKey,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            sharedCoverKey = sharedCoverKey,
        )
    }
}
