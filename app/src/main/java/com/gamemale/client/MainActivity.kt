package com.gamemale.client

import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.os.Build
import android.content.ContentValues
import android.provider.MediaStore
import android.webkit.CookieManager
import android.widget.Toast
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import com.itos.coui.theme.CouiTheme

/** 底部导航直达版块：按发布时间倒序（不是最后回复） */
private const val ANIME_URL =
    SITE + "forum.php?mod=forumdisplay&fid=150&filter=dateline&orderby=dateline&typeid=831"
private const val GAME_URL =
    SITE + "forum.php?mod=forumdisplay&fid=152&filter=dateline&orderby=dateline&typeid=737"

sealed interface Nav {
    data object Home : Nav
    data class Forum(val url: String, val title: String) : Nav
    data class Thread(val url: String, val title: String) : Nav
    data object Settings : Nav
    data object Search : Nav
    data object History : Nav
}

class MainActivity : ComponentActivity() {

    private lateinit var fetcher: Fetcher
    private lateinit var browserHost: FrameLayout

    private var nav by mutableStateOf<Nav>(Nav.Home)
    private var backStack = mutableListOf<Nav>()
    private var forums by mutableStateOf<List<ForumItem>>(emptyList())
    private var forumPage by mutableStateOf<ForumPage?>(null)
    private var threadPage by mutableStateOf<ThreadPage?>(null)
    private var loading by mutableStateOf(false)
    private var error by mutableStateOf<String?>(null)
    private var darkMode by mutableStateOf(false)
    private var lastHtml: String = ""
    private var busy by mutableStateOf(false)
    private var replyOpen by mutableStateOf(false)
    private var rateOpen by mutableStateOf(false)
    private var rateFields by mutableStateOf<List<Triple<String, String, String>>>(emptyList())
    private var loginName by mutableStateOf<String?>(null)
    private var loggedIn by mutableStateOf(false)
    private var avatarUrl by mutableStateOf<String?>(null)
    private var imageUrl by mutableStateOf<String?>(null)
    private lateinit var cache: ThreadCache
    /** 0 跟随系统 / 1 深色 / 2 浅色 */
    private var themeMode by mutableStateOf(0)
    private var fontScale by mutableStateOf(1f)
    private var cacheMb by mutableStateOf(300)
    private var cacheBytes by mutableStateOf(0L)
    private var cacheCount by mutableStateOf(0)
    private var rateFormDef: FormDef? = null
    private var rateReasonName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        showLastCrash()

        // 账号表：上次用在哪个账号，启动时先按它展示
        accounts = Accounts.load(this)
        currentAccountId = Accounts.currentId(this)
        history = HistoryStore.load(this)
        // 覆盖升级/进程被杀后，CookieManager 里的登录 Cookie 可能丢，但账号表里有快照
        runCatching {
            val saved = accounts.firstOrNull { it.id == currentAccountId } ?: accounts.firstOrNull()
            if (saved != null && !hasLoginCookie()) {
                Diag.log("account", "Cookie 缺失，用账号快照恢复 " + saved.name)
                Accounts.apply(saved.cookie) { }
            }
        }

        val prefs = getSharedPreferences("gm", MODE_PRIVATE)
        // 没手动选过就跟随系统的深浅色
        val systemDark = (resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        themeMode = prefs.getInt("themeMode", if (prefs.getBoolean("dark", systemDark)) 1 else 2)
        darkMode = when (themeMode) {
            1 -> true
            2 -> false
            else -> systemDark
        }
        fontScale = prefs.getFloat("fontScale", 1f)
        ImageStore.init(this)
        ThumbStore.init(this)
        SnapshotStore.init(this)
        Diag.init(this)
        Diag.init(this)
        cache = ThreadCache(this)
        cacheMb = cache.limitMb
        refreshCacheStats()
        applyStatusBar()

        browserHost = FrameLayout(this)
        fetcher = Fetcher(this)

        val root = ComposeView(this)
        root.setContent {
            CouiTheme(darkTheme = darkMode, colorScheme = rememberScheme(darkMode)) {
                // 预测性返回：手指拖动时内容跟着右移，松手提交后退
                PredictiveBackHandler(enabled = true) { progress ->
                    // 手势被取消时 collect 会抛 CancellationException，不会走到 goBack
                    try {
                        progress.collect { e -> backProgress = e.progress }
                    } finally {
                        backProgress = 0f
                    }
                    goBack()
                }
                AppUi(
                    nav = nav,
                    forums = forums,
                    forumPage = forumPage,
                    threadPage = threadPage,
                    loading = loading,
                    error = error,
                    dark = darkMode,
                    host = browserHost,
                    onBack = { goBack() },
                    onRetry = { reload() },
                    onOpenForum = { openForum(it) },
                    onOpenThread = { openThread(it) },
                    onLoad = { url -> linkLoad(url) },
                    onToggleDark = {
                        themeMode = if (darkMode) 2 else 1
                        darkMode = !darkMode
                        getSharedPreferences("gm", MODE_PRIVATE).edit()
                            .putInt("themeMode", themeMode).commit()
                        applyStatusBar()
                    },
                    onSettings = { openSettings() },
                    fontScale = fontScale,
                    themeMode = themeMode,
                    cacheMb = cacheMb,
                    cacheBytes = cacheBytes,
                    cacheCount = cacheCount,
                    onSetThemeMode = { m ->
                        themeMode = m
                        darkMode = when (m) {
                            1 -> true
                            2 -> false
                            else -> (resources.configuration.uiMode and
                                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                                android.content.res.Configuration.UI_MODE_NIGHT_YES
                        }
                        getSharedPreferences("gm", MODE_PRIVATE).edit()
                            .putInt("themeMode", m).commit()
                        applyStatusBar()
                    },
                    onSetFontScale = { v ->
                        fontScale = v
                        getSharedPreferences("gm", MODE_PRIVATE).edit()
                            .putFloat("fontScale", v).commit()
                    },
                    onSetCacheMb = { v ->
                        cacheMb = v
                        cache.limitMb = v
                        refreshCacheStats()
                    },
                    onExportLogs = { Diag.export(this) },
                    onRefreshThread = { (nav as? Nav.Thread)?.url?.let { reloadThread(it) } },
                    onOpenBrowser = { (nav as? Nav.Thread)?.url?.let { openInBrowser(it) } },
                    onClearCache = {
                        prefetchToken++
                        fetcher.dropPending()
                        cache.clear()
                        threadCache.clear()
                        forumCache.clear()
                        SnapshotStore.clear()
                        ThumbStore.clear()
                        refreshCacheStats()
                        toast("缓存已清除")
                    },
                    canGoBack = backStack.isNotEmpty() || nav is Nav.Thread ||
                        nav is Nav.Settings || nav is Nav.Search,
                    refreshing = refreshing,
                    onRefresh = { refreshCurrentForum() },
                    onOpenSearch = { openSearch() },
                    searchHistory = searchHistory,
                    backProgress = backProgress,
                    onThumbNeeded = { t -> ensureThumb(t) },
                    onLogin = { openLogin() },
                    needUpgrade = needUpgrade,
                    onOpenSite = { openInBrowser(SITE + "forum.php") },
                    needLogin = needLogin,
                    loginPage = loginPage,
                    loginWeb = loginWeb,
                    onLoginDone = { finishLogin() },
                    onLoginClose = { closeLogin() },
                    verifyPage = verifyPage,
                    verifyWeb = verifyWeb,
                    onVerifyClose = { closeVerify() },
                    accountsOpen = accountsOpen,
                    accounts = accounts,
                    currentAccountId = currentAccountId,
                    onOpenAccounts = { openAccounts() },
                    onSwitchAccount = { acc -> switchAccount(acc) },
                    onDeleteAccount = { acc -> deleteAccount(acc) },
                    onAddAccount = { addAccount() },
                    onCloseAccounts = { accountsOpen = false },
                    onAvatar = { },
                    onReply = { replyOpen = true },
                    onRate = { pid -> openRate(pid) },
                    onAttach = { at -> openAttach(at) },
                    onImage = { url -> openImageExternally(url) },
                    replyOpen = replyOpen,
                    onCancelReply = { replyOpen = false },
                    onSendReply = { text -> sendReply(text) },
                    onCancelRate = { rateOpen = false },
                    rateOpen = rateOpen,
                    rateFields = rateFields,
                    onSendRate = { reason -> sendRate(reason) },
                    imageUrl = imageUrl,
                    onCloseImage = { imageUrl = null },
                    busy = busy,
                    onDump = { dumpHtml("debug") },
                    onQuickForum = { url, title -> openQuick(url, title) },
                    onOpenHistoryTab = { openHistory() },
                    history = history,
                    onOpenHistory = { url, title -> openFromHistory(url, title) },
                    onClearHistory = { history = HistoryStore.clear(this) },
                    onSearch = { q -> doSearch(q) },
                    onExternal = { openExternal(it) },
                    currentUrl = navUrl(),
                    showBottomBar = backStack.isEmpty() && (nav is Nav.Forum || nav is Nav.History),
                    loginName = loginName,
                    avatarUrl = avatarUrl,
                    loggedIn = loggedIn,
                    onShare = { shareThread() },
                    onCopyLink = { copyThreadLink() }
                )
            }
        }
        setContentView(root)
        fetcher.attach(browserHost)
        // 默认直接进「动漫」版块，省掉版块总览这一层
        val deep = threadUrlFromIntent(intent)
        if (deep != null) {
            // 外部分享/深链进来的帖子也要记入浏览记录
            tidOf(deep)?.toLongOrNull()?.let { history = HistoryStore.record(this, it, "帖子", deep) }
            nav = Nav.Thread(deep, "帖子")
            load(deep)
        } else {
            nav = Nav.Forum(ANIME_URL, "和谐动漫")
            load(ANIME_URL)
        }
    }

    /** 从 VIEW / SEND intent 里取出帖子 URL（外部分享进来的 gamemale 帖子链接） */
    private fun threadUrlFromIntent(intent: Intent?): String? {
        if (intent == null) return null
        val raw = when (intent.action) {
            Intent.ACTION_VIEW -> intent.data?.toString()
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> null
        } ?: return null
        val tid = Regex("thread-(\\d+)").find(raw)?.groupValues?.get(1)
            ?: Regex("tid=(\\d+)").find(raw)?.groupValues?.get(1)
            ?: return null
        return SITE + "thread-" + tid + "-1-1.html"
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val url = threadUrlFromIntent(intent) ?: return
        tidOf(url)?.toLongOrNull()?.let { history = HistoryStore.record(this, it, "帖子", url) }
        backStack.clear()
        nav = Nav.Thread(url, "帖子")
        threadPage = null
        load(url)
    }

    override fun onResume() {
        super.onResume()
        fetcher.attach(browserHost)
        fetcher.keepRunning()
    }

    override fun onPause() {
        // 不主动暂停 WebView：切后台时保持当前加载继续跑
        fetcher.keepRunning()
        super.onPause()
    }

    // ---------------- 数据加载 ----------------
    private var homeWarmed = false
    private var homeAlt = false

    private fun loadHome() {
        loading = true
        error = null
        val url = if (homeAlt) SITE else SITE + "forum.php"
        fetcher.fetch(url) { r ->
            runOnUiThread {
                loading = false
                refreshing = false
                r.onSuccess { html ->
                    lastHtml = html
                    refreshLogin(html)
                    val list = Site.parseForums(html)
                    if (list.isEmpty()) {
                        if (!homeWarmed) {
                            homeWarmed = true
                            warmUpForum()
                        } else if (!homeAlt) {
                            // 再试站点根地址（门户/论坛入口不同模板）
                            homeAlt = true
                            loadHome()
                        } else {
                            dumpHtml("home")
                            error = "版块解析到 0 个 · " + pageDiag(html)
                        }
                    } else {
                        forums = list
                    }
                }.onFailure {
                    if (!homeWarmed) {
                        homeWarmed = true
                        warmUpForum()
                    } else {
                        error = it.message ?: "加载失败"
                    }
                }
            }
        }
    }

    /** 把最近一次取到的原始 HTML 导出到「下载」，方便排查解析问题 */
    private fun dumpHtml(tag: String) {
        val html = lastHtml
        if (html.isBlank()) {
            toast("还没取到页面内容")
            return
        }
        runCatching {
            val name = "gamemale-" + tag + ".html"
            if (Build.VERSION.SDK_INT >= 29) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/html")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: throw IllegalStateException("无法创建文件")
                contentResolver.openOutputStream(uri)?.use {
                    it.write(html.toByteArray(Charsets.UTF_8))
                } ?: throw IllegalStateException("无法写入文件")
            } else {
                @Suppress("DEPRECATION")
                java.io.File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    name
                ).writeText(html)
            }
            toast("已导出到 下载/" + name + "，把它发给我")
        }.onFailure { toast("导出失败：" + (it.message ?: "")) }
    }

    private fun dumpText(name: String, text: String) {
        runCatching {
            val file = "gamemale-" + name + ".html"
            runCatching {
                val dir = getExternalFilesDir(null) ?: filesDir
                java.io.File(dir, file).writeText(text)
            }
            if (Build.VERSION.SDK_INT >= 29) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, file)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/html")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return
                contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray(Charsets.UTF_8)) }
            }
        }
    }

    private fun pageDiag(html: String): String {
        val title = Regex("<title>(.*?)</title>", setOf(RegexOption.DOT_MATCHES_ALL))
            .find(html)?.groupValues?.get(1)?.trim()?.take(40) ?: "无标题"
        val tag = if (html.contains("fl_g")) "含版块节点" else "无版块节点"
        return "「" + title + "」" + html.length + "字节·" + tag
    }

    /** 隐藏 WebView 被拦时，用可见的 WebView 过一次站点校验（Cookie/证书落到同一个 CookieManager） */
    private fun warmUpForum() {
        toast("正在通过站点校验，稍等一下…")
        val web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.setBackgroundColor(android.graphics.Color.WHITE)
        val dialog = Dialog(this, android.R.style.Theme_DeviceDefault_NoActionBar)
        val container = FrameLayout(this)
        container.addView(web, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        dialog.setContentView(container)
        var closed = false
        fun closeAndRetry() {
            if (closed) return
            closed = true
            runCatching {
                (web.parent as? ViewGroup)?.removeView(web)
                web.destroy()
            }
            runCatching { dialog.dismiss() }
            loadHome()
        }
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                view.postDelayed({ closeAndRetry() }, 900)
            }
        }
        dialog.setOnDismissListener {
            runCatching {
                (web.parent as? ViewGroup)?.removeView(web)
                web.destroy()
            }
            if (!closed) {
                closed = true
                loadHome()
            }
        }
        dialog.show()
        web.loadUrl(SITE + "forum.php")
    }

    private fun load(rawUrl: String) {
        val url = if (rawUrl.contains("mod=forumdisplay") && !rawUrl.contains("orderby="))
            rawUrl + "&orderby=dateline" else rawUrl
        if (nav is Nav.Forum && forumPage == null) {
            val snap = forumCache[url] ?: SnapshotStore.get(url)
            if (snap != null) {
                Diag.log("load", "先用磁盘快照铺屏 threads=" + snap.threads.size)
                forumPage = snap.copy(cats = mergeCats(emptyList(), snap.cats), title = (nav as Nav.Forum).title)
            }
        }
        // 已经有旧列表（快照或上一屏）时，用顶部细条静默刷新，不铺全屏 loading
        val hasOldList = nav is Nav.Forum && forumPage?.threads?.isNotEmpty() == true
        if (hasOldList) {
            refreshing = true
            error = null
        } else {
            loading = true
            error = null
        }
        Diag.log("load", "start nav=" + nav + " url=" + url)
        if (nav is Nav.Forum && loadedForumUrl == url) retryCount = 0
        if (nav is Nav.Forum) {
            loadedForumUrl = url
            loadedForumTitle = (nav as Nav.Forum).title
        }
        runCatching { fetcher.fetch(url) { r ->
            runOnUiThread {
                loading = false
                r.onSuccess { html ->
                    lastHtml = html
                    if (url.contains("search.php")) {
                        val limited = listOf("两次搜索", "搜索间隔", "搜索太频繁", "频率限制", "请稍后再试")
                            .any { html.contains(it) }
                        if (limited) {
                            error = "搜索太频繁了，站点限制了搜索间隔，请等十几秒再试"
                            return@onSuccess
                        }
                    }
                    runCatching {
                        refreshLogin(html)
                        when (nav) {
                            is Nav.Thread -> {
                                if (!sameTarget((nav as Nav.Thread).url, url)) {
                                    Diag.log("load", "丢弃过期帖子响应 url=" + url + " 当前=" + (nav as Nav.Thread).url)
                                    return@onSuccess
                                }
                                // 650KB 页面的 jsoup 解析和落盘都放后台，UI 先把已有内容铺出来
                                applyThreadHtml(html, url)
                            }
                            is Nav.Forum -> {
                                if (!sameTarget((nav as Nav.Forum).url, url)) {
                                    Diag.log("load", "丢弃过期响应 url=" + url + " 当前=" + (nav as Nav.Forum).url)
                                    return@onSuccess
                                }
                                loadedForumUrl = url
                                loadedForumTitle = (nav as Nav.Forum).title
                                if (!loggedIn && isLoginRequired(html)) {
                                    Diag.log("load", "未登录且需要登录 url=" + url)
                                    needLogin = true
                                    needUpgrade = false
                                    refreshing = false
                                    loading = false
                                    error = null
                                    forumPage = (nav as Nav.Forum).let { f ->
                                        Site.parseForumPage(html).copy(title = f.title)
                                    }
                                    return@onSuccess
                                }
                                needLogin = false
                                if (isGroupBlocked(html)) {
                                    Diag.log("load", "用户组等级不足 url=" + url)
                                    needUpgrade = true
                                    refreshing = false
                                    loading = false
                                    error = null
                                    forumPage = (nav as Nav.Forum).let { f ->
                                        Site.parseForumPage(html).copy(title = f.title)
                                    }
                                    return@onSuccess
                                }
                                needUpgrade = false
                                var fp = Site.parseForumPage(html)
                                // 未登录时版块页是一条"游客无权查看"的短页（~46KB，没有任何帖子行），
                                // 不依赖具体文案，直接按"没登录 + 列表为空"判定
                                if (fp.threads.isEmpty() && !loggedIn && url.contains("mod=forumdisplay")) {
                                    Diag.log("load", "未登录，列表为空，提示登录 url=" + url)
                                    needLogin = true
                                    needUpgrade = false
                                    refreshing = false
                                    error = null
                                    forumPage = fp.copy(title = (nav as Nav.Forum).title)
                                    return@onSuccess
                                }
                                if (fp.threads.isEmpty() && url.contains("search.php")) {
                                    val hits = Site.parseSearchResults(html)
                                    if (hits.isNotEmpty()) fp = fp.copy(threads = hits)
                                }
                                if (url.contains("search.php") && fp.threads.isEmpty()) {
                                    // 搜索结果空：先重抓一次（站点偶尔还在生成结果），仍空就给明确文案
                                    val q = Regex("srchtxt=([^&]+)").find(url)?.groupValues?.get(1)
                                        ?.let { runCatching { Uri.decode(it) }.getOrNull() } ?: ""
                                    if (retryCount < 1) {
                                        retryCount++
                                        load(url)
                                        return@onSuccess
                                    }
                                    retryCount = 0
                                    dumpText("search-empty", html)
                                    error = if (q.isEmpty()) "没有搜到相关帖子"
                                    else "没有搜到与「$q」相关的帖子（也可能被站点限流，稍后再试）"
                                    return@onSuccess
                                }
                                if (fp.threads.isEmpty() && url.contains("typeid=") && !url.contains("search.php")) {
                                    // 分类参数失效时，退回不带 typeid 的版块页再抓一次
                                    val base = Regex("(https?://[^&]+mod=forumdisplay&fid=\\d+)").find(url)?.groupValues?.get(1)
                                    if (base != null) {
                                        val tid2 = Regex("fid=(\\d+)").find(base)?.groupValues?.get(1)
                                        val baseUrl = base + "&filter=dateline&orderby=dateline"
                                        dumpText("forum-empty", html)
                                        if (tid2 != null && nav is Nav.Forum && (nav as Nav.Forum).url != baseUrl) {
                                            Diag.log("load", "分类为空，回退 " + baseUrl)
                                            nav = Nav.Forum(baseUrl, (nav as Nav.Forum).title)
                                            load(baseUrl)
                                            return@onSuccess
                                        }
                                    }
                                }
                                fp = fp.copy(threads = fp.threads.map { item ->
                                    if (item.thumbs.isEmpty()) {
                                        val cachedThumbs = ThumbStore.get(item.tid)
                                        if (cachedThumbs.isNotEmpty()) item.copy(thumbs = cachedThumbs) else item
                                    } else item
                                })
                                refreshing = false
                                Diag.log("load", "forum ok threads=" + fp.threads.size + " bytes=" + html.length + " url=" + url)
                                forumPage = fp.copy(cats = mergeCats(forumPage?.cats ?: emptyList(), fp.cats))
                                forumCache[url] = forumPage!!
                                SnapshotStore.put(url, forumPage!!)
                                if (forumCache.size > 6) forumCache.remove(forumCache.keys.first())
                                if (fp.threads.isEmpty()) {
                                    val tp = Site.parseThread(html)
                                    if (tp.posts.isNotEmpty()) {
                                        threadPage = tp
                                        nav = Nav.Thread(url, tp.subject)
                                    }
                                }
                            }
                            else -> forums = Site.parseForums(html)
                        }
                    }.onFailure {
                        error = "解析失败：" + (it.message ?: it.javaClass.simpleName)
                    }
                    if (nav is Nav.Forum) forumPage?.let { prefetchThumbs(it) }
                }.onFailure {
                    refreshing = false
                    loading = false
                    Diag.log("load", "fail url=" + url + " reason=" + (it.message ?: it.javaClass.simpleName))
                    if ((it.message ?: "").contains("Cloudflare")) {
                        openVerify(url)
                        return@onFailure
                    }
                    if (nav is Nav.Forum && retryCount < 1) {
                        retryCount++
                        load(url)
                    } else {
                        retryCount = 0
                        error = (it.message ?: "加载失败") + "（可以下拉再试一次）"
                    }
                }
            }
        } }.onFailure {
            loading = false
            error = "打不开这个页面：" + (it.message ?: "")
        }
    }

    /**
     * 明确交给「别的」浏览器打开：
     * 本站帖子链接命中我们的深链（且系统默认「在应用内打开支持的链接」），
     * 直接 ACTION_VIEW 会被路由回自己，这里按包名挑一个真正的浏览器。
     */
    private fun openInBrowser(url: String) {
        val uri = Uri.parse(url)
        val base = Intent(Intent.ACTION_VIEW, uri)
        fun open(pkg: String): Boolean {
            val i = Intent(Intent.ACTION_VIEW, uri)
                .setPackage(pkg)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return runCatching { startActivity(i) }.isSuccess
        }

        // 1) 系统当前默认处理者（FV 悬浮球这类可能不声明 BROWSABLE，先认它）
        val def = packageManager.resolveActivity(base, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
        val defPkg = def?.activityInfo?.packageName
        Diag.log("browser", "default=" + defPkg + " candidates开始")
        if (defPkg != null && defPkg != packageName && open(defPkg)) return

        // 2) 所有能处理 http 的应用（不加 BROWSABLE 限制），排除自己
        val candidates = packageManager.queryIntentActivities(base, 0)
            .mapNotNull { it.activityInfo?.packageName }
            .filter { it != packageName }
            .distinct()
        Diag.log("browser", "candidates=" + candidates.joinToString(","))
        for (pkg in candidates) if (open(pkg)) return

        // 3) 兜底：让系统弹选择器，并把自己排除掉
        val self = ComponentName(packageName, MainActivity::class.java.name)
        val chooser = Intent.createChooser(Intent(Intent.ACTION_VIEW, uri), "选择浏览器")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(self))
        if (runCatching { startActivity(chooser) }.isSuccess) return
        toast("没能调起浏览器，请检查默认应用设置")
    }

    /** 帖子页手动刷新：重抓正文并保留当前页，失败只提示 */
    private fun reloadThread(url: String) {
        Diag.log("thread", "手动刷新 " + url)
        toast("正在刷新正文…")
        val keep = threadPage
        fetcher.fetch(url) { r ->
            runOnUiThread {
                r.onSuccess { html ->
                    if (!looksLikeThread(html)) {
                        toast("刷新失败：这次没拿到正文，稍后再试")
                        return@onSuccess
                    }
                    val tp = Site.parseThread(html)
                    if (tp.posts.isEmpty()) {
                        toast("刷新失败：页面里没有楼层")
                        return@onSuccess
                    }
                    threadPage = tp
                    lastHtml = html
                    error = null
                    tidOf(url)?.let { cache.save(it, tp.subject, tp.lastEdit, html) }
                    refreshCacheStats()
                    prefetchThreadImages(tp)
                    toast("正文已刷新")
                }.onFailure {
                    threadPage = keep
                    toast("刷新失败：" + (it.message ?: ""))
                }
            }
        }
    }

    private fun reload() {
        when (val n = nav) {
            Nav.Settings, Nav.Search, Nav.History -> {}
            is Nav.Home -> loadHome()
            is Nav.Forum -> load(n.url)
            is Nav.Thread -> {
                // 评分/回复后刷新正文：失败时保留当前页面，不要变成“加载失败”
                val keepThread = threadPage
                fetcher.fetch(n.url) { r ->
                    runOnUiThread {
                        r.onSuccess { html ->
                            lastHtml = html
                            runCatching {
                                refreshLogin(html)
                                threadPage = Site.parseThread(html)
                                threadPage?.let { tp ->
                                    tidOf(n.url)?.toLongOrNull()?.let { tid ->
                                        history = HistoryStore.record(this, tid, tp.subject, n.url)
                                        // 顺手把这篇帖子的首图记成它的预览图，浏览记录/列表都能用
                                        val imgs = tp.posts.flatMap { p ->
                                            p.blocks.mapNotNull { (it as? Block.Image)?.url }
                                        }.distinct()
                                        if (imgs.isNotEmpty()) ThumbStore.put(tid.toString(), imgs.take(3))
                                    }
                                }
                                error = null
                            }.onFailure {
                                threadPage = keepThread
                                toast("刷新正文失败，页面仍可正常阅读")
                            }
                        }.onFailure {
                            threadPage = keepThread
                            toast("刷新正文失败，页面仍可正常阅读")
                        }
                        loading = false
                    }
                }
            }
        }
    }

    private fun openForum(item: ForumItem) {
        backStack.add(nav)
        nav = Nav.Forum(item.url, item.name)
        forumPage = null
        threadPage = null
        load(item.url)
    }

    /** 底部导航直达版块（动漫/游戏） */
    /** 切到浏览记录页：与两个板块一样是一级页，不压栈 */
    private fun openHistory() {
        history = HistoryStore.load(this)
        val isTopLevel = backStack.isEmpty()
        if (!isTopLevel) backStack.add(nav)
        nav = Nav.History
        threadPage = null
        error = null
        loading = false
    }

    private fun openQuick(url: String, title: String) {
        Diag.log("nav", "switch forum -> " + title + " 缓存=" + (forumCache[url] != null))
        // 一级页之间切换：不压栈，底栏保持可见；有缓存就直接换过去
        val isTopLevel = backStack.isEmpty()
        if (!isTopLevel) backStack.add(nav)
        nav = Nav.Forum(url, title)
        threadPage = null
        val cached = forumCache[url] ?: SnapshotStore.get(url)
        if (cached != null) {
            forumPage = cached
            loadedForumUrl = url
            error = null
            return
        }
        // 只换标题，先保留上一屏的帖子，新数据到了再整体替换，动画期间不会白屏闪烁
        forumPage = forumPage?.copy(title = title)
        loadedForumUrl = null
        load(url)
    }

    private fun openSearch() {
        if (nav is Nav.Search) return
        backStack.add(nav)
        nav = Nav.Search
        loadSearchHistory()
    }

    private fun loadSearchHistory() {
        val raw = getSharedPreferences("gm", MODE_PRIVATE).getString("searchHistory", "").orEmpty()
        searchHistory = raw.split("\n").filter { it.isNotBlank() }
    }

    private fun pushSearchHistory(q: String) {
        val list = (listOf(q) + searchHistory.filter { it != q }).take(12)
        searchHistory = list
        getSharedPreferences("gm", MODE_PRIVATE).edit()
            .putString("searchHistory", list.joinToString("\n")).commit()
    }

    /** 搜索：直接走站内搜索页，再用版块页解析（列表结构与版块页一致） */
    private fun doSearch(q: String) {
        if (q.isBlank()) return
        pushSearchHistory(q.trim())
        val url = SITE + "search.php?mod=forum&srchtxt=" + Uri.encode(q.trim()) + "&searchsubmit=yes&mobile=no"
        prefetchToken++          // 作废上一页还在跑的预览图预取
        fetcher.dropPending()     // 别让搜索请求排在它们后面超时
        Diag.log("search", "query=" + q.trim())
        nav = Nav.Forum(url, "搜索：" + q.trim())
        forumPage = null
        threadPage = null
        loadedForumUrl = url
        load(url)
    }

    private fun tidOf(url: String): String? =
        Regex("thread-(\\d+)").find(url)?.groupValues?.get(1)
            ?: Regex("tid=(\\d+)").find(url)?.groupValues?.get(1)

    /** 统一成帖子正文页地址，丢掉 highlight 之类的搜索参数，避免拿到奇怪的页面 */
    private fun canonicalThreadUrl(url: String): String {
        val tid = tidOf(url) ?: return url
        return "https://www.gamemale.com/thread-$tid-1-1.html"
    }

    private fun looksLikeThread(html: String): Boolean =
        html.contains("id=\"postlist\"") || html.contains("id='postlist'")

    private fun openThread(item: ThreadItem) = openThreadUrl(canonicalThreadUrl(item.url), item.title)

    /** 从任意入口（列表 / 搜索 / 浏览记录 / 深链）打开帖子 */
    private fun openThreadUrl(rawUrl: String, title: String) {
        backStack.add(nav)
        val url = canonicalThreadUrl(rawUrl)
        tidOf(url)?.toLongOrNull()?.let { history = HistoryStore.record(this, it, title, url) }
        nav = Nav.Thread(url, title)
        val tid = tidOf(url)
        val disk = tid?.let { cache.read(it) }
        val cached = tid?.let { threadCache[it] } ?: disk?.first
        if (cached != null && !looksLikeThread(cached)) {
            threadCache.remove(tid)
            threadPage = null
            load(url)
            return
        }
        if (cached != null) {
            // 本地已有整页，立即还原；随后后台核对标题/编辑时间，有变化才替换
            runCatching {
                val tp = Site.parseThread(cached)
                threadPage = tp
                lastHtml = cached
                refreshLogin(cached)
                rememberThread(url, tp)
            }
            verifyThreadLater(url, cached)
            return
        }
        threadPage = null
        // 请求地址必须跟 nav 里的规范地址一致，否则回包会被判成「过期响应」而整页空白
        load(url)
    }

    /** 切分类 / 翻页：仍在当前列表页内换数据 */
    fun goList(url: String) {
        when (val n = nav) {
            is Nav.Thread -> { nav = Nav.Thread(url, ""); threadPage = null }
            else -> { nav = Nav.Forum(url, (nav as? Nav.Forum)?.title ?: ""); }
        }
        linkLoad(url)
    }

    private fun linkLoad(url: String) {
        if (url.isBlank() || !url.startsWith("http")) {
            toast("这个分类入口没有有效链接")
            return
        }
        loading = true
        error = null
        fetcher.fetch(url) { r ->
            runOnUiThread {
                loading = false
                r.onSuccess { html ->
                    lastHtml = html
                    runCatching {
                        refreshLogin(html)
                        if (url.contains("mod=viewthread") || url.contains("thread-")) {
                            val tp = Site.parseThread(html)
                            threadPage = tp
                            nav = Nav.Thread(url, tp.subject)
                        } else {
                            val fp = Site.parseForumPage(html)
                            forumPage = fp.copy(cats = mergeCats(forumPage?.cats ?: emptyList(), fp.cats))
                            nav = Nav.Forum(url, fp.title.ifBlank { "版块" })
                        }
                    }.onFailure { error = "解析失败：" + (it.message ?: it.javaClass.simpleName) }
                }.onFailure { error = it.message ?: "加载失败" }
            }
        }
    }

    private fun openSettings() {
        if (nav is Nav.Settings || nav is Nav.Search) return
        backStack.add(nav)
        nav = Nav.Settings
        refreshCacheStats()
    }

    /** 后台核对：标题与最后编辑时间都没变就当缓存有效，不再重绘 */
    private fun verifyThreadLater(url: String, cachedHtml: String) {
        val old = runCatching { Site.parseThread(cachedHtml) }.getOrNull()
        fetcher.fetch(url) { r ->
            runOnUiThread {
                r.onSuccess { html ->
                    runCatching {
                        val fresh = Site.parseThread(html)
                        val tid = Regex("thread-(\\d+)").find(url)?.groupValues?.get(1)
                        if (old == null ||
                            fresh.subject != old.subject ||
                            fresh.lastEdit != old.lastEdit ||
                            fresh.posts.size != old.posts.size
                        ) {
                            threadPage = fresh
                            lastHtml = html
                            refreshLogin(html)
                            if (tid != null) cache.save(tid, fresh.subject, fresh.lastEdit, html)
                            refreshCacheStats()
                        } else if (tid != null) {
                            cache.save(tid, fresh.subject, fresh.lastEdit, html)
                        }
                    }
                }
            }
        }
    }

    /** 下拉刷新：不动现有列表，成功后再整体替换 */
    private fun refreshCurrentForum() {
        Diag.log("refresh", "nav=" + nav)
        val url = (nav as? Nav.Forum)?.url ?: loadedForumUrl ?: return
        if (refreshing) return
        refreshing = true
        prefetchToken++          // 停掉这一轮的预览图预取
        fetcher.dropPending()     // 清掉积压请求，别让刷新排在它们后面超时
        fetchForumForRefresh(url, attempt = 0)
    }

    private fun fetchForumForRefresh(url: String, attempt: Int) {
        fetcher.fetch(url) { r ->
            runOnUiThread {
                r.onSuccess { html ->
                    runCatching {
                        lastHtml = html
                        refreshLogin(html)
                        var fp = Site.parseForumPage(html)
                        if (fp.threads.isEmpty() && url.contains("search.php")) {
                            val hits = Site.parseSearchResults(html)
                            if (hits.isNotEmpty()) fp = fp.copy(threads = hits)
                        }
                        val cur = forumPage
                        val merged = fp.copy(
                            cats = mergeCats(cur?.cats ?: emptyList(), fp.cats),
                            threads = fp.threads
                        )
                        forumPage = merged
                        forumCache[url] = merged
                        prefetchToken++
                        prefetchThumbs(merged)
                    }.onFailure { e ->
                        Diag.log("refresh", "parse fail " + e.message)
                        toast("刷新失败，列表保持不变")
                    }
                }.onFailure {
                    if (attempt < 1) {
                        fetchForumForRefresh(url, attempt + 1)
                        return@onFailure
                    }
                    refreshing = false
                    Diag.log("refresh", "fetch fail " + (r.exceptionOrNull()?.message))
                    toast("刷新失败，列表保持不变")
                }
                if (r.isSuccess) refreshing = false
            }
        }
    }

    /** 图片交给系统相册/看图应用：先下载到应用缓存，再用 FileProvider 授权打开 */
    private fun openImageExternally(url: String) {
        toast("正在下载图片…")
        Thread {
            runCatching {
                val name = Integer.toHexString(url.hashCode()) + ".img"
                val dir = java.io.File(filesDir, "images").apply { mkdirs() }
                val f = ImageStore.local(url) ?: java.io.File(dir, name)
                if (!f.exists()) {
                    val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                    conn.setRequestProperty("Referer", "https://www.gamemale.com/")
                    android.webkit.CookieManager.getInstance().getCookie(url)
                        ?.let { conn.setRequestProperty("Cookie", it) }
                    conn.connectTimeout = 15000
                    conn.readTimeout = 20000
                    conn.inputStream.use { input -> f.outputStream().use { out -> input.copyTo(out) } }
                    ImageStore.save(url, f.readBytes())
                }
                // 确保文件位于已声明的 root 内（filesDir/images）
                if (!f.absolutePath.contains("/files/images/")) {
                    val dst = java.io.File(dir, name)
                    f.copyTo(dst, overwrite = true)
                }
                val target = java.io.File(dir, name)
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    this, packageName + ".fileprovider", target
                )
                runOnUiThread {
                    runCatching {
                        startActivity(
                            Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "image/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        )
                    }.onFailure { toast("没有可用的看图应用，已用浏览器打开") }
                }
            }.onFailure {
                runOnUiThread { toast("图片下载失败：" + (it.message ?: "")) }
            }
        }.start()
    }

    /** 把帖子正文（含末尾附图）的图片存成本地文件，之后展示和相册都用本地图 */
    private fun prefetchThreadImages(page: ThreadPage) {
        val urls = page.posts.flatMap { p ->
            p.blocks.mapNotNull { (it as? Block.Image)?.url }
        }.distinct().filter { !ImageStore.hasUrl(it) }
        if (urls.isEmpty()) return
        Thread {
            for (u in urls) {
                runCatching {
                    val conn = java.net.URL(u).openConnection() as java.net.HttpURLConnection
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                    conn.setRequestProperty("Referer", "https://www.gamemale.com/")
                    android.webkit.CookieManager.getInstance().getCookie(u)
                        ?.let { conn.setRequestProperty("Cookie", it) }
                    conn.connectTimeout = 12000
                    conn.readTimeout = 15000
                    val bytes = conn.inputStream.use { it.readBytes() }
                    if (bytes.size in 1..(6 * 1024 * 1024)) ImageStore.save(u, bytes)
                }
            }
            runOnUiThread { refreshCacheStats() }
        }.start()
    }

    /** 回复结果以“页面上真的有这条回复”为准，不依赖站点返回值文案 */
    private fun verifyReply(text: String) {
        val url = (nav as? Nav.Thread)?.url
        if (url == null) {
            busy = false
            toast("回复已提交，回到帖子页刷新即可看到")
            return
        }
        busy = true
        val probe = text.trim().take(12)
        fetcher.fetch(url) { r ->
            runOnUiThread {
                busy = false
                r.onSuccess { html ->
                    // 帖子页也拿不到正文、且页面里没有退出链接：多半是 Cookie 过期被踢回登录页
                    if (Site.parseThread(html).posts.isEmpty() && !html.contains("action=logout")) {
                        promptLoginExpired()
                        return@onSuccess
                    }
                    val tp = Site.parseThread(html)
                    threadPage = tp
                    lastHtml = html
                    loadedThreadUrl = url
                    val tid = Regex("thread-(\\d+)").find(url)?.groupValues?.get(1)
                    if (tid != null) cache.save(tid, tp.subject, tp.lastEdit, html)
                    val found = tp.posts.any { p ->
                        p.blocks.filterIsInstance<Block.Text>().any { it.text.contains(probe) }
                    }
                    if (found) {
                        toast("回复成功")
                        replyOpen = false
                        replyTextSatisfied()
                    } else {
                        toast("回复已提交，但页面里还没看到这条内容，稍后刷新确认")
                        replyOpen = false
                    }
                }.onFailure {
                    busy = false
                    toast("回复已提交，刷新页面时网络不稳，稍后再看")
                }
            }
        }
    }

    private fun replyTextSatisfied() {
        refreshing = false
    }

    private fun refreshCacheStats() {
        if (!::cache.isInitialized) return
        cacheBytes = cache.totalBytes() + HistoryStore.bytes(this)
        cacheCount = cache.count()
    }

    fun openFromHistory(url: String, title: String) {
        openThreadUrl(url, title)
    }

    private fun goBack() {
        if (loginPage) { closeLogin(); return }
        if (backStack.isNotEmpty()) {
            nav = backStack.removeAt(backStack.size - 1)
            when (val n = nav) {
                Nav.Search -> {}
                Nav.Settings -> {}
                Nav.History -> {}
                is Nav.Home -> loadHome()
                is Nav.Forum -> {
                    // 按 URL 命中缓存就直接还原（从搜索结果返回列表页也走这里）
                    val cached = forumCache[n.url]
                    if (cached != null) {
                        forumPage = cached
                        loadedForumUrl = n.url
                        error = null
                    } else if (forumPage == null || loadedForumUrl != n.url || error != null) {
                        load(n.url)
                    }
                }
                is Nav.Thread -> {
                    // 设置页返回帖子详情：本地已有同一帖就直接还原，不重新抓取
                    if (threadPage != null && loadedThreadUrl == n.url) {
                        error = null
                    } else {
                        val tid = Regex("thread-(\\d+)").find(n.url)?.groupValues?.get(1)
                        val disk = tid?.let { cache.read(it) }
                        if (disk != null) {
                            runCatching {
                                threadPage = Site.parseThread(disk.first)
                                lastHtml = disk.first
                                loadedThreadUrl = n.url
                            }
                        } else load(n.url)
                    }
                }
            }
            return
        }
        finish()
    }

    /** 上次进程如果崩过，启动时把堆栈弹出来（文本可长按复制） */
    private fun showLastCrash() {
        runCatching {
            val f = java.io.File(filesDir, App.CRASH_FILE)
            if (!f.exists()) return
            val text = f.readText()
            f.delete()
            val tv = android.widget.TextView(this).apply {
                setText(text.take(1200))
                setTextIsSelectable(true)
                setPadding(40, 30, 40, 10)
                textSize = 12f
                setTextColor(android.graphics.Color.DKGRAY)
            }
            val sv = android.widget.ScrollView(this).apply { addView(tv) }
            android.app.AlertDialog.Builder(this)
                .setTitle("上次闪退日志（长按可复制）")
                .setView(sv)
                .setPositiveButton("知道了", null)
                .show()
        }
    }

    private fun refreshLogin(html: String) {
        if (!Site.isLoggedIn(html)) return          // 页面没有登录信息时不动，避免把头像刷回默认
        loggedIn = true
        Site.parseLoginName(html)?.takeIf { it.isNotBlank() }?.let { loginName = it }
        val newAvatar = Site.parseLoginAvatar(html)
        if (!newAvatar.isNullOrBlank() && !newAvatar.contains("uid=0")) avatarUrl = newAvatar
        commitPendingAccount()
        ensureAccountForCurrentLogin()
    }

    /**
     * 从旧版本升级上来（或刚在别的入口登录成功）时，账号表里是空的，
     * 但 CookieManager 里已经有登录态 —— 这里把当前登录顺手登记成一个账号，
     * 否则会出现"头像有、账号列表空"的矛盾。
     */
    private fun ensureAccountForCurrentLogin() {
        if (!loggedIn) return
        val cookie = Accounts.capture()
        if (cookie.isBlank()) return
        val uid = Regex("uid=(\\d+)").find(avatarUrl.orEmpty())?.groupValues?.get(1)
        if (uid == null) return
        val name = loginName?.takeIf { it.isNotBlank() } ?: ("账号 " + uid.takeLast(4))
        val existing = accounts.firstOrNull { it.id == uid }
        if (existing != null && existing.cookie == cookie) {
            if (currentAccountId != uid) {
                currentAccountId = uid
                Accounts.setCurrent(this, uid)
            }
            return
        }
        accounts = Accounts.upsert(this, Accounts.Acc(uid, name, avatarUrl.orEmpty(), cookie))
        currentAccountId = uid
        Accounts.setCurrent(this, uid)
        Diag.log("account", "登记当前登录为账号 " + name)
    }

    /** 登录成功后把当前 Cookie 存成一个账号，并设为当前账号 */
    private fun commitPendingAccount() {
        val cookie = pendingCookie ?: return
        // 已经是同一份 Cookie：直接切过去，不重复添加
        val same = accounts.firstOrNull { it.cookie == cookie }
        if (same != null) {
            pendingCookie = null
            currentAccountId = same.id
            Accounts.setCurrent(this, same.id)
            Diag.log("account", "新增的是已存在的账号，直接切换 " + same.name)
            toast("这个账号已经在列表里了：" + same.name)
            return
        }
        val uid = Regex("uid=(\\d+)").find(avatarUrl.orEmpty())?.groupValues?.get(1)
            ?: Regex("uid=(\\d+)").find(cookie)?.groupValues?.get(1)
            ?: System.currentTimeMillis().toString()
        val name = loginName?.takeIf { it.isNotBlank() } ?: ("账号 " + uid.takeLast(4))
        val acc = Accounts.Acc(uid, name, avatarUrl.orEmpty(), cookie)
        accounts = Accounts.upsert(this, acc)
        currentAccountId = uid
        Accounts.setCurrent(this, uid)
        pendingCookie = null
        Diag.log("account", "已保存账号 " + name + " 共" + accounts.size + "个")
        toast("已切换账号：" + name)
    }

    /** jsoup 解析一律放到后台单线程：650KB 级页面在主线程解析很容易 OOM */
    private val parsePool = java.util.concurrent.Executors.newSingleThreadExecutor()

    /** 页面在要求登录：未登录时版块/帖子会返回这类拦截文案 */
    private fun isLoginRequired(html: String): Boolean {
        if (html.contains("action=logout")) return false
        val marks = listOf(
            "您需要登录后才可以",
            "请先登录后",
            "需要登录才能",
            "登录后才能查看",
            "您还没有登录"
        )
        return marks.any { html.contains(it) }
    }

    /** 站点对用户组设限时的提示页特征 */
    private fun isGroupBlocked(html: String): Boolean {
        val marks = listOf(
            "需要升级您所在的用户组",
            "升级您所在的用户组后才能访问",
            "有权访问的用户组",
            "您所在的用户组无法"
        )
        return marks.any { html.contains(it) }
    }

    /** 两个地址指向同一个目标页：帖子比 tid，版块比 fid+页码（忽略 mobile=no、highlight 这类装饰参数） */
    private fun sameTarget(a: String?, b: String?): Boolean {
        if (a == null || b == null) return false
        val tidA = Regex("thread-(\\d+)").find(a)?.groupValues?.get(1)
            ?: Regex("[?&]tid=(\\d+)").find(a)?.groupValues?.get(1)
        val tidB = Regex("thread-(\\d+)").find(b)?.groupValues?.get(1)
            ?: Regex("[?&]tid=(\\d+)").find(b)?.groupValues?.get(1)
        if (tidA != null && tidB != null) return tidA == tidB
        val fidA = Regex("[?&]fid=(\\d+)").find(a)?.groupValues?.get(1)
        val fidB = Regex("[?&]fid=(\\d+)").find(b)?.groupValues?.get(1)
        val pageA = Regex("[?&]page=(\\d+)").find(a)?.groupValues?.get(1) ?: "1"
        val pageB = Regex("[?&]page=(\\d+)").find(b)?.groupValues?.get(1) ?: "1"
        return fidA == fidB && pageA == pageB
    }

    /** 帖子页：后台解析 + 后台落盘，只在最后一刻回主线程更新界面 */
    private fun applyThreadHtml(html: String, url: String) {
        val t0 = System.currentTimeMillis()
        bgParse({ Site.parseThread(html) }) { res ->
            val tp = res.getOrNull()
            if (tp == null) {
                Diag.log("thread", "解析失败 " + (res.exceptionOrNull()?.message ?: ""))
                error = "解析失败：" + (res.exceptionOrNull()?.message ?: "未知")
                loading = false
                return@bgParse
            }
            if ((nav as? Nav.Thread)?.url != url) {
                Diag.log("load", "丢弃过期帖子结果 url=" + url)
                return@bgParse
            }
            Diag.log("thread", "posts=" + tp.posts.size + " bytes=" + html.length +
                " 解析耗时=" + (System.currentTimeMillis() - t0) + "ms")
            if (tp.posts.isEmpty()) {
                dumpText("thread-empty", html)
                val canonical = canonicalThreadUrl(url)
                if (retryCount < 1 && canonical != url) {
                    retryCount++
                    Diag.log("thread", "正文为空，改用规范地址重试 " + canonical)
                    nav = Nav.Thread(canonical, tp.subject)
                    load(canonical)
                    return@bgParse
                }
            }
            threadPage = tp
            loadedThreadUrl = url
            refreshing = false
            prefetchThreadImages(tp)
            val tid = Regex("thread-(\\d+)").find(url)?.groupValues?.get(1)
            if (tid != null) {
                rememberThread(url, tp)
                bgParse({ cache.save(tid, tp.subject, tp.lastEdit, html); true }) { refreshCacheStats() }
            }
        }
    }

    /** 打开帖子后统一写浏览记录，并把正文首图登记为该帖的预览图 */
    private fun rememberThread(url: String, tp: ThreadPage) {
        val tid = Regex("thread-(\\d+)").find(url)?.groupValues?.get(1) ?: return
        runCatching {
            history = HistoryStore.record(this, tid.toLong(), tp.subject, url)
            val imgs = tp.posts.flatMap { p ->
                p.blocks.mapNotNull { (it as? Block.Image)?.url }
            }.distinct()
            if (imgs.isNotEmpty()) ThumbStore.put(tid, imgs.take(3))
            Diag.log("thumb", "tid=" + tid + " 首图数=" + imgs.size)
        }
    }

    private fun <T> bgParse(work: () -> T, done: (Result<T>) -> Unit) {
        parsePool.execute {
            val r = runCatching(work)
            runOnUiThread { done(r) }
        }
    }

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    private var prefetchToken = 0
    private var loadedForumUrl: String? = null
    private var loadedForumTitle: String = ""
    private var retryCount = 0
    private var loadedThreadUrl: String? = null
    private var refreshing by mutableStateOf(false)
    /** 版块因用户组等级不足打不开 */
    private var needUpgrade by mutableStateOf(false)
    /** 未登录导致列表为空 */
    private var needLogin by mutableStateOf(false)
    private var accounts by mutableStateOf<List<Accounts.Acc>>(emptyList())
    private var history by mutableStateOf<List<HistoryStore.Entry>>(emptyList())
    private var currentAccountId: String? = null
    private var accountsOpen by mutableStateOf(false)
    private var pendingCookie: String? = null
    private var addMode = false
    private var loginPage by mutableStateOf(false)
    private var loginWeb: WebView? = null
    private var loginPrompted = false
    /** 站点要求人工验证时，弹一个可见 WebView 让页面自己跑完验证 */
    private var verifyPage by mutableStateOf(false)
    private var verifyWeb: WebView? = null
    private var verifyTarget: String? = null
    private var searchOpen by mutableStateOf(false)
    private var backProgress by mutableStateOf(0f)
    private var searchHistory by mutableStateOf<List<String>>(emptyList())
    /** tid -> 已抓到的帖子 HTML（预取时顺手存下），按总体积限制，避免把堆吃满 */
    private val threadCache = object : LinkedHashMap<String, String>() {
        override fun put(key: String, value: String): String? {
            val old = super.put(key, value)
            trimToBytes(3 * 1024 * 1024)
            return old
        }
    }

    private fun trimToBytes(max: Long) {
        var total = threadCache.values.sumOf { it.length.toLong() }
        while (total > max && threadCache.isNotEmpty()) {
            val firstKey = threadCache.keys.first()
            total -= (threadCache[firstKey]?.length ?: 0)
            threadCache.remove(firstKey)
        }
    }

    private fun rememberThreadHtml(tid: String, html: String) {
        if (html.length > 900_000 || !looksLikeThread(html)) return
        threadCache[tid] = html
    }
    /** url -> 已加载好的列表页（回退时不重载，搜索页也各存各的） */
    private val forumCache = LinkedHashMap<String, ForumPage>()
    private var rateSourceHtml: String? = null

    /** 列表页本身没有缩略图（这个模板没开图文插件），就用正文首图补，最多补 6 条 */
    /** 单条帖子的预览图：本地有缓存就直接解析出来，不再联网 */
    fun ensureThumb(t: ThreadItem) {
        if (t.thumbs.isNotEmpty() || !t.url.contains("thread-")) return
        val saved = ThumbStore.get(t.tid)
        if (saved.isNotEmpty()) {
            applyThumb(t, saved)
            return
        }
        if (thumbBusy) return
        thumbBusy = true
        bgParse({
            val local = cache.read(t.tid)?.first ?: threadCache[t.tid]
            if (local != null) Site.parseFirstImages(local, 3) else null
        }) { r ->
            thumbBusy = false
            val imgs = r.getOrNull()
            if (imgs != null) {
                applyThumb(t, imgs)
            } else {
                fetchThumb(t)
            }
        }
    }

    private var thumbBusy = false

    private var thumbQueue = 0

    private fun fetchThumb(t: ThreadItem) {
        if (thumbQueue > 2) return
        thumbQueue++
        fetcher.fetch(t.url) { r ->
            runOnUiThread {
                thumbQueue--
                r.onSuccess { html ->
                    val tid = Regex("thread-(\\d+)").find(t.url)?.groupValues?.get(1)
                    // 内容必须属于这个 tid，否则说明拿到的是别的页面，直接丢弃
                    if (tid == null || !html.contains("thread-$tid")) return@onSuccess
                    bgParse({ Site.parseFirstImages(html, 3) }) { pr ->
                        pr.getOrNull()?.takeIf { it.isNotEmpty() }?.let { imgs ->
                            rememberThreadHtml(tid, html)
                            applyThumb(t, imgs)
                        }
                    }
                }
            }
        }
    }

    private fun applyThumb(t: ThreadItem, imgs: List<String>) {
        if (imgs.isEmpty()) return
        ThumbStore.put(t.tid, imgs)
        val cur = forumPage ?: return
        val updated = cur.copy(threads = cur.threads.map { item ->
            if (item.tid == t.tid) item.copy(thumbs = imgs) else item
        })
        forumPage = updated
        loadedForumUrl?.let { forumCache[it] = updated }
    }

    private fun prefetchThumbs(page: ForumPage) {
        val token = ++prefetchToken
        val targets = page.threads
            .filter { it.thumbs.isEmpty() && it.url.contains("thread-") }
            .filter { ThumbStore.get(it.tid).isEmpty() }
            .take(8)
        if (targets.isEmpty()) return
        fun step(i: Int) {
            if (i >= targets.size || token != prefetchToken || nav !is Nav.Forum) return
            val t = targets[i]
            val local = cache.read(t.tid)?.first ?: threadCache[t.tid]
            if (local != null) {
                bgParse({ Site.parseFirstImages(local, 3) }) { pr ->
                    pr.getOrNull()?.let { applyThumb(t, it) }
                    step(i + 1)
                }
                return
            }
            fetcher.fetch(t.url) { r ->
                runOnUiThread {
                    r.onSuccess { html ->
                        if (token != prefetchToken) return@onSuccess
                        val tid = Regex("thread-(\\d+)").find(t.url)?.groupValues?.get(1)
                        if (tid == null || !html.contains("thread-$tid")) { step(i + 1); return@onSuccess }
                        bgParse({ Site.parseFirstImages(html, 3) }) { pr ->
                            rememberThreadHtml(tid, html)
                            pr.getOrNull()?.takeIf { it.isNotEmpty() }?.let { thumbs ->
                                ThumbStore.put(t.tid, thumbs)
                                val cur = forumPage
                                if (cur != null) {
                                    val updated = cur.copy(threads = cur.threads.map { item ->
                                        if (item.tid == t.tid) item.copy(thumbs = thumbs) else item
                                    })
                                    forumPage = updated
                                    loadedForumUrl?.let { forumCache[it] = updated }
                                }
                            }
                            step(i + 1)
                        }
                    }
                }
            }
        }
        // 列表先渲染出来，再开始抓预览图，别和首屏抢那一个 WebView
        handler.postDelayed({ if (token == prefetchToken) step(0) }, 1200)
    }

    private fun mergeCats(prev: List<CatTab>, next: List<CatTab>): List<CatTab> {
        if (prev.isEmpty()) return next
        if (next.isEmpty()) return prev
        val out = LinkedHashMap<String, CatTab>()
        for (c in prev) out[c.label] = c
        for (c in next) {
            if (!out.containsKey(c.label)) out[c.label] = c
        }
        return out.values.toList()
    }

    private fun applyStatusBar() {
        runCatching {
            val c = WindowCompat.getInsetsController(window, window.decorView)
            c.isAppearanceLightStatusBars = !darkMode
            c.isAppearanceLightNavigationBars = !darkMode
        }
    }

    /** 只复制帖子链接本身，不带标题 */
    private fun copyThreadLink() {
        val url = (nav as? Nav.Thread)?.url ?: navUrl()
        if (url.isBlank()) { toast("当前页面没有可复制的链接"); return }
        runCatching {
            val cm = getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                as android.content.ClipboardManager
            cm.setPrimaryClip(android.content.ClipData.newPlainText("链接", url))
            toast("链接已复制")
        }.onFailure { toast("复制失败：" + (it.message ?: "")) }
    }

    private fun shareThread() {
        val url = (nav as? Nav.Thread)?.url ?: navUrl()
        val title = (nav as? Nav.Thread)?.title ?: "GameMale 帖子"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, title + "\n" + url)
        }
        runCatching {
            startActivity(Intent.createChooser(intent, "分享帖子"))
        }.onFailure { toast("没有可分享的应用") }
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun threadTid(): String? =
        Regex("thread-(\\d+)").find((nav as? Nav.Thread)?.url ?: "" )?.groupValues?.get(1)

    private fun navUrl(): String = when (val n = nav) {
        is Nav.Forum -> n.url
        is Nav.Thread -> n.url
        is Nav.Search -> SITE + "search.php?mod=forum"
        Nav.Home -> SITE + "forum.php"
        Nav.Settings, Nav.History -> ""
    }

    /** 回复：用页面里的 #fastpostform 字段，补上 message 后 POST（带 inajax=1 拿纯文本结果） */
    private fun sendReply(text: String) {
        prefetchToken++
        if (!loggedIn) {
            toast("当前会话没有登录，请点右上角账号图标登录后再试")
            openLogin()
            return
        }
        if (text.isBlank()) {
            toast("回复内容不能为空")
            return
        }
        val form = Site.parseForm(lastHtml, "#fastpostform")
        if (form != null) {
            form.set("message", text)
            form.set("posttime", (System.currentTimeMillis() / 1000).toString())
            busy = true
            submit(
                form,
                onDone = { replyOpen = false },
                okText = "回复成功",
                pageFirst = true,
                verifyText = text
            )
            return
        }
        // 页面里没有快速回复框时，用 formhash + tid 自己拼一次提交
        val tid = threadTid()
        val formhash = Site.parseFormhash(lastHtml)
        if (tid == null || formhash == null) {
            toast("会话信息没拿到（formhash 缺失），请重新登录一次再试")
            return
        }
        val fid = Site.parseFid(lastHtml).orEmpty()
        val url = SITE + "forum.php?mod=post&action=reply&fid=" + fid + "&tid=" + tid +
            "&extra=&replysubmit=yes&infloat=yes&handlekey=fastpost&inajax=1"
        val fields = listOf(
            "formhash" to formhash,
            "posttime" to (System.currentTimeMillis() / 1000).toString(),
            "wysiwyg" to "0",
            "subject" to "",
            "usesig" to "1",
            "message" to text
        )
        busy = true
        fetcher.post(url, fields.joinToString("&") { (k, v) ->
            java.net.URLEncoder.encode(k, "UTF-8") + "=" + java.net.URLEncoder.encode(v, "UTF-8")
        }) { r ->
            runOnUiThread {
                r.onSuccess { xml ->
                    dumpText("submit-reply", xml)
                    val (ok, detail) = judgeXml(xml)
                    if (!ok && isLoginExpired(detail)) { promptLoginExpired(); return@onSuccess }
                    replyOpen = false
                    verifyReply(text)
                }.onFailure {
                    busy = false
                    dumpText("submit-reply-timeout", it.message.orEmpty())
                    toast("提交失败：" + (it.message ?: ""))
                }
            }
        }
    }

    private fun cdataText(xml: String): String {
        Regex("<![CDATA[(.*?)]]>", setOf(RegexOption.DOT_MATCHES_ALL))
            .find(xml)?.groupValues?.get(1)
            ?.replace(Regex("<script[^>]*>.*?</script>", setOf(RegexOption.DOT_MATCHES_ALL)), " ")
            ?.replace("<br />", " ")
            ?.replace(Regex("<[^>]+>"), "")
            ?.trim()?.takeIf { it.isNotBlank() }
            ?.let { return it.take(140) }
        // 不是 XML 时，尽量把站点错误原话抠出来
        runCatching {
            val doc = org.jsoup.Jsoup.parse(xml)
            val node = doc.selectFirst(".alert_error, .alert_info, #messagetext, .error, #js_error")
            val text = node?.text()?.trim()
            if (!text.isNullOrBlank()) return text.take(140)
            val title = doc.title()
            if (!title.isNullOrBlank()) return ("站点返回：" + title).take(140)
        }
        return "站点没有返回结果（已保存返回页：下载/gamemale-submit.html）"
    }

    /** 站点返回里出现"请先登录"这类文案，说明 Cookie 已经过期 */
    private fun isLoginExpired(text: String): Boolean {
        val marks = listOf(
            "请先登录", "您需要登录", "请登录后", "登录后才能",
            "登录已失效", "请重新登录", "游客不能", "您还没有登录"
        )
        return marks.any { text.contains(it) }
    }

    /** Cookie 过期时统一提示，并直接给一个去登录的入口 */
    private fun promptLoginExpired() {
        busy = false
        replyOpen = false
        rateOpen = false
        loggedIn = false
        Diag.log("login", "提交时发现登录已失效")
        android.app.AlertDialog.Builder(this)
            .setTitle("登录状态已失效")
            .setMessage("站点那边的登录有效期到了，需要重新登录一次才能继续回复、评分和下载附件。")
            .setPositiveButton("去登录") { d, _ -> d.dismiss(); openLogin() }
            .setNegativeButton("稍后") { d, _ -> d.dismiss() }
            .show()
    }

    /** 站点 ajax 返回的统一判定：只看有没有 errorhandle_/alert_error */
    private fun judgeXml(xml: String): Pair<Boolean, String> {
        val clean = cdataText(xml)
        val hasErr = xml.contains("errorhandle_") || xml.contains("alert_error")
        // 站点成功时可能附带“回复奖励”等文案，只要没有错误处理脚本就算成功
        var ok = !hasErr || xml.contains("succeedhandle_")
        // “已经回复过本帖”说明这条回复其实已经在站上了，按成功处理，避免重复提交
        if (clean.contains("已经回复过")) ok = true
        return ok to clean
    }

    private fun isSuccess(msg: String): Boolean {
        val failed = listOf("失败", "抱歉", "错误", "不能", "无权", "不符", "间隔", "超过", "非法", "请先")
            .any { msg.contains(it) }
        return !failed && (msg.contains("成功") || msg.contains("完成") || msg.contains("感谢") || msg.contains("已发布"))
    }

    /** 评分：先 GET 评分页拿 #rateform，再按用户理由 POST */
    private var ratePid: String? = null

    private fun openRate(pid: String) {
        ratePid = pid
        if (!loggedIn) {
            toast("当前会话没有登录，请先登录再评分")
            openLogin()
            return
        }
        val tid = threadTid() ?: run { toast("拿不到帖子 ID"); return }
        val urls = listOf(
            SITE + "forum.php?mod=misc&action=rate&tid=" + tid + "&pid=" + pid + "&infloat=yes&handlekey=rate",
            SITE + "forum.php?mod=misc&action=rate&tid=" + tid + "&pid=" + pid,
            SITE + "forum.php?mod=misc&action=rate&tid=" + tid + "&pid=" + pid + "&infloat=yes&handlekey=rate&inajax=1"
        )
        tryRate(urls, 0)
    }

    private fun tryRate(urls: List<String>, index: Int) {
        prefetchToken++
        if (index >= urls.size) {
            // 评分页解析不到时，按本站真实表单结构直接拼一份可提交的表单（不再跳网页）
            val tid = threadTid() ?: run { toast("拿不到帖子 ID"); return }
            val pid = ratePid ?: run { toast("拿不到楼层 ID"); return }
            val hash = Site.parseFormhash(lastHtml) ?: run {
                toast("会话信息缺失（formhash），请重新登录后再试")
                return
            }
            rateFormDef = FormDef(
                SITE + "forum.php?mod=misc&action=rate&ratesubmit=yes&infloat=yes&handlekey=rate",
                mutableListOf(
                    "formhash" to hash,
                    "tid" to tid,
                    "pid" to pid,
                    "referer" to (navUrl()),
                    "handlekey" to "rate"
                )
            )
            rateReasonName = "reason"
            rateFields = listOf(
                Triple("score3", "血液", "3"),
                Triple("score4", "追随", "1"),
                Triple("score8", "堕落", "1")
            )
            rateOpen = true
            return
        }
        busy = true
        fetcher.fetch(urls[index]) { r ->
            runOnUiThread {
                busy = false
                r.onSuccess { raw ->
                    val html = Site.unwrapCdata(raw)
                    val sels = listOf("#rateform", "form[name=rateform]", "#fwin_rate form", "form[action*=rate]")
                    val form = Site.parseFormAny(html, sels)
                    if (form == null) {
                        tryRate(urls, index + 1)
                        return@onSuccess
                    }
                    rateFormDef = form
                    rateSourceHtml = html
                    rateReasonName = sels.firstNotNullOfOrNull { Site.textareaName(html, it) }
                    rateFields = sels.firstNotNullOfOrNull { s2 ->
                        Site.scoreOptions(html, s2).ifEmpty { null }
                    } ?: emptyList()
                    rateOpen = true
                }.onFailure { tryRate(urls, index + 1) }
            }
        }
    }

    private fun sendRate(reason: String) {
        val form = rateFormDef ?: return
        rateReasonName?.let { form.set(it, reason) }
        // 每个分值字段只取“数值最大且非空”的那一档，避免把空值/请选择提交上去
        for ((name, opts) in rateFields.groupBy { it.first }) {
            val best = opts
                .filter { it.third.isNotBlank() && it.third != "0" }
                .maxByOrNull { it.third.toIntOrNull() ?: Int.MIN_VALUE }
            if (best != null) form.set(name, best.third)
        }
        if (form.action.contains("action=rate") && !form.action.contains("ratesubmit=yes")) {
            form.action = form.action + (if (form.action.contains("?")) "&" else "?") + "ratesubmit=yes"
        }
        submit(form, onDone = { rateOpen = false })
    }

    /** 提交一个表单并解析 XML 结果 */
    private var submitOkText: String = "提交成功"

    private fun submit(
        form: FormDef,
        onDone: () -> Unit,
        okText: String = "评分成功",
        pageFirst: Boolean = false,
        verifyText: String? = null
    ) {
        submitOkText = okText
        prefetchToken++
        busy = true
        val raw = form.action
        val url = (if (raw.startsWith("http")) raw else SITE + raw) +
            (if (raw.contains("?")) "&" else "?") + "inajax=1"
        val pageCb: ((Result<String>) -> Unit) = { r ->
            runOnUiThread {
                busy = false
                r.onSuccess { xml ->
                    dumpText("submit-" + if (submitOkText == "回复成功") "reply" else "rate", xml)
                    val (ok, detail) = judgeXml(xml)
                    if (!ok && isLoginExpired(detail)) { promptLoginExpired(); return@onSuccess }
                    if (verifyText != null) {
                        onDone()
                        verifyReply(verifyText)
                        return@onSuccess
                    }
                    val msg = if (ok) submitOkText else detail.ifEmpty { "提交失败" }
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                    if (ok) {
                        onDone()
                        reload()
                    }
                }.onFailure {
                    fetcher.snapshotHtml { snap -> runOnUiThread { dumpText("submit-timeout", snap) } }
                    toast("提交失败：" + (it.message ?: "") + "，返回页已存到下载/gamemale-submit-timeout.html")
                }
            }
        }
        val post = { fetcher.post(url, form.body(), pageCb) }
        if (pageFirst) fetcher.submitOnPage(url, form.body()) { r ->
            runOnUiThread {
                busy = false
                r.onSuccess { xml ->
                    dumpText("submit-" + if (submitOkText == "回复成功") "reply" else "rate", xml)
                    val (ok, detail) = judgeXml(xml)
                    if (!ok && isLoginExpired(detail)) { promptLoginExpired(); return@onSuccess }
                    if (verifyText != null) {
                        onDone()
                        verifyReply(verifyText)
                        return@onSuccess
                    }
                    toast(if (ok) submitOkText else detail.ifEmpty { "提交失败" })
                    if (ok) { onDone(); reload() }
                }.onFailure { post() }
            }
        } else fetcher.post(url, form.body()) { r ->
            runOnUiThread {
                busy = false
                r.onSuccess { xml ->
                    dumpText("submit-" + if (submitOkText == "回复成功") "reply" else "rate", xml)
                    val (ok, detail) = judgeXml(xml)
                    if (!ok && isLoginExpired(detail)) { promptLoginExpired(); return@onSuccess }
                    val msg = if (ok) submitOkText else detail.ifEmpty { "提交失败" }
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                    if (ok) {
                        onDone()
                        reload()
                    } else {
                        // 失败就保留窗口，别让用户以为提交成功
                        busy = false
                    }
                }.onFailure {
                    rateSourceHtml?.let { dumpText("rate-form", it) }
                    // postUrl 没走通时，回退到页内表单提交再试一次
                    fetcher.submitOnPage(url, form.body()) { r2 ->
                        runOnUiThread {
                            r2.onSuccess { xml2 ->
                                dumpText("submit-rate", xml2)
                                val (ok2, detail2) = judgeXml(xml2)
                                if (!ok2 && isLoginExpired(detail2)) { promptLoginExpired(); return@onSuccess }
                                val msg = if (ok2) submitOkText else detail2.ifEmpty { "提交失败" }
                                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                                if (ok2) {
                                    onDone()
                                    reload()
                                }
                            }.onFailure {
                                fetcher.snapshotHtml { snap -> runOnUiThread { dumpText("submit-timeout", snap) } }
                                toast("提交失败：" + (it.message ?: "") + "，返回页已存到下载/gamemale-submit-timeout.html")
                            }
                        }
                    }
                }
            }
        }
    }

    /** 附件：付费的先走购买表单，免费的直接下载 */
    private fun openAttach(at: AttachItem) {
        // 走系统浏览器：那边有它自己的登录态，站点原生的购买/下载流程最稳
        val url = when {
            at.url.contains("action=attachpay") -> at.url
            else -> attachmentUrl(at.url)
        }
        toast("用浏览器打开：" + at.name)
        openExternal(url)
    }

    /** 附件卡链接里带 aid，拼出真正下载地址 */
    private fun attachmentUrl(url: String): String {
        val aid = Regex("aid=(\\d+)").find(url)?.groupValues?.get(1)
        return if (aid != null) SITE + "forum.php?mod=attachment&aid=" + aid else url
    }

    /** 购买页兜底：应用内网页完成购买 */
    private fun openAttachInWeb(at: AttachItem) {
        val web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(v: WebView, r: android.webkit.WebResourceRequest): Boolean {
                val u = r.url.toString()
                if (u.contains("mod=attachment")) {
                    startDownload(u, at.name)
                    return true
                }
                return false
            }
        }
        web.loadUrl(at.url)
        val dialog = Dialog(this, android.R.style.Theme_DeviceDefault_NoActionBar)
        val container = FrameLayout(this)
        container.addView(web, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        dialog.setContentView(container)
        dialog.setOnDismissListener {
            runCatching {
                (web.parent as? ViewGroup)?.removeView(web)
                web.destroy()
            }
            reload()
        }
        dialog.show()
    }

    private fun startDownload(url: String, name: String) {
        runCatching {
            val cookie = CookieManager.getInstance().getCookie(url).orEmpty()
            val req = DownloadManager.Request(Uri.parse(url)).apply {
                if (cookie.isNotEmpty()) addRequestHeader("Cookie", cookie)
                addRequestHeader("Referer", SITE)
                setTitle(name)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    name.ifBlank { "gamemale_attachment" }
                )
            }
            (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
            toast("开始下载：" + name)
        }.onFailure {
            toast("下载失败，改用浏览器打开")
            openExternal(url)
        }
    }

    private fun openExternal(url: String) {
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    /** 登录：全屏 WebView 页，右上角 ✓ 表示登录完成；同时实时盯着 Cookie，一旦拿到就弹确认 */
    fun openLogin() {
        val web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webViewClient = WebViewClient()
        web.isFocusableInTouchMode = true
        web.isFocusable = true
        web.setOnTouchListener { v, _ ->
            v.requestFocus()
            runCatching {
                (getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager)
                    .showSoftInput(v, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }
            false
        }
        web.loadUrl(SITE + "member.php?mod=logging&action=login")
        loginWeb = web
        loginPage = true
        loginPrompted = false
        handler.postDelayed(loginPoller, 800)
        Diag.log("login", "打开登录页")
    }

    private val loginPoller = object : Runnable {
        override fun run() {
            if (!loginPage) return
            if (!loginPrompted) {
                loginPageHasLogout { logged ->
                    if (logged || hasLoginCookie()) {
                        loginPrompted = true
                        Diag.log("login", "检测到登录成功 cookie=" + hasLoginCookie() + " dom=" + logged)
                        askReturnHome()
                    }
                }
            }
            handler.postDelayed(this, 900)
        }
    }

    /**
     * Discuz 的登录 Cookie 前缀不固定（cdb_auth / xxx_auth 都有可能），
     * 所以不写死名字：任何以 auth 结尾的 Cookie 有有效值就算登录；
     * 同时用页面 DOM 里有没有 action=logout 作为硬判据。
     */
    private fun hasLoginCookie(): Boolean {
        val cookies = listOfNotNull(
            CookieManager.getInstance().getCookie(SITE),
            CookieManager.getInstance().getCookie(SITE + "/")
        )
        for (cookie in cookies) {
            val m = Regex("(?:^|;\\s*)([A-Za-z0-9_]*auth)=([^;]*)").find(cookie)
            if (m != null) {
                val v = m.groupValues[2].trim()
                if (v.isNotEmpty() && v != "deleted") return true
            }
        }
        return false
    }

    /** 页面里出现 action=logout 就说明已经登录成功（不管 Cookie 名字叫啥） */
    private fun webHasLogout(web: WebView?, cb: (Boolean) -> Unit) {
        if (web == null) return cb(false)
        runCatching {
            web.evaluateJavascript(
                "(function(){try{return document.documentElement.innerHTML.indexOf('action=logout')>=0}catch(e){return false}})()"
            ) { v -> cb(v.contains("true")) }
        }.onFailure { cb(false) }
    }

    private fun loginPageHasLogout(cb: (Boolean) -> Unit) {
        webHasLogout(loginWeb, cb)
    }

    private fun askReturnHome() {
        android.app.AlertDialog.Builder(this)
            .setTitle("已获取到登录信息")
            .setMessage("检测到登录 Cookie，是否返回首页并刷新帖子列表？")
            .setPositiveButton("返回首页") { d, _ -> d.dismiss(); finishLogin() }
            .setNegativeButton("继续浏览") { d, _ -> d.dismiss() }
            .setCancelable(false)
            .show()
    }

    /** 点击 ✓ / 确认返回：收起登录页并刷新列表 */
    private fun finishLogin() {
        if (!loginPage) return
        handler.removeCallbacks(loginPoller)
        loginPage = false
        runCatching { CookieManager.getInstance().flush() }
        pendingCookie = Accounts.capture()
        addMode = false
        val web = loginWeb
        loginWeb = null
        if (web != null) {
            handler.postDelayed({
                runCatching {
                    (web.parent as? android.view.ViewGroup)?.removeView(web)
                    web.destroy()
                }
            }, 120)
        }
        homeWarmed = false
        homeAlt = false
        Diag.log("login", "登录完成，回首页刷新")
        reload()
    }

    fun openAccounts() {
        accounts = Accounts.load(this)
        accountsOpen = true
    }

    fun switchAccount(acc: Accounts.Acc) {
        accountsOpen = false
        Diag.log("account", "切换账号 " + acc.name)
        Accounts.apply(acc.cookie) {
            currentAccountId = acc.id
            Accounts.setCurrent(this, acc.id)
            loginName = acc.name
            if (acc.avatar.isNotBlank()) avatarUrl = acc.avatar
            loggedIn = true
            homeWarmed = false
            homeAlt = false
            reload()
        }
    }

    fun deleteAccount(acc: Accounts.Acc) {
        Diag.log("account", "删除账号 " + acc.name)
        accounts = Accounts.remove(this, acc.id)
        if (acc.id != currentAccountId) {
            accountsOpen = true
            return
        }
        val next = accounts.firstOrNull()
        if (next != null) {
            switchAccount(next)
            accountsOpen = true
        } else {
            currentAccountId = null
            Accounts.setCurrent(this, null)
            loggedIn = false
            loginName = null
            avatarUrl = null
            Accounts.clear {
                homeWarmed = false
                homeAlt = false
                reload()
            }
            accountsOpen = true
        }
    }

    /** 新增账号：先把当前 Cookie 备份到账号表，再清空 CookieManager，登录页才会显示空表单 */
    fun addAccount() {
        accountsOpen = false
        val cur = accounts.firstOrNull { it.id == currentAccountId }
        if (cur != null) Accounts.upsert(this, cur)
        addMode = true
        val backup = Accounts.capture()
        if (backup.isNotBlank()) {
            currentAccountId?.let { id ->
                val name = loginName ?: "账号"
                accounts = Accounts.upsert(this, Accounts.Acc(id, name, avatarUrl.orEmpty(), backup))
            }
        }
        Accounts.clear {
            openLogin()
        }
    }

    /** 弹可见验证页：隐藏 WebView 里过不去的 Cloudflare 挑战，改成让用户看着它跑完 */
    private fun openVerify(url: String) {
        if (verifyPage) return
        val web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webViewClient = WebViewClient()
        web.loadUrl(url)
        verifyWeb = web
        verifyTarget = url
        verifyPage = true
        Diag.log("verify", "打开可见验证页 " + url)
        handler.postDelayed(verifyPoller, 700)
    }

    private val verifyPoller: Runnable = object : Runnable {
        override fun run() {
            if (!verifyPage) return
            val clearance = hasClearance()
            // 验证页里如果被站点跳到登录页并登录成功，也要立刻放行：
            // 同一个 CookieManager，登录态已经落地，直接回列表刷新即可
            webHasLogout(verifyWeb) { logged ->
                if (!verifyPage) return@webHasLogout
                if (clearance || logged) {
                    Diag.log("verify", "验证页通过 clearance=" + clearance + " login=" + logged)
                    val target = verifyTarget
                    verifyTarget = null
                    closeVerify()
                    if (target != null) {
                        CookieManager.getInstance().flush()
                        reload()
                    }
                    return@webHasLogout
                }
                handler.postDelayed(this, 900)
            }
        }
    }

    private fun hasClearance(): Boolean {
        val cookie = CookieManager.getInstance().getCookie(SITE) ?: return false
        return cookie.contains("cf_clearance=")
    }

    private fun closeVerify() {
        handler.removeCallbacks(verifyPoller)
        verifyPage = false
        val web = verifyWeb
        verifyWeb = null
        if (web != null) {
            handler.postDelayed({
                runCatching {
                    (web.parent as? android.view.ViewGroup)?.removeView(web)
                    web.destroy()
                }
            }, 120)
        }
    }

    private fun closeLogin() {
        handler.removeCallbacks(loginPoller)
        loginPage = false
        if (addMode) {
            addMode = false
            val cur = accounts.firstOrNull { it.id == currentAccountId }
            if (cur != null) Accounts.apply(cur.cookie) { }
        }
        val web = loginWeb
        loginWeb = null
        if (web != null) {
            handler.postDelayed({
                runCatching {
                    (web.parent as? android.view.ViewGroup)?.removeView(web)
                    web.destroy()
                }
            }, 120)
        }
    }
}
