package io.legado.app.ui.widget.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import coil3.SingletonImageLoader
import coil3.asDrawable
import coil3.asImage
import coil3.request.ImageRequest
import coil3.request.CachePolicy
import coil3.size.Scale
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.databinding.DialogPhotoViewBinding
import io.legado.app.help.book.BookHelp
import io.legado.app.help.coil.LegadoFetcher
import io.legado.app.model.BookCover
import io.legado.app.model.ImageProvider
import io.legado.app.model.ReadBook
import io.legado.app.utils.setLayout
import io.legado.app.utils.viewbindingdelegate.viewBinding

/**
 * 显示图片
 */
class PhotoDialog() : BaseDialogFragment(R.layout.dialog_photo_view) {

    constructor(src: String, sourceOrigin: String? = null) : this() {
        arguments = Bundle().apply {
            putString("src", src)
            putString("sourceOrigin", sourceOrigin)
        }
    }

    private val binding by viewBinding(DialogPhotoViewBinding::bind)

    override fun onStart() {
        super.onStart()
        setLayout(1f, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    @SuppressLint("CheckResult")
    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        val arguments = arguments ?: return
        val src = arguments.getString("src") ?: return
        ImageProvider.get(src)?.let {
            binding.photoView.setImageBitmap(it)
            return
        }
        val file = ReadBook.book?.let { book ->
            BookHelp.getImage(book, src)
        }
        val request = if (file?.exists() == true) {
            ImageRequest.Builder(requireContext())
                .data(file)
                .error(android.graphics.drawable.ColorDrawable(android.graphics.Color.GRAY).asImage())
                .scale(Scale.FIT)
                .diskCachePolicy(CachePolicy.DISABLED)
                .target(
                    onSuccess = { result ->
                        binding.photoView.setImageDrawable(result.asDrawable(resources))
                    }
                )
                .build()
        } else {
            ImageRequest.Builder(requireContext())
                .data(src)
                .apply {
                    arguments.getString("sourceOrigin")?.let { sourceOrigin ->
                        extras[LegadoFetcher.sourceOriginKey] = sourceOrigin
                    }
                }
                .scale(Scale.FIT)
                .error(BookCover.defaultDrawable.asImage())
                .target(
                    onSuccess = { result ->
                        binding.photoView.setImageDrawable(result.asDrawable(resources))
                    }
                )
                .build()
        }
        SingletonImageLoader.get(requireContext()).enqueue(request)
    }

}
