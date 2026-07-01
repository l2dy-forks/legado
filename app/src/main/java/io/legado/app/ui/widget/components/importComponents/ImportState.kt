package io.legado.app.ui.widget.components.importComponents

data class ImportItemWrapper<T>(
    val data: T,
    val oldData: T? = null,
    val isSelected: Boolean = true,
    val status: ImportStatus = ImportStatus.New
)

enum class ImportStatus {
    New,
    Update,
    Existing,
    Error
}

sealed interface BaseImportUiState<out T> {
    data object Idle : BaseImportUiState<Nothing>
    data object Loading : BaseImportUiState<Nothing>
    data class Error(val msg: String) : BaseImportUiState<Nothing>
    data class Success<T>(
        val source: String,
        val items: List<ImportItemWrapper<T>>,
        val version: Int = 0,
        val keepOriginalName: Boolean = false,
        val customGroup: String? = null,
        val isAddGroup: Boolean = false
    ) : BaseImportUiState<T>
}
