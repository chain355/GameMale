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
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import com.itos.coui.theme.CouiTheme

class MainActivity : ComponentActivity() {

    private lateinit var fetcher: Fetcher
    private lateinit var browserHost: FrameLayout

    private lateinit var routes: AppNavigation

    private val homeVm: HomeViewModel by viewModels()
    private val forumVm: ForumViewModel by viewModels()
    private val threadVm: ThreadViewModel by viewModels()
    private val searchVm: SearchViewModel by viewModels()
    private val settingsVm: SettingsViewModel by viewModels()
    private val attachVm: AttachmentViewModel by viewModels()
    private val accountVm: AccountViewModel by viewModels()

    private var homeTab: Int
        get() = homeVm.homeTab
        set(value) { homeVm.homeTab = value }
    private var homeJump: Int
        get() = homeVm.homeJump
        set(value) { homeVm.homeJump = value }
    private var homeJumpGen: Int
        get() = homeVm.homeJumpGen
        set(value) { homeVm.homeJumpGen = value }

    private fun onHomeBoard(): Boolean = routes.top == AppRoute.Home && homeTab <= 1

    private fun requestHomeTab(index: Int) = homeVm.requestTab(index)

    private fun forumSurface(): Boolean = onHomeBoard() || routes.top is AppRoute.Forum

    private fun forumUrl(): String? = when (val r = routes.top) {
        AppRoute.Home -> when (homeTab) {
            0 -> animeUrl
            1 -> gameUrl
            else -> null
        }
        is AppRoute.Forum -> r.url
        else -> null
    }

    private fun forumTitle(): String = when (val r = routes.top) {
        AppRoute.Home -> when (homeTab) {
            0 -> "和谐动漫"
            1 -> "和谐游戏"
            else -> ""
        }
        is AppRoute.Forum -> r.title
        else -> ""
    }

    /** 首页两个版块只改自己的地址；已经压进来的版块替换栈顶，不再多一层。 */
    private fun retargetForum(url: String, title: String) {
        when (routes.top) {
            AppRoute.Home -> when (homeTab) {
                0 -> animeUrl = url
                1 -> gameUrl = url
                else -> routes.push(AppRoute.Forum(url, title))
            }
            is AppRoute.Forum -> routes.replaceTop(AppRoute.Forum(url, title))
            else -> routes.push(AppRoute.Forum(url, title))
        }
    }

    /** 根页面打开帖子是压栈；已经在版块或帖子上则替换栈顶。 */
    private fun showAsThread(url: String, title: String) {
        when (val top = routes.top) {
            is AppRoute.Thread -> routes.replaceTop(AppRoute.Thread(url, title.ifBlank { top.title }))
            is AppRoute.Forum -> routes.replaceTop(AppRoute.Thread(url, title))
            else -> routes.push(AppRoute.Thread(url, title))
        }
    }
    private var forums: List<ForumItem>
        get() = forumVm.forums
        set(value) { forumVm.forums = value }
    private var forumPage: ForumPage?
        get() = forumVm.forumPage
        set(value) { forumVm.forumPage = value }
    private var animePage: ForumPage?
        get() = homeVm.animePage
        set(value) { homeVm.animePage = value }
    private var gamePage: ForumPage?
        get() = homeVm.gamePage
        set(value) { homeVm.gamePage = value }
    private var animeUrl: String
        get() = homeVm.animeUrl
        set(value) { homeVm.animeUrl = value }
    private var gameUrl: String
        get() = homeVm.gameUrl
        set(value) { homeVm.gameUrl = value }
    private var gameFirstSwitchPending: Boolean
        get() = homeVm.gameFirstSwitchPending
        set(value) { homeVm.gameFirstSwitchPending = value }
    private var threadPage: ThreadPage?
        get() = threadVm.threadPage
        set(value) { threadVm.threadPage = value }
    /** 当前这次加载的状态，跟页面数据分开：响应回来时要写回发起请求的那一页。 */
    private var loading by mutableStateOf(false)
    private var error by mutableStateOf<String?>(null)
    private var darkMode: Boolean
        get() = settingsVm.darkMode
        set(value) { settingsVm.darkMode = value }
    private var lastHtml: String = ""
    private var busy by mutableStateOf(false)
    private var replyOpen: Boolean
        get() = threadVm.replyOpen
        set(value) { threadVm.replyOpen = value }
    private var rateOpen: Boolean
        get() = threadVm.rateOpen
        set(value) { threadVm.rateOpen = value }
    private var rateFields: List<Triple<String, String, String>>
        get() = threadVm.rateFields
        set(value) { threadVm.rateFields = value }
    private var loginName: String?
        get() = accountVm.loginName
        set(value) { accountVm.loginName = value }
    private var loggedIn: Boolean
        get() = accountVm.loggedIn
        set(value) { accountVm.loggedIn = value }
    private var avatarUrl: String?
        get() = accountVm.avatarUrl
        set(value) { accountVm.avatarUrl = value }
    private var imageUrl: String?
        get() = threadVm.imageUrl
        set(value) { threadVm.imageUrl = value }
    private lateinit var cache: ThreadCache
    private var themeMode: Int
        get() = settingsVm.themeMode
        set(value) { settingsVm.themeMode = value }
    private var fontScale: Float
        get() = settingsVm.fontScale
        set(value) { settingsVm.fontScale = value }
    private var autoSignEnabled: Boolean
        get() = settingsVm.autoSignEnabled
        set(value) { settingsVm.autoSignEnabled = value }
    private var autoAwardEnabled: Boolean
        get() = settingsVm.autoAwardEnabled
        set(value) { settingsVm.autoAwardEnabled = value }
    private var cacheMb: Int
        get() = settingsVm.cacheMb
        set(value) { settingsVm.cacheMb = value }
    private var cacheBytes: Long
        get() = settingsVm.cacheBytes
        set(value) { settingsVm.cacheBytes = value }
    private var cacheCount: Int
        get() = settingsVm.cacheCount
        set(value) { settingsVm.cacheCount = value }
    private var blockedWords: List<String>
        get() = settingsVm.blockedWords
        set(value) { settingsVm.blockedWords = value }
    private var watchWords: List<String>
        get() = settingsVm.watchWords
        set(value) { settingsVm.watchWords = value }
    private var contentPrefOpen: Boolean
        get() = settingsVm.contentPrefOpen
        set(value) { settingsVm.contentPrefOpen = value }
    private var contentPrefKind: Int
        get() = settingsVm.contentPrefKind
        set(value) { settingsVm.contentPrefKind = value }
    private var readTids: Set<Long>
        get() = homeVm.readTids
        set(value) { homeVm.readTids = value }
    private var changelogOpen: Boolean
        get() = settingsVm.changelogOpen
        set(value) { settingsVm.changelogOpen = value }
    private var upgradeReleases: List<Changelog.Release>
        get() = settingsVm.upgradeReleases
        set(value) { settingsVm.upgradeReleases = value }
    private var firstRunNotice: Boolean
        get() = settingsVm.firstRunNotice
        set(value) { settingsVm.firstRunNotice = value }
    private var appVersionName: String = ""
    private var rateFormDef: FormDef?
        get() = threadVm.rateFormDef
        set(value) { threadVm.rateFormDef = value }
    private var rateReasonName: String?
        get() = threadVm.rateReasonName
        set(value) { threadVm.rateReasonName = value }
    private var attachments: List<AttachmentStore.Entry>
        get() = attachVm.attachments
        set(value) { attachVm.attachments = value }
    private var attachSelecting: Boolean
        get() = attachVm.attachSelecting
        set(value) { attachVm.attachSelecting = value }
    private var attachSelectionCount: Int
        get() = attachVm.attachSelectionCount
        set(value) { attachVm.attachSelectionCount = value }
    private var attachLoads: Map<String, AttachLoad>
        get() = attachVm.attachLoads
        set(value) { attachVm.attachLoads = value }
    private var attachTexts: Map<String, String>
        get() = attachVm.attachTexts
        set(value) { attachVm.attachTexts = value }
    private var attachFocus: String?
        get() = attachVm.attachFocus
        set(value) { attachVm.attachFocus = value }
    private var fileTextOpen: Boolean
        get() = attachVm.fileTextOpen
        set(value) { attachVm.fileTextOpen = value }
    private var fileTextTitle: String
        get() = attachVm.fileTextTitle
        set(value) { attachVm.fileTextTitle = value }
    private var fileTextBody: String
        get() = attachVm.fileTextBody
        set(value) { attachVm.fileTextBody = value }
    private var attachConfirm: AttachItem?
        get() = attachVm.attachConfirm
        set(value) { attachVm.attachConfirm = value }
    private var attachPayAt: AttachItem?
        get() = attachVm.attachPayAt
        set(value) { attachVm.attachPayAt = value }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        showLastCrash()

        // 账号表：上次用在哪个账号，启动时先按它展示
        accounts = Accounts.load(this)
        currentAccountId = Accounts.currentId(this)
        history = HistoryStore.load(this)
        attachments = AttachmentStore.load(this)
        Diag.log("history", "启动载入 " + history.size + " 条")
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
        blurBg = prefs.getBoolean("blurBg", true)
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
        // 冷启动先沿用上次的登录态：头像/昵称直接显示，别再闪一下默认人头。
        // 注意不要在这里读 CookieManager：冷启动时 Cookie 还没初始化完，
        // 读到空值会把「上次明明是登录状态」误判成未登录。
        // 只有刷新后明确看到站点登录入口（action=login）才判定掉登录。
        loggedIn = prefs.getBoolean("wasLoggedIn", false)
        loginName = prefs.getString("lastLoginName", null)?.takeIf { it.isNotBlank() }
        avatarUrl = prefs.getString("lastAvatar", null)
            ?.takeIf { it.isNotBlank() && !it.contains("uid=0") }
        if (avatarUrl.isNullOrBlank()) {
            // 兜底：账号表里存着上次的头像（老版本可能没写 lastAvatar）
            val accs = Accounts.load(this)
            val curId = Accounts.currentId(this)
            avatarUrl = accs.firstOrNull { it.id == curId && it.avatar.isNotBlank() }?.avatar
                ?: accs.firstOrNull { loginName != null && it.name == loginName && it.avatar.isNotBlank() }?.avatar
                ?: accs.firstOrNull { it.avatar.isNotBlank() }?.avatar
            if (!avatarUrl.isNullOrBlank()) {
                prefs.edit().putString("lastAvatar", avatarUrl).apply()
            }
        }
        appVersionName = runCatching {
            packageManager.getPackageInfo(packageName, 0).versionName
        }.getOrNull().orEmpty()
        loadAutoTaskPrefs(prefs)
        watchLater = WatchStore.load(this)
        blockedWords = ContentPrefs.loadBlocked(this)
        watchWords = ContentPrefs.loadWatch(this)
        readTids = ReadStore.load(this)
        cache = ThreadCache(this)
        cacheMb = cache.limitMb
        refreshCacheStats()
        // 版本比对放后台线程，不挡首页首屏
        checkUpgradeLog(prefs)
        checkFirstRunNotice(prefs)
        applyStatusBar()

        browserHost = FrameLayout(this)
        fetcher = Fetcher(this)

        val root = ComposeView(this)
        val deepAtLaunch = threadUrlFromIntent(intent)
        root.setContent {
            val navigation = key("home-pager") { rememberAppNavigation(deepAtLaunch) }
            routes = navigation
            LaunchedEffect(deepAtLaunch) {
                if (deepAtLaunch != null) {
                    // 外部分享/深链进来的帖子：先不写浏览记录，等正文解析成功后再写。
                    load(deepAtLaunch)
                } else {
                    SnapshotStore.get(ANIME_URL)?.let { animePage = it.copy(title = "和谐动漫") }
                    SnapshotStore.get(GAME_URL)?.let { gamePage = it.copy(title = "和谐游戏") }
                    load(ANIME_URL)
                }
            }
            CouiTheme(darkTheme = darkMode, colorScheme = rememberScheme(darkMode)) {
                AppUi(
                    nav = navigation,
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
                    onRefreshThread = { (navigation.top as? AppRoute.Thread)?.url?.let { reloadThread(it) } },
                    onOpenBrowser = { (navigation.top as? AppRoute.Thread)?.url?.let { openInBrowser(it) } },
                    onClearCache = {
                        prefetchToken++
                        fetcher.dropPending()
                        cache.clear()
                        threadCache.clear()
                        forumCache.clear()
                        SnapshotStore.clear()
                        ThumbStore.clear()
                        // 附件也是缓存的一部分：一并删掉，用户已在确认弹窗里二次确认
                        AttachmentStore.clear(this)
                        attachments = emptyList()
                        attachTexts = emptyMap()
                        attachLoads = emptyMap()
                        fileTextOpen = false
                        refreshCacheStats()
                        toast("缓存与已下载附件已清除")
                    },
                    canGoBack = navigation.canGoBack,
                    refreshing = refreshing,
                    searchUpdating = searchUpdating,
                    dailyFailed = dailyFailed,
                    dailyRewards = dailyRewards,
                    onCloseDailyRewards = { dailyRewards = emptyList() },
                    onOpenSign = { openInBrowser(SITE + "plugin.php?id=k_misign:sign") },
                    onOpenAward = { openInBrowser(SITE + "plugin.php?id=it618_award") },
                    onRefresh = { refreshCurrentForum() },
                    onOpenSearch = { openSearch() },
                    searchHistory = searchHistory,
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
                    onOpenHistoryTab = { openHistory() },
                    history = history,
                    onOpenHistory = { url, title -> openFromHistory(url, title) },
                    watchLater = watchLater,
                    watchSelecting = watchSelecting,
                    watchSelectionCount = watchSelectionCount,
                    onWatchSelectionCount = { watchSelectionCount = it },
                    onWatchSelecting = { watchSelecting = it },
                    onOpenWatch = { url, title -> openFromHistory(url, title) },
                    onClearWatch = {
                        watchLater = WatchStore.clear(this)
                        toast("收藏已清空")
                    },
                    onDeleteWatch = { tids ->
                        watchLater = WatchStore.removeMany(this, tids)
                        toast("已移出收藏 " + tids.size + " 条")
                    },
                    watchTidSaved = ((navigation.top as? AppRoute.Thread)?.url?.let { tidOf(it) }?.toLongOrNull())
                        ?.let { t -> watchLater.any { it.tid == t } } == true,
                    onToggleWatch = { toggleWatchLater() },
                    attachPayOpen = attachPayInfo != null,
                    attachPayName = attachPayInfo?.fileName?.ifBlank { attachPayName } ?: attachPayName,
                    attachPayPrice = attachPayInfo?.price.orEmpty(),
                    attachPayBalance = attachPayInfo?.balance.orEmpty(),
                    attachPayBusy = attachPayBusy,
                    onConfirmAttachPay = { confirmAttachBuy() },
                    onCloseAttachPay = { closeAttachBuy() },
                    attachConfirmOpen = attachConfirm != null,
                    attachConfirmName = attachConfirm?.name.orEmpty(),
                    attachConfirmMeta = attachConfirm?.let {
                        listOfNotNull(it.meta.takeIf { m -> m.isNotEmpty() }, it.price)
                            .joinToString("  ")
                    }.orEmpty(),
                    onConfirmAttachDownload = { confirmAttachDownload() },
                    onCloseAttachConfirm = { attachConfirm = null },
                    threadTid = threadTid()?.toLongOrNull(),
                    attachments = attachments,
                    attachLoads = attachLoads,
                    attachTexts = attachTexts,
                    attachFocus = attachFocus,
                    onFocusConsumed = { attachFocus = null },
                    onNeedAttachText = { key -> ensureAttachText(key) },
                    onOpenAttachment = { e -> openAttachment(e) },
                    onCopyText = { copyText(it) },
                    onFreeCopyText = { title, text -> showAttachmentText(title, text) },
                    onOpenAttachManager = { openAttachManager() },
                    attachmentCount = attachments.size,
                    attachSelecting = attachSelecting,
                    attachSelectionCount = attachSelectionCount,
                    onAttachSelectionCount = { attachSelectionCount = it },
                    onAttachSelecting = { attachSelecting = it },
                    onDeleteAttachments = { ids ->
                        attachments = AttachmentStore.removeMany(this, ids)
                        toast("已删除 " + ids.size + " 个附件")
                    },
                    fileTextOpen = fileTextOpen,
                    fileTextTitle = fileTextTitle,
                    fileTextBody = fileTextBody,
                    onCloseFileText = { fileTextOpen = false },
                    onClearHistory = { history = HistoryStore.clear(this) },
                    historySelectionCount = historySelectionCount,
                    onHistorySelectionCount = { historySelectionCount = it },
                    threadKey = openThreadTid,
                    blurBg = blurBg,
                    onSetBlurBg = {
                        blurBg = it
                        prefs.edit().putBoolean("blurBg", it).commit()
                    },
                    autoSignEnabled = autoSignEnabled,
                    autoAwardEnabled = autoAwardEnabled,
                    onSetAutoSign = { on ->
                        autoSignEnabled = on
                        prefs.edit().putBoolean("autoSignEnabled", on).apply()
                    },
                    onSetAutoAward = { on ->
                        autoAwardEnabled = on
                        prefs.edit().putBoolean("autoAwardEnabled", on).apply()
                    },
                    historySelecting = historySelecting,
                    onHistorySelecting = { historySelecting = it },
                    onDeleteHistory = { tids ->
                        history = HistoryStore.removeMany(this, tids)
                        toast("已删除 " + tids.size + " 条浏览记录")
                    },
                    onSearch = { q -> doSearch(q) },
                    onExternal = { openExternal(it) },
                    currentUrl = navUrl(),
                    showBottomBar = navigation.showBottomBar,
                    loginName = loginName,
                    avatarUrl = avatarUrl,
                    loggedIn = loggedIn,
                    onShare = { shareThread() },
                    onCopyLink = { copyThreadLink() },
                    animePage = animePage,
                    gamePage = gamePage,
                    animeUrl = animeUrl,
                    gameUrl = gameUrl,
                    onRootTab = { selectRootTab(it) },
                    homeJump = homeJump,
                    homeJumpGen = homeJumpGen,
                    blockedWords = blockedWords,
                    watchWords = watchWords,
                    readTids = readTids,
                    contentPrefOpen = contentPrefOpen,
                    contentPrefKind = contentPrefKind,
                    contentPrefItems = if (contentPrefKind == 0) blockedWords else watchWords,
                    onOpenContentPref = { k ->
                        contentPrefKind = k
                        contentPrefOpen = true
                    },
                    onCloseContentPref = { contentPrefOpen = false },
                    onAddContentPref = { words ->
                        if (contentPrefKind == 0) {
                            blockedWords = (blockedWords + words).distinct()
                            ContentPrefs.save(this, blockedWords, watchWords)
                        } else {
                            watchWords = (watchWords + words).distinct()
                            ContentPrefs.save(this, blockedWords, watchWords)
                        }
                        toast(if (contentPrefKind == 0) "已添加 ${words.size} 个屏蔽词" else "已添加 ${words.size} 个特别关注")
                    },
                    onDeleteContentPref = { w ->
                        if (contentPrefKind == 0) {
                            blockedWords = blockedWords - w
                            ContentPrefs.save(this, blockedWords, watchWords)
                        } else {
                            watchWords = watchWords - w
                            ContentPrefs.save(this, blockedWords, watchWords)
                        }
                    },
                    changelogOpen = changelogOpen,
                    onOpenChangelog = { changelogOpen = true },
                    onCloseChangelog = { changelogOpen = false },
                    upgradeReleases = upgradeReleases,
                    onDismissUpgradeLog = { markUpgradeLogShown() },
                    firstRunNotice = firstRunNotice,
                    onDismissFirstRunNotice = { dismissFirstRunNotice() },
                    appVersion = appVersionName
                )
            }
        }
        setContentView(root)
        fetcher.attach(browserHost)
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
        if (!::routes.isInitialized) return
        // 同样推迟到解析成功后再写，避免删帖/审核页留下空记录
        routes.openDeepLink(AppRoute.Thread(url, "帖子"))
        threadPage = null
        load(url)
    }

    override fun onResume() {
        super.onResume()
        runDailyTasks()
        fetcher.attach(browserHost)
        fetcher.keepRunning()
    }

    override fun onPause() {
        // 不主动暂停 WebView：切后台时保持当前加载继续跑
        fetcher.keepRunning()
        // 退到后台再写一遍浏览记录：万一记录那一次落盘被打断，重启后还能找回来
        runCatching { HistoryStore.persist(this, history) }
        runCatching {
            (routes.top as? AppRoute.Thread)?.let { t ->
                threadPage?.let { tp ->
                    tidOf(t.url)?.toLongOrNull()?.let { tid ->
                        history = HistoryStore.record(this, tid, tp.subject, t.url)
                    }
                }
            }
        }
        super.onPause()
    }

    // ---------------- 数据加载 ----------------
    private var homeWarmed: Boolean
        get() = homeVm.homeWarmed
        set(value) { homeVm.homeWarmed = value }
    private var homeAlt: Boolean
        get() = homeVm.homeAlt
        set(value) { homeVm.homeAlt = value }

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

    /**
     * 当前列表。搜索和二级页只更新 forumPage；
     * 首页两个版块在回到顶层时才写回各自的 tab，避免把首页冲掉。
     */
    private fun showForum(url: String, page: ForumPage?) {
        forumPage = page
        homeVm.absorbBoard(routes.top == AppRoute.Home, url, page)
    }

    private fun boardOf(url: String): ForumPage? = homeVm.boardOf(url)

    private fun prevCats(url: String): List<CatTab> {
        val page = when (routes.top) {
            AppRoute.Home -> when {
                url.contains("search.php") -> forumPage
                homeTab == 0 -> animePage
                homeTab == 1 -> gamePage
                else -> forumPage
            }
            else -> forumPage
        }
        return page?.cats ?: emptyList()
    }

    /** 首页 Pager 停稳后加载这一页的数据。已经压着详情时底栏不在，这里直接忽略。 */
    private fun selectRootTab(index: Int) {
        if (routes.top != AppRoute.Home) return
        if (homeTab == index) return
        homeTab = index
        threadPage = null
        error = null
        when (index) {
            0 -> openRootBoard(animeUrl, "和谐动漫")
            1 -> {
                // 冷启动只刷了「和谐动漫」，游戏板块的数据还是上次的本地快照。
                // 所以冷启动后第一次切过来时补一次联网刷新；之后再切回来只复用已有列表。
                val firstSwitch = gameFirstSwitchPending
                gameFirstSwitchPending = false
                val hasLocal = (forumCache[gameUrl] ?: SnapshotStore.get(gameUrl) ?: gamePage)
                    ?.threads?.isNotEmpty() == true
                openRootBoard(gameUrl, "和谐游戏")
                if (firstSwitch && hasLocal) {
                    refreshing = true
                    fetchForumForRefresh(gameUrl, attempt = 0)
                }
            }
            2 -> {
                watchLater = WatchStore.load(this)
                loading = false
            }
            else -> {
                history = HistoryStore.load(this)
                loading = false
            }
        }
    }

    /** 切到动漫或游戏根页面：有缓存就铺上，没有再去拉。 */
    private fun openRootBoard(url: String, title: String) {
        val cached = forumCache[url] ?: SnapshotStore.get(url) ?: boardOf(url)
        if (cached != null && cached.threads.isNotEmpty()) {
            showForum(url, cached.copy(title = title))
            loadedForumUrl = url
            loadedForumTitle = title
            loading = false
            refreshing = false
            return
        }
        load(url)
    }

    private fun load(rawUrl: String) {
        val url = if (rawUrl.contains("mod=forumdisplay") && !rawUrl.contains("orderby="))
            rawUrl + "&orderby=dateline" else rawUrl
        if (forumSurface() && forumPage == null) {
            val snap = forumCache[url] ?: SnapshotStore.get(url)
            if (snap != null) {
                Diag.log("load", "先用磁盘快照铺屏 threads=" + snap.threads.size)
                showForum(url, snap.copy(cats = mergeCats(emptyList(), snap.cats), title = forumTitle()))
            }
        }
        // 已经有旧列表（快照或上一屏）时，用顶部细条静默刷新，不铺全屏 loading
        val hasOldList = forumSurface() && forumPage?.threads?.isNotEmpty() == true
        if (hasOldList) {
            refreshing = true
            error = null
        } else {
            loading = true
            error = null
        }
        Diag.log("load", "start nav=" + routes.top + " url=" + url)
        if (forumSurface() && loadedForumUrl == url) retryCount = 0
        if (forumSurface()) {
            loadedForumUrl = url
            loadedForumTitle = forumTitle()
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
                        when (val top = routes.top) {
                            is AppRoute.Thread -> {
                                if (!sameTarget(top.url, url)) {
                                    Diag.log("load", "丢弃过期帖子响应 url=" + url + " 当前=" + top.url)
                                    return@onSuccess
                                }
                                // 650KB 页面的 jsoup 解析和落盘都放后台，UI 先把已有内容铺出来
                                applyThreadHtml(html, url)
                            }
                            AppRoute.Home, is AppRoute.Forum -> {
                                if (top == AppRoute.Home && homeTab > 1) return@onSuccess
                                val pageUrl = forumUrl().orEmpty()
                                if (!sameTarget(pageUrl, url)) {
                                    Diag.log("load", "丢弃过期响应 url=" + url + " 当前=" + pageUrl)
                                    return@onSuccess
                                }
                                loadedForumUrl = url
                                loadedForumTitle = forumTitle()
                                // 页面结构校验：冷启动网络差时站点可能只回了 2KB 异常页/验证页，
                                // 这种既不是「未登录」也不是「空版块」，应报加载失败而不是误导用户去登录。
                                val looksForumPage = html.contains("threadlisttableid") ||
                                    html.contains("normalthread_") || html.contains("separatorline")
                                val challengePage = html.contains("Just a moment") ||
                                    html.contains("请稍候") || html.contains("challenges.cloudflare.com") ||
                                    html.contains("turnstile")
                                // 正常版块页 150KB+，游客提示页也有 40KB+；2KB 级别的一定是异常页。
                                // 这类页面里也可能残留 threadlisttableid 模板字样，所以不能只看结构标记。
                                if (url.contains("mod=forumdisplay") &&
                                    (html.length < 20_000 || (!looksForumPage && html.length < 60_000))
                                ) {
                                    Diag.log(
                                        "load",
                                        "页面不完整 bytes=" + html.length + " challenge=" + challengePage +
                                            " url=" + url
                                    )
                                    needLogin = false
                                    needUpgrade = false
                                    refreshing = false
                                    loading = false
                                    error = if (challengePage)
                                        "站点安全验证未通过（网络不稳时常见），点重试再来一次"
                                    else
                                        "页面没有完整加载（网络中断或站点返回异常页），点重试再来一次"
                                    return@onSuccess
                                }
                                if (!loggedIn && !challengePage && isLoginRequired(html)) {
                                    Diag.log("load", "未登录且需要登录 url=" + url)
                                    needLogin = true
                                    needUpgrade = false
                                    refreshing = false
                                    loading = false
                                    error = null
                                    showForum(url, Site.parseForumPage(html).copy(title = forumTitle()))
                                    return@onSuccess
                                }
                                needLogin = false
                                if (isGroupBlocked(html)) {
                                    Diag.log("load", "用户组等级不足 url=" + url)
                                    needUpgrade = true
                                    refreshing = false
                                    loading = false
                                    error = null
                                    showForum(url, Site.parseForumPage(html).copy(title = forumTitle()))
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
                                    showForum(url, fp.copy(title = forumTitle()))
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
                                        if (tid2 != null && forumSurface() && forumUrl() != baseUrl) {
                                            Diag.log("load", "分类为空，回退 " + baseUrl)
                                            retargetForum(baseUrl, forumTitle())
                                            load(baseUrl)
                                            return@onSuccess
                                        }
                                    }
                                }
                                fp = fp.copy(threads = withCachedThumbs(fp.threads))
                                refreshing = false
                                searchUpdating = false
                                Diag.log("load", "forum ok threads=" + fp.threads.size + " bytes=" + html.length + " url=" + url)
                                val shown = fp.copy(cats = mergeCats(prevCats(url), fp.cats))
                                showForum(url, shown)
                                forumCache[url] = shown
                                SnapshotStore.put(url, shown)
                                if (forumCache.size > 6) forumCache.remove(forumCache.keys.first())
                                // 不再顺手在后台刷新另一个板块：冷启动与日常刷新都只动当前板块，
                                // 另一个板块等用户真正切过去时再取
                                if (fp.threads.isEmpty()) {
                                    val tp = Site.parseThread(html)
                                    if (tp.posts.isNotEmpty()) {
                                        threadPage = tp
                                        showAsThread(url, tp.subject)
                                    }
                                }
                            }
                            else -> forums = Site.parseForums(html)
                        }
                    }.onFailure {
                        error = "解析失败：" + (it.message ?: it.javaClass.simpleName)
                    }
                    if (forumSurface()) forumPage?.let { prefetchThumbs(it) }
                }.onFailure {
                    refreshing = false
                    loading = false
                    searchUpdating = false
                    val reason = it.message ?: it.javaClass.simpleName
                    Diag.log("load", "fail url=" + url + " reason=" + reason)
                    if (reason.contains("Cloudflare")) {
                        openVerify(url)
                        return@onFailure
                    }
                    if (routes.top is AppRoute.Thread &&
                        (reason.contains("删除") || reason.contains("审核") ||
                            reason.contains("不存在") || reason.contains("不是目标帖子"))
                    ) {
                        // 帖子没了：不写浏览记录，把之前可能记下的那条也删掉。
                        // 「拿到的不是目标帖子」在删帖/审核时也会反复出现（站点只回异常页），
                        // 不能无限重试，直接给明确提示并清掉记录。
                        tidOf((routes.top as AppRoute.Thread).url)?.toLongOrNull()?.let { tid ->
                            history = HistoryStore.remove(this, tid)
                        }
                        error = "这篇帖子可能已被删除或正在审核，无法查看\n可以复制链接到浏览器确认，或稍后再试"
                        retryCount = 0
                        return@onFailure
                    }
                    if (forumSurface() && retryCount < 1) {
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
        when (val n = routes.top) {
            AppRoute.Settings, AppRoute.Search, AppRoute.Attachments, AppRoute.Deep -> {}
            AppRoute.Home -> when (homeTab) {
                0 -> load(animeUrl)
                1 -> load(gameUrl)
                else -> {}
            }
            is AppRoute.Forum -> load(n.url)
            is AppRoute.Thread -> {
                // 从「加载不出来」页点重试时，补一层和首次加载一样的全屏刷新动画；
                // 普通评分/回复后的静默刷新不加，避免每次提交都遮一屏。
                if (error != null) loading = true
                // 评分/回复后刷新正文：失败时保留当前页面，不要变成“加载失败”
                val keepThread = threadPage
                fetcher.fetch(n.url) { r ->
                    runOnUiThread {
                        r.onSuccess { html ->
                            lastHtml = html
                            runCatching {
                                refreshLogin(html)
                                threadPage = Site.parseThread(html)
                                val fresh = threadPage
                                if (fresh == null || fresh.posts.isEmpty() || Site.threadMissing(html)) {
                                    threadPage = keepThread
                                    loading = false
                                    toast("刷新失败，页面内容没有加载出来")
                                    return@runOnUiThread
                                }
                                fresh?.let { tp ->
                                    tidOf(n.url)?.toLongOrNull()?.let { tid ->
                                        // 手动刷新出正文同样算成功打开：正常写浏览记录并标已读
                                        history = HistoryStore.record(this, tid, tp.subject, n.url)
                                        readTids = ReadStore.mark(this, tid)
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
        routes.push(AppRoute.Forum(item.url, item.name))
        forumPage = null
        threadPage = null
        load(item.url)
    }

    /** 切到浏览记录页：只滚动首页 Pager，不压栈 */
    private fun openHistory() {
        history = HistoryStore.load(this)
        if (routes.top == AppRoute.Home) requestHomeTab(3)
        threadPage = null
        error = null
        loading = false
    }

    private fun openWatch() {
        watchLater = WatchStore.load(this)
        if (routes.top == AppRoute.Home) requestHomeTab(2)
        threadPage = null
        error = null
        loading = false
    }

    /** 详情页右上角菜单：收藏 / 取消收藏当前帖子 */
    private fun toggleWatchLater() {
        val tid = (routes.top as? AppRoute.Thread)?.url?.let { tidOf(it) }?.toLongOrNull() ?: return
        // 优先用正文解析出来的真实标题，避免深链刚进来时把「帖子」存进收藏
        val title = threadPage?.subject?.takeIf { it.isNotBlank() }
            ?: (routes.top as? AppRoute.Thread)?.title.orEmpty()
        val url = (routes.top as? AppRoute.Thread)?.url.orEmpty()
        val saved = watchLater.any { it.tid == tid } || WatchStore.has(this, tid)
        watchLater = if (saved) WatchStore.remove(this, tid)
        else WatchStore.add(this, tid, title, url)
        toast(if (saved) "已取消收藏" else "已加入收藏")
    }

    private fun openQuick(url: String, title: String) {
        Diag.log("nav", "switch forum -> " + title + " 缓存=" + (forumCache[url] != null))
        when {
            url.contains("fid=150") -> {
                animeUrl = url
                if (routes.top == AppRoute.Home) requestHomeTab(0) else routes.push(AppRoute.Forum(url, title))
            }
            url.contains("fid=152") -> {
                gameUrl = url
                if (routes.top == AppRoute.Home) requestHomeTab(1) else routes.push(AppRoute.Forum(url, title))
            }
            else -> routes.push(AppRoute.Forum(url, title))
        }
        threadPage = null
        error = null
        val cached = forumCache[url] ?: SnapshotStore.get(url) ?: boardOf(url)
        if (cached != null && cached.threads.isNotEmpty()) {
            showForum(url, cached.copy(title = title))
            loadedForumUrl = url
            loadedForumTitle = title
            loading = false
            refreshing = false
            return
        }
        forumPage = null
        loadedForumUrl = null
        load(url)
    }

    private fun openSearch() {
        if (routes.top is AppRoute.Search) return
        routes.push(AppRoute.Search)
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
        // 先拿出这个关键词的旧结果（内存 / 磁盘快照）铺屏，再后台拉新结果替换
        val cached = forumCache[url] ?: SnapshotStore.get(url)
        val title = "搜索：" + q.trim()
        forumPage = cached?.copy(title = title)
        searchUpdating = cached != null
        val top = routes.top
        if (top is AppRoute.Forum && top.url.contains("search.php")) {
            routes.replaceTop(AppRoute.Forum(url, title))
        } else if (top is AppRoute.Search) {
            routes.push(AppRoute.Forum(url, title))
        } else {
            routes.push(AppRoute.Search)
            routes.push(AppRoute.Forum(url, title))
        }
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
        val url = canonicalThreadUrl(rawUrl)
        openThreadTid = tidOf(url)
        // 浏览记录推迟到正文真的解析成功后再写：
        // 帖子被删/审核中时不应该留下一条「帖子」这种空标题记录。
        routes.push(AppRoute.Thread(url, title))
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
        when (val n = routes.top) {
            is AppRoute.Thread -> {
                routes.replaceTop(AppRoute.Thread(url, ""))
                threadPage = null
            }
            is AppRoute.Forum -> routes.replaceTop(n.copy(url = url))
            AppRoute.Home -> when (homeTab) {
                0 -> animeUrl = url
                1 -> gameUrl = url
                else -> routes.push(AppRoute.Forum(url, forumTitle()))
            }
            else -> routes.push(AppRoute.Forum(url, forumTitle()))
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
                            showAsThread(url, tp.subject)
                        } else {
                            var fp = Site.parseForumPage(html)
                            // 搜索结果的 DOM 不是版块列表：第二页用版块解析会得到空列表，
                            // 之前就出现「页码在、内容是空白」，要拉一次才补上。这里补搜索解析。
                            if (fp.threads.isEmpty() && url.contains("search.php")) {
                                val hits = Site.parseSearchResults(html)
                                if (hits.isNotEmpty()) fp = fp.copy(threads = hits)
                            }
                            val shown = fp.copy(
                                cats = mergeCats(prevCats(url), fp.cats),
                                threads = withCachedThumbs(fp.threads)
                            )
                            showForum(url, shown)
                            loadedForumUrl = url
                            val keepTitle = routes.showBottomBar &&
                                (url.contains("fid=150") || url.contains("fid=152"))
                            val title = if (keepTitle) forumTitle().takeIf { it.isNotBlank() } else null
                            retargetForum(url, title ?: fp.title.ifBlank { "版块" })
                        }
                    }.onFailure { error = "解析失败：" + (it.message ?: it.javaClass.simpleName) }
                }.onFailure { error = it.message ?: "加载失败" }
            }
        }
    }

    private fun openSettings() {
        // 搜索页也能进设置：之前这里把 Search 一起拦掉了，导致搜索页点头像完全没反应
        if (routes.top is AppRoute.Settings) return
        routes.push(AppRoute.Settings)
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

    /** 版块页 HTML 没有预览图。解析结果缺图时，用 ThumbStore 里已经缓存的首图补上。 */
    private fun withCachedThumbs(threads: List<ThreadItem>): List<ThreadItem> {
        return threads.map { item ->
            if (item.thumbs.isNotEmpty()) item
            else {
                val cached = ThumbStore.get(item.tid)
                if (cached.isNotEmpty()) item.copy(thumbs = cached) else item
            }
        }
    }

    /** 下拉刷新：不动现有列表，成功后再整体替换 */
    private fun refreshCurrentForum() {
        Diag.log("refresh", "nav=" + routes.top)
        val url = forumUrl() ?: loadedForumUrl ?: return
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
                            threads = withCachedThumbs(fp.threads)
                        )
                        showForum(url, merged)
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
        val url = (routes.top as? AppRoute.Thread)?.url
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
        // 附件也算内部缓存，一起展示、一起受上限约束
        cacheBytes = cache.totalBytes() + HistoryStore.bytes(this) + AttachmentStore.bytes(this)
        cacheCount = cache.count()
        if (cacheBytes > cacheMb.toLong() * 1024L * 1024L) enforceCacheBudget()
    }

    /**
     * 缓存超限时先清帖子缓存（正文图、最旧的帖子页），附件留到最后：
     * 帖子缓存压到「上限 − 附件占用」后如果仍然超标，才按最旧优先删附件。
     */
    private fun enforceCacheBudget() {
        if (!::cache.isInitialized) return
        val limit = cacheMb.toLong() * 1024L * 1024L
        val attachBytes = AttachmentStore.bytes(this)
        if (cacheBytes <= limit) return
        cache.trimTo((limit - attachBytes).coerceAtLeast(0))
        val over = cache.totalBytes() + HistoryStore.bytes(this) + AttachmentStore.bytes(this) - limit
        if (over > 0) {
            val left = AttachmentStore.trim(this, over)
            val keys = attachments.filterNot { e -> left.any { it.id == e.id } }
                .map { attachKey(it.tid, it.name) }.toSet()
            if (left.size != attachments.size) {
                attachments = left
                attachTexts = attachTexts.filterKeys { it !in keys }
                attachLoads = attachLoads.filterKeys { it !in keys }
                Diag.log("cache", "trim attachments removed=" + (keys.size) + " over=" + over)
            }
        }
        cacheBytes = cache.totalBytes() + HistoryStore.bytes(this) + AttachmentStore.bytes(this)
        cacheCount = cache.count()
    }

    fun openFromHistory(url: String, title: String) {
        openThreadUrl(url, title)
    }

    /**
     * 自动任务开关只在首次进入这个版本时决定一次：
     * 全新安装默认关闭；从旧版本升上来则保留开启。
     * 初始化结果立即落盘，之后升级不会再改用户的选择。
     */
    private fun loadAutoTaskPrefs(prefs: android.content.SharedPreferences) {
        if (!prefs.contains("autoTaskPrefsInitialized")) {
            val usedBefore = prefs.contains("lastSeenVersion") ||
                prefs.contains("firstRunNoticeShown") ||
                prefs.contains("themeMode") || prefs.contains("blurBg") ||
                prefs.contains("fontScale") || java.io.File(filesDir, "history.json").exists()
            prefs.edit()
                .putBoolean("autoSignEnabled", usedBefore)
                .putBoolean("autoAwardEnabled", usedBefore)
                .putBoolean("autoTaskPrefsInitialized", true)
                .commit()
        }
        autoSignEnabled = prefs.getBoolean("autoSignEnabled", false)
        autoAwardEnabled = prefs.getBoolean("autoAwardEnabled", false)
    }

    /**
     * 升级后首次启动：后台算出跨过的版本区间，再弹窗；不阻塞首页刷新。
     *
     * 旧版本可能压根没写过 lastSeenVersion（这个功能是 4.3.0 才加的），
     * 所以不能只看「有没有值」，还要看这台设备以前有没有用过：
     * 设置项或浏览记录存在就当升级，至少把当前版本的更新点展示一次。
     * 另外 lastSeenVersion 只在用户关掉弹窗时才落盘，避免中途被杀之后再也看不到。
     */
    private fun checkUpgradeLog(prefs: android.content.SharedPreferences) {
        val current = appVersionName
        if (current.isBlank()) return
        val last = prefs.getString("lastSeenVersion", null)
        val shown = prefs.getString("logShownVersion", null)
        val usedBefore = prefs.contains("themeMode") || prefs.contains("blurBg") ||
            prefs.contains("fontScale") || java.io.File(filesDir, "history.json").exists()

        val span: List<Changelog.Release> = when {
            last == null && !usedBefore -> emptyList()      // 真正的新装：不打扰
            last == null -> Changelog.find(current)?.let { listOf(it) } ?: emptyList()
            else -> Changelog.between(last, current)
        }
        // 上一版升级时没能把日志展示出来的，顺手补上那一版
        val extra: Changelog.Release? =
            if (shown == null && last != null) Changelog.find(last) else null
        val target: List<Changelog.Release> = (span + listOfNotNull(extra))
            .distinctBy { it.version }

        if (target.isEmpty()) {
            prefs.edit()
                .putString("lastSeenVersion", current)
                .putString("logShownVersion", current)
                .apply()
            return
        }
        Thread {
            val sorted: List<Changelog.Release> = runCatching {
                target.sortedByDescending { r -> Changelog.all.indexOf(r) }
            }.getOrDefault(target)
            runOnUiThread { upgradeReleases = sorted }
        }.start()
    }

    /**
     * 新装，以及从旧版本升级到本版，都提示一次使用声明；确认后写入标记，
     * 从此以后继续升级不再提示。标记只在用户确认后落盘，中途被杀下次启动还会再提示。
     */
    private fun checkFirstRunNotice(prefs: android.content.SharedPreferences) {
        if (prefs.getBoolean("firstRunNoticeShown", false)) return
        firstRunNotice = true
    }

    private fun dismissFirstRunNotice() {
        firstRunNotice = false
        getSharedPreferences("gm", MODE_PRIVATE).edit()
            .putBoolean("firstRunNoticeShown", true)
            .apply()
    }

    /** 用户在弹窗上点「知道了」后调用：这时才记下「已看过哪个版本」 */
    private fun markUpgradeLogShown() {
        upgradeReleases = emptyList()
        getSharedPreferences("gm", MODE_PRIVATE).edit()
            .putString("lastSeenVersion", appVersionName)
            .putString("logShownVersion", appVersionName)
            .apply()
    }

    private fun goBack() {
        if (loginPage) { closeLogin(); return }
        val revealed = routes.pop() ?: run {
            finish()
            return
        }
        when (revealed) {
            is AppRoute.Forum -> {
                // 按 URL 命中缓存就直接还原（从搜索结果返回列表页也走这里）
                val cached = forumCache[revealed.url]
                if (cached != null) {
                    showForum(revealed.url, cached)
                    loadedForumUrl = revealed.url
                    error = null
                } else if (forumPage == null || loadedForumUrl != revealed.url || error != null) {
                    load(revealed.url)
                }
            }
            is AppRoute.Thread -> {
                // 设置页返回帖子详情：本地已有同一帖就直接还原，不重新抓取
                if (threadPage != null && loadedThreadUrl == revealed.url) {
                    error = null
                } else {
                    val tid = Regex("thread-(\\d+)").find(revealed.url)?.groupValues?.get(1)
                    val disk = tid?.let { cache.read(it) }
                    if (disk != null) {
                        runCatching {
                            threadPage = Site.parseThread(disk.first)
                            lastHtml = disk.first
                            loadedThreadUrl = revealed.url
                        }
                    } else load(revealed.url)
                }
            }
            AppRoute.Home -> error = null
            else -> {}
        }
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
        if (Site.isLoggedIn(html)) {
            loggedIn = true
            Site.parseLoginName(html)?.takeIf { it.isNotBlank() }?.let { loginName = it }
            val newAvatar = Site.parseLoginAvatar(html)
            if (!newAvatar.isNullOrBlank() && !newAvatar.contains("uid=0")) avatarUrl = newAvatar
            persistLoginState()
            commitPendingAccount()
            ensureAccountForCurrentLogin()
            return
        }
        // 只有「像完整页面」且明确出现登录入口时才判掉登录，
        // 避免网络失败时拿到的 2KB 异常页/验证页把登录态误杀。
        if (html.length > 30_000 && html.contains("action=login")) {
            loggedIn = false
            loginName = null
            avatarUrl = null
            persistLoginState()
        }
    }

    private fun persistLoginState() {
        getSharedPreferences("gm", MODE_PRIVATE).edit()
            .putBoolean("wasLoggedIn", loggedIn)
            .putString("lastLoginName", loginName.orEmpty())
            .putString("lastAvatar", avatarUrl.orEmpty())
            .apply()
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
        if (html.contains("主题不存在") || html.contains("正在被审核") ||
            (html.length < 60_000 && html.contains("已被删除"))
        ) {
            tidOf(url)?.toLongOrNull()?.let { tid -> history = HistoryStore.remove(this, tid) }
            error = "这篇帖子已被删除或正在审核，无法查看"
            loading = false
            return
        }
        val t0 = System.currentTimeMillis()
        bgParse({ Site.parseThread(html) }) { res ->
            val tp = res.getOrNull()
            if (tp == null) {
                Diag.log("thread", "解析失败 " + (res.exceptionOrNull()?.message ?: ""))
                error = "解析失败：" + (res.exceptionOrNull()?.message ?: "未知")
                loading = false
                return@bgParse
            }
            if ((routes.top as? AppRoute.Thread)?.url != url) {
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
                    routes.replaceTop(AppRoute.Thread(canonical, tp.subject))
                    load(canonical)
                    return@bgParse
                }
                // 空正文视为加载失败：不覆盖当前正文，也不写浏览记录
                val plain = runCatching { org.jsoup.Jsoup.parse(html).text() }.getOrElse { "" }
                error = when {
                    plain.contains("网页无法打开") || plain.contains("无法访问") ||
                        plain.contains("网络") || plain.contains("ERR_") -> "网络连接失败，请稍后重试"
                    plain.contains("请稍候") || plain.contains("Just a moment") ->
                        "站点安全验证未通过，请稍后重试"
                    else -> "页面没有加载完整，请稍后重试"
                }
                loading = false
                return@bgParse
            }
            threadPage = tp
            loadedThreadUrl = url
            refreshing = false
            prefetchThreadImages(tp)
            val tid = Regex("thread-(\\d+)").find(url)?.groupValues?.get(1)
            if (tp.subject.isNotBlank()) syncThreadSubject(url, tp.subject)
            if (tid != null) {
                rememberThread(url, tp)
                bgParse({ cache.save(tid, tp.subject, tp.lastEdit, html); true }) { refreshCacheStats() }
            }
        }
    }

    /**
     * 正文解析出真实标题后，把顶栏和「收藏」里的占位标题一起换掉。
     * 用 tid 比对而不是整串 URL：深链地址可能带参数，URL 不等但其实是同一帖。
     */
    private fun syncThreadSubject(url: String, subject: String) {
        if (subject.isBlank()) return
        val tid = tidOf(url)?.toLongOrNull()
        val cur = routes.top as? AppRoute.Thread
        if (cur != null && tid != null && tidOf(cur.url)?.toLongOrNull() == tid) {
            if (cur.title != subject) routes.replaceTop(cur.copy(title = subject))
        }
        if (tid != null) watchLater = WatchStore.rename(this, tid, subject)
    }

    /** 打开帖子后统一写浏览记录，并把正文首图登记为该帖的预览图 */
    private fun rememberThread(url: String, tp: ThreadPage) {
        val tid = Regex("thread-(\\d+)").find(url)?.groupValues?.get(1) ?: return
        runCatching {
            history = HistoryStore.record(this, tid.toLong(), tp.subject, url)
            readTids = ReadStore.mark(this, tid.toLong())
            // 深链进来时标题只有「帖子」，正文解析出真实标题后同步到导航与收藏
            if (tp.subject.isNotBlank()) syncThreadSubject(url, tp.subject)
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
    private var loadedForumUrl: String?
        get() = forumVm.loadedForumUrl
        set(value) { forumVm.loadedForumUrl = value }
    private var loadedForumTitle: String
        get() = forumVm.loadedForumTitle
        set(value) { forumVm.loadedForumTitle = value }
    private var retryCount = 0
    private var loadedThreadUrl: String?
        get() = threadVm.loadedThreadUrl
        set(value) { threadVm.loadedThreadUrl = value }
    private var refreshing: Boolean
        get() = forumVm.refreshing
        set(value) { forumVm.refreshing = value }
    private var needUpgrade: Boolean
        get() = forumVm.needUpgrade
        set(value) { forumVm.needUpgrade = value }
    private var needLogin: Boolean
        get() = forumVm.needLogin
        set(value) { forumVm.needLogin = value }
    private var accounts: List<Accounts.Acc>
        get() = accountVm.accounts
        set(value) { accountVm.accounts = value }
    private var history: List<HistoryStore.Entry>
        get() = homeVm.history
        set(value) { homeVm.history = value }
    private var historySelectionCount: Int
        get() = homeVm.historySelectionCount
        set(value) { homeVm.historySelectionCount = value }
    private var historySelecting: Boolean
        get() = homeVm.historySelecting
        set(value) { homeVm.historySelecting = value }
    private var watchLater: List<WatchStore.Entry>
        get() = homeVm.watchLater
        set(value) { homeVm.watchLater = value }
    private var watchSelectionCount: Int
        get() = homeVm.watchSelectionCount
        set(value) { homeVm.watchSelectionCount = value }
    private var watchSelecting: Boolean
        get() = homeVm.watchSelecting
        set(value) { homeVm.watchSelecting = value }
    private var attachPayInfo: AttachPay?
        get() = attachVm.attachPayInfo
        set(value) { attachVm.attachPayInfo = value }
    private var attachPayName: String
        get() = attachVm.attachPayName
        set(value) { attachVm.attachPayName = value }
    private var attachPayBusy: Boolean
        get() = attachVm.attachPayBusy
        set(value) { attachVm.attachPayBusy = value }
    private var openThreadTid: String?
        get() = threadVm.openThreadTid
        set(value) { threadVm.openThreadTid = value }
    private var blurBg: Boolean
        get() = settingsVm.blurBg
        set(value) { settingsVm.blurBg = value }
    private var currentAccountId: String?
        get() = accountVm.currentAccountId
        set(value) { accountVm.currentAccountId = value }
    private var accountsOpen: Boolean
        get() = accountVm.accountsOpen
        set(value) { accountVm.accountsOpen = value }
    private var pendingCookie: String?
        get() = accountVm.pendingCookie
        set(value) { accountVm.pendingCookie = value }
    private var addMode: Boolean
        get() = accountVm.addMode
        set(value) { accountVm.addMode = value }
    private var loginPage: Boolean
        get() = accountVm.loginPage
        set(value) { accountVm.loginPage = value }
    private var loginWeb: WebView? = null
    private var loginPrompted: Boolean
        get() = accountVm.loginPrompted
        set(value) { accountVm.loginPrompted = value }
    private var verifyPage: Boolean
        get() = accountVm.verifyPage
        set(value) { accountVm.verifyPage = value }
    private var verifyWeb: WebView? = null
    private var verifyTarget: String?
        get() = accountVm.verifyTarget
        set(value) { accountVm.verifyTarget = value }
    private var searchOpen: Boolean
        get() = searchVm.searchOpen
        set(value) { searchVm.searchOpen = value }
    private var searchHistory: List<String>
        get() = searchVm.searchHistory
        set(value) { searchVm.searchHistory = value }
    private var searchUpdating: Boolean
        get() = forumVm.searchUpdating
        set(value) { forumVm.searchUpdating = value }
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
    private var rateSourceHtml: String?
        get() = threadVm.rateSourceHtml
        set(value) { threadVm.rateSourceHtml = value }

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
        val u = loadedForumUrl
        if (u != null) {
            showForum(u, updated)
            forumCache[u] = updated
        } else {
            forumPage = updated
        }
    }

    private fun prefetchThumbs(page: ForumPage) {
        val token = ++prefetchToken
        val targets = page.threads
            .filter { it.thumbs.isEmpty() && it.url.contains("thread-") }
            .filter { ThumbStore.get(it.tid).isEmpty() }
            .take(8)
        if (targets.isEmpty()) return
        fun step(i: Int) {
            if (i >= targets.size || token != prefetchToken || !forumSurface()) return
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
                                    val u = loadedForumUrl
                                    if (u != null) {
                                        showForum(u, updated)
                                        forumCache[u] = updated
                                    } else {
                                        forumPage = updated
                                    }
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
        val url = (routes.top as? AppRoute.Thread)?.url ?: navUrl()
        if (url.isBlank()) { toast("当前页面没有可复制的链接"); return }
        runCatching {
            val cm = getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                as android.content.ClipboardManager
            cm.setPrimaryClip(android.content.ClipData.newPlainText("链接", url))
            toast("链接已复制")
        }.onFailure { toast("复制失败：" + (it.message ?: "")) }
    }

    private fun shareThread() {
        val url = (routes.top as? AppRoute.Thread)?.url ?: navUrl()
        val title = (routes.top as? AppRoute.Thread)?.title ?: "GameMale 帖子"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, title + "\n" + url)
        }
        runCatching {
            startActivity(Intent.createChooser(intent, "分享帖子"))
        }.onFailure { toast("没有可分享的应用") }
    }

    private var dailyRunning = false
    private var dailyFailed: Boolean
        get() = homeVm.dailyFailed
        set(value) { homeVm.dailyFailed = value }
    private var dailyRewards: List<String>
        get() = homeVm.dailyRewards
        set(value) { homeVm.dailyRewards = value }

    private fun todayKey(): String =
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

    /**
     * 每日任务结果判定。
     *
     * 签到接口（k_misign）返回的是带提示文字的文本/HTML；
     * 抽卡接口（it618_award:ajax&ac=getaward）返回的是 JSON：{"tipname":"ok", "yes":奖品, ...}，
     * 里面既没有「成功」也没有「恭喜」，按中文关键词判定必然误判为失败。
     *
     * 所以改成：先看 JSON 的 tipname，再按「已经做过 → 明确失败 → 明确成功 → 未知」分级，
     * 未知时按成功处理（请求已经正常返回、服务器没有报错），并把原始返回体截断写进诊断日志，
     * 以后真出问题能直接看到服务器到底回了什么。
     */
    private fun judgeDaily(text: String?, kind: Int): Boolean {
        if (text == null) return false
        val norm = normalizeDaily(text)
        Diag.log("daily", "resp[" + (if (kind == 0) "sign" else "award") + "] " + norm.take(200).replace("\n", " "))
        if (norm.isBlank()) return false

        // 1) JSON（抽卡接口）：只看 tipname
        val tip = Regex("\"tipname\"\\s*:\\s*\"([^\"]*)\"").find(norm)?.groupValues?.get(1)?.lowercase()
        if (tip != null) return tip == "ok"

        // 2) 已经做过 = 成功（"抱歉，您今天已经签到过了" 这类也归到这里，别被"抱歉"误伤）
        val already = listOf("已签到", "已经签到", "今日已签到", "签到排名", "已抽奖", "已经抽奖", "已领取")
        if (already.any { norm.contains(it) }) return true

        // 3) 明确失败
        val fail = listOf(
            "失败", "错误", "未登录", "请先登录", "没有机会", "次数不足", "不能重复",
            "无权", "没有权限", "未开始", "已结束", "Just a moment", "challenge-platform", "cf-chl"
        )
        if (fail.any { norm.contains(it, ignoreCase = true) }) return false

        // 4) 明确成功
        val success = listOf("成功", "恭喜", "获得", "抽到", "奖励", "积分", "排名", "领取", "签到完成")
        if (success.any { norm.contains(it) }) return true

        // 5) 未知：服务器已经正常返回、既没拒绝也没报错，按成功处理（日志里留了原文）
        Diag.log("daily", "ambiguous[" + (if (kind == 0) "sign" else "award") + "] 按成功处理")
        return true
    }

    /** 归一化：\uXXXX 转义、HTML 实体、标签都去掉，免得关键词被编码挡住 */
    private fun normalizeDaily(t: String): String {
        var s = t
        s = Regex("\\\\u([0-9a-fA-F]{4})").replace(s) { m ->
            m.groupValues[1].toIntOrNull(16)?.toChar()?.toString() ?: ""
        }
        s = Regex("&#x([0-9a-fA-F]+);").replace(s) { m ->
            m.groupValues[1].toIntOrNull(16)?.toChar()?.toString() ?: ""
        }
        s = Regex("&#(\\d+);").replace(s) { m ->
            m.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: ""
        }
        s = s.replace("&nbsp;", " ").replace("&amp;", "&")
            .replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
        s = Regex("<[^>]*>").replace(s, " ")
        return s
    }

    /** 每日首次启动：按设置页开关决定执行自动签到 / 自动翻卡抽奖 */
    private fun runDailyTasks() {
        if (!loggedIn || dailyRunning) return
        val sp = getSharedPreferences("gm", MODE_PRIVATE)
        if (sp.getString("dailyTaskDay", "") == todayKey()) {
            dailyFailed = sp.getBoolean("dailyTaskFailed", false)
            return
        }
        val doSign = autoSignEnabled
        val doAward = autoAwardEnabled
        if (!doSign && !doAward) {
            dailyFailed = false
            sp.edit()
                .putString("dailyTaskDay", todayKey())
                .putBoolean("dailyTaskFailed", false)
                .apply()
            return
        }
        dailyRunning = true
        toast(if (doSign) "正在自动签到…" else "正在自动翻卡抽奖…")
        val enabled = booleanArrayOf(doSign, doAward)
        val done = arrayOfNulls<Boolean>(2)
        var signReward: String? = null
        var awardReward: String? = null
        fun finish() {
            if (enabled.indices.any { enabled[it] && done[it] == null }) return
            val msgs = ArrayList<String>()
            if (doSign) msgs.add(
                if (done[0] == true) signReward?.let { "签到奖励：$it" } ?: "已自动签到"
                else "签到失败"
            )
            if (doAward) msgs.add(
                if (done[1] == true) awardReward?.let { "抽卡奖励：$it" } ?: "已自动翻卡抽奖"
                else "翻卡抽奖失败"
            )
            val failed = enabled.indices.any { enabled[it] && done[it] == false }
            dailyFailed = failed
            dailyRewards = msgs
            sp.edit()
                .putString("dailyTaskDay", todayKey())
                .putBoolean("dailyTaskFailed", failed)
                .apply()
            dailyRunning = false
        }
        fun fetchText(url: String, cb: (String?) -> Unit) {
            fetcher.fetch(url) { r ->
                r.onSuccess { html -> cb(html) }.onFailure {
                    Diag.log("daily", "fetch fail " + url + " " + it.message)
                    cb(null)
                }
            }
        }
        val signUrl = SITE + "plugin.php?id=k_misign:sign"
        val awardUrl = SITE + "plugin.php?id=it618_award"

        fun loadAwardReward(formhash: String, after: () -> Unit) {
            fetchText(SITE + "plugin.php?id=it618_award:ajax&page=1&formhash=$formhash&ac=myaward_get") { html ->
                awardReward = DailyReward.latestAward(html)
                if (awardReward == null) Diag.log("daily", "award record not parsed")
                after()
            }
        }

        fun lottery() {
            if (!doAward) {
                finish()
                return
            }
            fetchText(awardUrl) { html ->
                if (html == null) {
                    done[1] = false; finish(); return@fetchText
                }
                val drawn = html.contains("已抽奖") || html.contains("已经抽奖") ||
                    Regex("抽奖\\s*\\d+\\s*次").containsMatchIn(html)
                val fh = Regex("name=\"formhash\" value=\"([^\"]+)\"").find(html)?.groupValues?.get(1)
                if (drawn) {
                    done[1] = true
                    if (fh == null) finish() else loadAwardReward(fh) { finish() }
                } else {
                    toast("正在自动翻卡抽奖…")
                    if (fh == null) {
                        done[1] = false; finish()
                    } else {
                        // 抽卡接口的返回体直接判断成败，不再重载页面（重载容易超时）
                        fetchText(SITE + "plugin.php?id=it618_award:ajax&ac=getaward&formhash=$fh") { r ->
                            val ok = judgeDaily(r, 1)
                            done[1] = ok
                            if (ok) loadAwardReward(fh) { finish() } else finish()
                        }
                    }
                }
            }
        }

        if (!doSign) {
            lottery()
            return
        }

        fetchText(signUrl) { html ->
            if (html == null) {
                done[0] = false
                lottery()
                return@fetchText
            }
            signReward = DailyReward.sign(html)
            if (!html.contains("您今天还没有签到")) {
                done[0] = true
                toast(if (doAward) "今日已签到，正在检查翻卡…" else "今日已签到")
                lottery()
            } else {
                val fh = Regex("name=\"formhash\" value=\"([^\"]+)\"").find(html)?.groupValues?.get(1)
                if (fh == null) {
                    done[0] = false
                    lottery()
                } else {
                    fetchText(SITE + "plugin.php?id=k_misign:sign&operation=qiandao&formhash=$fh&format=empty") { r ->
                        done[0] = judgeDaily(r, 0)
                        if (done[0] == true) toast("签到完成")
                        lottery()
                    }
                }
            }
        }
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun threadTid(): String? =
        Regex("thread-(\\d+)").find((routes.top as? AppRoute.Thread)?.url ?: "" )?.groupValues?.get(1)

    private fun navUrl(): String = when (val n = routes.top) {
        AppRoute.Home -> when (homeTab) {
            0 -> animeUrl
            1 -> gameUrl
            else -> ""
        }
        is AppRoute.Forum -> n.url
        is AppRoute.Thread -> n.url
        AppRoute.Search -> SITE + "search.php?mod=forum"
        else -> ""
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
        persistLoginState()
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
    private var ratePid: String?
        get() = threadVm.ratePid
        set(value) { threadVm.ratePid = value }

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

    /** 附件：已下载的直接打开；付费的先走购买表单；免费的直接下载 */
    private fun openAttach(at: AttachItem) {
        if (!loggedIn) {
            toast("未登录不能下载附件，请先登录")
            return
        }
        val tid = threadTid()?.toLongOrNull() ?: 0L
        val done = if (tid > 0L) AttachmentStore.find(this, tid, at.name) else null
        if (done != null) {
            openAttachment(done)
            return
        }
        val key = attachKey(tid, at.name)
        val st = attachLoads[key]
        if (st != null && !st.failed && st.progress < 1f) return
        proceedAttach(at)
    }

    /** 真正去取购买页 / 下载。需要付费或权限的会先弹窗，免费的直接开始 */
    private fun proceedAttach(at: AttachItem) {
        val tid = threadTid()?.toLongOrNull() ?: 0L
        val threadTitle = threadPage?.subject.orEmpty().ifBlank { (routes.top as? AppRoute.Thread)?.title.orEmpty() }
        val threadUrl = (routes.top as? AppRoute.Thread)?.url.orEmpty()
        if (!at.url.contains("action=attachpay")) {
            // 免费附件 / 已经付过费：站点直接给下载地址，不需要确认
            downloadAttach(attachmentUrl(at.url), at.name, tid, threadTitle, threadUrl)
            return
        }
        // 需要付费或权限：先把购买窗口取回来，确认后才提交 / 下载
        attachPayAt = at
        attachPayName = at.name
        toast("正在检查附件…")
        // 用 HttpURLConnection 直接 GET 购买页：不再排隐藏 WebView 的队，避免整页重载/超时
        Thread {
            val r = runCatching { httpGet(at.url) }
            runOnUiThread {
                r.onSuccess { html ->
                    val pay = Site.parseAttachPay(html, at.url)
                    if (pay == null) {
                        // 站点已经明说「已购买过，无需重复购买」：直接开始下载，别再弹确认或跳网页
                        if (Site.alreadyPurchased(html)) {
                            val aid = Regex("""aid=(\d+)""").find(at.url)?.groupValues?.get(1).orEmpty()
                            val dl = Site.attachmentDownloadUrl(html, at.url)
                                ?: (if (aid.isNotBlank() && lastHtml.isNotBlank())
                                    Site.attachmentDownloadUrlForAid(lastHtml, SITE, aid) else null)
                            attachPayAt = null
                            attachPayName = ""
                            if (dl == null) {
                                toast("没有获取到附件下载地址，请稍后重试")
                                Diag.log("attach", "already purchased but no url tid=" + tid + " name=" + at.name)
                                return@runOnUiThread
                            }
                            Diag.log("attach", "already purchased, direct download tid=" + tid + " name=" + at.name)
                            toast("已购买过，开始下载")
                            downloadAttach(dl, at.name, tid, threadTitle, threadUrl)
                            return@runOnUiThread
                        }
                        // 取不到表单：多半是权限不足 / 金币不足 / 已购买但站点直接拒绝，先看站点怎么说
                        val notice = Site.attachPayNotice(html)
                        if (notice != null) {
                            toast("无法下载：" + notice)
                            Diag.log("attach", "pay notice tid=" + tid + " name=" + at.name + " msg=" + notice)
                            return@runOnUiThread
                        }
                        // 只有权限要求、没有可购买的表单：先让用户确认，再按已满足权限试下载
                        attachConfirm = at
                    } else {
                        // 购买前先比一下余额，不足就直接说清楚，不要等提交后超时
                        val price = pay.price.filter { it.isDigit() }.toLongOrNull()
                        val balance = pay.balance.filter { it.isDigit() }.toLongOrNull()
                        if (price != null && balance != null && balance < price) {
                            toast("金币不足：需要 " + price + "，当前余额 " + balance)
                            Diag.log("attach", "pay short tid=" + tid + " price=" + price + " balance=" + balance)
                            return@runOnUiThread
                        }
                        attachPayInfo = pay.copy(fileName = pay.fileName.ifBlank { at.name })
                    }
                }.onFailure {
                    toast("购买页加载失败，改用浏览器打开")
                    openExternal(at.url)
                }
            }
        }.start()
    }

    /** 从「已购买/开始下载」页里抠出站点给的下载地址（带签名 aid），抠不到就返回 null 走数字 aid */
    private fun extractAttachDownload(html: String, base: String): String? {
        val m = Regex("""[^"'\s<>]*mod=attachment[^"'\s<>]*""").find(html) ?: return null
        val raw = m.value.replace("&amp;", "&")
        return runCatching { URL(URL(base), raw).toString() }.getOrNull()
    }

    /** 带登录 Cookie 的 GET，专供购买页这类不该占用隐藏 WebView 的请求 */
    private fun httpGet(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            instanceFollowRedirects = true
            connectTimeout = 20000
            readTimeout = 25000
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
            )
            setRequestProperty("Referer", SITE)
            CookieManager.getInstance().getCookie(SITE)?.takeIf { it.isNotBlank() }
                ?.let { setRequestProperty("Cookie", it) }
        }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val html = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        Diag.log("attach", "http get pay code=" + code + " len=" + html.length + " url=" + url)
        if (code !in 200..299) throw java.io.IOException("HTTP " + code)
        if (html.isBlank()) throw java.io.IOException("空页面")
        return html
    }

    /** 权限/金币附件的信息确认：用户点确认后才真的去下载 */
    private fun confirmAttachDownload() {
        val at = attachConfirm ?: return
        attachConfirm = null
        val tid = threadTid()?.toLongOrNull() ?: 0L
        val title = threadPage?.subject.orEmpty().ifBlank { (routes.top as? AppRoute.Thread)?.title.orEmpty() }
        val threadUrl = (routes.top as? AppRoute.Thread)?.url.orEmpty()
        downloadAttach(attachmentUrl(at.url), at.name, tid, title, threadUrl)
    }

    /** 确认购买：直接 POST attachpay 表单（不再走 file:// 表单页，那条路会一直超时） */
    private fun confirmAttachBuy() {
        val pay = attachPayInfo ?: return
        if (attachPayBusy) return
        attachPayBusy = true
        val threadUrl = (routes.top as? AppRoute.Thread)?.url.orEmpty().ifBlank { SITE }
        val srcAt = attachPayAt
        val tid = pay.tid.toLongOrNull() ?: threadTid()?.toLongOrNull() ?: 0L
        val title = threadPage?.subject.orEmpty().ifBlank { (routes.top as? AppRoute.Thread)?.title.orEmpty() }
        val pageUrl = (routes.top as? AppRoute.Thread)?.url.orEmpty()
        Thread {
            var ok = false
            var already = false
            var msg: String? = null
            var detail = ""
            var dlOverride: String? = null
            runCatching {
                val fields = listOf(
                    "formhash" to pay.formhash,
                    "referer" to pay.referer.ifBlank { threadUrl },
                    "aid" to pay.aid,
                    "paysubmit" to "true"
                )
                val body = fields.joinToString("&") { (k, v) ->
                    java.net.URLEncoder.encode(k, "UTF-8") + "=" +
                        java.net.URLEncoder.encode(v, "UTF-8")
                }
                val conn = (URL(pay.action).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    instanceFollowRedirects = false
                    connectTimeout = 20000
                    readTimeout = 30000
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    setRequestProperty("Referer", threadUrl)
                    setRequestProperty("Origin", SITE.trimEnd('/'))
                    setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
                    )
                    CookieManager.getInstance().getCookie(SITE)?.takeIf { it.isNotBlank() }
                        ?.let { setRequestProperty("Cookie", it) }
                }
                conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                val code = conn.responseCode
                val loc = conn.getHeaderField("Location")
                if (code in 300..399) {
                    detail = "HTTP $code -> " + loc.orEmpty()
                    val abs = runCatching { URL(URL(pay.action), loc ?: "").toString() }.getOrDefault("")
                    ok = abs.contains("mod=attachment") || abs.contains("attachpay")
                    if (!ok) msg = "站点返回了意外的跳转"
                } else {
                    val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                    val html = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                    detail = "HTTP $code " + html.take(200).replace("\n", " ")
                    val plain = html.replace(Regex("<[^>]*>"), " ")
                    val challenge = plain.contains("Just a moment") || plain.contains("请稍候") ||
                        plain.contains("challenges.cloudflare.com") || plain.contains("cf_chl")
                    // 站点对「已经买过」的附件会回一句：您已购买过此附件，无需重复购买，现在将开始附件下载。
                    // 这不是失败，直接走下载，绝不再退到 WebView 购买页。
                    already = plain.contains("已购买") || plain.contains("无需重复购买") ||
                        plain.contains("开始附件下载")
                    val bad = plain.contains("参数错误") ||
                        plain.contains("余额不足") || plain.contains("权限不足") ||
                        plain.contains("不能购买") || plain.contains("无法购买")
                    ok = !challenge && (already || (!bad &&
                        (plain.contains("购买成功") || plain.contains("成功"))))
                    if (already) dlOverride = extractAttachDownload(html, pay.action)
                    if (!ok) {
                        msg = Site.attachPayNotice(html)
                            ?: if (challenge) "站点验证未通过" else if (code !in 200..299) "HTTP $code" else null
                    }
                }
            }.onFailure {
                msg = when (it) {
                    is java.net.SocketTimeoutException -> "站点响应超时，请稍后重试"
                    is java.net.UnknownHostException -> "网络不可用"
                    else -> it.message ?: it.javaClass.simpleName
                }
            }

            if (ok) {
                // 买成功后立刻关窗，不再让用户盯着购买窗口等取链
                val dlName = pay.fileName.ifBlank { attachPayName }
                runOnUiThread {
                    attachPayBusy = false
                    attachPayInfo = null
                    attachPayAt = null
                    attachPayName = ""
                    toast(if (already) "已购买过，正在获取附件…" else "购买成功，正在获取附件…")
                    Diag.log(
                        "attach",
                        "http buy aid=" + pay.aid + " ok=true already=" + already +
                            " dl=" + dlOverride.orEmpty() + " detail=" + detail
                    )
                }
                // 回包经常不带下载链；先站内找，再等几秒重取购买页（站点购买状态有时要过一会儿才反映出来）
                var dl = dlOverride ?: findSignedAttachUrl(pay, threadUrl)
                if (dl == null) dl = retrySignedAttachUrl(pay, threadUrl)
                val fixed = dl
                runOnUiThread {
                    if (fixed != null) {
                        downloadAttach(fixed, dlName, tid, title, pageUrl)
                    } else {
                        Diag.log("attach", "buy ok but still no download url aid=" + pay.aid)
                        toast("购买成功，附件地址还在生成，请再点一次该附件")
                    }
                }
            } else {
                runOnUiThread {
                    attachPayBusy = false
                    Diag.log(
                        "attach",
                        "http buy aid=" + pay.aid + " ok=false already=" + already +
                            " dl=" + dlOverride.orEmpty() + " detail=" + detail
                    )
                    toast("购买失败：" + (msg ?: "站点拒绝了这次购买"))
                    // 自动支付没成：退回可见的购买页，用户自己点一次也能完成
                    srcAt?.let { openAttachInWeb(it) }
                }
            }
        }.start()
    }

    /**
     * 购买成功后站点偶尔要等一会儿才把附件状态改成「已购买」。
     * 这里隔几秒重取购买页，直到拿到签名下载链；全程后台，不重新弹购买窗。
     */
    private fun retrySignedAttachUrl(pay: AttachPay, threadUrl: String): String? {
        val url = SITE + "forum.php?mod=misc&action=attachpay&aid=" +
            java.net.URLEncoder.encode(pay.aid, "UTF-8") + "&tid=" +
            java.net.URLEncoder.encode(pay.tid, "UTF-8")
        val waits = listOf(0L, 700L, 1800L, 4000L)
        for (wait in waits) {
            if (wait > 0) runCatching { Thread.sleep(wait) }
            val html = runCatching { httpGet(url) }.getOrNull() ?: continue
            Site.attachmentDownloadUrlForAid(html, SITE, pay.aid)?.let { return it }
            if (Site.alreadyPurchased(html)) {
                Site.attachmentDownloadUrl(html, url)?.let { return it }
            }
        }
        if (threadUrl.isNotBlank()) {
            runCatching { httpGet(threadUrl) }.getOrNull()?.let { page ->
                Site.attachmentDownloadUrlForAid(page, SITE, pay.aid)?.let { return it }
            }
        }
        return null
    }

    /**
     * 站点购买成功后，用当前帖子页 / 购买页重新找带签名的附件下载链。
     * 找不到再重新取一次帖子页；都找不到就返回 null，由调用方提示，不再退回数字 aid。
     */
    private fun findSignedAttachUrl(pay: AttachPay, threadUrl: String): String? {
        val tid = pay.tid
        val pages = ArrayList<String>()
        val curThread = routes.top as? AppRoute.Thread
        if (lastHtml.isNotBlank() && curThread != null &&
            tidOf(curThread.url).orEmpty() == tid
        ) pages.add(lastHtml)
        runCatching {
            val aid = java.net.URLEncoder.encode(pay.aid, "UTF-8")
            val t = java.net.URLEncoder.encode(tid, "UTF-8")
            httpGet(SITE + "forum.php?mod=misc&action=attachpay&aid=" + aid + "&tid=" + t)
        }.getOrNull()?.let { pages.add(it) }
        for (page in pages) {
            Site.attachmentDownloadUrlForAid(page, SITE, pay.aid)?.let { return it }
        }
        if (threadUrl.isNotBlank()) {
            runCatching { httpGet(threadUrl) }.getOrNull()?.let { page ->
                Site.attachmentDownloadUrlForAid(page, SITE, pay.aid)?.let { return it }
            }
        }
        return null
    }

    private fun closeAttachBuy() {
        attachPayInfo = null
        attachPayAt = null
        attachPayName = ""
    }

    /** 内置下载：落到 filesDir/attachments/<tid>/，带 Cookie，边下边推进度 */
    private fun downloadAttach(url: String, name: String, tid: Long, title: String, threadUrl: String) {
        val key = attachKey(tid, name)
        synchronized(attachLoads) {
            val st = attachLoads[key]
            if (st != null && st.progress < 1f && !st.failed) return
        }
        attachLoads = attachLoads + (key to AttachLoad(0f, false, false))
        Diag.log("attach", "download start tid=" + tid + " name=" + name + " url=" + url)
        Thread {
            var dest: File? = null
            var failure: String? = null
            runCatching {
                val safe = name.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim().take(120)
                    .ifBlank { "attachment" }
                val dir = AttachmentStore.dirFor(this, tid)
                val tmp = File(dir, "$safe.part")
                val out = File(dir, safe)
                var current = URL(url)
                var finished = false
                var hops = 0
                while (!finished && hops < 6) {
                    hops++
                    val conn = (current.openConnection() as HttpURLConnection).apply {
                        instanceFollowRedirects = false
                        connectTimeout = 20000
                        readTimeout = 45000
                        setRequestProperty("Referer", SITE)
                        setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                                "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
                        )
                        CookieManager.getInstance().getCookie(SITE)?.takeIf { it.isNotBlank() }
                            ?.let { setRequestProperty("Cookie", it) }
                    }
                    val code = conn.responseCode
                    if (code in 300..399) {
                        val loc = conn.getHeaderField("Location") ?: throw java.io.IOException("跳转地址缺失")
                        current = URL(current, loc)
                        continue
                    }
                    if (code !in 200..299) throw java.io.IOException("HTTP $code")
                    val ctype = conn.contentType.orEmpty()
                    // 附件下载不能以扩展名放宽：.txt 也必须拒绝 text/html，
                    // 否则「提示信息」网页会被当成 txt 附件存下来
                    if (ctype.contains("text/html", true)) {
                        throw java.io.IOException("拿到的是网页而不是文件，下载地址可能已失效")
                    }
                    val total = conn.contentLengthLong
                    conn.inputStream.use { input ->
                        FileOutputStream(tmp).use { fos ->
                            val buf = ByteArray(8192)
                            var read = 0L
                            var lastRpt = 0L
                            while (true) {
                                val n = input.read(buf)
                                if (n <= 0) break
                                fos.write(buf, 0, n)
                                read += n
                                if (total > 0 && System.currentTimeMillis() - lastRpt > 180) {
                                    lastRpt = System.currentTimeMillis()
                                    val p = (read.toFloat() / total).coerceIn(0f, 0.99f)
                                    runOnUiThread { attachLoads = attachLoads + (key to AttachLoad(p, false, false)) }
                                }
                            }
                        }
                    }
                    if (out.exists()) out.delete()
                    // 双保险：有些错误页的 Content-Type 不规范，再看一眼前 512 字节
                    val head = ByteArray(512)
                    val hn = runCatching { java.io.FileInputStream(tmp).use { it.read(head) } }.getOrDefault(0)
                    val headText = if (hn > 0) String(head, 0, hn, Charsets.UTF_8)
                        .trimStart('\uFEFF', ' ', '\n', '\r', '\t').lowercase() else ""
                    if (headText.startsWith("<!doctype html") || headText.startsWith("<html")) {
                        tmp.delete()
                        throw java.io.IOException("站点返回的是网页而不是附件")
                    }
                    if (!tmp.renameTo(out)) {
                        tmp.copyTo(out, overwrite = true)
                        tmp.delete()
                    }
                    dest = out
                    finished = true
                }
                if (dest == null) throw java.io.IOException("未拿到文件")
            }.onFailure { failure = it.message ?: it.javaClass.simpleName }

            val file = dest
            if (file != null && file.exists() && file.length() > 0) {
                runOnUiThread {
                    val entry = AttachmentStore.add(this, tid, title, threadUrl, name, file)
                    attachments = AttachmentStore.load(this)
                    attachLoads = attachLoads + (key to AttachLoad(1f, true, false))
                    if (AttachmentStore.kindOf(name) == "txt") {
                        val text = runCatching { AttachmentStore.readText(file) }.getOrNull()
                        if (text != null) attachTexts = attachTexts + (key to text)
                    }
                    val onThisThread = (routes.top as? AppRoute.Thread)?.url == threadUrl
                    if (onThisThread) attachFocus = key
                    toast(if (AttachmentStore.kindOf(name) == "txt") "下载完成，已读取正文" else "下载完成：" + name)
                    Diag.log("attach", "download ok tid=" + tid + " name=" + name + " size=" + entry.size)
                }
            } else {
                runOnUiThread {
                    attachLoads = attachLoads + (key to AttachLoad(0f, false, true))
                    toast("附件下载失败：" + (failure ?: "未知错误"))
                    Diag.log("attach", "download fail tid=" + tid + " name=" + name + " err=" + failure)
                }
            }
        }.start()
    }

    /** 列表/详情点已下载的附件：txt 进文本窗口，其他交给系统应用 */
    private fun openAttachment(e: AttachmentStore.Entry) {
        if (!e.exists) {
            toast("文件已不存在，可能被清理掉了")
            attachments = AttachmentStore.load(this)
            return
        }
        if (AttachmentStore.kindOf(e.name) == "txt") {
            val text = runCatching { AttachmentStore.readText(e.file) }.getOrElse {
                toast("读取失败：" + (it.message ?: "无法解码"))
                return
            }
            fileTextTitle = e.name
            fileTextBody = text
            fileTextOpen = true
        } else openAttachmentExternally(e)
    }

    private fun openAttachmentExternally(e: AttachmentStore.Entry) {
        runCatching {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this, packageName + ".fileprovider", e.file
            )
            val intent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, AttachmentStore.mimeOf(e.name))
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        }.onFailure {
            toast("没有能打开这个文件的应用")
        }
    }

    private fun showAttachmentText(title: String, text: String) {
        fileTextTitle = title
        fileTextBody = text
        fileTextOpen = true
    }

    private fun copyText(text: String) {
        runCatching {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            cm.setPrimaryClip(android.content.ClipData.newPlainText("附件正文", text))
            toast("已复制全文")
        }.onFailure { toast("复制失败") }
    }

    /** 详情页需要内联展示 txt 时按需读一次，读到的放缓存 */
    private fun ensureAttachText(key: String) {
        if (attachTexts.containsKey(key)) return
        val sep = key.lastIndexOf('|')
        if (sep < 0) return
        val tid = key.substring(0, sep).toLongOrNull() ?: return
        val name = key.substring(sep + 1)
        Thread {
            val e = AttachmentStore.find(this, tid, name) ?: return@Thread
            val text = runCatching { AttachmentStore.readText(e.file) }.getOrNull() ?: return@Thread
            runOnUiThread { attachTexts = attachTexts + (key to text) }
        }.start()
    }

    private fun openAttachManager() {
        attachments = AttachmentStore.load(this)
        if (routes.top is AppRoute.Attachments) return
        routes.push(AppRoute.Attachments)
        error = null
        loading = false
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
                    downloadAttach(
                        u,
                        at.name,
                        threadTid()?.toLongOrNull() ?: 0L,
                        threadPage?.subject.orEmpty().ifBlank { (routes.top as? AppRoute.Thread)?.title.orEmpty() },
                        (routes.top as? AppRoute.Thread)?.url.orEmpty()
                    )
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
            persistLoginState()
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
            persistLoginState()
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
