package io.legado.app.ui.book.group

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.fragment.app.DialogFragment
import io.legado.app.R
import io.legado.app.data.appDb
import io.legado.app.data.entities.BookGroup
import io.legado.app.ui.common.compose.LegadoTheme
import io.legado.app.ui.common.compose.legadoPopupBackgroundColor
import io.legado.app.ui.common.compose.legadoPopupPrimaryTextColor
import io.legado.app.ui.common.compose.rememberLegadoColorScheme
import io.legado.app.utils.showDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn


class GroupSelectDialog() : DialogFragment() {

    constructor(groupId: Long, requestCode: Int = -1) : this() {
        arguments = Bundle().apply {
            putLong("groupId", groupId)
            putInt("requestCode", requestCode)
        }
    }

    private var requestCode: Int = -1
    private var groupId: Long = 0
    private val callBack get() = (activity as? CallBack)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = object : Dialog(
            requireContext(),
            android.R.style.Theme_Translucent_NoTitleBar_Fullscreen
        ) {}
        dialog.window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {
            groupId = it.getLong("groupId")
            requestCode = it.getInt("requestCode", -1)
        }
        return ComposeView(requireContext()).apply {
            setContent {
                LegadoTheme {
                    GroupSelectDialogContent(
                        initialGroupId = groupId,
                        onDismiss = { dismissAllowingStateLoss() },
                        onConfirm = { selectedGroupId ->
                            callBack?.upGroup(requestCode, selectedGroupId)
                            dismissAllowingStateLoss()
                        },
                        onAddGroup = {
                            showDialogFragment(GroupEditDialog())
                        },
                    )
                }
            }
        }
    }

    interface CallBack {
        fun upGroup(requestCode: Int, groupId: Long)
    }
}

@Composable
private fun GroupSelectDialogContent(
    initialGroupId: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    onAddGroup: () -> Unit,
) {
    var groups by remember { mutableStateOf<List<BookGroup>>(emptyList()) }
    var selectedGroupId by remember { mutableLongStateOf(initialGroupId) }
    val colorScheme = rememberLegadoColorScheme()
    val popupBg = legadoPopupBackgroundColor()
    val popupTextColor = legadoPopupPrimaryTextColor()

    LaunchedEffect(Unit) {
        appDb.bookGroupDao.flowSelect()
            .catch { /* ignore */ }
            .flowOn(Dispatchers.IO)
            .conflate()
            .collect { groups = it }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = popupBg,
            tonalElevation = 6.dp,
            shadowElevation = 6.dp,
        ) {
            Column {
                // 标题栏：primaryColor 背景，对应旧版 Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(colorScheme.primary)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.group_select),
                        style = MaterialTheme.typography.headlineSmall,
                        color = colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onAddGroup) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = stringResource(R.string.add_group),
                            tint = colorScheme.onPrimary,
                        )
                    }
                }

                // 内容区域
                Column(modifier = Modifier.padding(24.dp)

                ) {
                    // 分组列表
                    if (groups.isEmpty()) {
                        Text(
                            text = stringResource(R.string.empty),
                            color = popupTextColor.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp)
                                .verticalScroll(rememberScrollState()),
                        ) {
                            groups.forEach { group ->
                                val isSelected = (selectedGroupId and group.groupId) != 0L
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedGroupId = if (checked) {
                                                selectedGroupId + group.groupId
                                            } else {
                                                selectedGroupId - group.groupId
                                            }
                                        },
                                    )
                                    Text(
                                        text = group.groupName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = popupTextColor,
                                        modifier = Modifier.padding(start = 8.dp),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 取消 / 确定按钮
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.cancel), color = popupTextColor)
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { onConfirm(selectedGroupId) }) {
                            Text(stringResource(R.string.ok), color = colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
