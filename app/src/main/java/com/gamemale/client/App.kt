package com.gamemale.client

import android.app.Application
import android.webkit.CookieManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import okhttp3.OkHttpClient
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 启动崩溃自检：把最近一次未捕获异常写到 filesDir/crash.txt。
 * 下次启动时 MainActivity 检测到该文件就以「安全模式」运行（不加载 Compose 界面），
 * 这样即使美化层出问题，App 依然能看帖，并且能把日志取出来定位。
 */
class App : Application(), ImageLoaderFactory {

    /**
     * 图片加载：站点图片域有防盗链，必须带上 Referer 和登录 Cookie，
     * 否则头像、正文配图全部 403。
     */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .okHttpClient {
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val b = original.newBuilder()
                        .header("Referer", "https://www.gamemale.com/")
                        .header("User-Agent", android.webkit.WebSettings.getDefaultUserAgent(this@App))
                    val cookie = CookieManager.getInstance().getCookie(original.url.toString())
                    if (!cookie.isNullOrEmpty()) b.header("Cookie", cookie)
                    chain.proceed(b.build())
                }
                .build()
        }
        .crossfade(true)
        .build()

    override fun onCreate() {
        super.onCreate()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching {
                val trace = StringWriter()
                error.printStackTrace(PrintWriter(trace))
                val text = buildString {
                    appendLine("time: " + SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
                    appendLine("device: " + Build.BRAND + " " + Build.MODEL)
                    appendLine("android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")")
                    appendLine("abort: " + thread.name)
                    appendLine()
                    append(trace.toString())
                }
                File(filesDir, CRASH_FILE).writeText(text)
            }
            previous?.uncaughtException(thread, error)
        }
    }

    companion object {
        const val CRASH_FILE = "crash.txt"
    }
}
