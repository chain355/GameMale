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

    /** 追加日志：整表覆盖写万一被打断，下次启动还能从这里把丢失的条目补回来 */
    private fun journal(ctx: Context) = File(ctx.filesDir, "history.jsonl")

    fun load(ctx: Context): List<Entry> {
        val main = runCatching {
            val f = file(ctx)
            if (!f.exists()) return@runCatching emptyList<Entry>()
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
        return mergeJournal(ctx, main)
    }

    /** 把追加日志里比主表更新/缺失的条目并回来，并顺手把主表压实 */
    private fun mergeJournal(ctx: Context, main: List<Entry>): List<Entry> {
        val jf = journal(ctx)
        if (!jf.exists()) return main
        val extra = runCatching {
            jf.readLines().mapNotNull { line ->
                val o = runCatching { JSONObject(line) }.getOrNull() ?: return@mapNotNull null
                val tid = o.optLong("tid", 0)
                val url = o.optString("url", "")
                if (tid <= 0 || url.isBlank()) null
                else Entry(tid, o.optString("title", ""), url, o.optLong("ts", 0))
            }
        }.getOrDefault(emptyList())
        if (extra.isEmpty()) return main
        val map = LinkedHashMap<Long, Entry>()
        for (e in main) map[e.tid] = e
        for (e in extra) {
            val old = map[e.tid]
            if (old == null || e.ts > old.ts) map[e.tid] = e
        }
        val merged = map.values.sortedByDescending { it.ts }.take(MAX)
        runCatching {
            save(ctx, merged)
            jf.delete()
        }
        return merged
    }

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
            // 先写临时文件再改名：进程在写一半被杀，也不会把整张表截断成空文件
            val tmp = File(ctx.filesDir, "history.json.tmp")
            tmp.writeText(arr.toString())
            val target = file(ctx)
            if (target.exists()) target.delete()
            if (!tmp.renameTo(target)) {
                target.writeText(arr.toString())
                tmp.delete()
            }
        }.onFailure { Diag.log("history", "save fail: " + (it.message ?: it.javaClass.simpleName)) }
    }

    /** 把内存里的整张表落盘（退到后台时兜底用） */
    fun persist(ctx: Context, list: List<Entry>) {
        save(ctx, list)
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
        runCatching {
            journal(ctx).appendText(JSONObject()
                .put("tid", tid)
                .put("title", title)
                .put("url", url)
                .put("ts", now)
                .toString() + "\n")
        }
        Diag.log("history", "record tid=" + tid + " total=" + capped.size)
        return capped
    }

    /** 批量删除（选择模式用） */
    fun removeMany(ctx: Context, tids: Collection<Long>): List<Entry> {
        if (tids.isEmpty()) return load(ctx)
        val set = tids.toSet()
        val left = load(ctx).filterNot { it.tid in set }
        save(ctx, left)
        runCatching { journal(ctx).delete() }
        return left
    }

    fun remove(ctx: Context, tid: Long): List<Entry> {
        val left = load(ctx).filterNot { it.tid == tid }
        save(ctx, left)
        runCatching { journal(ctx).delete() }
        return left
    }

    /** 浏览记录占用的字节数（计入全局缓存用量） */
    fun bytes(ctx: Context): Long = runCatching { file(ctx).length() }.getOrDefault(0L)

    /** 批量删除 */
    fun removeMany(ctx: Context, tids: Set<Long>): List<Entry> {
        val left = load(ctx).filterNot { it.tid in tids }
        save(ctx, left)
        runCatching { journal(ctx).delete() }
        return left
    }

    fun clear(ctx: Context): List<Entry> {
        runCatching { file(ctx).delete() }
        runCatching { journal(ctx).delete() }
        return emptyList()
    }
}
