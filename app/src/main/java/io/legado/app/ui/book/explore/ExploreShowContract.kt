package io.legado.app.ui.book.explore

import androidx.compose.runtime.Stable
import io.legado.app.data.entities.SearchBook

@Stable
data class ExploreShowUiState(
    val title: String = "",
    val books: List<SearchBook> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
)

sealed interface ExploreShowIntent {
    data class Initialize(val sourceUrl: String, val exploreUrl: String, val title: String) : ExploreShowIntent
    data object LoadMore : ExploreShowIntent
    data class OpenBook(val book: SearchBook) : ExploreShowIntent
}

sealed interface ExploreShowEffect {
    data class OpenBookInfo(val name: String, val author: String, val bookUrl: String) : ExploreShowEffect
    data class ShowError(val message: String) : ExploreShowEffect
}
