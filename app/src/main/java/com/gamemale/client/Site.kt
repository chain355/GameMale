package com.gamemale.client

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

const val SITE = "https://www.gamemale.com/"

data class ForumItem(val name: String, val url: String, val icon: String?, val desc: String)

data class ThreadItem(
    val tid: String,
    val title: String,
    val url: String,
    val typeName: String?,
    val author: String,
    val date: String,
    val replies: String,
    val views: String,
    val sticky: Boolean,
    val thumbs: List<String> = emptyList()
)

data class CatTab(val label: String, val url: String)

data class ForumPage(
    val title: String,
    val cats: List<CatTab>,
    val threads: List<ThreadItem>,
    val pages: List<Pair<Int, String>>,
    val pageNo: Int,
    val pageCount: Int
)

data class AttachItem(val name: String, val url: String, val meta: String, val price: String?)

/**
 * 购买附件窗口（forum.php?mod=misc&action=attachpay&aid=..&tid=..）解析出来的信息。
 * 表单字段：formhash / referer / aid / paysubmit，提交目标带 tid。
 */
data class AttachPay(
    val action: String,
    val aid: String,
    val tid: String,
    val formhash: String,
    val referer: String,
    val fileName: String,
    val price: String,
    val balance: String
)

data class RatingSummary(val people: Int, val parts: List<String>)

sealed interface Block {
    data class Text(val text: String) : Block
    data class Image(val url: String) : Block
    /** 正文里的内联超链接（点一下直接调浏览器） */
    data class Link(val text: String, val url: String) : Block
    /** [spoiler] 折叠按钮块 */
    data class Spoiler(val title: String, val blocks: List<Block>) : Block
    data object Break : Block
}

data class PostItem(
    val pid: String,
    val author: String,
    val avatar: String?,
    val time: String,
    val floor: String,
    val blocks: List<Block>,
    val attachments: List<AttachItem>,
    val rating: RatingSummary?,
    val isOp: Boolean,
    val links: List<Pair<String, String>> = emptyList()
)

data class ThreadPage(
    val subject: String,
    val typeName: String?,
    val posts: List<PostItem>,
    val pages: List<Pair<Int, String>>,
    val pageNo: Int,
    val pageCount: Int,
    val forumUrl: String?,
    val lastEdit: String = ""
)

data class FormDef(var action: String, val fields: MutableList<Pair<String, String>>) {
    fun set(name: String, value: String) {
        val i = fields.indexOfFirst { it.first == name }
        if (i >= 0) fields[i] = name to value else fields.add(name to value)
    }
    fun get(name: String): String? = fields.firstOrNull { it.first == name }?.second
    fun body(): String = fields.joinToString("&") { (k, v) ->
        java.net.URLEncoder.encode(k, "UTF-8") + "=" + java.net.URLEncoder.encode(v, "UTF-8")
    }
}

object Site {

    private val ATTACH_URL = Regex("""[^"'\s<>]{0,220}mod=attachment[^"'\s<>]*""")

    /** 解析购买附件窗口；页面不是购买窗口时返回 null */
    fun parseAttachPay(html: String, baseUrl: String = SITE): AttachPay? = runCatching {
        val doc = Jsoup.parse(html, baseUrl)
        val form = doc.selectFirst("#attachpayform")
            ?: doc.selectFirst("form[action*=attachpay]")
            ?: return null
        val action = form.absUrl("action").ifBlank { SITE + form.attr("action") }
        val formhash = form.selectFirst("input[name=formhash]")?.attr("value").orEmpty()
        val aid = form.selectFirst("input[name=aid]")?.attr("value").orEmpty()
        val referer = form.selectFirst("input[name=referer]")?.attr("value").orEmpty()
        val tid = Regex("tid=(\\d+)").find(action)?.groupValues?.get(1).orEmpty()
        if (action.isBlank() || aid.isBlank() || formhash.isBlank()) return null
        var fileName = ""
        var price = ""
        var balance = ""
        for (tr in form.select("tr")) {
            val cells = tr.select("td")
            if (cells.size < 2) continue
            val k = cells[0].text().trim()
            val v = cells[1].text().trim()
            when {
                k.contains("附件") -> fileName = v
                k.contains("售价") -> price = v
                k.contains("余额") -> balance = v
            }
        }
        AttachPay(action, aid, tid, formhash, referer, fileName, price, balance)
    }.getOrNull()

    /** 站点在购买页/购买回包里说「已购买过此附件」：这种情况不用再走网页，直接下载 */
    fun alreadyPurchased(html: String): Boolean {
        val plain = runCatching { Jsoup.parse(html).text() }.getOrElse { html }
        return plain.contains("已购买过") || plain.contains("无需重复购买") ||
            plain.contains("已经购买") || plain.contains("已购买此附件")
    }

    /** 从购买页/回包里找出所有附件下载地址（带签名 aid），包括 JS 里拼接的地址 */
    fun attachmentDownloadUrls(html: String, base: String = SITE): List<String> = runCatching {
        val doc = Jsoup.parse(html, base)
        val out = ArrayList<String>()
        doc.select("[href*=mod=attachment]").forEach { el ->
            val href = el.attr("href").replace("&amp;", "&")
            if (href.isNotBlank()) {
                val abs = el.absUrl("href").ifBlank {
                    runCatching { java.net.URL(java.net.URL(base), href).toString() }.getOrDefault(href)
                }
                out.add(abs.replace("&amp;", "&"))
            }
        }
        // 有些提示页只在脚本里写地址，Jsoup 的选择器抓不到，再扫一遍原文
        ATTACH_URL.findAll(html).forEach { m ->
            val raw = m.value.replace("&amp;", "&").trim('"', '\'', ',', ')', ';')
            if (raw.isNotBlank()) {
                val abs = runCatching { java.net.URL(java.net.URL(base), raw).toString() }
                    .getOrDefault(raw)
                out.add(abs.replace("&amp;", "&"))
            }
        }
        out.distinct()
    }.getOrDefault(emptyList())

    /** 从购买页/回包里找出站点直接给的附件下载地址（带签名 aid） */
    fun attachmentDownloadUrl(html: String, base: String = SITE): String? =
        attachmentDownloadUrls(html, base).firstOrNull()

    /**
     * 按数字 aid 找到同一个附件的签名下载链。
     * 站点的签名 aid 形如 base64("698248|signkey|expire|uid|tid")，纯数字的直链会被拒。
     */
    fun attachmentDownloadUrlForAid(html: String, base: String = SITE, aid: String): String? {
        val want = aid.trim()
        if (want.isBlank()) return null
        val signed = findSignedAid(html, want) ?: return null
        val url = runCatching {
            java.net.URL(java.net.URL(base), "forum.php?mod=attachment&aid=" + signed).toString()
        }.getOrNull() ?: return null
        return url.replace("&amp;", "&")
    }

    /**
     * 在帖子页/购买页里找同一个附件的签名 aid（页面里可能出现在附件链接或购买链接上）。
     * 只认 base64 解码后以「数字aid|」开头的签名串，绝不接受纯数字 aid。
     */
    private fun findSignedAid(html: String, want: String): String? {
        val matches = Regex("""(?:aid=|aid%3D)([A-Za-z0-9%_+\-/=]+)""").findAll(html)
        for (m in matches) {
            val raw = m.groupValues.getOrNull(1) ?: continue
            val decoded = runCatching { java.net.URLDecoder.decode(raw, "UTF-8") }.getOrDefault(raw)
            if (!decoded.contains('|')) continue
            val payload = decoded.substringBefore('|')
            val plain = runCatching {
                String(android.util.Base64.decode(payload, android.util.Base64.DEFAULT), Charsets.UTF_8)
            }.getOrNull() ?: continue
            if (plain.startsWith(want + "|")) return raw
        }
        return null
    }

    /** 帖子被删除 / 审核中的站点提示；命中后不应重试，也不该写浏览记录 */
    fun threadMissing(html: String): Boolean {
        val plain = runCatching { Jsoup.parse(html).text() }.getOrElse { html }
        return plain.contains("指定的主题不存在") || plain.contains("主题不存在") ||
            plain.contains("已被删除") || plain.contains("正在被审核") ||
            plain.contains("主题已删除")
    }

    /**
     * 购买页/购买返回页上站点给的提示语（权限不足、金币不足、不能购买自己附件等）。
     * 取第一个 alert_error / alert_info 或含关键词的短句，给用户看人话，而不是「请求超时」。
     */
    fun attachPayNotice(html: String): String? = runCatching {
        val doc = Jsoup.parse(html)
        val boxes = doc.select(".alert_error, .alert_info, #messagetext, .boxmessage")
        for (b in boxes) {
            val t = b.text().trim()
            if (t.length in 2..80) return@runCatching t
        }
        val plain = doc.text()
        val keys = listOf("权限不足", "没有权限", "金币不足", "余额不足", "积分不足", "不能购买", "无法购买", "已购买")
        for (k in keys) {
            val i = plain.indexOf(k)
            if (i >= 0) {
                val head = plain.substring(maxOf(0, i - 12), minOf(plain.length, i + 28)).trim()
                return@runCatching head
            }
        }
        null
    }.getOrNull()

    /** Discuz 的 ajax 弹窗内容常被包在 <root><![CDATA[...]]></root> 里，先剥出来再当 HTML 解析 */
    fun unwrapCdata(raw: String): String {
        val i = raw.indexOf("<![CDATA[")
        if (i < 0) return raw
        val j = raw.indexOf("]]>", i)
        if (j < 0) return raw
        return raw.substring(i + 9, j)
    }

    /** 依次尝试多个选择器找表单 */
    fun parseFormAny(html: String, selectors: List<String>): FormDef? {
        val unwrapped = unwrapCdata(html)
        for (sel in selectors) {
            parseForm(unwrapped, sel)?.let { return it }
        }
        return null
    }


    /** 首页：版块列表（`.fl_tb` 里每个 `td.fl_g` 是一个版块） */
    fun parseForums(html: String): List<ForumItem> {
        val doc = Jsoup.parse(html, SITE)
        val out = ArrayList<ForumItem>()
        // 登录态/游客态的首页结构略有差异，多套选择器都试一遍
        val cells = doc.select("#ct td.fl_g, #ct td.fl_g_g, #ct .fl_g, dl.fl_g")
        for (td in cells) {
            val a = td.selectFirst("dl dt a") ?: td.selectFirst("dl a") ?: continue
            val name = a.text().trim().replace(Regex("\\s+"), " ")
            if (name.isEmpty()) continue
            val href = a.absUrl("href").ifEmpty { SITE }
            val icon = td.selectFirst("img")?.absUrl("src")
            val desc = td.selectFirst("dl dd")?.text()?.trim()?.replace(Regex("\\s+"), " ")
                ?.take(70) ?: ""
            out.add(ForumItem(name, href, icon, desc))
        }
        return out
    }

    /** 主题行里的附图预览（插件用 img.wmsjlazy + data-src，最多取 9 张） */
    private fun collectThumbs(tr: Element): List<String> {
        val out = ArrayList<String>()
        for (img in tr.select("img")) {
            val u = listOf("data-src", "zoomfile", "file", "data-original", "src")
                .map { img.absUrl(it) }
                .firstOrNull { it.startsWith("http") } ?: continue
            if (u.contains("static/image") || u.contains("none.gif") || u.contains("smiley")) continue
            if (u.contains("source/plugin")) continue
            if (out.add(u) && out.size >= 9) break
        }
        return out
    }

    /** 版块页：标题、主题分类、主题列表、分页 */
    /** 去掉标题里附带的"今日/主题/排名"等站点统计，以及 - 站点名 后缀 */
    private fun cleanForumTitle(raw: String?): String {
        var t = (raw ?: "").trim()
        for (mark in listOf(" 今日:", "今日:", " |主题", "|主题", " 排名", " -  Powered")) {
            val i = t.indexOf(mark)
            if (i > 0) t = t.substring(0, i)
        }
        return t.trim()
    }

    fun parseForumPage(html: String): ForumPage {
        val doc = Jsoup.parse(html, SITE)
        // 版块页标题后面会挂站点统计（"和谐动漫 今日: 556 |主题: 24173|排名: 1"），
        // 翻页后这些数字也会变，这里只取版块名本身
        val title = cleanForumTitle(
            doc.selectFirst("#pt .bm_h h1")?.text()
                ?: doc.selectFirst(".bm_h h1")?.text()
                ?: doc.title()
        )

        val cats = ArrayList<CatTab>()
        val seenCat = HashSet<String>()
        for (a in doc.select("#thread_types li a")) {
            val label = a.text().replace(Regex("\\s+"), "")
            if (label.isEmpty()) continue
            val href = a.absUrl("href")
            if (href.isBlank() || !href.startsWith("http")) continue
            if (!seenCat.add(href)) continue
            cats.add(CatTab(label, href))
        }

        val threads = ArrayList<ThreadItem>()
        // 公告（非 normalthread_ 的 tbody）
        for (tb in doc.select("#threadlist tbody:not([id^=normalthread_])")) {
            val tr = tb.selectFirst("tr") ?: continue
            val a = tr.selectFirst("strong a") ?: continue
            val tid = Regex("thread-(\\d+)").find(a.absUrl("href"))?.groupValues?.get(1) ?: ""
            threads.add(
                ThreadItem(
                    tid = tid,
                    title = "【公告】" + a.text().trim(),
                    url = a.absUrl("href"),
                    typeName = "公告",
                    author = tr.selectFirst("td.by cite a")?.text() ?: "",
                    date = tr.selectFirst("td.by em")?.text() ?: "",
                    replies = "",
                    views = "",
                    sticky = true
                )
            )
        }
        for (tb in doc.select("#threadlist tbody[id^=normalthread_]")) {
            val tr = tb.selectFirst("tr") ?: continue
            val a = tr.selectFirst("a.s.xst") ?: tr.selectFirst("th a[href*=thread-]") ?: continue
            val tid = tb.attr("id").removePrefix("normalthread_")
            val typeName = tr.selectFirst("th em a")?.text()?.trim()
            val author = tr.selectFirst("td.by cite a")?.text() ?: ""
            val date = tr.selectFirst("td.by em")?.text() ?: ""
            val num = tr.selectFirst("td.num")?.text()?.trim() ?: ""
            val parts = num.split("/")
            threads.add(
                ThreadItem(
                    tid = tid,
                    title = a.text().trim(),
                    url = a.absUrl("href"),
                    typeName = typeName,
                    author = author,
                    date = date,
                    replies = parts.getOrNull(0)?.trim() ?: "",
                    views = parts.getOrNull(1)?.trim() ?: "",
                    sticky = false,
                    thumbs = collectThumbs(tr)
                )
            )
        }

        val (pages, pageNo, pageCount) = pagination(doc)
        return ForumPage(title, cats, threads, pages, pageNo, pageCount)
    }

    /**
     * 把纯文本里的 http(s) 链接拆成可点击的 Block.Link。
     * 很多楼主只贴了网址字符串、没做超链接，网页上也是纯文本，这里替它补上。
     */
    private fun linkifyBlocks(input: List<Block>): List<Block> {
        val out = ArrayList<Block>()
        for (b in input) {
            if (b !is Block.Text) { out.add(b); continue }
            var idx = 0
            for (m in URL_RE.findAll(b.text)) {
                var url = m.value.trimEnd('.', ',', ')', ']', '。', '，', '、', '；')
                if (url.length < 12) continue
                if (m.range.first > idx) out.add(Block.Text(b.text.substring(idx, m.range.first)))
                val href = if (url.startsWith("http")) url else "https://" + url
                out.add(Block.Link(url, href))
                idx = m.range.first + url.length
            }
            if (idx == 0) out.add(b)
            else if (idx < b.text.length) out.add(Block.Text(b.text.substring(idx)))
        }
        return out
    }

    /** http(s) 链接，以及"只写了域名、没做超链接"的裸网址（xxx.com/yyy、www.xxx.net） */
    private val URL_RE = Regex(
        "(?i)(?:https?://|www\\.)[^\\s<>\"'（）()【】，。、；]+|" +
        "(?:[a-z0-9][a-z0-9_-]*\\.)+(?:com|net|org|io|me|cc|tv|cn|jp|us|uk|de|fr|ru|xyz|top|link|app|dev|gg|info|biz|kr|tw|hk|co)" +
        "(?:/[^\\s<>\"'（）()【】，。、；]*)?"
    )

    /** 帖子页：标题、楼层、附件、评分、分页 */
    fun parseThread(html: String): ThreadPage {
        val doc = Jsoup.parse(html, SITE)
        val subject = doc.selectFirst("#thread_subject")?.text()?.trim() ?: doc.title()
        val typeName = doc.selectFirst("h1.ts a")?.text()?.trim()?.removeSurrounding("[", "]")
        val forumUrl = doc.selectFirst("#pt a[href*=forumdisplay], #pt a[href*=forum-]")?.absUrl("href")

        val posts = ArrayList<PostItem>()
        // 正常帖子是 table.plhin；个别页面（比如带 highlight 参数、或某些模板）会退化成别的 table/div
        var postTables = doc.select("#postlist table.plhin")
        if (postTables.isEmpty()) postTables = doc.select("#postlist table:has(td.plc)")
        if (postTables.isEmpty()) postTables = doc.select("table.plhin")
        for (table in postTables) {
            val plc = table.selectFirst("td.plc") ?: continue
            val pi = plc.selectFirst(".pi")
            val authi = pi?.selectFirst(".authi")
            val author = authi?.selectFirst("a.xi2")?.text()?.trim()
                ?: table.selectFirst("td.pls a.xi2")?.text()?.trim()
                ?: "未知"
            val time = authi?.selectFirst("em")?.text()?.replace("发表于", "")?.trim() ?: ""
            val floor = pi?.selectFirst("strong")?.text()?.trim() ?: ""
            val avatarEl = table.selectFirst("td.pls img[src*=avatar]") ?: authi?.selectFirst("img[src*=avatar]")
            val avatar = avatarEl?.absUrl("src")?.takeIf { it.startsWith("http") }
            val content = plc.selectFirst("td.t_f") ?: plc.nextElementSibling()?.selectFirst("td.t_f")
            val blocks = ArrayList<Block>()
            if (content != null) collectBlocks(content, blocks)

            val attachments = ArrayList<AttachItem>()
            // 正文内插入的附件（[attach] 标签）：普通文件 + 付费附件（action=attachpay）
            for (a in plc.select("a[href*=mod=attachment], a[href*=action=attachpay]")) {
                // 图片附件（[attach]img[/attach]）不当作可下载文件，也不显示附件说明
                if (a.selectFirst("img") != null) continue
                // 悬浮提示里的“下载附件/保存到相册”不是附件本体
                if (a.closest(".tip") != null) continue
                val name = a.text().trim()
                if (name.isEmpty() || name.length < 2) continue
                if (name in listOf("下载附件", "保存到相册", "购买", "记录")) continue
                val url = a.absUrl("href")
                if (url.isBlank() || attachments.any { it.url == url }) continue
                // 大小 / 下载次数 / 售价
                val em = a.parent()?.selectFirst("em.xg1") ?: a.nextElementSibling() as? Element
                val raw = em?.text()?.trim()?.removeSurrounding("(", ")")?.replace(", ", " · ").orEmpty()
                val price = Regex("售价[:：]\\s*(\\d+)\\s*枚").find(raw)?.let { it.groupValues[1] + " 枚金币" }
                var meta = raw.replace(Regex(" · *售价[:：]\\s*\\d+\\s*枚金币"), "").trim()
                if (meta.startsWith("售价")) meta = ""
                val aid = Regex("aid=([A-Za-z0-9%]+)").find(url)?.groupValues?.get(1).orEmpty()
                val tipText = Regex("aid=(\\d+)").find(url)?.groupValues?.get(1)
                    ?.let { n -> plc.selectFirst("#attach_" + n + "_menu .tip_c")?.text() }
                    ?: plc.selectFirst(".tip_c")?.text()
                val perm = tipText?.let {
                    Regex("阅读权限[:：]\\s*(\\d+)").find(it)?.let { m -> "阅读权限 " + m.groupValues[1] }
                }
                val note = listOfNotNull(perm).joinToString(" · ")
                val metaFinal = listOfNotNull(meta.ifEmpty { null }, note.ifEmpty { null }).joinToString(" · ")
                attachments.add(AttachItem(name, url, metaFinal, price))
            }
            for (dl in plc.select(".pattl dl.tattl")) {
                for (a in dl.select("dd a[href*=mod=attachment], dd a[href*=action=attachpay]")) {
                    if (a.closest(".tip") != null) continue
                    if (a.selectFirst("img") != null) continue
                    val name = a.text().trim()
                    if (name.length < 2 || name in listOf("下载附件", "保存到相册", "购买", "记录")) continue
                    val url = a.absUrl("href")
                    if (url.isBlank()) continue
                    val lines = dl.select("dd > p").map { it.text().trim() } +
                        listOfNotNull((a.parent()?.selectFirst("em.xg1") ?: dl.selectFirst("em.xg1"))?.text()?.trim())
                    val raw = lines.firstOrNull { it.contains("下载次数") || it.contains("Bytes") || it.contains("KB") }
                        .orEmpty().removeSurrounding("(", ")").replace(", ", " · ")
                    val priceLine = lines.firstOrNull { it.contains("售价") }.orEmpty()
                    val price = Regex("售价[:：]\\s*(\\d+)\\s*枚").find(priceLine)?.let { it.groupValues[1] + " 枚金币" }
                    val perm = dl.selectFirst(".tip_c")?.text()?.let {
                        Regex("阅读权限[:：]\\s*(\\d+)").find(it)?.let { m -> "阅读权限 " + m.groupValues[1] }
                    }
                    val meta = listOfNotNull(raw.ifEmpty { null }, perm).joinToString(" · ")
                    attachments.add(AttachItem(name, url, meta, price))
                    break
                }
            }
            // 同一附件可能在正文与末尾各出现一次：按 URL 去重，保留信息更全的那条
            if (attachments.size > 1) {
                val merged = LinkedHashMap<String, AttachItem>()
                for (at in attachments) {
                    val old = merged[at.url]
                    merged[at.url] = if (old == null) at
                    else if ((old.meta.length + (old.price?.length ?: 0)) >= (at.meta.length + (at.price?.length ?: 0))) old else at
                }
                attachments.clear()
                attachments.addAll(merged.values)
            }

            val rating = parseRating(plc.selectFirst(".rate .ratl, dl.rate .ratl"))
            val pid = table.attr("id").removePrefix("pid")

            val links = ArrayList<Pair<String, String>>()
            if (content != null) {
                for (a in content.select("a[href]")) {
                    val href = a.absUrl("href")
                    val text = a.text().trim()
                    if (!href.startsWith("http")) continue
                    if (href.contains("mod=attachment") || href.contains("forum.php?mod=viewthread")) continue
                    if (text.length < 4) continue
                    if (links.any { it.first == href }) continue
                    links.add(text to href)
                    if (links.size >= 12) break
                }
            }
            // 帖子末尾的“更多图片”在网页里是 display:none，靠 JS 展开，这里直接补进正文
            for (img in plc.select(".pattl img, .mbn.savephotop img")) {
                val u = listOf("zoomfile", "file", "data-src", "src").map { img.absUrl(it) }
                    .firstOrNull { it.startsWith("http") && it.contains("img.gamemale.com") }
                    .orEmpty()
                if (u.isEmpty()) continue
                if (blocks.any { it is Block.Image && it.url == u }) continue
                blocks.add(Block.Image(u))
            }
            posts.add(PostItem(pid, author, avatar, time, floor, linkifyBlocks(blocks), attachments, rating, posts.isEmpty(), links))
        }

        val lastEdit = doc.select("em").map { it.text() }.firstOrNull { it.contains("编辑") }
            ?.let { t -> Regex("于\\s*(.+?)\\s*编辑").find(t)?.groupValues?.get(1) ?: t }
            .orEmpty().trim()
        val (pages, pageNo, pageCount) = pagination(doc)
        return ThreadPage(subject, typeName, posts, pages, pageNo, pageCount, forumUrl, lastEdit)
    }

    /**
     * 搜索结果兜底解析：搜索页模板和版块页不一样，直接抓所有 thread-<tid> 链接，
     * 用链接文字当标题，去重后按出现顺序返回。
     */
    fun parseSearchResults(html: String): List<ThreadItem> {
        val doc = Jsoup.parse(html, SITE)
        val out = ArrayList<ThreadItem>()
        val root = doc.selectFirst("#threadlist") ?: doc
        for (li in root.select("li.pbw, li")) {
            val a = li.selectFirst("h3 a[href], a[href*=mod=viewthread], a[href*=thread-]") ?: continue
            val href = a.absUrl("href")
            val tid = Regex("tid=(\\d+)").find(href)?.groupValues?.get(1)
                ?: Regex("thread-(\\d+)").find(href)?.groupValues?.get(1)
                ?: continue
            val title = a.text().trim()
            if (title.length < 4) continue
            val nums = li.selectFirst("p.xg1")?.text().orEmpty()
            val replies = Regex("(\\d+)\\s*个回复").find(nums)?.groupValues?.get(1)
                ?: Regex("回复\\s*(\\d+)").find(nums)?.groupValues?.get(1).orEmpty()
            val views = Regex("(\\d+)\\s*次查看").find(nums)?.groupValues?.get(1).orEmpty()
            val author = li.select("p a[href*=space-uid]").lastOrNull()?.text()?.trim().orEmpty()
            val date = li.select("p span").firstOrNull { it.text().contains("-") || it.text().contains(":") }
                ?.text()?.trim().orEmpty()
            val typeName = li.selectFirst("p a[href*=forum-]")?.text()?.trim()
            if (out.any { it.tid == tid }) continue
            out.add(
                ThreadItem(
                    tid = tid,
                    title = title,
                    url = SITE + "thread-" + tid + "-1-1.html",
                    typeName = typeName,
                    author = author,
                    date = date,
                    replies = replies,
                    views = views,
                    sticky = false
                )
            )
            if (out.size >= 60) break
        }
        return out
    }

    /** 抽取某个表单：action 绝对化，收集 input/textarea/select 的当前值 */
    fun parseForm(html: String, selector: String): FormDef? {
        val doc = Jsoup.parse(unwrapCdata(html), SITE)
        val form = doc.selectFirst(selector) ?: return null
        return buildForm(form, SITE)
    }

    private fun buildForm(form: Element, pageUrl: String): FormDef? {
        val action = form.absUrl("action").ifEmpty { pageUrl }
        if (action.isBlank()) return null
        val fields = ArrayList<Pair<String, String>>()
        val radios = HashMap<String, MutableList<Pair<String, Boolean>>>()
        for (el in form.select("input[name]")) {
            val type = el.attr("type").lowercase()
            if (type in listOf("submit", "button", "reset", "file", "image")) continue
            when (type) {
                "radio" -> radios.getOrPut(el.attr("name")) { ArrayList() }
                    .add(el.attr("value") to el.hasAttr("checked"))
                "checkbox" -> if (el.hasAttr("checked")) fields.add(el.attr("name") to el.attr("value"))
                else -> fields.add(el.attr("name") to el.attr("value"))
            }
        }
        // 单选组：优先选中项，没有就取最后一个（评分通常是最高档）
        for ((name, opts) in radios) {
            val chosen = opts.firstOrNull { it.second } ?: opts.lastOrNull() ?: continue
            fields.add(name to chosen.first)
        }
        for (el in form.select("textarea[name]")) {
            fields.add(el.attr("name") to el.text())
        }
        for (el in form.select("select[name]")) {
            val opts = el.select("option")
            val chosen = opts.firstOrNull { it.hasAttr("selected") } ?: opts.lastOrNull()
            if (chosen != null) fields.add(el.attr("name") to chosen.attr("value"))
        }
        return FormDef(action, fields)
    }

    /** 这个模板的登录标志是 header 里的“退出”链接（member.php?...action=logout） */
    fun isLoggedIn(html: String): Boolean = html.contains("action=logout")

    /** 这个模板的昵称在 #wp_user 的 .u-info-name 里，头像在 .u-pic */
    fun parseLoginName(html: String): String? {
        if (!isLoggedIn(html)) return null
        val doc = Jsoup.parse(html, SITE)
        val name = doc.selectFirst("#wp_user .u-info-name a")?.text()
            ?: doc.selectFirst("#um .u-info-name a")?.text()
        val t = name?.trim()?.replace(Regex("\\s+"), " ")
        return t?.takeIf { it.isNotBlank() && it.length <= 24 }
    }

    /** 顶栏头像 */
    fun parseLoginAvatar(html: String): String? {
        if (!isLoggedIn(html)) return null
        val doc = Jsoup.parse(html, SITE)
        val img = doc.selectFirst("#wp_user .u-pic img") ?: doc.selectFirst("#um .u-pic img")
        return img?.absUrl("src")?.takeIf { it.startsWith("http") }
    }

    /** 帖子正文里的前 n 张图片（列表页没有缩略图时用来补预览） */
    fun parseFirstImages(html: String, n: Int): List<String> {
        val doc = Jsoup.parse(html, SITE)
        val out = ArrayList<String>()
        for (img in doc.select("#postlist td.t_f img, #postlist .pattl .savephotop img")) {
            val u = listOf("zoomfile", "file", "data-src", "src")
                .map { img.absUrl(it) }
                .firstOrNull { it.startsWith("http") && !it.contains("none.gif") }
                ?: continue
            if (u.contains("static/image")) continue
            if (out.add(u) && out.size >= n) break
        }
        return out
    }

    /** 页面里任意表单的 formhash，回复兜底用 */
    fun parseFormhash(html: String): String? =
        Regex("name=\"formhash\" value=\"([^\"]+)\"").find(html)?.groupValues?.get(1)
            ?: Regex("formhash=([0-9a-zA-Z]+)").find(html)?.groupValues?.get(1)

    /** 帖子所在版块 fid */
    fun parseFid(html: String): String? =
        Regex("mod=forumdisplay&amp;fid=(\\d+)").find(html)?.groupValues?.get(1)
            ?: Regex("mod=forumdisplay&fid=(\\d+)").find(html)?.groupValues?.get(1)

    /** 按 action 关键字挑表单，比如 attachpay */
    fun parseFormByAction(html: String, needle: String): FormDef? {
        val doc = Jsoup.parse(unwrapCdata(html), SITE)
        for (f in doc.select("form")) {
            if (!f.absUrl("action").contains(needle)) continue
            return buildForm(f, SITE)
        }
        return null
    }

    /** 表单里 textarea 的字段名（评分理由等） */
    fun textareaName(html: String, selector: String): String? {
        val doc = Jsoup.parse(html, SITE)
        val form = doc.selectFirst(selector) ?: return null
        return form.selectFirst("textarea[name]")?.attr("name")
            ?: form.selectFirst("input[name=reason], input[id=reason]")?.attr("name")
            ?: form.selectFirst("input[name=message]")?.attr("name")
    }

    /**
     * 评分维度。这个站点的真实表单是：
     * <input type="text" name="score3" value="0">，旁边 <ul id="scoreoption3"><li>+3</li>...
     * 所以要从 text 输入框 + 候选值列表里取“该维度允许的最大分值”。
     */
    fun scoreOptions(html: String, selector: String): List<Triple<String, String, String>> {
        val doc = Jsoup.parse(html, SITE)
        val form = doc.selectFirst(selector) ?: return emptyList()
        val out = ArrayList<Triple<String, String, String>>()
        // 1) scoreN 文本输入框
        for (inp in form.select("input[name^=score]")) {
            val name = inp.attr("name")
            val row = inp.closest("tr") ?: continue
            val label = row.select("td").firstOrNull()?.text()?.trim()?.take(10).orEmpty()
            val suffix = name.removePrefix("score")
            val ul = form.selectFirst("#scoreoption" + suffix)
            val max = ul?.select("li")?.mapNotNull { it.text().trim().removePrefix("+").toIntOrNull() }
                ?.maxOrNull()
                ?: row.select("td").firstOrNull { it.text().contains("~") }
                    ?.text()?.substringAfter("~")?.trim()?.toIntOrNull()
                ?: 0
            if (max > 0) out.add(Triple(name, label.ifEmpty { name }, max.toString()))
        }
        // 2) 兼容 select / radio 模板
        for (sel in form.select("select[name]")) {
            val name = sel.attr("name")
            if (!name.startsWith("score")) continue
            for (opt in sel.select("option")) {
                val v = opt.attr("value")
                if (v.isNotBlank() && v != "0") out.add(Triple(name, opt.text().trim(), v))
            }
        }
        return out
    }

    private fun parseRating(ratl: Element?): RatingSummary? {
        if (ratl == null) return null
        val people = ratl.selectFirst("th a span, th span")?.text()?.filter { it.isDigit() }?.toIntOrNull() ?: 0
        val parts = ratl.select("th").drop(1).mapNotNull { th ->
            val label = th.text().trim().replace(Regex("\\s+"), " ")
            if (label.isEmpty()) null else label
        }.filter { it.contains("+") || it.contains("-") }
        if (people == 0 && parts.isEmpty()) return null
        return RatingSummary(people, parts)
    }

    private fun pagination(doc: org.jsoup.nodes.Document): Triple<List<Pair<Int, String>>, Int, Int> {
        val pgs = doc.select("#pgt .pg, .pgs .pg").distinct()
        val parsed = LinkedHashMap<Int, String>()
        var pageNo = 1
        var pageCount = 1
        for (pg in pgs) {
            pg.selectFirst("strong")?.let { pageNo = it.text().trim().toIntOrNull() ?: pageNo }
            for (a in pg.select("a")) {
                val n = a.text().trim().toIntOrNull() ?: continue
                if (!parsed.containsKey(n)) parsed[n] = a.absUrl("href")
            }
            Regex("共\\s*(\\d+)\\s*页").find(pg.text())?.let {
                pageCount = pageCount.coerceAtLeast(it.groupValues[1].toIntOrNull() ?: pageCount)
            }
        }
        if (pageCount <= 1) pageCount = (parsed.keys + pageNo).max()
        val sample = parsed.entries.firstOrNull()?.value.orEmpty()
        val firstNo = parsed.keys.firstOrNull() ?: 0
        val template = if (sample.isNotEmpty() && firstNo > 0)
            sample.replaceFirst(Regex("-$firstNo-"), "-{p}-") else ""
        val out = ArrayList<Pair<Int, String>>()
        for (n in 1..pageCount) {
            val url = parsed[n] ?: when {
                template.isNotEmpty() -> template.replace("{p}", n.toString())
                sample.isNotEmpty() -> "$sample&page=$n"
                else -> ""
            }
            if (url.isNotEmpty()) out.add(n to url)
        }
        return Triple(out, pageNo.coerceIn(1, pageCount), pageCount)
    }

    private fun collectBlocks(el: Element, out: MutableList<Block>) {
        for (node in el.childNodes()) {
            when (node) {
                is TextNode -> {
                    val t = node.text().replace('\u00a0', ' ').trim()
                    if (t.isNotEmpty()) out.add(Block.Text(t))
                }
                is Element -> when {
                    node.hasClass("spoiler") -> {
                        val title = node.selectFirst("input.spoilerbutton")?.attr("value")
                            ?.takeIf { it.isNotBlank() } ?: "展开"
                        val body = node.selectFirst(".spoilerbody")
                        val inner = ArrayList<Block>()
                        if (body != null) collectBlocks(body, inner)
                        out.add(Block.Spoiler(title, inner))
                        out.add(Block.Break)
                    }
                    node.hasClass("tip") || node.hasClass("aimg_tip") -> {}
                    else -> when (node.tagName().lowercase()) {
                    "script", "style" -> {}
                    "br" -> out.add(Block.Break)
                    "img" -> {
                        val u = listOf("zoomfile", "file", "data-src", "src")
                            .map { node.absUrl(it) }
                            .firstOrNull { it.startsWith("http") && !it.contains("none.gif") }
                            .orEmpty()
                        if (u.isNotEmpty() &&
                            !u.contains("/static/image/") &&
                            !u.contains("static/image/common")
                        ) out.add(Block.Image(u))
                    }
                    "a" -> {
                        val href = node.absUrl("href")
                        val text = node.text().trim()
                        when {
                            // 包着图片的附件链接（正文穿插附图）不当文件，也不显示附件信息
                            node.selectFirst("img") != null -> collectBlocks(node, out)
                            href.contains("mod=attachment") -> collectBlocks(node, out)
                            href.startsWith("http") && text.length >= 2 ->
                                out.add(Block.Link(text, href))
                            else -> collectBlocks(node, out)
                        }
                    }
                    "div", "p", "center", "blockquote", "li", "tr", "h1", "h2", "h3" -> {
                        collectBlocks(node, out)
                        out.add(Block.Break)
                    }
                    else -> collectBlocks(node, out)
                    }
                }
                else -> {}
            }
        }
    }
}
