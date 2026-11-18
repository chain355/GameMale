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
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
    private var loginOpen by mutableStateOf(false)
    private var lastHtml: String = ""
    private var busy by mutableStateOf(false)
    private var replyOpen by mutableStateOf(false)
    private var rateOpen by mutableStateOf(false)
    private var rateFields by mutableStateOf<List<Triple<String, String, String>>>(emptyList())
    private var loginName by mutableStateOf<String?>(null)
    private var loggedIn by mutableStateOf(false)
    private var avatarUrl by mutableStateOf<String?>(null)
    private var imageUrl by mutableStateOf<String?>(null)
    private var rateFormDef: FormDef? = null
    private var rateReasonName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        showLastCrash()

        val prefs = getSharedPreferences("gm", MODE_PRIVATE)
        // 没手动选过就跟随系统的深浅色
        val systemDark = (resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        darkMode = prefs.getBoolean("dark", systemDark)
        applyStatusBar()

        browserHost = FrameLayout(this)
        fetcher = Fetcher(this)

        val root = ComposeView(this)
        root.setContent {
            CouiTheme(darkTheme = darkMode, colorScheme = rememberScheme(darkMode)) {
                BackHandler { goBack() }
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
                        darkMode = !darkMode
                        getSharedPreferences("gm", MODE_PRIVATE).edit()
                            .putBoolean("dark", darkMode).commit()
                        applyStatusBar()
                    },
                    onLogin = { openLogin() },
                    onReply = { replyOpen = true },
                    onRate = { pid -> openRate(pid) },
                    onAttach = { at -> openAttach(at) },
                    onImage = { url -> imageUrl = url },
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
                    onSearch = { q -> doSearch(q) },
                    onExternal = { openExternal(it) },
                    currentUrl = navUrl(),
                    showBottomBar = backStack.isEmpty() && nav is Nav.Forum,
                    loginName = loginName,
                    avatarUrl = avatarUrl,
                    loggedIn = loggedIn,
                    onShare = { shareThread() }
                )
            }
        }
        setContentView(root)
        fetcher.attach(browserHost)
        // 默认直接进「动漫」版块，省掉版块总览这一层
        val deep = threadUrlFromIntent(intent)
        if (deep != null) {
            nav = Nav.Thread(deep, "帖子")
            load(deep)
        } else {
            nav = Nav.Forum(ANIME_URL, "动漫")
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
        loading = true
        error = null
        runCatching { fetcher.fetch(url) { r ->
            runOnUiThread {
                loading = false
                r.onSuccess { html ->
                    lastHtml = html
                    runCatching {
                        refreshLogin(html)
                        when (nav) {
                            is Nav.Thread -> threadPage = Site.parseThread(html)
                            is Nav.Forum -> {
                                loadedForumUrl = url
                                var fp = Site.parseForumPage(html)
                                if (fp.threads.isEmpty() && url.contains("search.php")) {
                                    val hits = Site.parseSearchResults(html)
                                    if (hits.isNotEmpty()) {
                                        fp = fp.copy(threads = hits)
                                    }
                                }
                                if (fp.threads.isEmpty() && url.contains("typeid=") && !url.contains("search.php")) {
                                    // 分类参数失效时，退回不带 typeid 的版块页再抓一次
                                    val base = Regex("(https?://[^&]+mod=forumdisplay&fid=\\d+)").find(url)?.groupValues?.get(1)
                                    if (base != null) {
                                        val tid2 = Regex("fid=(\\d+)").find(base)?.groupValues?.get(1)
                                        val baseUrl = base + "&filter=dateline&orderby=dateline"
                                        dumpText("forum-empty", html)
                                        if (tid2 != null && nav is Nav.Forum && (nav as Nav.Forum).url != baseUrl) {
                                            load(baseUrl)
                                            return@onSuccess
                                        }
                                    }
                                }
                                forumPage = fp.copy(cats = mergeCats(forumPage?.cats ?: emptyList(), fp.cats))
                                forumCache[url] = forumPage!!
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
                }.onFailure { error = it.message ?: "加载失败" }
            }
        } }.onFailure {
            loading = false
            error = "打不开这个页面：" + (it.message ?: "")
        }
    }

    private fun reload() {
        when (val n = nav) {
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
    private fun openQuick(url: String, title: String) {
        backStack.add(nav)
        nav = Nav.Forum(url, title)
        forumPage = null
        threadPage = null
        load(url)
    }

    /** 搜索：直接走站内搜索页，再用版块页解析（列表结构与版块页一致） */
    private fun doSearch(q: String) {
        val url = SITE + "search.php?mod=forum&srchtxt=" + Uri.encode(q) + "&searchsubmit=yes&mobile=no"
        backStack.add(nav)
        nav = Nav.Forum(url, "搜索：" + q)
        forumPage = null
        load(url)
    }

    private fun openThread(item: ThreadItem) {
        backStack.add(nav)
        nav = Nav.Thread(item.url, item.title)
        val tid = Regex("thread-(\\d+)").find(item.url)?.groupValues?.get(1)
        val cached = tid?.let { threadCache[it] }
        if (cached != null) {
            // 预取时已经抓过整页，直接解析，不再二次加载
            threadPage = Site.parseThread(cached)
            lastHtml = cached
            refreshLogin(cached)
            return
        }
        threadPage = null
        load(item.url)
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

    private fun goBack() {
        if (loginOpen) { loginOpen = false; return }
        if (backStack.isNotEmpty()) {
            nav = backStack.removeAt(backStack.size - 1)
            when (val n = nav) {
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
                is Nav.Thread -> load(n.url)
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
        loggedIn = Site.isLoggedIn(html)
        if (loggedIn) {
            loginName = Site.parseLoginName(html)
            avatarUrl = Site.parseLoginAvatar(html)
        } else {
            loginName = null
            avatarUrl = null
        }
    }

    private var prefetchToken = 0
    private var loadedForumUrl: String? = null
    /** tid -> 已抓到的帖子 HTML（预取时顺手存下，再次打开直接用） */
    private val threadCache = LinkedHashMap<String, String>()
    /** url -> 已加载好的列表页（回退时不重载，搜索页也各存各的） */
    private val forumCache = LinkedHashMap<String, ForumPage>()
    private var rateSourceHtml: String? = null

    /** 列表页本身没有缩略图（这个模板没开图文插件），就用正文首图补，最多补 6 条 */
    private fun prefetchThumbs(page: ForumPage) {
        val token = ++prefetchToken
        val targets = page.threads.filter { it.thumbs.isEmpty() && it.url.contains("thread-") }.take(6)
        if (targets.isEmpty()) return
        fun step(i: Int) {
            if (i >= targets.size || token != prefetchToken || nav !is Nav.Forum) return
            val t = targets[i]
            fetcher.fetch(t.url) { r ->
                runOnUiThread {
                    r.onSuccess { html ->
                        if (token != prefetchToken) return@onSuccess
                        val imgs = Site.parseFirstImages(html, 3)
                        val tid = Regex("thread-(\\d+)").find(t.url)?.groupValues?.get(1)
                        if (tid != null) {
                            if (threadCache.size > 8) threadCache.remove(threadCache.keys.first())
                            threadCache[tid] = html
                        }
                        val cur = forumPage
                        if (imgs.isNotEmpty() && cur != null) {
                            forumPage = cur.copy(threads = cur.threads.map { item ->
                                if (item.tid == t.tid) item.copy(thumbs = imgs) else item
                            })
                        }
                    }
                    step(i + 1)
                }
            }
        }
        step(0)
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
        Nav.Home -> SITE + "forum.php"
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
            submit(form, onDone = { replyOpen = false })
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
        fetcher.submitOnPage(url, fields.joinToString("&") { (k, v) ->
            java.net.URLEncoder.encode(k, "UTF-8") + "=" + java.net.URLEncoder.encode(v, "UTF-8")
        }) { r ->
            runOnUiThread {
                r.onSuccess { xml ->
                    dumpText("reply", xml)
                    val ok = xml.contains("succeedhandle_") || isSuccess(cdataText(xml))
                    val msg = if (xml.contains("succeedhandle_")) "回复成功" else cdataText(xml)
                    toast(msg)
                    if (ok) {
                        replyOpen = false
                        reload()
                    }
                }.onFailure { toast("提交失败：" + (it.message ?: "")) }
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
    private fun submit(form: FormDef, onDone: () -> Unit) {
        prefetchToken++
        busy = true
        val url = form.action + (if (form.action.contains("?")) "&" else "?") + "inajax=1"
        fetcher.submitOnPage(url, form.body()) { r ->
            runOnUiThread {
                busy = false
                r.onSuccess { xml ->
                    dumpText("submit", xml)
                    val ok = xml.contains("succeedhandle_") || isSuccess(cdataText(xml))
                    val msg = if (xml.contains("succeedhandle_")) "提交成功" else cdataText(xml)
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
                    // 页内提交没走通时，回退到 postUrl 再试一次
                    fetcher.post(url, form.body()) { r2 ->
                        runOnUiThread {
                            r2.onSuccess { xml2 ->
                                dumpText("submit", xml2)
                                val ok2 = xml2.contains("succeedhandle_") || isSuccess(cdataText(xml2))
                                val msg = if (xml2.contains("succeedhandle_")) "提交成功" else cdataText(xml2)
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

    /** 登录：临时弹一个 WebView，登录完 Cookie 就留在 CookieManager 里 */
    fun openLogin() {
        val web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webViewClient = WebViewClient()
        web.loadUrl(SITE + "member.php?mod=logging&action=login")
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
        }
        loginOpen = true
        dialog.show()
        dialog.setOnDismissListener {
            loginOpen = false
            runCatching {
                (web.parent as? ViewGroup)?.removeView(web)
                web.destroy()
            }
            // 登录后的 Cookie 同步落盘，隐藏 WebView 才能立刻带上登录态
            runCatching { CookieManager.getInstance().flush() }
            homeWarmed = false
            homeAlt = false
            reload()
        }
    }
}
