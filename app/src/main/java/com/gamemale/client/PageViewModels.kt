package com.gamemale.client

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * 首页：横向 Pager 的四个页（动漫、游戏、收藏、浏览记录）。
 * 拉列表、写缓存仍由 Activity 做，因为一次响应可能改导航或改成帖子。
 */
class HomeViewModel : ViewModel() {
    /** 0 动漫，1 游戏，2 收藏，3 浏览记录。Pager 停稳后才改。 */
    var homeTab: Int = 0

    /** 需要把首页 Pager 滚到这一页时写入；界面只在序号变化时滚。 */
    var homeJump by mutableStateOf(-1)
    var homeJumpGen by mutableStateOf(0)

    var animePage by mutableStateOf<ForumPage?>(null)
    var gamePage by mutableStateOf<ForumPage?>(null)
    var animeUrl by mutableStateOf(ANIME_URL)
    var gameUrl by mutableStateOf(GAME_URL)

    /** 冷启动后还没进过「和谐游戏」：第一次切过去要联网刷新。 */
    var gameFirstSwitchPending: Boolean = true

    var history by mutableStateOf<List<HistoryStore.Entry>>(emptyList())
    var historySelectionCount by mutableStateOf(0)
    var historySelecting by mutableStateOf(false)

    var watchLater by mutableStateOf<List<WatchStore.Entry>>(emptyList())
    var watchSelectionCount by mutableStateOf(0)
    var watchSelecting by mutableStateOf(false)

    var readTids by mutableStateOf<Set<Long>>(emptySet())
    var dailyFailed by mutableStateOf(false)

    var homeWarmed: Boolean = false
    var homeAlt: Boolean = false

    fun requestTab(index: Int) {
        homeTab = index
        homeJump = index
        homeJumpGen++
    }

    /**
     * 只有回到首页顶层、并且地址对得上这个板块时，才把列表写回对应 tab。
     * 搜索结果和已经压进来的版块页不写首页。
     */
    fun absorbBoard(onHome: Boolean, url: String, page: ForumPage?) {
        if (!onHome || page == null || url.contains("search.php")) return
        if (homeTab == 0 && url.contains("fid=150")) {
            animePage = page
            animeUrl = url
        } else if (homeTab == 1 && url.contains("fid=152")) {
            gamePage = page
            gameUrl = url
            gameFirstSwitchPending = false
        }
    }

    fun boardOf(url: String): ForumPage? = when {
        url.contains("fid=150") -> animePage
        url.contains("fid=152") -> gamePage
        else -> null
    }
}

/** 压栈进来的版块页，以及列表这一侧的刷新/登录门槛。 */
class ForumViewModel : ViewModel() {
    var forums by mutableStateOf<List<ForumItem>>(emptyList())
    var forumPage by mutableStateOf<ForumPage?>(null)
    var refreshing by mutableStateOf(false)
    var searchUpdating by mutableStateOf(false)
    var needUpgrade by mutableStateOf(false)
    var needLogin by mutableStateOf(false)

    var loadedForumUrl: String? = null
    var loadedForumTitle: String = ""
}

/** 帖子页：正文、回复、评分、看图。WebView 仍留在 Activity。 */
class ThreadViewModel : ViewModel() {
    var threadPage by mutableStateOf<ThreadPage?>(null)
    var replyOpen by mutableStateOf(false)
    var rateOpen by mutableStateOf(false)
    var rateFields by mutableStateOf<List<Triple<String, String, String>>>(emptyList())
    var imageUrl by mutableStateOf<String?>(null)
    var openThreadTid by mutableStateOf<String?>(null)

    var loadedThreadUrl: String? = null
    var rateFormDef: FormDef? = null
    var rateReasonName: String? = null
    var rateSourceHtml: String? = null
    var ratePid: String? = null
}

/** 搜索页自己的历史。结果列表写在 ForumViewModel。 */
class SearchViewModel : ViewModel() {
    var searchOpen by mutableStateOf(false)
    var searchHistory by mutableStateOf<List<String>>(emptyList())
}

/** 设置页：主题、字号、缓存额度、屏蔽词、更新说明。 */
class SettingsViewModel : ViewModel() {
    var darkMode by mutableStateOf(false)
    /** 0 跟随系统 / 1 深色 / 2 浅色 */
    var themeMode by mutableStateOf(0)
    var fontScale by mutableStateOf(1f)
    var blurBg by mutableStateOf(true)
    var cacheMb by mutableStateOf(300)
    var cacheBytes by mutableStateOf(0L)
    var cacheCount by mutableStateOf(0)
    var blockedWords by mutableStateOf<List<String>>(emptyList())
    var watchWords by mutableStateOf<List<String>>(emptyList())
    var contentPrefOpen by mutableStateOf(false)
    var contentPrefKind by mutableStateOf(0)
    var changelogOpen by mutableStateOf(false)
    var upgradeReleases by mutableStateOf<List<Changelog.Release>>(emptyList())
    var firstRunNotice by mutableStateOf(false)
}

/** 附件管理页，以及帖子里的下载进度、购买和文本预览。 */
class AttachmentViewModel : ViewModel() {
    var attachments by mutableStateOf<List<AttachmentStore.Entry>>(emptyList())
    var attachSelecting by mutableStateOf(false)
    var attachSelectionCount by mutableStateOf(0)
    var attachLoads by mutableStateOf<Map<String, AttachLoad>>(emptyMap())
    var attachTexts by mutableStateOf<Map<String, String>>(emptyMap())
    var attachFocus by mutableStateOf<String?>(null)
    var fileTextOpen by mutableStateOf(false)
    var fileTextTitle by mutableStateOf("")
    var fileTextBody by mutableStateOf("")
    var attachConfirm by mutableStateOf<AttachItem?>(null)
    var attachPayAt by mutableStateOf<AttachItem?>(null)
    var attachPayInfo by mutableStateOf<AttachPay?>(null)
    var attachPayName by mutableStateOf("")
    var attachPayBusy by mutableStateOf(false)
}

/** 账号、登录和验证。WebView 实例留在 Activity，避免跟页面一起泄漏。 */
class AccountViewModel : ViewModel() {
    var loginName by mutableStateOf<String?>(null)
    var loggedIn by mutableStateOf(false)
    var avatarUrl by mutableStateOf<String?>(null)
    var accounts by mutableStateOf<List<Accounts.Acc>>(emptyList())
    var currentAccountId by mutableStateOf<String?>(null)
    var accountsOpen by mutableStateOf(false)
    var pendingCookie: String? = null
    var addMode: Boolean = false
    var loginPage by mutableStateOf(false)
    var loginPrompted: Boolean = false
    var verifyPage by mutableStateOf(false)
    var verifyTarget: String? = null
}
