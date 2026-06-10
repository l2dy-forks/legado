package io.legado.app.ui.widget.dialog

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.textclassifier.TextClassifier
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.DialogFragment
import io.legado.app.help.coil.CoilImagesPlugin
import io.legado.app.ui.common.compose.LegadoTheme
import io.legado.app.ui.common.compose.legadoCardBackgroundColor
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MarkdownBottomSheetDialog : DialogFragment() {

    companion object {
        fun newInstance(title: String, content: String): MarkdownBottomSheetDialog {
            return MarkdownBottomSheetDialog().apply {
                arguments = Bundle().apply {
                    putString("title", title)
                    putString("content", content)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = object : Dialog(requireContext(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen) {}
        dialog.window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = android.graphics.Color.TRANSPARENT
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val title = arguments?.getString("title") ?: ""
        val content = arguments?.getString("content") ?: ""
        return ComposeView(requireContext()).apply {
            setContent {
                LegadoTheme {
                    MarkdownSheet(
                        title = title,
                        content = content,
                        onDismiss = { dismiss() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MarkdownSheet(
    title: String,
    content: String,
    onDismiss: () -> Unit,
) {
    val ctx = LocalContext.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val textColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val sheetContainerColor = legadoCardBackgroundColor()
    var markdown by remember { mutableStateOf<Spanned?>(null) }
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    val titleStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)

    // 延迟 dismiss：等 sheet 退场动画播完（Hidden）再关闭 Dialog
    // 否则 dismiss() 会瞬间杀死 Dialog，收窄动画还没开始就被掐断
    var dismissRequested by remember { mutableStateOf(false) }
    LaunchedEffect(dismissRequested) {
        if (dismissRequested) {
            snapshotFlow { sheetState.currentValue }
                .first { it == SheetValue.Hidden }
            onDismiss()
        }
    }

    // Markwon 实例只创建一次，LaunchedEffect 和 AndroidView.update 共享
    val markwon = remember {
        Markwon.builder(ctx)
            .usePlugin(CoilImagesPlugin.create(ctx))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(TablePlugin.create(ctx))
            .build()
    }

    // 用 targetValue（非 currentValue）驱动动画，在用户开始滑动时就启动
    val isExpanding by remember {
        derivedStateOf { sheetState.targetValue == SheetValue.Expanded }
    }
    val expansionProgress by animateFloatAsState(
        targetValue = if (isExpanding) 1f else 0f,
        label = "titleOffset"
    )

    // 测量标题宽度（只测一次，内容不变）
    val titleWidthDp = with(density) {
        textMeasurer.measure(
            text = title,
            style = titleStyle,
            maxLines = 1,
        ).size.width.toDp()
    }

    LaunchedEffect(content) {
        markdown = withContext(Dispatchers.IO) {
            markwon.toMarkdown(content)
        }
    }

    ModalBottomSheet(
        onDismissRequest = { dismissRequested = true },
        sheetState = sheetState,
        // containerColor = primaryColor → 状态栏后面始终显示 primaryColor
        // 正文区域用卡片色 Box 包裹
        containerColor = primaryColor,
        dragHandle = null,
        shape = BottomSheetDefaults.ExpandedShape,
    ) {
            Column {
                // 标题栏：DragHandle 始终占位 alpha 淡出 + 标题 graphicsLayer 平移居中
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    val containerWidthPx = with(density) { maxWidth.toPx() }
                    val titleWidthPx = with(density) { titleWidthDp.toPx() }
                    val paddingStartPx = with(density) { 24.dp.toPx() }
                    // graphicsLayer translationX 用像素级精度
                   val centerOffsetPx = (containerWidthPx - titleWidthPx) / 2f

                    // DragHandle — 始终占据布局空间，仅 alpha 淡出，不改变高度
                    BottomSheetDefaults.DragHandle(
                        color = onPrimaryColor,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .alpha(1f - expansionProgress)
                    )

                    // 标题 — graphicsLayer 纯视觉平移，不影响布局测量
                    Text(
                        text = title,
                        style = titleStyle,
                        color = onPrimaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = centerOffsetPx * expansionProgress
                            }
                    )
                }

                // 正文区域：卡片色背景 + 顶部圆角（衔接 sheet 自身圆角）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(sheetContainerColor)
                        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                ) {
                    Spacer(Modifier.height(16.dp))
                    SelectionContainer {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            androidx.compose.runtime.key(markdown) {
                                AndroidView(
                                    factory = { c ->
                                        android.widget.TextView(c).apply {
                                            setTextColor(textColor.toArgb())
                                            textSize = 15f
                                            setLineSpacing(4f, 1f)
                                            setTextIsSelectable(true)
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                setTextClassifier(TextClassifier.NO_OP)
                                            }
                                        }
                                    },
                                    update = { tv ->
                                        markdown?.let { md ->
                                            markwon.setParsedMarkdown(tv, md)
                                        }
                                    },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
}
