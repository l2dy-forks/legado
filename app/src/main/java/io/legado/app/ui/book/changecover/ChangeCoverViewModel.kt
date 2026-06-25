package io.legado.app.ui.book.changecover

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.legado.app.constant.AppConst
import io.legado.app.constant.AppLog
import io.legado.app.constant.AppPattern
import io.legado.app.data.appDb
import io.legado.app.data.entities.BookSource
import io.legado.app.data.entities.SearchBook
import io.legado.app.help.config.AppConfig
import io.legado.app.model.webBook.WebBook
import io.legado.app.utils.mapParallelSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.Collections
import java.util.concurrent.Executors
import kotlin.math.min

class ChangeCoverViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChangeCoverUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ChangeCoverEffect>(extraBufferCapacity = 4)
    val effects = _effects.asSharedFlow()

    private val threadCount = AppConfig.threadCount
    private var searchPool: ExecutorCoroutineDispatcher? = null
    private val searchBooks: MutableList<SearchBook> = Collections.synchronizedList(arrayListOf())
    private var task: Job? = null
    private var name: String = ""
    private var author: String = ""

    private val defaultCover by lazy {
        SearchBook(
            originName = "默认封面",
            name = name,
            author = author,
            coverUrl = "use_default_cover"
        )
    }

    fun onIntent(intent: ChangeCoverIntent) {
        when (intent) {
            is ChangeCoverIntent.Initialize -> initialize(intent.name, intent.author)
            ChangeCoverIntent.ToggleSearch -> startOrStopSearch()
            is ChangeCoverIntent.SelectCover -> selectCover(intent.coverUrl)
            ChangeCoverIntent.Dismiss -> _effects.tryEmit(ChangeCoverEffect.Dismiss)
        }
    }

    private fun initialize(name: String, author: String) {
        this.name = name
        this.author = author.replace(AppPattern.authorRegex, "")

        viewModelScope.launch {
            val cached = withContext(Dispatchers.IO) {
                appDb.searchBookDao.getEnableHasCover(this@ChangeCoverViewModel.name, this@ChangeCoverViewModel.author)
            }
            searchBooks.clear()
            searchBooks.addAll(cached)
            emitBooks()

            if (searchBooks.isEmpty()) {
                startSearch()
            }
        }
    }

    private fun emitBooks() {
        _uiState.update {
            it.copy(books = listOf(defaultCover) + searchBooks.sortedBy { book -> book.originOrder })
        }
    }

    private fun startOrStopSearch() {
        if (task?.isActive == true) {
            stopSearch()
        } else {
            startSearch()
        }
    }

    private fun startSearch() {
        viewModelScope.launch {
            stopSearch()
            searchBooks.clear()
            emitBooks()
            val sourceParts = withContext(Dispatchers.IO) {
                appDb.bookSourceDao.allEnabledPart
            }

            searchPool?.close()
            searchPool = Executors
                .newFixedThreadPool(min(threadCount, AppConst.MAX_THREAD))
                .asCoroutineDispatcher()

            task = viewModelScope.launch(searchPool!!) {
                flow {
                    for (bs in sourceParts) {
                        bs.getBookSource()?.let { emit(it) }
                    }
                }
                    .onStart { _uiState.update { it.copy(isSearching = true) } }
                    .mapParallelSafe(threadCount) { source ->
                        withTimeout(60000L) { searchSource(source) }
                    }
                    .onCompletion { _uiState.update { it.copy(isSearching = false) } }
                    .catch { AppLog.put("封面换源搜索出错\n${it.localizedMessage}", it) }
                    .collect {}
            }
        }
    }

    private fun stopSearch() {
        task?.cancel()
        searchPool?.close()
        _uiState.update { it.copy(isSearching = false) }
    }

    private suspend fun searchSource(source: BookSource) {
        if (source.getSearchRule().coverUrl.isNullOrBlank()) return
        val searchBook = WebBook.searchBookAwait(source, name, shouldBreak = { it > 0 }).firstOrNull()
            ?: return
        if (searchBook.name == name && searchBook.author == author && !searchBook.coverUrl.isNullOrEmpty()) {
            appDb.searchBookDao.insert(searchBook)
            if (!searchBooks.contains(searchBook)) {
                searchBooks.add(searchBook)
                emitBooks()
            }
        }
    }

    private fun selectCover(coverUrl: String) {
        _effects.tryEmit(ChangeCoverEffect.CoverSelected(coverUrl))
    }

    override fun onCleared() {
        super.onCleared()
        searchPool?.close()
    }
}
