package io.legado.app.ui.replace.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.legado.app.R
import io.legado.app.help.config.AppConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplaceEditRouteScreen(
    viewModel: ReplaceEditViewModel,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val onSurfaceColor = if (AppConfig.isEInkMode) MaterialTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onPrimary
    val containerColor = if (AppConfig.isEInkMode) MaterialTheme.colorScheme.surface
    else MaterialTheme.colorScheme.primary

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ReplaceEditEffect.NavigateBack -> onSaveSuccess()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑替换规则", color = onSurfaceColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = onSurfaceColor
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.onIntent(ReplaceEditIntent.Save) }) {
                        Text("保存", color = onSurfaceColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = onSurfaceColor,
                    navigationIconContentColor = onSurfaceColor,
                    actionIconContentColor = onSurfaceColor,
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onIntent(ReplaceEditIntent.OnNameChange(it)) },
                label = { Text("名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.pattern,
                onValueChange = { viewModel.onIntent(ReplaceEditIntent.OnPatternChange(it)) },
                label = { Text("替换模式") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.replacement,
                onValueChange = { viewModel.onIntent(ReplaceEditIntent.OnReplacementChange(it)) },
                label = { Text("替换为") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.scope,
                onValueChange = { viewModel.onIntent(ReplaceEditIntent.OnScopeChange(it)) },
                label = { Text("作用范围") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.excludeScope,
                onValueChange = { viewModel.onIntent(ReplaceEditIntent.OnExcludeScopeChange(it)) },
                label = { Text("排除范围") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.group,
                onValueChange = { viewModel.onIntent(ReplaceEditIntent.OnGroupChange(it)) },
                label = { Text("分组") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("正则表达式", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = uiState.isRegex,
                    onCheckedChange = { viewModel.onIntent(ReplaceEditIntent.OnRegexChange(it)) }
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("作用于标题", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = uiState.scopeTitle,
                    onCheckedChange = { viewModel.onIntent(ReplaceEditIntent.OnScopeTitleChange(it)) }
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("作用于正文", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = uiState.scopeContent,
                    onCheckedChange = { viewModel.onIntent(ReplaceEditIntent.OnScopeContentChange(it)) }
                )
            }

            OutlinedTextField(
                value = uiState.timeout,
                onValueChange = { viewModel.onIntent(ReplaceEditIntent.OnTimeoutChange(it)) },
                label = { Text("超时时间(ms)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
