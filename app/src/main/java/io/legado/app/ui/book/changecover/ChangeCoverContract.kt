package io.legado.app.ui.book.changecover

import androidx.compose.runtime.Stable
import io.legado.app.data.entities.SearchBook

@Stable
data class ChangeCoverUiState(
    val books: List<SearchBook> = emptyList(),
    val isSearching: Boolean = false,
)

sealed interface ChangeCoverIntent {
    data class Initialize(val name: String, val author: String) : ChangeCoverIntent
    data object ToggleSearch : ChangeCoverIntent
    data class SelectCover(val coverUrl: String) : ChangeCoverIntent
    data object Dismiss : ChangeCoverIntent
}

sealed interface ChangeCoverEffect {
    data class CoverSelected(val coverUrl: String) : ChangeCoverEffect
    data object Dismiss : ChangeCoverEffect
}
