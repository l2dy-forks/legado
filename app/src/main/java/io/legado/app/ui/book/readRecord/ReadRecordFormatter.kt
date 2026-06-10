package io.legado.app.ui.book.readRecord

import java.util.concurrent.TimeUnit

/**
 * 阅读记录格式化工具
 */
object ReadRecordFormatter {

    /**
     * 将毫秒转换为中文阅读时长字符串
     * e.g. 9000000 -> "2小时30分", 1800000 -> "30分钟"
     */
    fun formatDuring(millis: Long): String {
        if (millis <= 0) return "—"

        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}小时${minutes}分"
            hours > 0 -> "${hours}小时"
            minutes > 0 -> "${minutes}分钟"
            else -> "1分钟"
        }
    }

    /**
     * 格式化日期为 MM-dd
     */
    fun formatDateShort(timestamp: Long): String {
        if (timestamp <= 0) return ""
        val sdf = java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}
