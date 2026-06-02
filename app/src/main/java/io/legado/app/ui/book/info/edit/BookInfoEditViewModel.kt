package io.legado.app.ui.book.info.edit

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.legado.app.base.BaseViewModel
import io.legado.app.constant.AppLog
import io.legado.app.constant.EventBus
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.model.ReadBook
import io.legado.app.utils.postEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookInfoEditViewModel(application: Application) : BaseViewModel(application) {
    var book: Book? = null
    val bookData = MutableLiveData<Book>()

    fun loadBook(bookUrl: String) {
        execute {
            book = appDb.bookDao.getBook(bookUrl)
            book?.let {
                bookData.postValue(it)
            }
        }
    }

    fun saveBook(book: Book, success: (() -> Unit)?) {
        // 使用标准协程替代 Coroutine 链式 API，避免「协程太快完成导致回调不执行」的问题
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (ReadBook.book?.bookUrl == book.bookUrl) {
                        ReadBook.book = book
                    }
                    appDb.bookDao.update(book)
                }
                postEvent(EventBus.BOOKSHELF_REFRESH, "")
                success?.invoke()
            } catch (e: Exception) {
                if (e is SQLiteConstraintException) {
                    AppLog.put("书籍信息保存失败，存在相同书名作者书籍\n$e", e, true)
                } else {
                    AppLog.put("书籍信息保存失败\n$e", e, true)
                }
            }
        }
    }
}