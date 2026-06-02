package io.legado.app.ui.rss.article

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import coil3.SingletonImageLoader
import coil3.asDrawable
import coil3.asImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.crossfade
import io.legado.app.R
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.data.entities.RssArticle
import io.legado.app.databinding.ItemRssArticle1Binding
import io.legado.app.help.coil.LegadoFetcher
import io.legado.app.utils.gone
import io.legado.app.utils.visible
import io.legado.app.lib.theme.primaryTextColor
import io.legado.app.lib.theme.secondaryTextColor


class RssArticlesAdapter1(context: Context, callBack: CallBack) :
    BaseRssArticlesAdapter<ItemRssArticle1Binding>(context, callBack) {

    override fun getViewBinding(parent: ViewGroup): ItemRssArticle1Binding {
        return ItemRssArticle1Binding.inflate(inflater, parent, false)
    }

    @SuppressLint("CheckResult")
    override fun convert(
        holder: ItemViewHolder,
        binding: ItemRssArticle1Binding,
        item: RssArticle,
        payloads: MutableList<Any>
    ) {
        binding.run {
            tvTitle.text = item.title
            tvPubDate.text = item.pubDate
            if (item.image.isNullOrBlank() && !callBack.isGridLayout) {
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
                    .placeholder(context.getDrawable(R.drawable.image_rss_article)?.asImage())
                    .target(
                        onSuccess = { result: coil3.Image ->
                            imageView.visible()
                            imageView.setImageDrawable(result.asDrawable(context.resources))
                        },
                        onError = { _: coil3.Image? ->
                            imageView.gone()
                        }
                    )
                    .build()
                SingletonImageLoader.get(context).enqueue(request)
            }
            if (item.read) {
                tvTitle.setTextColor(context.secondaryTextColor)
            } else {
                tvTitle.setTextColor(context.primaryTextColor)
            }
        }
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemRssArticle1Binding) {
        holder.itemView.setOnClickListener {
            getItem(holder.layoutPosition)?.let {
                callBack.readRss(it)
            }
        }
    }

}