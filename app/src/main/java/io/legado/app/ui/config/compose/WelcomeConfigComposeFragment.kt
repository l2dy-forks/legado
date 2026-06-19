package io.legado.app.ui.config.compose

import android.net.Uri
import androidx.compose.runtime.Composable
import io.legado.app.R
import io.legado.app.constant.PreferKey
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.dialogs.selector
import io.legado.app.model.BookCover
import io.legado.app.ui.file.HandleFileContract
import io.legado.app.utils.FileUtils
import io.legado.app.utils.MD5Utils
import io.legado.app.utils.externalFiles
import io.legado.app.utils.getPrefString
import io.legado.app.utils.inputStream
import io.legado.app.utils.putPrefString
import io.legado.app.utils.readUri
import io.legado.app.utils.removePref
import io.legado.app.utils.toastOnUi
import splitties.init.appCtx
import java.io.FileOutputStream

class WelcomeConfigComposeFragment : ConfigComposeFragment() {

    private var pendingWelcomeIsNight: Boolean = false

    private val selectWelcomeImage = registerForActivityResult(HandleFileContract()) { result ->
        result.uri?.let { uri ->
            val key = if (pendingWelcomeIsNight) PreferKey.welcomeImageDark else PreferKey.welcomeImage
            setImageFromUri(key, uri)
        }
    }

    @Composable
    override fun ConfigContent() {
        WelcomeConfigScreen(
            onBackClick = { activity?.finish() },
            onWelcomeImageClick = { isNight ->
                pendingWelcomeIsNight = isNight
                val key = if (isNight) PreferKey.welcomeImageDark else PreferKey.welcomeImage
                if (getPrefString(key).isNullOrEmpty()) {
                    launchImagePicker(isNight)
                } else {
                    context?.selector(
                        items = arrayListOf(
                            getString(R.string.delete),
                            getString(R.string.select_image),
                        )
                    ) { _, i ->
                        if (i == 0) {
                            removePref(key)
                            if (isNight) {
                                AppConfig.welcomeShowTextDark = true
                                AppConfig.welcomeShowIconDark = true
                            } else {
                                AppConfig.welcomeShowText = true
                                AppConfig.welcomeShowIcon = true
                            }
                            BookCover.upDefaultCover()
                        } else {
                            launchImagePicker(isNight)
                        }
                    }
                }
            },
        )
    }

    private fun launchImagePicker(isNight: Boolean) {
        pendingWelcomeIsNight = isNight
        selectWelcomeImage.launch {
            requestCode = if (isNight) 222 else 221
            mode = HandleFileContract.IMAGE
        }
    }

    private fun setImageFromUri(preferenceKey: String, uri: Uri) {
        readUri(uri) { fileDoc, inputStream ->
            kotlin.runCatching {
                var file = requireContext().externalFiles
                val suffix = fileDoc.name.substringAfterLast(".")
                val fileName = uri.inputStream(requireContext()).getOrThrow().use {
                    MD5Utils.md5Encode(it) + ".$suffix"
                }
                file = FileUtils.createFileIfNotExist(file, "covers", fileName)
                FileOutputStream(file).use {
                    inputStream.copyTo(it)
                }
                putPrefString(preferenceKey, file.absolutePath)
            }.onFailure {
                appCtx.toastOnUi(it.localizedMessage)
            }
        }
    }
}
