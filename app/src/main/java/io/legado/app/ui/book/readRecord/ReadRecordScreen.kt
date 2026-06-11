package io.legado.app.ui.book.readRecord

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.legado.app.R
import io.legado.app.help.config.AppConfig
import io.legado.app.ui.common.compose.BookCoverImage
import io.legado.app.ui.common.compose.RoundDropdownMenu
import io.legado.app.ui.common.compose.RoundDropdownMenuItem
import io.legado.app.ui.common.compose.legadoCardBackgroundColor
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadRecordScreen(
    state: ReadRecordUiState,
    onIntent: (ReadRecordIntent) -> Unit,
    effects: Flow<ReadRecordEffect>? = null,
    onBack: () -> Unit,
    onNavigateToBook: (String, String) -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onOverviewClick: () -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val listState = rememberLazyListState()
    var showSearch by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var searchText by remember(state.searchKey) { mutableStateOf(state.searchKey ?: "") }

    LaunchedEffect(Unit) { onIntent(ReadRecordIntent.Load) }
    LaunchedEffect(effects) {
        effects?.collect {
            when (it) {
                is ReadRecordEffect.NavigateToBook -> onNavigateToBook(it.bookName, it.author)
                is ReadRecordEffect.OpenSearch -> onNavigateToSearch(it.bookName)
                is ReadRecordEffect.ShowToast -> {}
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(
                        "阅读记录", 
                        color = if (AppConfig.isEInkMode) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        },
                    ) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (AppConfig.isEInkMode) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    ),
                    navigationIcon = { IconButton(onClick = onBack) { Icon(painterResource(R.drawable.ic_arrow_back), "返回") } },
                    actions = {
                        IconButton(onClick = {
                            val modes = DisplayMode.entries
                            val next = modes[(modes.indexOf(state.displayMode) + 1) % modes.size]
                            onIntent(ReadRecordIntent.SetMode(next))
                        }) { Icon(painterResource(R.drawable.ic_baseline_sort_24), "切换视图") }
                        IconButton(onClick = { showSearch = !showSearch }) { Icon(painterResource(R.drawable.ic_search), "搜索") }
                        IconButton(onClick = { showMenu = true }) { Icon(painterResource(R.drawable.ic_more_vert), "菜单") }
                        RoundDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) { dismiss ->
                            DisplayMode.entries.forEach { m ->
                                RoundDropdownMenuItem(
                                    text = m.label,
                                    onClick = { dismiss(); onIntent(ReadRecordIntent.SetMode(m)) },
                                )
                            }
                            RoundDropdownMenuItem(
                                text = if (state.enableRecord) "关闭阅读记录" else "开启阅读记录",
                                onClick = { dismiss(); onIntent(ReadRecordIntent.ToggleEnableRecord) },
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
                AnimatedVisibility(visible = showSearch) {
                    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showSearch = false; searchText = ""; onIntent(ReadRecordIntent.Search(null)) }) { Icon(painterResource(R.drawable.ic_arrow_back), "关闭") }
                            OutlinedTextField(value = searchText, onValueChange = { v -> searchText = v; onIntent(ReadRecordIntent.Search(v.ifBlank { null })) },
                                modifier = Modifier.weight(1f), placeholder = { Text("搜索书名") }, singleLine = true, shape = RoundedCornerShape(12.dp))
                        }
                    }
                }
            }
        },
    ) { padding ->
        val contentKey = when { state.isLoading -> "loading"; state.books.isEmpty() -> "empty"; else -> "content" }
        AnimatedContent(targetState = contentKey, label = "content", transitionSpec = { fadeIn() togetherWith fadeOut() }) { key ->
            when (key) {
                "loading" -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("加载中…") }
                "empty" -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("暂无阅读记录", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("开始阅读后这里将会显示你的阅读记录", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                "content" -> LazyColumn(
                    state = listState, modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 16.dp),
                ) {
                    // 1. Summary — 整个卡片可点击进入二级总览页
                    item(key = "summary") { ReadingSummaryCard(state, onOverviewClick) }
                    // 2. Section title
                    item(key = "list_header") { Text("${state.displayMode.label}（${state.books.size}）", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) }
                    // 3. Book list
                    items(state.books, key = { "book_${it.bookName}" }) { item ->
                        BookItem(item, onClick = { onIntent(ReadRecordIntent.ClickBook(item.bookName, item.author)) }, onDelete = { onIntent(ReadRecordIntent.DeleteBook(item.bookName)) })
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// ── ReadingSummaryCard — MD3 exact: GlassCard style, title primary labelLarge, "已读 N 本书" split styling ──

@Composable
private fun ReadingSummaryCard(state: ReadRecordUiState, onClick: () -> Unit) {
    val cardBg = legadoCardBackgroundColor()
    val totalMinutes = state.totalReadTime / 60000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val timeStr = if (hours > 0) "${hours}小时${minutes}分钟" else "${minutes}分钟"

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(remember { MutableInteractionSource() }, null, onClick = onClick),
        shape = RoundedCornerShape(16.dp), color = cardBg, shadowElevation = 0.dp,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("累计阅读成就", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("已读 ", style = MaterialTheme.typography.titleMedium)
                    Text("${state.totalBooks}", style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(" 本书", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(4.dp))
                Text("共阅读 $timeStr", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (state.summaryCovers.isNotEmpty()) {
                BookStackView(state.summaryCovers.take(5))
            }
        }
    }
}

@Composable private fun StatChip(label: String, value: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.width(4.dp))
            Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

// ── BookStackView — 不整齐堆叠效果：旋转 + 偏移 ──

@Composable
private fun BookStackView(covers: List<BookReadRecordItem>) {
    // 每本书的旋转角度（交替 ±）
    val rotations = listOf(-8f, 6f, -5f, 10f, -3f)
    // 垂直偏移
    val offsetsY = listOf(0f, 4f, -2f, 6f, -4f)

    Box(
        modifier = Modifier.width(56.dp).height(72.dp),
        contentAlignment = Alignment.Center,
    ) {
        covers.forEachIndexed { i, item ->
            val rot = rotations[i % rotations.size]
            val offY = offsetsY[i % offsetsY.size]
            Surface(
                modifier = Modifier
                    .zIndex(i.toFloat())
                    .graphicsLayer {
                        rotationZ = rot
                        translationY = offY * density
                    },
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(4.dp),
                color = Color.Transparent,
            ) {
                BookCoverImage(item.coverPath, item.bookName, item.author,
                    Modifier.width(44.dp), compact = true)
            }
        }
    }
}

// ── Book Item ──

@Composable
private fun BookItem(item: BookReadRecordItem, onClick: () -> Unit, onDelete: () -> Unit) {
    val cardBg = legadoCardBackgroundColor()
    var showDelete by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp)).clickable(remember { MutableInteractionSource() }, null, onClick = onClick),
        shape = RoundedCornerShape(12.dp), color = cardBg, shadowElevation = 0.dp,
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            BookCoverImage(item.coverPath, item.bookName, item.author, Modifier.size(48.dp, 64.dp), compact = true)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.bookName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.author.isNotBlank()) Text(item.author, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("最后阅读: ${ReadRecordFormatter.formatDateShort(item.lastRead)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
            Text(ReadRecordFormatter.formatDuring(item.readTime), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = { showDelete = true }) { Icon(painterResource(R.drawable.ic_outline_delete), "删除", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
        }
    }
    if (showDelete) AlertDialog(
        onDismissRequest = { showDelete = false }, title = { Text("确认删除") }, text = { Text("确定要删除「${item.bookName}」的阅读记录吗？") },
        confirmButton = { TextButton(onClick = { showDelete = false; onDelete() }) { Text("删除", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { showDelete = false }) { Text("取消") } },
    )
}
