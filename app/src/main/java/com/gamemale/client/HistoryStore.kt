package com.gamemale.client

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** 浏览记录：打开过的帖子，按打开时间倒序，最近的在最前 */
object HistoryStore {
    const val MAX = 300

    data class Entry(
        val tid: Long,
        val title: String,
        val url: String,
        val ts: Long
    )

    private fun file(ctx: Context) = File(ctx.filesDir, "history.json")

    fun load(ctx: Context): List<Entry> = runCatching {
        val f = file(ctx)
        if (!f.exists()) return emptyList()
        val arr = JSONArray(f.readText())
        val out = ArrayList<Entry>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val tid = o.optLong("tid", 0)
            val url = o.optString("url", "")
            if (tid <= 0 || url.isBlank()) continue
            out.add(Entry(tid, o.optString("title", ""), url, o.optLong("ts", 0)))
        }
        out
    }.getOrDefault(emptyList())

    private fun save(ctx: Context, list: List<Entry>) {
        runCatching {
            val arr = JSONArray()
            for (e in list.take(MAX)) {
                arr.put(
                    JSONObject()
                        .put("tid", e.tid)
                        .put("title", e.title)
                        .put("url", e.url)
                        .put("ts", e.ts)
                )
            }
            file(ctx).writeText(arr.toString())
        }
    }

    /** 记一条（同 tid 去重后置顶），返回最新列表 */
    fun record(ctx: Context, tid: Long, title: String, url: String): List<Entry> {
        val now = System.currentTimeMillis()
        val cur = load(ctx)
        val old = cur.firstOrNull { it.tid == tid }
        val merged = ArrayList<Entry>(cur.size + 1)
        merged.add(
            Entry(
                tid = tid,
                title = title.ifBlank { old?.title.orEmpty() },
                url = url,
                ts = now
            )
        )
        for (e in cur) if (e.tid != tid) merged.add(e)
        val capped = merged.take(MAX)
        save(ctx, capped)
        return capped
    }

    fun remove(ctx: Context, tid: Long): List<Entry> {
        val left = load(ctx).filterNot { it.tid == tid }
        save(ctx, left)
        return left
    }

    fun clear(ctx: Context): List<Entry> {
        runCatching { file(ctx).delete() }
        return emptyList()
    }
}
