package com.gamemale.client

import android.content.Context
import android.webkit.CookieManager
import android.webkit.ValueCallback
import java.io.File

/**
 * 多账号：每个账号保存一份 Cookie 头字符串（name=value; ...），
 * 切换账号 = 清空 CookieManager 再把那份 Cookie 写回去。
 * 站点 Cookie 没有可靠的"多份会话"机制，所以只能这样整份换。
 */
object Accounts {

    data class Acc(
        val id: String,
        val name: String,
        val avatar: String,
        val cookie: String
    )

    private const val FILE = "accounts.tsv"
    private const val SITE = "https://www.gamemale.com"
    private const val SEP = "\t"

    private fun file(ctx: Context) = File(ctx.filesDir, FILE)

    fun currentId(ctx: Context): String? =
        runCatching { File(ctx.filesDir, "current_account.txt").readText().trim() }.getOrNull()?.takeIf { it.isNotEmpty() }

    fun setCurrent(ctx: Context, id: String?) {
        runCatching {
            val f = File(ctx.filesDir, "current_account.txt")
            if (id.isNullOrBlank()) f.delete() else f.writeText(id)
        }
    }

    fun load(ctx: Context): MutableList<Acc> {
        val out = ArrayList<Acc>()
        val f = file(ctx)
        if (!f.exists()) return out
        runCatching {
            f.readLines().forEach { line ->
                if (line.isBlank()) return@forEach
                val p = line.split(SEP)
                if (p.size >= 4) out.add(Acc(p[0], p[1], p[2], p[3]))
            }
        }
        return out
    }

    fun saveAll(ctx: Context, list: List<Acc>) {
        runCatching {
            file(ctx).writeText(list.joinToString("\n") { a ->
                listOf(a.id, a.name, a.avatar, a.cookie).joinToString(SEP)
            })
        }
    }

    fun upsert(ctx: Context, acc: Acc): MutableList<Acc> {
        val list = load(ctx)
        val i = list.indexOfFirst { it.id == acc.id }
        if (i >= 0) list[i] = acc else list.add(acc)
        saveAll(ctx, list)
        return list
    }

    fun remove(ctx: Context, id: String): MutableList<Acc> {
        val list = load(ctx)
        list.removeAll { it.id == id }
        saveAll(ctx, list)
        return list
    }

    /** 当前 CookieManager 里的登录 Cookie 串 */
    fun capture(): String = CookieManager.getInstance().getCookie(SITE).orEmpty()

    /** 用一份 Cookie 串整体替换当前 CookieManager 内容 */
    fun apply(cookie: String, done: () -> Unit) {
        val cm = CookieManager.getInstance()
        val pairs = cookie.split(";").map { it.trim() }.filter { it.contains("=") }
        cm.removeAllCookies {
            for (p in pairs) runCatching { cm.setCookie(SITE, p) }
            runCatching { cm.setCookie(SITE, "mobile=no") }
            cm.flush()
            android.os.Handler(android.os.Looper.getMainLooper()).post(done)
        }
    }

    fun clear(done: () -> Unit) {
        val cm = CookieManager.getInstance()
        cm.removeAllCookies(ValueCallback<Boolean> {
            cm.flush()
            android.os.Handler(android.os.Looper.getMainLooper()).post(done)
        })
    }
}
