package io.legado.app.ui.book.manga.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil3.SingletonImageLoader
import coil3.asDrawable
import coil3.request.ImageRequest
import coil3.request.target
import coil3.request.transformations
import coil3.transform.Transformation
import io.legado.app.help.http.progress.ProgressManager
import io.legado.app.model.BookCover
import io.legado.app.model.ReadManga
import io.legado.app.utils.printOnDebug

open class MangaVH<VB : ViewBinding>(val binding: VB, private val context: Context) :
    RecyclerView.ViewHolder(binding.root) {

    protected lateinit var mLoading: ProgressBar
    protected lateinit var mImage: AppCompatImageView
    protected lateinit var mProgress: TextView
    protected lateinit var mFlProgress: FrameLayout
    protected var mRetry: Button? = null

    private val minHeight = context.resources.displayMetrics.heightPixels * 2 / 3

    fun initComponent(
        loading: ProgressBar,
        image: AppCompatImageView,
        progress: TextView,
        button: Button? = null,
        flProgress: FrameLayout,
    ) {
        mLoading = loading
        mImage = image
        mRetry = button
        mProgress = progress
        mFlProgress = flProgress
    }

    @SuppressLint("CheckResult")
    fun loadImageWithRetry(
        imageUrl: String,
        isHorizontal: Boolean,
        isLastImage: Boolean,
        transformation: Transformation?
    ) {
        mFlProgress.isVisible = true
        mLoading.isVisible = true
        mRetry?.isGone = true
        mProgress.isVisible = true
        ProgressManager.removeListener(imageUrl)
        ProgressManager.addListener(imageUrl) { _, percentage, _, _ ->
            @SuppressLint("SetTextI18n")
            mProgress.text = "$percentage%"
        }
        try {
            mImage.tag = imageUrl
            val request = BookCover.loadMangaRequest(
                context,
                imageUrl,
                sourceOrigin = ReadManga.book?.origin,
            ).newBuilder()
                .apply {
                    if (transformation != null) {
                        transformations(transformation)
                    }
                }
                .target(
                    onError = { _ ->
                        mFlProgress.isVisible = true
                        mLoading.isGone = true
                        mRetry?.isVisible = true
                        mProgress.isGone = true
                        itemView.updateLayoutParams<ViewGroup.LayoutParams> {
                            height = ViewGroup.LayoutParams.MATCH_PARENT
                        }
                    },
                    onSuccess = { result ->
                        mFlProgress.isGone = true
                        if (!isHorizontal) {
                            itemView.updateLayoutParams<ViewGroup.LayoutParams> {
                                height = ViewGroup.LayoutParams.WRAP_CONTENT
                            }
                            mImage.updateLayoutParams<FrameLayout.LayoutParams> {
                                gravity = Gravity.NO_GRAVITY
                            }
                            if (isLastImage) {
                                mImage.updateLayoutParams<FrameLayout.LayoutParams> {
                                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                                }
                                itemView.minimumHeight = minHeight
                            } else {
                                mImage.updateLayoutParams<FrameLayout.LayoutParams> {
                                    height = ViewGroup.LayoutParams.MATCH_PARENT
                                }
                                itemView.minimumHeight = 0
                            }
                            mImage.scaleType = ImageView.ScaleType.FIT_XY
                        } else {
                            itemView.updateLayoutParams<ViewGroup.LayoutParams> {
                                height = ViewGroup.LayoutParams.MATCH_PARENT
                            }
                            itemView.minimumHeight = 0
                            mImage.updateLayoutParams<FrameLayout.LayoutParams> {
                                height = ViewGroup.LayoutParams.MATCH_PARENT
                                gravity = Gravity.CENTER
                            }
                            mImage.scaleType = ImageView.ScaleType.FIT_CENTER
                        }
                        mImage.setImageDrawable(result.asDrawable(context.resources))
                    }
                )
                .build()
            SingletonImageLoader.get(context).enqueue(request)
        } catch (e: Exception) {
            e.printOnDebug()
        }
    }
}