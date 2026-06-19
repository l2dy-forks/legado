package io.legado.app.ui.dict

import io.legado.app.ui.widget.dialog.LegadoSheetDialog

fun createDictSheetDialog(word: String): LegadoSheetDialog =
    LegadoSheetDialog.create { requestDismiss ->
        DictSheetScreen(word = word, onDismiss = requestDismiss)
    }
