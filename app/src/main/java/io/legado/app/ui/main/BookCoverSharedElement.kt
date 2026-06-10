package io.legado.app.ui.main

/**
 * 书籍封面共享元素过渡动画 Key 生成器
 * 用于书架 → 书籍详情页的封面"飞入"动画
 */
fun bookCoverSharedElementKey(bookUrl: String, sourceId: String? = null): String {
    val source = sourceId?.takeIf { it.isNotBlank() } ?: return "book-cover:$bookUrl"
    return "book-cover:$source:$bookUrl"
}
