package com.gamemale.client

/**
 * 浏览记录标题的清洗规则。
 *
 * 帖子正文还没解析出来时，导航标题只有「帖子」；WebView 遇到网络错误时，
 * 页面标题会被 Chrome 的错误页标题污染成「网页无法打开」。这两种标题都不该
 * 写进浏览记录，否则会顶掉真实标题，重启后也一直留着。
 */
object HistoryTitle {
    private val placeholders = setOf(
        "帖子",
        "网页无法打开",
        "无法访问此网站",
        "无法访问",
        "网络连接失败",
        "请稍候",
        "请稍候...",
        "just a moment",
        "just a moment...",
        "attention required! | cloudflare"
    )

    fun clean(raw: String?): String =
        raw.orEmpty().trim().replace(Regex("\\s+"), " ")

    fun usable(raw: String?): Boolean {
        val title = clean(raw)
        if (title.isEmpty()) return false
        val lower = title.lowercase()
        if (lower in placeholders) return false
        if (lower.startsWith("err_")) return false
        return true
    }

    /** 新标题可用就用新标题；否则退回旧条目里可用的标题；都不可用就留空。 */
    fun resolve(incoming: String?, old: String?): String =
        clean(incoming).takeIf(::usable) ?: clean(old).takeIf(::usable).orEmpty()
}
