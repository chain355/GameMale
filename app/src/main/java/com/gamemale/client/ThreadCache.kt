package com.gamemale.client

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * 帖子本地缓存：把抓到的整页 HTML 连同标题/最后编辑时间存到 APP 私有目录，
 * 下次打开同一条帖子先本地还原，再后台比对标题与编辑时间决定是否替换。
 */
class ThreadCache(ctx: Context) {

    data class Entry(val tid: String, var title: String, var edited: String, var bytes: Long, var ts: Long)

    private val app = ctx.applicationContext
    private val dir = File(app.filesDir, "threadcache")
    private val indexFile = File(dir, "index.json")
    private val prefs = app.getSharedPreferences("gm", Context.MODE_PRIVATE)
    private val index = LinkedHashMap<String, Entry>()

    init {
        dir.mkdirs()
        loadIndex()
        enforceLimit()
    }

    var limitMb: Int
        get() = prefs.getInt("cacheMb", 300)
        set(value) {
            prefs.edit().putInt("cacheMb", value.coerceIn(50, 2000)).commit()
            enforceLimit()
        }

    private fun fileOf(tid: String) = File(dir, "$tid.html")

    private fun loadIndex() {
        runCatching {
            if (!indexFile.exists()) return
            val root = JSONObject(indexFile.readText())
            val arr = root.optJSONArray("items") ?: return
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val tid = o.optString("tid")
                if (tid.isEmpty()) continue
                index[tid] = Entry(
                    tid = tid,
                    title = o.optString("title"),
                    edited = o.optString("edited"),
                    bytes = o.optLong("bytes"),
                    ts = o.optLong("ts")
                )
            }
        }
    }

    private fun saveIndex() {
        runCatching {
            val arr = org.json.JSONArray()
            index.values.forEach { e ->
                arr.put(
                    JSONObject()
                        .put("tid", e.tid)
                        .put("title", e.title)
                        .put("edited", e.edited)
                        .put("bytes", e.bytes)
                        .put("ts", e.ts)
                )
            }
            indexFile.writeText(JSONObject().put("items", arr).toString())
        }
    }

    /** 命中就直接返回整页 HTML；标题/编辑时间用于决定要不要后台换新 */
    fun read(tid: String): Pair<String, Entry>? {
        val e = index[tid] ?: return null
        val f = fileOf(tid)
        if (!f.exists()) {
            index.remove(tid)
            saveIndex()
            return null
        }
        val html = runCatching { f.readText() }.getOrNull() ?: return null
        e.ts = System.currentTimeMillis()
        saveIndex()
        return html to e
    }

    fun save(tid: String, title: String, edited: String, html: String) {
        runCatching {
            val f = fileOf(tid)
            f.writeText(html)
            index[tid] = Entry(tid, title, edited, f.length(), System.currentTimeMillis())
            saveIndex()
            enforceLimit()
        }
    }

    fun totalBytes(): Long = index.values.sumOf { it.bytes } + runCatching { indexFile.length() }.getOrDefault(0L)

    fun count(): Int = index.size

    fun clear() {
        runCatching {
            dir.listFiles()?.forEach { it.delete() }
            index.clear()
        }
        dir.mkdirs()
        saveIndex()
    }

    private fun enforceLimit() {
        val limit = limitMb.toLong() * 1024L * 1024L
        var total = totalBytes()
        if (total <= limit) return
        val old = index.values.sortedBy { it.ts }
        for (e in old) {
            if (total <= limit) break
            fileOf(e.tid).delete()
            index.remove(e.tid)
            total -= e.bytes
        }
        saveIndex()
    }
}
