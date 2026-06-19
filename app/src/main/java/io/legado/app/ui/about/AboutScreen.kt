package io.legado.app.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.legado.app.R
import io.legado.app.constant.AppConst
import io.legado.app.ui.common.compose.SectionCard
import io.legado.app.ui.common.compose.settingItem.ClickableSettingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onScoringClick: () -> Unit,
    onContributorsClick: () -> Unit,
    onUpdateLogClick: () -> Unit,
    onCheckUpdateClick: () -> Unit,
    onCrashLogClick: () -> Unit,
    onSaveLogClick: () -> Unit,
    onCreateHeapDumpClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onLicenseClick: () -> Unit,
    onDisclaimerClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onScoringClick) {
                        Icon(painterResource(R.drawable.ic_scoring), contentDescription = stringResource(R.string.scoring))
                    }
                    IconButton(onClick = onShareClick) {
                        Icon(painterResource(R.drawable.ic_share), contentDescription = stringResource(R.string.share))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        ) {
            // App info header
            item {
                SectionCard {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.about_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 8.dp).padding(horizontal = 4.dp),
                        )
                    }
                }
            }

            // Main items
            item {
                SectionCard {
                    ClickableSettingItem(
                        title = stringResource(R.string.contributors),
                        description = stringResource(R.string.contributors_summary),
                        onClick = onContributorsClick,
                    )
                    ClickableSettingItem(
                        title = stringResource(R.string.update_log),
                        description = "${stringResource(R.string.version)} ${AppConst.appInfo.versionName}",
                        onClick = onUpdateLogClick,
                    )
                    ClickableSettingItem(
                        title = stringResource(R.string.check_update),
                        onClick = onCheckUpdateClick,
                    )
                }
            }

            // Other category
            item {
                CategoryHeader(stringResource(R.string.other))
                SectionCard {
                    ClickableSettingItem(
                        title = stringResource(R.string.crash_log),
                        onClick = onCrashLogClick,
                    )
                    ClickableSettingItem(
                        title = stringResource(R.string.save_log),
                        onClick = onSaveLogClick,
                    )
                    ClickableSettingItem(
                        title = stringResource(R.string.create_heap_dump),
                        onClick = onCreateHeapDumpClick,
                    )
                    ClickableSettingItem(
                        title = stringResource(R.string.privacy_policy),
                        onClick = onPrivacyPolicyClick,
                    )
                    ClickableSettingItem(
                        title = stringResource(R.string.license),
                        onClick = onLicenseClick,
                    )
                    ClickableSettingItem(
                        title = stringResource(R.string.disclaimer),
                        onClick = onDisclaimerClick,
                    )
                }
            }

            item { Modifier.padding(bottom = 16.dp) }
        }
    }
}

@Composable
private fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
    )
}