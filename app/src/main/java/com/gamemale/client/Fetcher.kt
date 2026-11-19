package com.gamemale.client

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import java.net.URLDecoder
import android.webkit.WebViewClient
import android.widget.FrameLayout
import org.json.JSONTokener

/**
 * 隐藏的取数引擎：用 WebView 过 Cloudflare、带登录 Cookie 去抓页面 HTML，
 * 再由 Jsoup 解析成原生界面的数据。不渲染网页，只取字符串。
 */
@SuppressLint("SetJavaScriptEnabled")
class Fetcher(private val ctx: Context) {

    private val main = Handler(Looper.getMainLooper())
    private val web = WebView(ctx)
    private var callback: ((Result<String>) -> Unit)? = null
    private var currentUrl: String? = null
    private var done = false
    private var attempt = 0

    init {
        with(web.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = false
            blockNetworkImage = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            setSupportZoom(false)
        }
        web.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        CookieManager.getInstance().setAcceptCookie(true)
        // Discuz 会按 UA 自动切手机模板（mwt2），手机模板里没有 td.fl_g 这类桌面节点；
        // 这里把模板强制钉在桌面版，隐藏 WebView 只负责取数据，渲染由我们自己做。
        runCatching {
            CookieManager.getInstance().setCookie("https://www.gamemale.com", "mobile=no")
            CookieManager.getInstance().setCookie("https://www.gamemale.com/", "mobile=no")
        }
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)
        web.webViewClient = object : WebViewClient() {
            /**
             * 我们只需要 HTML 文本：图片/CSS/字体/媒体一律拦掉，
             * 页面能在几百毫秒内到达 onPageFinished，而不是等所有图片下载完。
             */
            override fun shouldInterceptRequest(
                view: WebView,
                request: android.webkit.WebResourceRequest
            ): android.webkit.WebResourceResponse? {
                val url = request.url.toString()
                val path = url.substringBefore('?').lowercase()
                val block = path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") ||
                    path.endsWith(".gif") || path.endsWith(".webp") || path.endsWith(".bmp") ||
                    path.endsWith(".css") || path.endsWith(".woff") || path.endsWith(".woff2") ||
                    path.endsWith(".ttf") || path.endsWith(".otf") ||
                    path.endsWith(".mp4") || path.endsWith(".webm") || path.endsWith(".mp3") ||
                    (!request.isForMainFrame && request.requestHeaders["Accept"]?.contains("image/") == true)
                if (!block) return null
                blocked += 1
                return android.webkit.WebResourceResponse(
                    "text/plain", "utf-8", java.io.ByteArrayInputStream(ByteArray(0))
                )
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: android.graphics.Bitmap?) {
                if (done) return
                Diag.log("fetch", "开始 " + url)
            }

            /**
             * 主文档一到就先试着取 HTML：Discuz 的正文/列表其实早就在 DOM 里了，
             * 没必要等那些被拦掉的长尾资源把 onPageFinished 拖到十几秒。
             */
            override fun onPageCommitVisible(view: WebView, url: String?) {
                if (done || currentUrl == null) return
                if (!matchesRequest(url)) return
                val g = gen
                main.postDelayed({
                    if (!done && g == gen && matchesRequest(view.url)) extract(early = true)
                }, 120)
            }

            override fun onPageFinished(view: WebView, url: String?) {
                if (done) return
                Diag.log("fetch", "finish 事件 " + url)
                if (url == null || currentUrl == null) return
                if (!matchesRequest(url)) return
                main.postDelayed({
                    // 页面切换瞬间可能还有上一页的 finish 事件，这里再确认一次当前文档
                    if (!done && matchesRequest(view.url)) extract()
                }, 350)
            }
        }
    }

    /** 让隐藏 WebView 在前后台切换时继续跑（避免切后台后加载卡死） */
    fun keepRunning() {
        runCatching {
            web.resumeTimers()
            web.onResume()
        }
    }

    fun attach(host: FrameLayout) {
        if (web.parent == null) {
            host.addView(web, FrameLayout.LayoutParams(1, 1))
        }
    }

    fun detach() {
        runCatching { (web.parent as? FrameLayout)?.removeView(web) }
    }

    // 只有一个 WebView，两个请求同时发会互相覆盖回调，导致“拿到的是另一个页面的内容”。
    // 所有请求一律排队串行执行。
    private val jobs = ArrayDeque<() -> Unit>()
    private var running = false

    private fun enqueue(start: () -> Unit) {
        jobs.addLast(start)
        if (!running) {
            val next = jobs.removeFirstOrNull() ?: return
            running = true
            next()
        }
    }

    private fun pump() {
        if (running) return
        val next = jobs.removeFirstOrNull() ?: return
        running = true
        next()
    }

    /** 丢掉排队中的请求（刷新/换页时清掉积压的预览图抓取） */
    fun dropPending() {
        jobs.clear()
    }

    /** 回包的地址必须真是这次请求的目标：帖子看 tid、版块看 fid，否则一律当成上一页的迟到事件 */
    private fun matchesRequest(loaded: String?): Boolean {
        val target = currentUrl ?: return false
        if (loaded == null) return false
        if (!samePage(loaded, target)) return false
        val tid = Regex("thread-(\\d+)").find(target)?.groupValues?.get(1)
            ?: Regex("tid=(\\d+)").find(target)?.groupValues?.get(1)
        if (tid != null) return loaded.contains("thread-$tid") || loaded.contains("tid=$tid")
        val fid = Regex("fid=(\\d+)").find(target)?.groupValues?.get(1)
        if (fid != null) return loaded.contains("fid=$fid") || loaded.contains("forum-$fid-")
        return true
    }

    /** 每次请求一个代号：旧请求残留的定时器/回调一律作废，避免把新请求判成超时 */
    private var gen = 0
    private var blocked = 0
    private var challengeWait = 0

    private fun newGen(): Int { gen++; challengeWait = 0; return gen }

    fun fetch(url: String, cb: (Result<String>) -> Unit) {
        if (running && jobs.size >= 5) {
            Diag.log("fetch", "队列已满，丢弃 " + url)
            cb(Result.failure(IllegalStateException("请求太密集，稍后再试")))
            return
        }
        Diag.log("fetch", "排队 " + url + " 队列=" + jobs.size)
        enqueue {
            callback = cb
            currentUrl = url
            done = false
            attempt = 0
            val g = newGen()
            main.postDelayed({
                if (g == gen && !done) finish(Result.failure(IllegalStateException("超时")))
            }, 30_000)
            runCatching { web.stopLoading() }
            web.loadUrl(forceDesktop(url))
        }
    }

    /** 给站点加 mobile=no：不改 UA（避免 Cloudflare 判定机器人），只改模板偏好 */
    private fun forceDesktop(url: String): String {
        if (!url.contains("gamemale.com")) return url
        if (url.contains("mobile=")) return url
        val sep = if (url.contains("?")) "&" else "?"
        return url + sep + "mobile=no"
    }

    /** 抓当前 WebView 里的完整 HTML（截图诊断用） */
    fun snapshotHtml(cb: (String) -> Unit) {
        web.evaluateJavascript("(function(){return document.documentElement.outerHTML})()") { v ->
            val html = runCatching {
                val t = JSONTokener(v).nextValue()
                if (t is String) t else v
            }.getOrDefault("")
            if (html.isNotBlank()) cb(html)
        }
    }

    /** 在已加载的同源页面上用 JS 建表单提交（带 referer 和真实会话，比 postUrl 更接近浏览器行为） */
    fun submitOnPage(url: String, body: String, cb: (Result<String>) -> Unit) {
        val cur = web.url
        if (cur == null || !cur.contains("gamemale.com")) {
            post(url, body, cb)
            return
        }
        enqueue { doSubmitOnPage(url, body, cb) }
    }

    private fun doSubmitOnPage(url: String, body: String, cb: (Result<String>) -> Unit) {
        callback = cb
        currentUrl = url
        done = false
        attempt = 0
        val g = newGen()
        main.postDelayed({
            if (g == gen && !done) finish(Result.failure(IllegalStateException("超时")))
        }, 30_000)
        val pairs = body.split("&").mapNotNull { kv ->
            val i = kv.indexOf('=')
            if (i < 0) null else URLDecoder.decode(kv.substring(0, i), "UTF-8") to
                URLDecoder.decode(kv.substring(i + 1), "UTF-8")
        }
        val sb = StringBuilder()
        sb.append("(function(){")
        sb.append("var f=document.createElement('form');")
        sb.append("f.method='POST';f.action=").append(jsStr(url)).append(";")
        sb.append("f.style.display='none';")
        for ((k, v) in pairs) {
            sb.append("var i=document.createElement('input');i.type='hidden';i.name=")
                .append(jsStr(k)).append(";i.value=").append(jsStr(v)).append(";")
            sb.append("f.appendChild(i);")
        }
        sb.append("document.body.appendChild(f);f.submit();})()")
        web.evaluateJavascript(sb.toString(), null)
    }

    private fun jsStr(v: String): String {
        val q = StringBuilder("\"")
        for (c in v) {
            when (c) {
                '"' -> q.append("\\\"")
                '\\' -> q.append("\\\\")
                '\n' -> q.append("\\n")
                '\r' -> q.append("\\r")
                else -> if (c.code < 32) q.append(" ") else q.append(c)
            }
        }
        return q.append('"').toString()
    }

    /** 直接 POST 表单串（Content-Type 由 WebView 按 x-www-form-urlencoded 处理） */
    fun post(url: String, body: String, cb: (Result<String>) -> Unit) {
        enqueue { doPost(url, body, cb) }
    }

    private fun doPost(url: String, body: String, cb: (Result<String>) -> Unit) {
        callback = cb
        currentUrl = url
        done = false
        attempt = 0
        val g = newGen()
        main.postDelayed({
            if (g == gen && !done) finish(Result.failure(IllegalStateException("超时")))
        }, 30_000)
        web.postUrl(url, body.toByteArray(Charsets.UTF_8))
    }

    /**
     * 表单提交：在本地生成一个带隐藏域的 form 页面，让 WebView 自己按标准表单编码提交。
     * 比 postUrl 稳：Content-Type、Accept、Referer 都由浏览器按真实表单行为生成。
     */
    fun postForm(action: String, fields: List<Pair<String, String>>, cb: (Result<String>) -> Unit) {
        enqueue { doPostForm(action, fields, cb) }
    }

    private fun doPostForm(action: String, fields: List<Pair<String, String>>, cb: (Result<String>) -> Unit) {
        callback = cb
        currentUrl = action
        done = false
        attempt = 0
        val g = newGen()
        main.postDelayed({
            if (g == gen && !done) finish(Result.failure(IllegalStateException("超时")))
        }, 30_000)
        val sb = StringBuilder()
        sb.append("<html><head><meta charset=\"utf-8\"></head><body>")
        sb.append("<form id=\"f\" method=\"post\" action=\"").append(htmlEscape(action)).append("\">")
        for ((k, v) in fields) {
            sb.append("<input type=\"hidden\" name=\"")
                .append(htmlEscape(k)).append("\" value=\"")
                .append(htmlEscape(v)).append("\" />")
        }
        sb.append("</form><script>document.getElementById('f').submit();</script></body></html>")
        val f = java.io.File(ctx.filesDir, "submit.html")
        f.writeText(sb.toString(), Charsets.UTF_8)
        web.loadUrl("file://" + f.absolutePath)
    }

    private fun htmlEscape(v: String): String = v
        .replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

    /**
     * 只比 host：站点会做几层跳转（forum.php 可能被改写到别的路径/带上参数），
     * 原来严格比对 URL 会把跳转后的页面当成"不是我要的页面"而永远取不到内容，最后只剩超时。
     */
    private fun samePage(a: String, b: String): Boolean {
        val ha = runCatching { java.net.URI(a).host }.getOrNull()
        val hb = runCatching { java.net.URI(b).host }.getOrNull()
        return ha != null && ha.equals(hb, ignoreCase = true)
    }

    private fun extract(early: Boolean = false) {
        val g = gen
        web.evaluateJavascript("(function(){return document.documentElement.outerHTML;})()") { value ->
            if (done || g != gen) return@evaluateJavascript
            val html = runCatching {
                (JSONTokener(value).nextValue() as? String) ?: ""
            }.getOrDefault("")
            val tid = currentUrl?.let { Regex("thread-(\\d+)").find(it)?.groupValues?.get(1) }
            val looksComplete = html.contains("id=\"postlist\"") ||
                html.contains("id=\"threadlist\"") || html.contains("normalthread_")
            when {
                html.isBlank() -> if (early) return@evaluateJavascript else retryOrFail("页面为空")
                // Cloudflare 的验证页是"活的"：它要靠页面里的 JS 自己算完再跳转。
                // 之前一见验证页就失败重载，等于每次把验证重新开始一遍，永远过不去。
                // 这里改成原地等它自己跳走。
                isChallenge(html) -> {
                    if (early) return@evaluateJavascript
                    challengeWait++
                    if (challengeWait > 6) {
                        retryOrFail("遇到 Cloudflare 验证")
                    } else {
                        Diag.log("fetch", "等待验证通过 第" + challengeWait + "次")
                        main.postDelayed({
                            if (!done && g == gen) extract()
                        }, 900)
                    }
                }
                tid != null && !html.contains("thread-$tid") ->
                    if (early) return@evaluateJavascript else retryOrFail("拿到的不是目标帖子")
                // 提前取到的可能只是半截 DOM，没看到正文容器就等 onPageFinished 再取
                early && (html.length < 40_000 || !looksComplete) -> return@evaluateJavascript
                else -> {
                    if (early) Diag.log("fetch", "提前取到 bytes=" + html.length)
                    finish(Result.success(html))
                }
            }
        }
    }

    private fun isChallenge(html: String): Boolean =
        html.contains("challenges.cloudflare.com") ||
            html.contains("challenge-running") ||
            html.contains("cf_chl_opt") ||
            html.contains("cf-browser-verification") ||
            html.contains("验证组件") ||
            html.contains("Just a moment") ||
            html.contains("请稍候") ||
            html.contains("Verifying you are human")

    private fun retryOrFail(reason: String) {
        val g = gen
        if (attempt >= 2) {
            finish(Result.failure(IllegalStateException(reason)))
            return
        }
        attempt++
        val url = currentUrl
        if (url == null) {
            finish(Result.failure(IllegalStateException(reason)))
        } else {
            main.postDelayed({
                if (g == gen && !done) web.loadUrl(forceDesktop(url))
            }, 1200)
        }
    }

    private fun finish(result: Result<String>) {
        if (done) return
        result.onSuccess { Diag.log("fetch", "完成 bytes=" + it.length + " 拦截资源=" + blocked + " url=" + currentUrl) }
            .onFailure { Diag.log("fetch", "失败 " + it.message + " url=" + currentUrl) }
        done = true
        val cb = callback
        callback = null
        running = false
        cb?.invoke(result)
        main.post { pump() }
    }
}
