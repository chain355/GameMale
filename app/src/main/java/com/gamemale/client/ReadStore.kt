package com.gamemale.client

import android.content.Context
import org.json.JSONArray
import java.io.File

/**
 * 已读帖子：只记 tid，独立于浏览记录。
 * 浏览记录删掉、清空都不影响这里，因为两者是不同的文件。
 */
object ReadStore {
    /** 上限够用即可，超出后丢弃最早的 */
    private const val MAX = 6000

    private fun file(ctx: Context) = File(ctx.filesDir, "read_tids.json")

    fun load(ctx: Context): Set<Long> = runCatching {
        val f = file(ctx)
        if (!f.exists()) return emptySet()
        val arr = JSONArray(f.readText())
        val out = LinkedHashSet<Long>(arr.length())
        for (i in 0 until arr.length()) {
            val v = arr.optLong(i, 0)
            if (v > 0) out.add(v)
        }
        out
    }.getOrDefault(emptySet())

    private fun save(ctx: Context, ids: Collection<Long>) {
        runCatching {
            val arr = JSONArray()
            ids.forEach { arr.put(it) }
            val tmp = File(ctx.filesDir, "read_tids.json.tmp")
            tmp.writeText(arr.toString())
            tmp.renameTo(file(ctx))
        }
    }

    /** 打开帖子时调用，返回更新后的集合（已读则不重复写盘） */
    fun mark(ctx: Context, tid: Long): Set<Long> {
        if (tid <= 0) return load(ctx)
        val old = load(ctx)
        if (tid in old) return old
        val next = LinkedHashSet(old)
        next.add(tid)
        val trimmed = if (next.size > MAX) next.toList().takeLast(MAX) else next.toList()
        save(ctx, trimmed)
        return LinkedHashSet(trimmed)
    }

    fun clear(ctx: Context) {
        runCatching { file(ctx).delete() }
    }
}
