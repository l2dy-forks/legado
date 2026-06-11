package io.legado.app.ui.main.rss

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import coil3.SingletonImageLoader
import coil3.asDrawable
import coil3.asImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.google.android.material.card.MaterialCardView
import io.legado.app.R
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.RssSource
import io.legado.app.databinding.ItemRssBinding
import io.legado.app.help.coil.LegadoFetcher
import io.legado.app.lib.theme.colorSurfaceContainer
import io.legado.app.ui.common.compose.RoundDropdownMenuItem
import io.legado.app.ui.common.compose.showComposeDropdownMenu
import splitties.views.onLongClick

class RssAdapter(
    context: Context,
    private val fragment: Fragment,
    private val callBack: CallBack,
    private val lifecycle: Lifecycle
) : RecyclerAdapter<RssSource, ItemRssBinding>(context) {

    override fun getViewBinding(parent: ViewGroup): ItemRssBinding {
        return ItemRssBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemRssBinding,
        item: RssSource,
        payloads: MutableList<Any>
    ) {
        binding.apply {
            (root as? MaterialCardView)?.setCardBackgroundColor(context.colorSurfaceContainer)
            tvName.text = item.sourceName
            val request = ImageRequest.Builder(context)
                .data(item.sourceIcon)
                .apply {
                    extras[LegadoFetcher.sourceOriginKey] = item.sourceUrl
                }
                .crossfade(true)
                .placeholder(context.getDrawable(R.drawable.image_rss)?.asImage())
                .error(context.getDrawable(R.drawable.image_rss)?.asImage())
                .target(
                    onSuccess = { result ->
                        ivIcon.setImageDrawable(result.asDrawable(context.resources))
                    }
                )
                .build()
            SingletonImageLoader.get(context).enqueue(request)
        }
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemRssBinding) {
        binding.apply {
            root.setOnClickListener {
                getItemByLayoutPosition(holder.layoutPosition)?.let {
                    callBack.openRss(it)
                }
            }
            root.onLongClick {
                getItemByLayoutPosition(holder.layoutPosition)?.let {
                    showMenu(ivIcon, it)
                }
            }
        }
    }

    private fun showMenu(view: View, rssSource: RssSource) {
        showComposeDropdownMenu(context, view) { dismiss ->
            RoundDropdownMenuItem(
                text = context.getString(R.string.to_top),
                onClick = { dismiss(); callBack.toTop(rssSource) },
            )
            RoundDropdownMenuItem(
                text = context.getString(R.string.edit),
                onClick = { dismiss(); callBack.edit(rssSource) },
            )
            RoundDropdownMenuItem(
                text = context.getString(R.string.disable_source),
                onClick = { dismiss(); callBack.disable(rssSource) },
            )
            RoundDropdownMenuItem(
                text = context.getString(R.string.delete),
                onClick = { dismiss(); callBack.del(rssSource) },
            )
        }
    }

    interface CallBack {
        fun openRss(rssSource: RssSource)
        fun toTop(rssSource: RssSource)
        fun edit(rssSource: RssSource)
        fun del(rssSource: RssSource)
        fun disable(rssSource: RssSource)
    }
}