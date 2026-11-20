package com.gamemale.client

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** 一次应用内附件下载的界面状态：进度 0~1，done 表示已落到私有目录 */
data class AttachLoad(
    val progress: Float = 0f,
    val done: Boolean = false,
    val failed: Boolean = false
)

/** 帖子附件在列表 / 详情里用的键：同帖同名的附件算一份 */
fun attachKey(tid: Long?, name: String): String = (tid ?: 0L).toString() + "|" + name.trim()

/**
 * 已下载附件索引：文件本体放在 filesDir/attachments/<tid>/ 下，这里记元信息并关联原帖。
 */
object AttachmentStore {

    data class Entry(
        val id: Long,
        val tid: Long,
        val title: String,
        val url: String,
        val name: String,
        val path: String,
        val size: Long,
        val ts: Long
    ) {
        val file: File get() = File(path)
        val exists: Boolean get() = file.exists()
    }

    private fun indexFile(ctx: Context) = File(ctx.filesDir, "attachments.json")

    fun dirFor(ctx: Context, tid: Long): File =
        File(File(ctx.filesDir, "attachments"), tid.toString()).apply { mkdirs() }

    fun load(ctx: Context): List<Entry> {
        val f = indexFile(ctx)
        if (!f.exists()) return emptyList()
        return runCatching {
            val arr = JSONArray(f.readText())
            val out = ArrayList<Entry>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(
                    Entry(
                        o.optLong("id"),
                        o.optLong("tid"),
                        o.optString("title"),
                        o.optString("url"),
                        o.optString("name"),
                        o.optString("path"),
                        o.optLong("size"),
                        o.optLong("ts")
                    )
                )
            }
            out.sortByDescending { it.ts }
            out
        }.getOrElse { emptyList() }
    }

    private fun save(ctx: Context, list: List<Entry>) {
        val arr = JSONArray()
        for (e in list) {
            arr.put(
                JSONObject()
                    .put("id", e.id)
                    .put("tid", e.tid)
                    .put("title", e.title)
                    .put("url", e.url)
                    .put("name", e.name)
                    .put("path", e.path)
                    .put("size", e.size)
                    .put("ts", e.ts)
            )
        }
        runCatching { indexFile(ctx).writeText(arr.toString()) }
    }

    fun find(ctx: Context, tid: Long, name: String): Entry? =
        load(ctx).firstOrNull { it.tid == tid && it.name == name.trim() && it.exists }

    /** 下载完成后登记；同帖同名覆盖旧记录，避免重复占空间 */
    fun add(
        ctx: Context,
        tid: Long,
        title: String,
        url: String,
        name: String,
        file: File
    ): Entry {
        val old = load(ctx).filterNot { it.tid == tid && it.name == name.trim() }
        val entry = Entry(
            id = System.currentTimeMillis() + (0..999).random(),
            tid = tid,
            title = title,
            url = url,
            name = name.trim(),
            path = file.absolutePath,
            size = file.length(),
            ts = System.currentTimeMillis()
        )
        save(ctx, (old + entry).sortedByDescending { it.ts })
        return entry
    }

    fun removeMany(ctx: Context, ids: List<Long>): List<Entry> {
        val all = load(ctx)
        val gone = all.filter { it.id in ids }
        val keep = all.filterNot { it.id in ids }
        gone.forEach { e -> runCatching { e.file.delete() } }
        save(ctx, keep)
        return keep
    }

    fun remove(ctx: Context, id: Long): List<Entry> = removeMany(ctx, listOf(id))

    fun bytes(ctx: Context): Long = load(ctx).sumOf { it.size }

    fun count(ctx: Context): Int = load(ctx).size

    fun clear(ctx: Context): List<Entry> {
        load(ctx).forEach { e -> runCatching { e.file.delete() } }
        runCatching { File(ctx.filesDir, "attachments").deleteRecursively() }
        runCatching { indexFile(ctx).delete() }
        return emptyList()
    }

    /** 缓存超限时的最后手段：从最旧的附件开始删，直到释放够 [excess] 字节 */
    fun trim(ctx: Context, excess: Long): List<Entry> {
        if (excess <= 0) return load(ctx)
        val all = load(ctx)
        var freed = 0L
        val keep = ArrayList<Entry>(all.size)
        for (e in all.sortedBy { it.ts }) {
            if (freed >= excess) {
                keep.add(e)
            } else {
                freed += e.size
                runCatching { e.file.delete() }
            }
        }
        val left = keep.sortedByDescending { it.ts }
        runCatching {
            indexFile(ctx).writeText("[]")
        }
        // 复用 save 的序列化，避免手写 JSON
        save(ctx, left)
        return left
    }

    /** 按扩展名判断走哪套打开方式 */
    fun kindOf(name: String): String {
        val n = name.lowercase()
        return when {
            n.endsWith(".txt") || n.endsWith(".md") || n.endsWith(".log") || n.endsWith(".json") -> "txt"
            n.endsWith(".pdf") -> "pdf"
            n.endsWith(".doc") || n.endsWith(".docx") || n.endsWith(".rtf") -> "word"
            n.endsWith(".pages") -> "pages"
            n.endsWith(".zip") || n.endsWith(".rar") || n.endsWith(".7z") -> "archive"
            n.endsWith(".apk") -> "apk"
            n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") ||
                n.endsWith(".gif") || n.endsWith(".webp") -> "image"
            else -> "file"
        }
    }

    fun mimeOf(name: String): String = when (kindOf(name)) {
        "txt" -> "text/plain"
        "pdf" -> "application/pdf"
        "word" -> if (name.lowercase().endsWith(".docx"))
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        else "application/msword"
        "pages" -> "application/vnd.apple.pages"
        "archive" -> "application/zip"
        "apk" -> "application/vnd.android.package-archive"
        "image" -> "image/*"
        else -> "application/octet-stream"
    }

    /** txt 读取：先按 UTF-8 严格解，失败再试 GBK（论坛里不少老附件是 GBK） */
    fun readText(file: File): String {
        val bytes = file.readBytes()
        val utf8 = runCatching {
            val dec = Charsets.UTF_8.newDecoder()
            dec.onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
            dec.onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
            dec.decode(java.nio.ByteBuffer.wrap(bytes)).toString()
        }.getOrNull()
        if (utf8 != null) return utf8
        return runCatching { String(bytes, charset("GBK")) }.getOrElse { String(bytes) }
    }

    fun sizeText(size: Long): String = when {
        size >= 1024L * 1024 -> "%.1f MB".format(size / 1024.0 / 1024.0)
        size >= 1024L -> "%.0f KB".format(size / 1024.0)
        else -> "$size B"
    }
}
