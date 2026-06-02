package io.legado.app.help.coil

import android.graphics.Bitmap
import androidx.annotation.IntRange
import coil3.size.Size
import coil3.transform.Transformation
import io.legado.app.utils.stackBlur

/**
 * Coil 模糊变换，替代 Glide 的 BlurTransformation。
 * @param radius 0..25
 */
class BlurTransformation(
    @param:IntRange(from = 0, to = 25) private val radius: Int,
) : Transformation() {

    override val cacheKey: String = "BlurTransformation(radius=$radius)"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        return input.stackBlur(radius)
    }
}
