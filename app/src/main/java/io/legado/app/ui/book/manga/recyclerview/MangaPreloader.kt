package io.legado.app.ui.book.manga.recyclerview

import androidx.recyclerview.widget.RecyclerView
import coil3.SingletonImageLoader
import coil3.request.Disposable
import io.legado.app.model.BookCover
import io.legado.app.model.ReadManga
import io.legado.app.ui.book.manga.entities.MangaPage

/**
 * 基于 Coil 的漫画图片预加载器，替代 Glide 的 RecyclerViewPreloader。
 *
 * 原理：
 * - 监听 RecyclerView 滚动，提前 enqueue 后面 N 页的 ImageRequest
 * - Coil 会将图片下载到磁盘缓存，用户翻到该页时直接从磁盘解码，几乎无等待
 * - 预加载请求仅写磁盘缓存（memoryCachePolicy=DISABLED），不占内存
 */
class MangaPreloader(
    private val recyclerView: RecyclerView,
    private val adapter: MangaAdapter,
    private var maxPreload: Int = 3,
) : RecyclerView.OnScrollListener() {

    private val preloadQueue = mutableMapOf<String, Disposable>()

    fun setMaxPreload(count: Int) {
        maxPreload = count.coerceAtLeast(0)
    }

    fun attach() {
        recyclerView.addOnScrollListener(this)
    }

    fun detach() {
        recyclerView.removeOnScrollListener(this)
        cancelAll()
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (maxPreload <= 0) return
        preloadAhead()
    }

    private fun preloadAhead() {
        val layoutManager = recyclerView.layoutManager ?: return
        val lastVisiblePos = when (layoutManager) {
            is androidx.recyclerview.widget.LinearLayoutManager -> layoutManager.findLastVisibleItemPosition()
            else -> return
        }

        val context = recyclerView.context
        val sourceOrigin = ReadManga.book?.origin
        val totalItems = adapter.getItems().size

        // 预加载接下来的 maxPreload 个 MangaPage
        var preloaded = 0
        for (i in 1..maxPreload) {
            val pos = lastVisiblePos + i
            if (pos >= totalItems) break

            val item = adapter.getItem(pos) ?: continue
            if (item !is MangaPage) continue

            val url = item.mImageUrl
            if (url.isNullOrBlank()) continue
            if (preloadQueue.containsKey(url)) continue // 已经在预加载中

            val request = BookCover.loadMangaRequest(
                context, url,
                sourceOrigin = sourceOrigin,
            ).newBuilder()
                .memoryCachePolicy(coil3.request.CachePolicy.DISABLED)
                .diskCachePolicy(coil3.request.CachePolicy.ENABLED)
                .build()

            val disposable = SingletonImageLoader.get(context).enqueue(request)
            preloadQueue[url] = disposable
            preloaded++

            // 限制同时预加载的数量
            if (preloaded >= maxPreload) break
        }

        // 清理已不在预加载范围内的旧请求
        cleanupOldPreloads(lastVisiblePos)
    }

    private fun cleanupOldPreloads(currentPos: Int) {
        val items = adapter.getItems()
        val validUrls = mutableSetOf<String>()
        for (i in 1..maxPreload) {
            val pos = currentPos + i
            if (pos < items.size) {
                val item = adapter.getItem(pos)
                if (item is MangaPage && !item.mImageUrl.isNullOrBlank()) {
                    validUrls.add(item.mImageUrl)
                }
            }
        }
        val toRemove = preloadQueue.keys.filter { it !in validUrls }
        toRemove.forEach { url ->
            preloadQueue[url]?.dispose()
            preloadQueue.remove(url)
        }
    }

    fun cancelAll() {
        preloadQueue.values.forEach { it.dispose() }
        preloadQueue.clear()
    }
}
