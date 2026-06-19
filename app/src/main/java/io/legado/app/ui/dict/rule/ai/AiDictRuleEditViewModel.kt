package io.legado.app.ui.dict.rule.ai

import android.app.Application
import io.legado.app.base.BaseViewModel
import io.legado.app.data.appDb
import io.legado.app.data.entities.AiDictRule
import io.legado.app.help.http.newCallStrResponse
import io.legado.app.help.http.okHttpClient
import io.legado.app.utils.GSON
import io.legado.app.utils.fromJsonObject

class AiDictRuleEditViewModel(application: Application) : BaseViewModel(application) {

    var aiDictRule: AiDictRule? = null

    fun initData(name: String?, onSuccess: () -> Unit) {
        execute {
            if (name != null) {
                aiDictRule = appDb.aiDictRuleDao.getByName(name)
            }
            if (aiDictRule == null) {
                aiDictRule = AiDictRule()
            }
        }.onSuccess {
            onSuccess()
        }
    }

    fun save(rule: AiDictRule, onSuccess: () -> Unit) {
        execute {
            appDb.aiDictRuleDao.insert(rule)
        }.onSuccess {
            onSuccess()
        }
    }

    fun delete(name: String, onSuccess: () -> Unit) {
        execute {
            val rule = appDb.aiDictRuleDao.getByName(name)
            if (rule != null) {
                appDb.aiDictRuleDao.delete(rule)
            }
        }.onSuccess {
            onSuccess()
        }
    }

    /**
     * 从 endpoint 推导出 /v1/models 地址并拉取可用模型列表
     */
    fun fetchModels(endpoint: String, apiKey: String, onResult: (List<String>) -> Unit) {
        execute {
            val modelsUrl = buildModelsUrl(endpoint)
            val response = okHttpClient.newCallStrResponse {
                url(modelsUrl)
                addHeader("Authorization", "Bearer $apiKey")
            }
            val data = GSON.fromJsonObject<Map<String, Any>>(response.body).getOrNull() ?: emptyMap()
            @Suppress("UNCHECKED_CAST")
            val list = data["data"] as? List<Map<String, Any>> ?: emptyList()
            list.mapNotNull { it["id"] as? String }.sorted()
        }.onSuccess {
            onResult(it)
        }.onError {
            onResult(emptyList())
        }
    }

    private fun buildModelsUrl(endpoint: String): String {
        // 从 chat/completions 等路径推导出 models 地址
        val base = endpoint.substringBefore("/v1/")
        return "$base/v1/models"
    }
}
