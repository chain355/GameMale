package com.gamemale.client

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 轻量本地日志：只记「谁在什么时候请求了什么、拿到了多少、错在哪」，
 * 出问题时可以在设置页一键导出成 txt 发给开发者。
 */
object Diag {
    private var file: File? = null
    private val fmt = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.CHINA)

    fun init(ctx: Context) {
        val f = File(ctx.applicationContext.filesDir, "logs/app.log")
        f.parentFile?.mkdirs()
        file = f
        log("app", "----- 启动 -----")
    }

    @Synchronized
    fun log(tag: String, msg: String) {
        val f = file ?: return
        runCatching {
            if (f.length() > 2_000_000) f.delete()
            f.appendText(fmt.format(Date()) + " [" + tag + "] " + msg + "\n")
        }
    }

    fun export(ctx: Context) {
        val f = file ?: return
        if (!f.exists()) {
            f.parentFile?.mkdirs()
            f.writeText("(还没有日志)\n")
        }
        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", f)
        val i = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "GameMale 诊断日志")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(Intent.createChooser(i, "导出诊断日志").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun clear() {
        runCatching { file?.delete() }
    }
}
