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
            override fun onPageFinished(view: WebView, url: String?) {
                if (done) return
                if (url == null || currentUrl == null) return
                if (!samePage(url, currentUrl!!)) return
                main.postDelayed({ extract() }, 350)
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

    fun fetch(url: String, cb: (Result<String>) -> Unit) {
        enqueue {
            callback = cb
            currentUrl = url
            done = false
            attempt = 0
            main.postDelayed({ if (!done) finish(Result.failure(IllegalStateException("超时"))) }, 30_000)
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
        main.postDelayed({ if (!done) finish(Result.failure(IllegalStateException("超时"))) }, 30_000)
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
        main.postDelayed({ if (!done) finish(Result.failure(IllegalStateException("超时"))) }, 30_000)
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
        main.postDelayed({ if (!done) finish(Result.failure(IllegalStateException("超时"))) }, 30_000)
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

    private fun extract() {
        web.evaluateJavascript("(function(){return document.documentElement.outerHTML;})()") { value ->
            if (done) return@evaluateJavascript
            val html = runCatching {
                (JSONTokener(value).nextValue() as? String) ?: ""
            }.getOrDefault("")
            when {
                html.isBlank() -> retryOrFail("页面为空")
                isChallenge(html) -> retryOrFail("遇到 Cloudflare 验证")
                else -> finish(Result.success(html))
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
        if (attempt >= 2) {
            finish(Result.failure(IllegalStateException(reason)))
            return
        }
        attempt++
        val url = currentUrl
        if (url == null) {
            finish(Result.failure(IllegalStateException(reason)))
        } else {
            main.postDelayed({ if (!done) web.loadUrl(forceDesktop(url)) }, 1200)
        }
    }

    private fun finish(result: Result<String>) {
        if (done) return
        done = true
        val cb = callback
        callback = null
        running = false
        cb?.invoke(result)
        main.post { pump() }
    }
}
