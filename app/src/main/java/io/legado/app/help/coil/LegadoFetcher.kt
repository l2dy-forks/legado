package io.legado.app.help.coil

import coil3.Extras
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.script.rhino.runScriptWithContext
import io.legado.app.data.entities.BaseSource
import io.legado.app.exception.NoStackTraceException
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.help.http.okHttpClient
import io.legado.app.help.http.okHttpClientManga
import io.legado.app.help.source.SourceHelp
import io.legado.app.model.ReadManga
import io.legado.app.model.analyzeRule.AnalyzeUrl
import io.legado.app.utils.ImageUtils
import io.legado.app.utils.isWifiConnect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.SupervisorJob
import okhttp3.Request
import okio.Buffer
import okio.FileSystem
import okio.buffer
import splitties.init.appCtx

/**
 * Coil Fetcher，替代 Glide 的 OkHttpModelLoader + OkHttpStreamFetcher。
 *
 * 负责：
 * 1. 通过 AnalyzeUrl 处理 URL（书源 URL 选项、Cookie）
 * 2. 注入自定义请求头
 * 3. WiFi 限制检查
 * 4. 图片解密（ImageUtils.decode）
 * 5. 使用正确的 OkHttp 客户端（普通/漫画）
 */
class LegadoFetcher private constructor(
    private val url: String,
    private val options: Options,
) {

    // Coil Extras keys
    companion object {
        val sourceOriginKey = Extras.Key<String>("")
        val loadOnlyWifiKey = Extras.Key<Boolean>(false)
        val mangaKey = Extras.Key<Boolean>(false)
    }

    suspend fun fetch(): FetchResult {
        val loadOnlyWifi = options.extras[loadOnlyWifiKey] ?: false
        if (loadOnlyWifi && !appCtx.isWifiConnect) {
            throw NoStackTraceException("只在wifi加载图片")
        }

        val sourceOrigin = options.extras[sourceOriginKey]
        val isManga = options.extras[mangaKey] ?: false

        var source: BaseSource? = null
        if (sourceOrigin != null) {
            source = SourceHelp.getSource(sourceOrigin)
        }

        val coroutineContext = SupervisorJob()
        val analyzedUrl = AnalyzeUrl(
            url,
            source = source,
            coroutineContext = coroutineContext
        )

        val requestBuilder = Request.Builder().url(analyzedUrl.getGlideUrl().toStringUrl())
        analyzedUrl.getGlideUrl().headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }
        val request = requestBuilder.build()

        val call = if (isManga) {
            okHttpClientManga.newCall(request)
        } else {
            okHttpClient.newCall(request)
        }

        val response = call.execute()
        if (!response.isSuccessful) {
            throw NoStackTraceException("图片加载失败: ${response.code}")
        }

        val responseBody = response.body ?: throw NoStackTraceException("响应体为空")

        // 图片解密
        val finalSource: Buffer = if (!ImageUtils.skipDecode(source, !isManga)) {
            val decoded = kotlinx.coroutines.withContext(IO) {
                runScriptWithContext(coroutineContext) {
                    if (isManga) {
                        ImageUtils.decode(
                            url,
                            responseBody.bytes(),
                            isCover = false,
                            source,
                            ReadManga.book
                        )?.inputStream()
                    } else {
                        ImageUtils.decode(
                            analyzedUrl.getGlideUrl().toStringUrl(),
                            responseBody.byteStream(),
                            isCover = true,
                            source
                        )
                    }
                }
            }
            val decodedStream = decoded as? java.io.InputStream
            if (decodedStream == null) {
                throw NoStackTraceException("图片解密失败")
            }
            Buffer().readFrom(decodedStream)
        } else {
            Buffer().readFrom(responseBody.byteStream())
        }

        return SourceFetchResult(
            source = ImageSource(finalSource, FileSystem.SYSTEM),
            mimeType = "image/*",
            dataSource = DataSource.NETWORK,
        )
    }

    class Factory : coil3.fetch.Fetcher.Factory<String> {
        override fun create(url: String, options: Options, imageLoader: ImageLoader): coil3.fetch.Fetcher? {
            return Fetcher { LegadoFetcher(url, options).fetch() }
        }
    }
}

private fun Fetcher(block: suspend () -> FetchResult): coil3.fetch.Fetcher {
    return object : coil3.fetch.Fetcher {
        override suspend fun fetch(): FetchResult = block()
    }
}
