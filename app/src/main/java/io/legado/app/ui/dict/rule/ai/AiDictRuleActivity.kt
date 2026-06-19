package io.legado.app.ui.dict.rule.ai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.legado.app.ui.common.compose.LegadoTheme
import io.legado.app.utils.startActivity

class AiDictRuleActivity : AppCompatActivity() {

    private val viewModel by viewModels<AiDictRuleViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LegadoTheme {
                val rules by viewModel.rulesFlow.collectAsState(initial = emptyList())
                AiDictRuleListScreen(
                    rules = rules,
                    onAdd = { startActivity<AiDictRuleEditActivity>() },
                    onEdit = { name ->
                        startActivity<AiDictRuleEditActivity> {
                            putExtra("name", name)
                        }
                    },
                    onToggleEnabled = { viewModel.toggleEnabled(it) },
                    onDelete = { viewModel.delete(it) },
                    onBack = { finish() },
                )
            }
        }
    }
}
