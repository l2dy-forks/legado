package io.legado.app.ui.book.manga.entities

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.annotation.IntRange
import coil3.size.Size
import coil3.transform.Transformation

/**
 * 墨水屏图片转换器。
 * 将彩色图片转换为灰度图，并可选择进行简单的二值化处理，以提高墨水屏显示效果。
 *
 * @param threshold 二值化的阈值（0-255）。低于此值的像素变为黑色，高于此值的像素变为白色。
 */
class EpaperTransformation(
    @param:IntRange(0, 255) private val threshold: Int = 128,
) : Transformation() {

    override val cacheKey: String = "EpaperTransformation(threshold=$threshold)"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val resultBitmap = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint()

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter
        canvas.drawBitmap(input, 0f, 0f, paint)

        val pixels = IntArray(input.width * input.height)
        resultBitmap.getPixels(pixels, 0, input.width, 0, 0, input.width, input.height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val gray = Color.red(pixel)
            pixels[i] =
                if (gray < threshold) Color.BLACK else Color.WHITE
        }
        resultBitmap.setPixels(pixels, 0, input.width, 0, 0, input.width, input.height)

        return resultBitmap
    }
}