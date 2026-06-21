package io.legado.app.ui.dict.rule.ai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.legado.app.ui.common.compose.LegadoTheme

class AiDictRuleEditActivity : AppCompatActivity() {

    private val editViewModel by viewModels<AiDictRuleEditViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val editName = intent.getStringExtra("name")

        setContent {
            LegadoTheme {
                var saving by remember { mutableStateOf(false) }

                AiDictRuleEditScreen(
                    editName = editName,
                    editViewModel = editViewModel,
                    saving = saving,
                    onSaved = { finish() },
                    onDeleted = { finish() },
                    onBack = { finish() },
                    onSaveRequested = { saving = true },
                )
            }
        }
    }
}
