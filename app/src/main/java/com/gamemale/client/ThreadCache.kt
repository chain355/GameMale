package com.gamemale.client

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * 帖子本地缓存：把抓到的整页 HTML 连同标题/最后编辑时间存到 APP 私有目录，
 * 下次打开同一条帖子先本地还原，再后台比对标题与编辑时间决定是否替换。
 */
/** 正文图片的本地存图（不是只存链接）：按 url 哈希落盘，展示/相册都直接用本地文件 */
object ImageStore {
    private var dir: File? = null

    fun init(ctx: Context) {
        val d = File(ctx.applicationContext.filesDir, "images")
        d.mkdirs()
        dir = d
    }

    fun fileFor(url: String): File? {
        val d = dir ?: return null
        return File(d, Integer.toHexString(url.hashCode()) + ".img")
    }

    fun local(url: String): File? = fileFor(url)?.takeIf { it.exists() && it.length() > 0 }

    fun hasUrl(url: String): Boolean = local(url) != null

    fun save(url: String, bytes: ByteArray): Boolean {
        val f = fileFor(url) ?: return false
        return runCatching { f.writeBytes(bytes); true }.getOrDefault(false)
    }

    fun readBytes(url: String): ByteArray? = local(url)?.let { runCatching { it.readBytes() }.getOrNull() }

    fun files(): List<File> = dir?.listFiles()?.toList() ?: emptyList()

    fun totalBytes(): Long = files().sumOf { it.length() }

    fun count(): Int = dir?.listFiles()?.size ?: 0

    fun clear() {
        dir?.listFiles()?.forEach { it.delete() }
    }
}

/** 帖子首图（列表预览图）的持久缓存：tid -> 图片 url 列表，避免列表刷新后又去联网抓预览图 */
object ThumbStore {
    private val map = LinkedHashMap<String, List<String>>()
    private var file: File? = null

    fun init(ctx: Context) {
        val f = File(ctx.applicationContext.filesDir, "thumbs.json")
        file = f
        runCatching {
            if (!f.exists()) return
            val root = JSONObject(f.readText())
            val arr = root.optJSONArray("items") ?: return
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val tid = o.optString("tid")
                val urls = o.optJSONArray("urls") ?: continue
                val list = ArrayList<String>()
                for (j in 0 until urls.length()) list.add(urls.getString(j))
                if (tid.isNotEmpty() && list.isNotEmpty()) map[tid] = list
            }
        }
    }

    fun get(tid: String): List<String> = map[tid] ?: emptyList()

    fun put(tid: String, urls: List<String>) {
        if (urls.isEmpty()) return
        map[tid] = urls
        val f = file ?: return
        runCatching {
            if (map.size > 500) map.remove(map.keys.first())
            val arr = org.json.JSONArray()
            map.forEach { (k, v) ->
                arr.put(JSONObject().put("tid", k).put("urls", org.json.JSONArray(v)))
            }
            f.writeText(JSONObject().put("items", arr).toString())
        }
    }

    fun clear() {
        map.clear()
        file?.delete()
    }
}

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

    fun has(tid: String): Boolean = index[tid]?.let { fileOf(tid).exists() } ?: false

    fun totalBytes(): Long = index.values.sumOf { it.bytes } + runCatching { indexFile.length() }.getOrDefault(0L) +
        ImageStore.totalBytes()

    fun count(): Int = index.size

    fun clear() {
        runCatching {
            dir.listFiles()?.forEach { it.delete() }
            index.clear()
            ImageStore.clear()
        }
        dir.mkdirs()
        saveIndex()
    }

    private fun enforceLimit() {
        val limit = limitMb.toLong() * 1024L * 1024L
        var total = totalBytes()
        if (total > limit) {
            // 图片超限时按修改时间从旧到新清理
            for (f in ImageStore.files().sortedBy { it.lastModified() }) {
                if (totalBytes() <= limit) break
                f.delete()
            }
            total = totalBytes()
        }
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

/**
 * 列表快照：把「版块/搜索结果列表」的结构（标题、tid、作者、时间、预览图地址）存到磁盘，
 * 冷启动先拿它铺一屏，再静默刷新替换，避免每次启动都空等整页加载。
 */
object SnapshotStore {
    private var dir: File? = null
    private val mem = HashMap<String, ForumPage>()

    fun init(ctx: Context) {
        dir = File(ctx.applicationContext.filesDir, "lists").apply { mkdirs() }
    }

    private fun fileOf(url: String): File? {
        val d = dir ?: return null
        return File(d, Integer.toHexString(url.hashCode()) + ".json")
    }

    fun get(url: String): ForumPage? {
        mem[url]?.let { return it }
        val f = fileOf(url) ?: return null
        if (!f.exists()) return null
        return runCatching {
            val o = JSONObject(f.readText())
            if (o.optString("url") != url) return null
            val arr = o.optJSONArray("threads") ?: return null
            val threads = ArrayList<ThreadItem>()
            for (i in 0 until arr.length()) {
                val t = arr.getJSONObject(i)
                val thumbs = ArrayList<String>()
                t.optJSONArray("thumbs")?.let { ta ->
                    for (j in 0 until ta.length()) thumbs.add(ta.getString(j))
                }
                threads.add(
                    ThreadItem(
                        tid = t.optString("tid"),
                        title = t.optString("title"),
                        url = t.optString("url"),
                        typeName = t.optString("type").ifEmpty { null },
                        author = t.optString("author"),
                        date = t.optString("date"),
                        replies = t.optString("replies"),
                        views = t.optString("views"),
                        sticky = t.optBoolean("sticky"),
                        thumbs = thumbs
                    )
                )
            }
            if (threads.isEmpty()) return null
            ForumPage(
                title = o.optString("title"),
                cats = emptyList(),
                threads = threads,
                pages = emptyList(),
                pageNo = o.optInt("pageNo", 1),
                pageCount = o.optInt("pageCount", 1)
            ).also { mem[url] = it }
        }.getOrNull()
    }

    fun put(url: String, page: ForumPage) {
        if (page.threads.isEmpty()) return
        mem[url] = page
        val f = fileOf(url) ?: return
        runCatching {
            val arr = org.json.JSONArray()
            for (t in page.threads.take(60)) {
                arr.put(
                    JSONObject()
                        .put("tid", t.tid).put("title", t.title).put("url", t.url)
                        .put("type", t.typeName ?: "").put("author", t.author)
                        .put("date", t.date).put("replies", t.replies).put("views", t.views)
                        .put("sticky", t.sticky)
                        .put("thumbs", org.json.JSONArray(t.thumbs))
                )
            }
            f.writeText(
                JSONObject().put("url", url).put("title", page.title)
                    .put("pageNo", page.pageNo).put("pageCount", page.pageCount)
                    .put("threads", arr).toString()
            )
            // 最多留 8 个列表快照
            dir?.listFiles()?.sortedByDescending { it.lastModified() }?.drop(8)?.forEach { it.delete() }
        }
    }

    fun clear() {
        mem.clear()
        dir?.listFiles()?.forEach { it.delete() }
    }
}
