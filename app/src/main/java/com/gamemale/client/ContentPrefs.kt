package com.gamemale.client

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * 内容偏好：屏蔽词 / 特别关注。
 *
 * 每个条目按「不区分大小写的正则表达式」匹配帖子标题；无法编译为正则的条目
 * 退回普通文本包含匹配，避免用户输入 C++、[ 这类内容时直接报错。
 * 作用于板块列表、搜索结果、浏览记录和收藏列表，已打开的帖子正文不过滤。
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

    /**
     * 批量输入：一行一条规则，保留正则里的逗号、分号等字符，不再按标点拆分。
     * 去首尾空白、忽略空行，完全相同的规则只保留第一条。
     */
    fun parseBatch(text: String): List<String> {
        val seen = LinkedHashSet<String>()
        text.split('\n', '\r')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { seen.add(it) }
        return seen.toList()
    }

    /** 正则缓存：同一条规则只编译一次；无效正则返回 null，调用方退回文本匹配。 */
    private val regexCache = java.util.concurrent.ConcurrentHashMap<String, Regex>()
    private val invalidRegex = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    private fun regexOf(word: String): Regex? {
        if (word in invalidRegex) return null
        regexCache[word]?.let { return it }
        return runCatching { Regex(word, RegexOption.IGNORE_CASE) }
            .getOrNull()
            ?.also { regexCache[word] = it }
            ?: run {
                invalidRegex.add(word)
                null
            }
    }

    fun hit(title: String, words: List<String>): Boolean {
        if (words.isEmpty()) return false
        return words.any { w ->
            if (w.isBlank()) return@any false
            val regex = regexOf(w)
            if (regex != null) regex.containsMatchIn(title)
            else title.contains(w, ignoreCase = true)
        }
    }
}
