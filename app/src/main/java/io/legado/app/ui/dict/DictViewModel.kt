package io.legado.app.ui.dict

import android.app.Application
import io.legado.app.base.BaseViewModel
import io.legado.app.data.appDb
import io.legado.app.data.entities.AiDictRule
import io.legado.app.data.entities.DictRule
import io.legado.app.help.coroutine.Coroutine

sealed class DictTab {
    abstract val name: String
    abstract val enabled: Boolean
    abstract val sortNumber: Int

    data class Web(val rule: DictRule) : DictTab() {
        override val name get() = rule.name
        override val enabled get() = rule.enabled
        override val sortNumber get() = rule.sortNumber
    }

    data class Ai(val rule: AiDictRule) : DictTab() {
        override val name get() = rule.name
        override val enabled get() = rule.enabled
        override val sortNumber get() = rule.sortNumber
    }
}

class DictViewModel(application: Application) : BaseViewModel(application) {

    private var dictJob: Coroutine<String>? = null

    fun initData(onSuccess: (List<DictTab>) -> Unit) {
        execute {
            val webRules = appDb.dictRuleDao.enabled.map { DictTab.Web(it) }
            val aiRules = appDb.aiDictRuleDao.enabled.map { DictTab.Ai(it) }
            (webRules + aiRules).sortedBy { it.sortNumber }
        }.onSuccess {
            onSuccess.invoke(it)
        }
    }

    fun search(
        tab: DictTab,
        word: String,
        onFinally: (String) -> Unit
    ) {
        dictJob?.cancel()
        dictJob = execute {
            when (tab) {
                is DictTab.Web -> tab.rule.search(word)
                is DictTab.Ai -> tab.rule.search(word)
            }
        }.onSuccess {
            onFinally.invoke(it)
        }.onError {
            onFinally.invoke(it.localizedMessage ?: "ERROR")
        }
    }
}
