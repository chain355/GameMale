package com.gamemale.client

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * 内容偏好：屏蔽词 / 特别关注。
 * 只作用于首页三个板块（和谐动漫、和谐游戏、浏览记录），
 * 搜索结果页与外部分享进来的帖子一律不过滤。
 */
object ContentPrefs {
    private fun file(ctx: Context) = File(ctx.filesDir, "content_prefs.json")

    fun loadBlocked(ctx: Context): List<String> = load(ctx).first

    fun loadWatch(ctx: Context): List<String> = load(ctx).second

    fun load(ctx: Context): Pair<List<String>, List<String>> = runCatching {
        val f = file(ctx)
        if (!f.exists()) return emptyList<String>() to emptyList<String>()
        val o = JSONObject(f.readText())
        read(o.optJSONArray("blocked")) to read(o.optJSONArray("watch"))
    }.getOrDefault(emptyList<String>() to emptyList<String>())

    private fun read(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        val out = ArrayList<String>(arr.length())
        for (i in 0 until arr.length()) {
            val v = arr.optString(i, "").trim()
            if (v.isNotEmpty()) out.add(v)
        }
        return out
    }

    fun save(ctx: Context, blocked: List<String>, watch: List<String>) {
        runCatching {
            val o = JSONObject()
            o.put("blocked", JSONArray(blocked))
            o.put("watch", JSONArray(watch))
            val tmp = File(ctx.filesDir, "content_prefs.json.tmp")
            tmp.writeText(o.toString())
            tmp.renameTo(file(ctx))
        }
    }

    /** 批量输入：按行拆、去空白、去重、忽略空行 */
    fun parseBatch(text: String): List<String> {
        val seen = LinkedHashSet<String>()
        text.split('\n', '\r', '，', ',', ';', '；')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { seen.add(it) }
        return seen.toList()
    }

    fun hit(title: String, words: List<String>): Boolean {
        if (words.isEmpty()) return false
        val t = title.lowercase()
        return words.any { w -> w.isNotBlank() && t.contains(w.lowercase()) }
    }
}
