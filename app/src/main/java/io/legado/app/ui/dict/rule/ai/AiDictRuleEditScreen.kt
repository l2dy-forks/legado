package io.legado.app.ui.dict.rule.ai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.legado.app.R
import io.legado.app.data.entities.AiDictRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDictRuleEditScreen(
    editName: String?,
    editViewModel: AiDictRuleEditViewModel,
    saving: Boolean,
    onSaved: () -> Unit,
    onDeleted: () -> Unit,
    onBack: () -> Unit,
    onSaveRequested: () -> Unit,
) {
    val isNew = editName == null

    var loaded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var endpoint by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("deepseek-v4-flash") }
    var systemPrompt by remember { mutableStateOf("") }
    var userPromptTemplate by remember { mutableStateOf("") }
    var temperature by remember { mutableFloatStateOf(0.7f) }
    var maxTokens by remember { mutableStateOf("512") }
    var apiKeyVisible by remember { mutableStateOf(false) }
    var fetchingModels by remember { mutableStateOf(false) }
    var fetchedModels by remember { mutableStateOf<List<String>?>(null) }
    var extraJson by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        editViewModel.initData(editName) {
            val r = editViewModel.aiDictRule!!
            name = r.name
            endpoint = r.endpoint
            apiKey = r.apiKey
            model = r.model.ifBlank { "deepseek-v4-flash" }
            systemPrompt = r.systemPrompt
            userPromptTemplate = r.userPromptTemplate
            temperature = r.temperature
            maxTokens = r.maxTokens.toString()
            extraJson = r.extraJson
            loaded = true
        }
    }

    val maxTokensInt = maxTokens.toIntOrNull() ?: 512

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isNew) stringResource(R.string.add_ai_dict_rule)
                        else stringResource(R.string.edit_ai_dict_rule)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        if (!loaded) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text(stringResource(R.string.endpoint)) },
                    placeholder = { Text("https://api.openai.com/v1/chat/completions") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text(stringResource(R.string.api_key)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (apiKeyVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    trailingIcon = {
                        Text(
                            text = if (apiKeyVisible) "隐藏" else "显示",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { apiKeyVisible = !apiKeyVisible }
                                .padding(8.dp),
                        )
                    },
                )

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text(stringResource(R.string.model)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        if (fetchingModels) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    if (endpoint.isNotBlank()) {
                                        fetchingModels = true
                                        editViewModel.fetchModels(endpoint, apiKey) { models ->
                                            fetchingModels = false
                                            fetchedModels = models
                                        }
                                    }
                                },
                                enabled = endpoint.isNotBlank(),
                            ) {
                                Icon(
                                    Icons.Filled.Refresh,
                                    contentDescription = "获取模型列表",
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    },
                )

                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    label = { Text(stringResource(R.string.system_prompt)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                )

                Text(
                    text = stringResource(R.string.prompt_template_presets),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AssistChip(
                        onClick = { userPromptTemplate = "请解释词语: {{word}}" },
                        label = { Text("简要") },
                    )
                    AssistChip(
                        onClick = { userPromptTemplate = "请详细解释'{{word}}'的含义，包括词源、用法示例和同义词。" },
                        label = { Text("详细") },
                    )
                    AssistChip(
                        onClick = { userPromptTemplate = "请将'{{word}}'翻译并给出释义。" },
                        label = { Text("翻译") },
                    )
                }

                OutlinedTextField(
                    value = userPromptTemplate,
                    onValueChange = { userPromptTemplate = it },
                    label = { Text(stringResource(R.string.user_prompt_template)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                )

                Text(
                    text = "${stringResource(R.string.temperature)}: ${"%.1f".format(temperature)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = temperature,
                    onValueChange = { temperature = it },
                    valueRange = 0f..2f,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = maxTokens,
                    onValueChange = { maxTokens = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.max_tokens)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                OutlinedTextField(
                    value = extraJson,
                    onValueChange = { extraJson = it },
                    label = { Text("额外配置 (JSON)") },
                    placeholder = { Text("{\"top_p\": 0.9, \"frequency_penalty\": 0.5}") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5,
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = {
                            onSaveRequested()
                            editViewModel.save(
                                AiDictRule(
                                    name = name,
                                    endpoint = endpoint,
                                    apiKey = apiKey,
                                    model = model,
                                    systemPrompt = systemPrompt,
                                    userPromptTemplate = userPromptTemplate,
                                    temperature = temperature,
                                    maxTokens = maxTokensInt,
                                    enabled = editViewModel.aiDictRule?.enabled ?: true,
                                    sortNumber = editViewModel.aiDictRule?.sortNumber ?: 0,
                                    extraJson = extraJson,
                                )
                            ) { onSaved() }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !saving && name.isNotBlank() && endpoint.isNotBlank(),
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                }

                if (!isNew) {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            editViewModel.delete(name) { onDeleted() }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // 模型选择对话框
    fetchedModels?.let { models ->
        if (models.isEmpty()) {
            AlertDialog(
                onDismissRequest = { fetchedModels = null },
                title = { Text("获取模型列表") },
                text = { Text("未能获取到模型列表，请检查 API 地址和 API Key。") },
                confirmButton = {
                    TextButton(onClick = { fetchedModels = null }) {
                        Text(stringResource(R.string.ok))
                    }
                },
            )
        } else {
            AlertDialog(
                onDismissRequest = { fetchedModels = null },
                title = { Text("选择模型 (${models.size})") },
                text = {
                    LazyColumn {
                        items(models) { m ->
                            TextButton(
                                onClick = {
                                    model = m
                                    fetchedModels = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = m,
                                    color = if (m == model) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { fetchedModels = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }
    }
}
