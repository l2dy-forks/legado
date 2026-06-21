package io.legado.app.ui.dict.rule.ai

import android.app.Application
import io.legado.app.base.BaseViewModel
import io.legado.app.data.appDb
import io.legado.app.data.entities.AiDictRule
import io.legado.app.help.http.newCallStrResponse
import io.legado.app.help.http.okHttpClient
import io.legado.app.help.http.postJson
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

    /**
     * 测试连接：用当前配置发送一条简单请求，返回原始响应或错误信息
     */
    fun testConnection(
        endpoint: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPromptTemplate: String,
        temperature: Float,
        maxTokens: Int,
        extraJson: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        execute {
            val messages = mutableListOf<Map<String, String>>()
            if (systemPrompt.isNotBlank()) {
                messages.add(mapOf("role" to "system", "content" to systemPrompt))
            }
            val userContent = if (userPromptTemplate.isNotBlank()) {
                userPromptTemplate.replace("{{word}}", "test")
            } else {
                "请解释词语: test"
            }
            messages.add(mapOf("role" to "user", "content" to userContent))

            val requestBody = mutableMapOf<String, Any>(
                "model" to model,
                "messages" to messages,
                "temperature" to temperature,
                "max_tokens" to maxTokens,
            )
            if (extraJson.isNotBlank()) {
                @Suppress("UNCHECKED_CAST")
                val extra = GSON.fromJsonObject<Map<String, Any>>(extraJson).getOrNull()
                extra?.let { requestBody.putAll(it) }
            }
            val jsonBody = GSON.toJson(requestBody)

            val response = okHttpClient.newCallStrResponse {
                url(endpoint)
                addHeader("Authorization", "Bearer $apiKey")
                addHeader("Content-Type", "application/json")
                postJson(jsonBody)
            }
            val body = response.body
            if (body.isNullOrBlank()) {
                throw RuntimeException("返回为空 (HTTP ${response.raw.code})")
            }
            body
        }.onSuccess {
            onResult(true, it)
        }.onError {
            onResult(false, it.localizedMessage ?: "未知错误")
        }
    }

    private fun buildModelsUrl(endpoint: String): String {
        // 去掉请求后缀得到 API base，再拼 /v1/models
        val base = endpoint.trimEnd('/')
            .removeSuffix("/v1/chat/completions")
            .removeSuffix("/chat/completions")
            .removeSuffix("/v1")
            .trimEnd('/')
        return "$base/v1/models"
    }
}
