package io.legado.app.ui.rss.favorites

import android.content.Context
import android.view.ViewGroup
import coil3.SingletonImageLoader
import coil3.asDrawable
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.RssStar
import io.legado.app.databinding.ItemRssArticleBinding
import io.legado.app.help.coil.LegadoFetcher
import io.legado.app.utils.gone
import io.legado.app.utils.visible


class RssFavoritesAdapter(context: Context, val callBack: CallBack) :
    RecyclerAdapter<RssStar, ItemRssArticleBinding>(context) {

    override fun getViewBinding(parent: ViewGroup): ItemRssArticleBinding {
        return ItemRssArticleBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemRssArticleBinding,
        item: RssStar,
        payloads: MutableList<Any>
    ) {
        binding.run {
            tvTitle.text = item.title
            tvPubDate.text = item.pubDate
            if (item.image.isNullOrBlank()) {
                imageView.gone()
            } else {
                val request = ImageRequest.Builder(context)
                    .data(item.image)
                    .apply {
                        if (item.origin != null) {
                            extras[LegadoFetcher.sourceOriginKey] = item.origin
                        }
                    }
                    .crossfade(true)
                    .target(
                        onSuccess = { result ->
                            imageView.visible()
                            imageView.setImageDrawable(result.asDrawable(context.resources))
                        },
                        onError = { _ ->
                            imageView.gone()
                        }
                    )
                    .build()
                SingletonImageLoader.get(context).enqueue(request)
            }
        }
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemRssArticleBinding) {
        holder.itemView.setOnClickListener {
            getItem(holder.layoutPosition)?.let {
                callBack.readRss(it)
            }
        }
        holder.itemView.setOnLongClickListener {
            getItem(holder.layoutPosition)?.let {
                callBack.delStar(it)
            }
            true
        }
    }

    interface CallBack {
        fun readRss(rssStar: RssStar)

        fun delStar(rssStar: RssStar)
    }
}