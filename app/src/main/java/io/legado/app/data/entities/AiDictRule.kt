package io.legado.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.legado.app.help.http.okHttpClient
import io.legado.app.help.http.postJson
import io.legado.app.help.http.newCallStrResponse
import io.legado.app.model.analyzeRule.AnalyzeRule
import io.legado.app.model.analyzeRule.AnalyzeRule.Companion.setCoroutineContext
import io.legado.app.utils.GSON
import io.legado.app.utils.fromJsonObject
import kotlin.coroutines.coroutineContext

/**
 * AI 字典规则
 */
@Entity(tableName = "aiDictRules")
data class AiDictRule(
    @PrimaryKey
    var name: String = "",
    var endpoint: String = "",
    var apiKey: String = "",
    var model: String = "deepseek-v4-flash",
    var systemPrompt: String = "",
    var userPromptTemplate: String = "",
    @ColumnInfo(defaultValue = "0.7")
    var temperature: Float = 0.7f,
    @ColumnInfo(defaultValue = "512")
    var maxTokens: Int = 512,
    @ColumnInfo(defaultValue = "1")
    var enabled: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    var sortNumber: Int = 0,
    var extraJson: String = "",
) {

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is AiDictRule) {
            return name == other.name
        }
        return false
    }

    suspend fun search(word: String): String {
        val messages = mutableListOf<Map<String, String>>()
        if (systemPrompt.isNotBlank()) {
            messages.add(mapOf("role" to "system", "content" to systemPrompt))
        }
        val userContent = if (userPromptTemplate.isNotBlank()) {
            userPromptTemplate.replace("{{word}}", word)
        } else {
            "请解释词语: $word"
        }
        messages.add(mapOf("role" to "user", "content" to userContent))

        val requestBody = mutableMapOf<String, Any>(
            "model" to model,
            "messages" to messages,
            "temperature" to temperature,
            "max_tokens" to maxTokens
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

        val analyzeRule = AnalyzeRule().setCoroutineContext(coroutineContext)
        return analyzeRule.getString("$.choices[0].message.content", mContent = response.body)
    }
}
