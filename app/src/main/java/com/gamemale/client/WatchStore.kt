package com.gamemale.client

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** 稍后再看：手动收藏的帖子，按收藏时间倒序，最近的在最前 */
object WatchStore {
    const val MAX = 200

    data class Entry(
        val tid: Long,
        val title: String,
        val url: String,
        val ts: Long
    )

    private fun file(ctx: Context) = File(ctx.filesDir, "watch.json")

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
            val tmp = File(ctx.filesDir, "watch.json.tmp")
            tmp.writeText(arr.toString())
            tmp.renameTo(file(ctx))
        }
    }

    fun has(ctx: Context, tid: Long): Boolean = load(ctx).any { it.tid == tid }

    /** 收藏；已在列表里则把时间刷新到最前（重复点击不会产生两条） */
    fun add(ctx: Context, tid: Long, title: String, url: String): List<Entry> {
        val old = load(ctx).filter { it.tid != tid }
        val next = (listOf(Entry(tid, title, url, System.currentTimeMillis())) + old).take(MAX)
        save(ctx, next)
        return next
    }

    fun remove(ctx: Context, tid: Long): List<Entry> {
        val next = load(ctx).filter { it.tid != tid }
        save(ctx, next)
        return next
    }

    fun removeMany(ctx: Context, tids: Collection<Long>): List<Entry> {
        val next = load(ctx).filter { it.tid !in tids }
        save(ctx, next)
        return next
    }

    fun clear(ctx: Context): List<Entry> {
        runCatching { file(ctx).delete() }
        return emptyList()
    }
}
