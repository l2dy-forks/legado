package io.legado.app.ui.dict.rule.ai

import android.app.Application
import io.legado.app.base.BaseViewModel
import io.legado.app.data.appDb
import io.legado.app.data.entities.AiDictRule
import kotlinx.coroutines.flow.Flow

class AiDictRuleViewModel(application: Application) : BaseViewModel(application) {

    val rulesFlow: Flow<List<AiDictRule>> = appDb.aiDictRuleDao.flowAll()

    fun toggleEnabled(rule: AiDictRule) {
        execute {
            val updated = rule.copy(enabled = !rule.enabled)
            appDb.aiDictRuleDao.update(updated)
        }
    }

    fun delete(name: String) {
        execute {
            val rule = appDb.aiDictRuleDao.getByName(name)
            if (rule != null) {
                appDb.aiDictRuleDao.delete(rule)
            }
        }
    }
}
