package io.legado.app.utils.canvasrecorder

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.LruCache
import io.legado.app.utils.canvasrecorder.pools.CanvasPool

/**
 * 简单的 Bitmap 复用池，替代 Glide 的 BitmapPool。
 */
private class SimpleBitmapPool(maxSize: Int = 4 * 1024 * 1024) { // 4MB
    private val cache = object : LruCache<Int, Bitmap>(maxSize) {
        override fun sizeOf(key: Int, value: Bitmap): Int = value.allocationByteCount
        override fun entryRemoved(evicted: Boolean, key: Int, oldValue: Bitmap, newValue: Bitmap?) {
            if (evicted) oldValue.recycle()
        }
    }

    fun get(width: Int, height: Int, config: Bitmap.Config): Bitmap {
        val size = width * height * 4
        val key = (width shl 16) or height
        cache.remove(key)?.let { cached ->
            if (cached.width == width && cached.height == height && !cached.isRecycled) {
                cached.eraseColor(Color.TRANSPARENT)
                return cached
            }
            cached.recycle()
        }
        return Bitmap.createBitmap(width, height, config)
    }

    fun put(bitmap: Bitmap) {
        if (bitmap.isRecycled) return
        val key = (bitmap.width shl 16) or bitmap.height
        cache.put(key, bitmap)
    }
}

class CanvasRecorderImpl : BaseCanvasRecorder() {

    var bitmap: Bitmap? = null
    var canvas: Canvas? = null

    override val width get() = bitmap?.width ?: -1
    override val height get() = bitmap?.height ?: -1

    private fun init(width: Int, height: Int) {
        if (width <= 0 || height <= 0) {
            return
        }
        if (bitmap == null) {
            bitmap = bitmapPool.get(width, height, Bitmap.Config.ARGB_8888)
        }
        if (bitmap!!.width != width || bitmap!!.height != height) {
            if (bitmap!!.isMutable && canReconfigure(width, height)) {
                bitmap!!.reconfigure(width, height, Bitmap.Config.ARGB_8888)
            } else {
                bitmapPool.put(bitmap!!)
                bitmap = bitmapPool.get(width, height, Bitmap.Config.ARGB_8888)
            }
        }
    }

    private fun canReconfigure(width: Int, height: Int): Boolean {
        return bitmap!!.allocationByteCount >= width * height * 4
    }

    override fun beginRecording(width: Int, height: Int): Canvas {
        init(width, height)
        bitmap?.eraseColor(Color.TRANSPARENT)
        canvas = canvasPool.obtain().apply { setBitmap(bitmap) }
        return canvas!!
    }

    override fun endRecording() {
        bitmap?.prepareToDraw()
        super.endRecording()
        canvasPool.recycle(canvas!!)
        canvas = null
    }

    override fun draw(canvas: Canvas) {
        if (bitmap == null) return
        canvas.drawBitmap(bitmap!!, 0f, 0f, null)
    }

    override fun recycle() {
        super.recycle()
        val bitmap = bitmap ?: return
        bitmapPool.put(bitmap)
        this.bitmap = null
    }

    companion object {
        private val canvasPool = CanvasPool(2)
        private val bitmapPool = SimpleBitmapPool()
    }

}
