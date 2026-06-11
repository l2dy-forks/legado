package io.legado.app.ui.book.info.compose

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.preference.PreferenceManager
import io.legado.app.R
import io.legado.app.constant.BookType
import io.legado.app.constant.PreferKey
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookChapter
import io.legado.app.help.AppWebDav
import io.legado.app.help.book.addType
import io.legado.app.help.book.isAudio
import io.legado.app.help.book.isImage
import io.legado.app.help.book.isLocal
import io.legado.app.help.book.isWebFile
import io.legado.app.help.config.AppConfig
import io.legado.app.help.config.LocalConfig
import io.legado.app.lib.dialogs.alert
import io.legado.app.lib.dialogs.selector
import io.legado.app.ui.about.AppLogDialog
import io.legado.app.ui.book.audio.AudioPlayActivity
import io.legado.app.ui.book.changesource.ChangeBookSourceDialog
import io.legado.app.ui.book.group.GroupSelectDialog
import io.legado.app.ui.book.info.BookInfoViewModel
import io.legado.app.ui.book.info.edit.BookInfoEditActivity
import io.legado.app.ui.book.manga.ReadMangaActivity
import io.legado.app.ui.book.read.ReadBookActivity
import io.legado.app.ui.book.source.edit.BookSourceEditActivity
import io.legado.app.ui.book.toc.TocActivityResult
import io.legado.app.ui.login.SourceLoginActivity
import io.legado.app.ui.widget.dialog.VariableDialog
import io.legado.app.ui.widget.dialog.WaitDialog
import io.legado.app.utils.GSON
import io.legado.app.utils.StartActivityContract
import io.legado.app.utils.openFileUri
import io.legado.app.utils.sendToClip
import io.legado.app.utils.shareWithQr
import io.legado.app.utils.showDialogFragment
import io.legado.app.utils.startActivity
import io.legado.app.utils.toastOnUi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 完整功能版 — BookInfoComposeActivity 全部能力迁移。
 * UI 保持 BookDetailScreen 不变。
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BookInfoRouteScreen(
    bookUrl: String?,
    name: String?,
    author: String?,
    onBack: () -> Unit,
    onReadBook: (String, Boolean, Boolean) -> Unit = { _, _, _ -> },
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedCoverKey: String? = null,
) {
    val context = LocalContext.current
    val activity = context as? AppCompatActivity
    val fragActivity = context as? FragmentActivity
    val vm: BookInfoViewModel = viewModel(context as androidx.lifecycle.ViewModelStoreOwner)

    // ── ActivityResult launchers ──
    val tocLauncher = rememberLauncherForActivityResult(TocActivityResult()) { result ->
        result?.let { (i, p) -> vm.getBook(false)?.let { b ->
            fragActivity?.lifecycleScope?.launch {
                withContext(IO) { b.durChapterIndex = i; b.durChapterPos = p; appDb.bookDao.update(b) }
                activity?.startActivity(makeReadIntent(activity, vm, b))
            }
        } } ?: run { if (!vm.inBookshelf) vm.delBook() }
    }
    val readLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        vm.upBook(Intent())
        if (it.resultCode == Activity.RESULT_OK) vm.inBookshelf = true
        if (it.resultCode == ReadBookActivity.RESULT_DELETED) onBack()
    }
    val editLauncher = rememberLauncherForActivityResult(StartActivityContract(BookInfoEditActivity::class.java)) {
        if (it.resultCode == Activity.RESULT_OK) vm.upEditBook()
    }
    val srcLauncher = rememberLauncherForActivityResult(StartActivityContract(BookSourceEditActivity::class.java)) {
        if (it.resultCode != Activity.RESULT_CANCELED) vm.getBook()?.let { b ->
            vm.bookSource = appDb.bookSourceDao.getBookSource(b.origin); vm.refreshBook(b)
        }
    }

    val waitDialog = remember(activity) { activity?.let { WaitDialog(it) } }

    LaunchedEffect(bookUrl, name, author) {
        // Clear old data immediately to avoid flash of previous book
        vm.bookData.value = null
        vm.chapterListData.value = null
        vm.initData(Intent().apply {
            name?.let { putExtra("name", it) }; author?.let { putExtra("author", it) }
            bookUrl?.let { putExtra("bookUrl", it) }
        })
    }

    var book by remember { mutableStateOf<Book?>(null) }
    var chapters by remember { mutableStateOf<List<BookChapter>?>(null) }
    var inShelf by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val bo = Observer<Book?> { book = it }
        val co = Observer<List<BookChapter>?> { chapters = it }
        vm.bookData.observeForever(bo); vm.chapterListData.observeForever(co)
        onDispose { vm.bookData.removeObserver(bo); vm.chapterListData.removeObserver(co) }
    }
    DisposableEffect(Unit) {
        val p = PreferenceManager.getDefaultSharedPreferences(context)
        val l = SharedPreferences.OnSharedPreferenceChangeListener { _, k ->
            if (k == PreferKey.useDefaultCover) AppConfig.useDefaultCover = p.getBoolean(PreferKey.useDefaultCover, false)
        }
        p.registerOnSharedPreferenceChangeListener(l); onDispose { p.unregisterOnSharedPreferenceChangeListener(l) }
    }
    DisposableEffect(Unit) {
        val wo = Observer<Boolean> { show ->
            waitDialog?.let { if (show) { it.setText("Loading....."); it.show() } else it.dismiss() }
        }
        vm.waitDialogData.observeForever(wo); onDispose { vm.waitDialogData.removeObserver(wo) }
    }
    LaunchedEffect(Unit) { inShelf = vm.inBookshelf }

    if (book != null) {
        val b = book!!
        var upd by remember(b) { mutableStateOf(b.canUpdate) }
        var spl by remember(b) { mutableStateOf(b.getSplitLongChapter()) }

        BookDetailScreen(
            book = b, latestChapterTitle = b.latestChapterTitle, totalChapterNum = b.totalChapterNum,
            onBack = onBack,
            onReadClick = {
                val ctx = activity ?: return@BookDetailScreen
                if (b.isWebFile) showWebAlert(vm, fragActivity) { bk ->
                    if (!vm.inBookshelf) {
                        bk.addType(BookType.notShelf)
                        vm.saveBook(bk) { vm.saveChapterList { readLauncher.launch(makeReadIntent(ctx, vm, bk)) } }
                    } else readLauncher.launch(makeReadIntent(ctx, vm, bk))
                }
                else {
                    if (!vm.inBookshelf) {
                        b.addType(BookType.notShelf)
                        vm.saveBook(b) { vm.saveChapterList { readLauncher.launch(makeReadIntent(ctx, vm, b)) } }
                    } else readLauncher.launch(makeReadIntent(ctx, vm, b))
                }
            },
            onShelfClick = {
                if (b.isWebFile) showWebAlert(vm, fragActivity)
                else vm.addToBookshelf { inShelf = true }
            },
            inBookshelf = inShelf,
            onTocClick = {
                if (chapters.isNullOrEmpty()) activity?.toastOnUi(R.string.chapter_list_empty)
                else vm.getBook()?.let { tocLauncher.launch(it.bookUrl) }
            },
            onEditClick = { editLauncher.launch { putExtra("bookUrl", b.bookUrl) } },
            onMenuAction = { act ->
                if (act == MENU_CAN_UPDATE) { b.canUpdate = !b.canUpdate; upd = b.canUpdate }
                if (act == MENU_SPLIT_LONG_CHAPTER) { b.setSplitLongChapter(!b.getSplitLongChapter()); spl = b.getSplitLongChapter() }
                doMenu(act, b, vm, activity, fragActivity,
                    { editLauncher.launch { putExtra("bookUrl", b.bookUrl) } },
                    { srcLauncher.launch { putExtra("sourceUrl", b.origin) } },
                    waitDialog)
            },
            canUpdate = upd, splitLongChapter = spl,
            isLoginVisible = !vm.bookSource?.loginUrl.isNullOrBlank(),
            isSourceVariableVisible = vm.bookSource != null,
            isBookVariableVisible = vm.bookSource != null,
            coverTransitionName = sharedCoverKey,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            sharedCoverKey = sharedCoverKey,
        )
    }
}

// ═══════════════════════ 菜单 ═══════════════════════

private fun doMenu(id: Int, b: Book, vm: BookInfoViewModel, act: AppCompatActivity?, fa: FragmentActivity?,
                   onEdit: () -> Unit, onSrc: () -> Unit, wd: WaitDialog?) {
    val c = act ?: return
    when (id) {
        MENU_EDIT -> onEdit()
        MENU_SHARE -> c.shareWithQr("${b.bookUrl}#${GSON.toJson(b)}", b.name)
        MENU_REFRESH -> vm.getBook()?.let { vm.refreshBook(it) }
        MENU_LOGIN -> vm.bookSource?.let { c.startActivity<SourceLoginActivity> { putExtra("type","bookSource"); putExtra("key",it.bookSourceUrl) } }
        MENU_TOP -> vm.topBook()
        MENU_SET_SOURCE_VARIABLE -> doSrcVar(vm, act)
        MENU_SET_BOOK_VARIABLE -> doBookVar(vm, act)
        MENU_COPY_BOOK_URL -> c.sendToClip(b.bookUrl)
        MENU_COPY_TOC_URL -> b.tocUrl?.let { c.sendToClip(it) }
        MENU_CAN_UPDATE -> { if (vm.inBookshelf) vm.saveBook(b) }
        MENU_SPLIT_LONG_CHAPTER -> vm.loadBookInfo(b, false)
        MENU_CLEAR_CACHE -> vm.clearCache()
        MENU_LOG -> act?.showDialogFragment<AppLogDialog>()
        MENU_UPLOAD -> doUpload(b, vm, c, wd)
        MENU_DELETE -> doDelete(vm, act)
        MENU_CHANGE_SOURCE -> {
            val n = appDb.bookSourceDao.getBookSource(b.origin)?.bookSourceName ?: b.originName
            act?.alert(titleResource = R.string.change_origin) {
                if (n.isNotBlank()) setMessage(n)
                neutralButton(R.string.view_source) {
                    if (!b.isLocal && appDb.bookSourceDao.has(b.origin)) onSrc()
                }
                okButton { act?.showDialogFragment(ChangeBookSourceDialog(b.name, b.author)) }
                cancelButton()
            }
        }
        MENU_GROUP -> act?.showDialogFragment(GroupSelectDialog(b.group))
    }
}

// ═══════════════════════ 阅读 ═══════════════════════

private fun makeReadIntent(ctx: android.content.Context, vm: BookInfoViewModel, b: Book): Intent {
    val cls: Class<*> = when {
        b.isAudio -> AudioPlayActivity::class.java
        !b.isLocal && b.isImage && AppConfig.showMangaUi -> ReadMangaActivity::class.java
        else -> ReadBookActivity::class.java
    }
    return Intent(ctx, cls).apply { putExtra("bookUrl", b.bookUrl); putExtra("inBookshelf", vm.inBookshelf) }
}

// ═══════════════════════ 删除/上传/WebFile ═══════════════════════

@SuppressLint("InflateParams")
private fun doDelete(vm: BookInfoViewModel, act: AppCompatActivity?) {
    val c = act ?: return
    vm.getBook()?.let { b ->
        if (LocalConfig.bookInfoDeleteAlert) {
            c.alert(titleResource = R.string.draw, messageResource = R.string.sure_del) {
                var cb: CheckBox? = null
                if (b.isLocal) {
                    cb = CheckBox(c).apply {
                        setText(R.string.delete_book_file)
                        isChecked = LocalConfig.deleteBookOriginal
                    }
                    customView {
                        val ll = LinearLayout(c)
                        ll.setPadding((16 * c.resources.displayMetrics.density).toInt(), 0, (16 * c.resources.displayMetrics.density).toInt(), 0)
                        cb?.let { ll.addView(it) }; ll
                    }
                }
                yesButton {
                    if (cb != null) LocalConfig.deleteBookOriginal = cb!!.isChecked
                    vm.delBook(LocalConfig.deleteBookOriginal)
                }
                noButton()
            }
        } else {
            vm.delBook(LocalConfig.deleteBookOriginal)
        }
    }
}

private fun doUpload(b: Book, vm: BookInfoViewModel, c: android.content.Context, wd: WaitDialog?) {
    (c as? AppCompatActivity)?.lifecycleScope?.launch {
        wd?.setText("上传中....."); wd?.show()
        try { AppWebDav.defaultBookWebDav?.upload(b) ?: c.toastOnUi("未配置webDav"); b.lastCheckTime = System.currentTimeMillis(); vm.saveBook(b) }
        catch (e: Exception) { c.toastOnUi(e.localizedMessage) }
        finally { wd?.dismiss() }
    }
}

private fun showWebAlert(vm: BookInfoViewModel, fa: FragmentActivity?, cb: ((Book) -> Unit)? = null) {
    val a = fa ?: return; val fs = vm.webFiles
    if (fs.isEmpty()) { a.toastOnUi("Unexpected webFileData"); return }
    a.selector(R.string.download_and_import_file, fs) { _, wf, _ -> when {
        wf.isSupported -> vm.importOrDownloadWebFile<Book>(wf) { cb?.invoke(it) }
        wf.isSupportDecompress -> vm.importOrDownloadWebFile<android.net.Uri>(wf) { uri -> vm.getArchiveFilesName(uri) { ns ->
            if (ns.size == 1) vm.importArchiveBook(uri, ns[0]) { cb?.invoke(it) }
            else a.selector(R.string.import_select_book, ns) { _, n, _ -> vm.importArchiveBook(uri, n) { cb?.invoke(it) } }
        } }
        else -> a.alert(title = a.getString(R.string.draw), message = a.getString(R.string.file_not_supported, wf.name)) {
            neutralButton(R.string.open_fun) { vm.importOrDownloadWebFile<android.net.Uri>(wf) { a.openFileUri(it, "*/*") } }; noButton()
        }
    } }
}

private fun doSrcVar(vm: BookInfoViewModel, act: AppCompatActivity?) {
    val a = act ?: return; a.lifecycleScope.launch {
        val s = vm.bookSource ?: run { a.toastOnUi("书源不存在"); return@launch }
        a.showDialogFragment(VariableDialog(a.getString(R.string.set_source_variable), s.getKey(), withContext(IO) { s.getVariable() }, s.getDisplayVariableComment("源变量可在js中通过source.getVariable()获取")))
    }
}

private fun doBookVar(vm: BookInfoViewModel, act: AppCompatActivity?) {
    val a = act ?: return; a.lifecycleScope.launch {
        val s = vm.bookSource ?: run { a.toastOnUi("书源不存在"); return@launch }
        val bk = vm.getBook() ?: return@launch
        a.showDialogFragment(VariableDialog(a.getString(R.string.set_book_variable), bk.bookUrl, withContext(IO) { bk.getCustomVariable() }, s.getDisplayVariableComment("""书籍变量可在js中通过book.getVariable("custom")获取""")))
    }
}
