package io.legado.app.ui.book.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.legado.app.BuildConfig
import io.legado.app.constant.AppLog
import io.legado.app.data.appDb
import io.legado.app.data.entities.BookSource
import io.legado.app.data.entities.SearchBook
import io.legado.app.help.book.isNotShelf
import io.legado.app.model.webBook.WebBook
import io.legado.app.utils.printOnDebug
import io.legado.app.utils.stackTraceStr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreShowViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ExploreShowUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ExploreShowEffect>(extraBufferCapacity = 8)
    val effects = _effects.asSharedFlow()

    private var bookSource: BookSource? = null
    private var exploreUrl: String? = null
    private var page = 1
    private var books = linkedSetOf<SearchBook>()
    private var bookshelfKeys = hashSetOf<String>()
    private var isLoading = false

    init {
        observeBookshelf()
    }

    fun onIntent(intent: ExploreShowIntent) {
        when (intent) {
            is ExploreShowIntent.Initialize -> initialize(intent.sourceUrl, intent.exploreUrl, intent.title)
            ExploreShowIntent.LoadMore -> loadMore()
            is ExploreShowIntent.OpenBook -> openBook(intent.book)
        }
    }

    private fun initialize(sourceUrl: String, exploreUrl: String, title: String) {
        _uiState.update { it.copy(title = title) }
        this.exploreUrl = exploreUrl
        viewModelScope.launch {
            bookSource = withContext(Dispatchers.IO) {
                appDb.bookSourceDao.getBookSource(sourceUrl)
            }
            loadMore()
        }
    }

    private fun loadMore() {
        if (isLoading) return
        val url = exploreUrl ?: return
        val source = bookSource
        if (source == null || url.isBlank()) return

        isLoading = true
        _uiState.update { it.copy(isLoading = true, error = null) }

        WebBook.exploreBook(viewModelScope, source, url, page)
            .timeout(if (BuildConfig.DEBUG) 0L else 30000L)
            .onSuccess(Dispatchers.IO) { searchBooks ->
                books.addAll(searchBooks)
                appDb.searchBookDao.insert(*searchBooks.toTypedArray())
                page++
                _uiState.update {
                    it.copy(
                        books = books.toList(),
                        isLoading = false,
                        hasMore = searchBooks.isNotEmpty(),
                    )
                }
            }.onError {
                it.printOnDebug()
                val msg = it.stackTraceStr
                _uiState.update { it.copy(isLoading = false, error = msg) }
                _effects.tryEmit(ExploreShowEffect.ShowError(msg))
            }.onFinally {
                isLoading = false
            }
    }

    private fun openBook(book: SearchBook) {
        _effects.tryEmit(
            ExploreShowEffect.OpenBookInfo(
                name = book.name, author = book.author, bookUrl = book.bookUrl,
            )
        )
    }

    fun isInBookshelf(book: SearchBook): Boolean {
        val key = if (book.author.isNotBlank()) "${book.name}-${book.author}" else book.name
        return bookshelfKeys.contains(key) || bookshelfKeys.contains(book.bookUrl)
    }

    private fun observeBookshelf() {
        viewModelScope.launch {
            appDb.bookDao.flowAll().mapLatest { books ->
                val keys = hashSetOf<String>()
                books.filterNot { it.isNotShelf }.forEach {
                    keys.add("${it.name}-${it.author}")
                    keys.add(it.name)
                    keys.add(it.bookUrl)
                }
                keys
            }.catch {
                AppLog.put("发现列表界面获取书籍数据失败\n${it.localizedMessage}", it)
            }.collect { keys ->
                bookshelfKeys = keys
                // Trigger recomposition by updating state
                _uiState.update { it.copy() }
            }
        }
    }
}
