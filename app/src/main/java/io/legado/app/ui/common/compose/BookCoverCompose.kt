@file:OptIn(ExperimentalSharedTransitionApi::class)

package io.legado.app.ui.common.compose

import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import coil3.compose.AsyncImage
import io.legado.app.help.config.AppConfig
import io.legado.app.model.BookCover
import io.legado.app.ui.widget.image.CoverImageView

private const val SharedCoverRadiusCacheMaxSize = 256
private val sharedCoverRadiusCache = mutableStateMapOf<String, Dp>()

/**
 * Compose 原生书籍封面组件，替代基于 AndroidView 的 BookCoverImage。
 * 支持 SharedTransitionScope 共享元素过渡动画。
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BookCoverCompose(
    coverUrl: String?,
    name: String = "",
    author: String = "",
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    compact: Boolean = false,
    radius: Dp = if (compact) 4.dp else 8.dp,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedCoverKey: String? = null,
    showLoadingPlaceholder: Boolean = true,
    showShadow: Boolean = false,
) {
    val context = LocalContext.current
    val useDefaultCover = coverUrl.isNullOrBlank()
        || coverUrl == "use_default_cover"
        || AppConfig.useDefaultCover
    val finalPath = if (useDefaultCover) null else coverUrl

    var isOnlineCoverLoaded by remember(finalPath) {
        mutableStateOf(sharedCoverKey != null && finalPath != null)
    }

    LaunchedEffect(finalPath) {
        if (finalPath == null) {
            isOnlineCoverLoaded = false
        }
    }

    val transitionRadius = if (sharedCoverKey != null && animatedVisibilityScope != null) {
        rememberSharedCoverTransitionRadius(
            sharedCoverKey = sharedCoverKey,
            radius = radius,
            animatedVisibilityScope = animatedVisibilityScope
        )
    } else {
        radius
    }
    val shape = remember(transitionRadius) { RoundedCornerShape(transitionRadius) }

    val sharedElementModifier = with(sharedTransitionScope) {
        if (this != null && animatedVisibilityScope != null && sharedCoverKey != null) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(sharedCoverKey),
                animatedVisibilityScope = animatedVisibilityScope,
                clipInOverlayDuringTransition = OverlayClip(shape)
            )
        } else Modifier
    }

    Box(
        modifier = modifier
            .then(sharedElementModifier)
            .then(if (showShadow) Modifier.shadow(4.dp, shape) else Modifier)
            .background(
                if (!isOnlineCoverLoaded) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                } else Color.Transparent,
                shape
            )
            .clip(shape)
    ) {
        // Cover image
        if (finalPath != null) {
            AsyncImage(
                model = BookCover.loadRequest(context, finalPath),
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                onSuccess = { isOnlineCoverLoaded = true },
                onError = { isOnlineCoverLoaded = false },
            )
        }

        // Default cover: use legacy CoverImageView (draws name/author on default bg)
        if (!isOnlineCoverLoaded) {
            AndroidView(
                factory = { ctx ->
                    CoverImageView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                    }
                },
                update = { it.load(coverUrl, name, author) },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * 共享封面过渡动画中的圆角半径变化。
 * 从缓存的前一个半径值平滑过渡到当前半径。
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun rememberSharedCoverTransitionRadius(
    sharedCoverKey: String,
    radius: Dp,
    animatedVisibilityScope: AnimatedVisibilityScope,
): Dp {
    val transition = animatedVisibilityScope.transition
    val startRadius = sharedCoverRadiusCache[sharedCoverKey] ?: radius
    val animatedRadiusValue by transition.animateFloat(
        label = "book-cover-corner-radius"
    ) { state ->
        if (state == EnterExitState.Visible) radius.value else startRadius.value
    }

    LaunchedEffect(
        sharedCoverKey, radius,
        transition.currentState, transition.targetState
    ) {
        if (transition.currentState == EnterExitState.Visible
            && transition.targetState == EnterExitState.Visible
        ) {
            sharedCoverRadiusCache[sharedCoverKey] = radius
            if (sharedCoverRadiusCache.size > SharedCoverRadiusCacheMaxSize) {
                sharedCoverRadiusCache.keys
                    .firstOrNull { it != sharedCoverKey }
                    ?.let(sharedCoverRadiusCache::remove)
            }
        }
    }

    return animatedRadiusValue.dp
}
