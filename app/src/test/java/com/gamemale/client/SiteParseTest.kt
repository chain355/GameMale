package com.gamemale.client

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SiteParseTest {
    private fun load(name: String): String =
        javaClass.classLoader!!.getResourceAsStream(name)!!.readBytes().toString(Charsets.UTF_8)

    @Test
    fun forumIndexParses() {
        val forums = Site.parseForums(load("index.html"))
        println("forums=${forums.size} first=${forums.firstOrNull()}")
        assertTrue(forums.size > 0)
        assertTrue(forums.first().name.isNotBlank())
        assertTrue(forums.first().url.startsWith("https://"))
    }

    @Test
    fun threadListParses() {
        val page = Site.parseForumPage(load("forum.html"))
        println("title=${page.title} cats=${page.cats.size} threads=${page.threads.size} pages=${page.pageCount}")
        page.threads.take(3).forEach { println(it) }
        assertTrue(page.threads.size > 3)
        assertTrue(page.cats.size > 1)
        assertTrue(page.threads.any { it.tid.isNotEmpty() })
    }

    @Test
    fun mobileListParsesAvatarAndKeepsDesktopThreadLink() {
        val html = """
            <header class="header"><h1>和谐动漫</h1></header>
            <div class="threadlist"><ul><li>
            <a class="forumDisplayImgList" href="forum.php?mod=viewthread&tid=200154&mobile=2">
            <div class="threadListTit">
            <div class="h_avatar"><img src="https://www.gamemale.com/uc_server/avatar.php?uid=610657&size=small"></div>
            <h4><div class="count y"><span class="views icon">115</span><span class="replies icon">10</span></div>cdcai</h4>
            <p>发布于 1 小时前</p>
            </div>
            <h3 class="threadSubject"><span>【自购】合集</span></h3>
            </a></li></ul>
            <div class="pg"><strong>1</strong>
            <a href="forum.php?mod=forumdisplay&fid=150&typeid=831&mobile=yes&page=2">2</a>
            <a href="forum.php?mod=forumdisplay&fid=150&typeid=831&mobile=yes&page=343" class="last">... 343</a>
            <span title="共 343 页"> / 343 页</span>
            <a href="forum.php?mod=forumdisplay&fid=150&typeid=831&mobile=yes&page=2" class="nxt">下一页</a>
            </div></div>
        """.trimIndent()
        val page = Site.parseForumPage(html)
        assertEquals("和谐动漫", page.title)
        assertTrue(page.cats.isEmpty())
        assertEquals(1, page.threads.size)
        val t = page.threads[0]
        assertEquals("200154", t.tid)
        assertEquals("cdcai", t.author)
        assertEquals("1 小时前", t.date)
        assertEquals("10", t.replies)
        assertEquals("115", t.views)
        assertNull(t.typeName)
        assertTrue(t.avatar.contains("avatar.php?uid=610657"))
        assertEquals("https://www.gamemale.com/thread-200154-1-1.html", t.url)
        assertEquals(343, page.pageCount)
        assertEquals(1, page.pageNo)
        val p1 = page.pages.first { it.first == 1 }.second
        val p20 = page.pages.first { it.first == 20 }.second
        assertTrue(p1.contains("mobile=yes"))
        assertTrue(p1.contains("page=1"))
        assertTrue(!p1.contains("page=2"))
        assertTrue(p20.contains("page=20"))
        assertTrue(p20.contains("mobile=yes"))
    }

    @Test
    fun webViewMobilePagerUsesPageSelect() {
        val html = """
            <div class="threadlist"><ul><li>
            <a href="forum.php?mod=viewthread&tid=9&mobile=2">
            <div class="threadListTit"><h4>某人</h4><p>发布于 刚刚</p></div>
            <h3 class="threadSubject"><span>标题</span></h3>
            </a></li></ul></div>
            <div class="page">
            <a href="javascript:;" class="grey">上一页</a>
            <select id="dumppage">
            <option value="1" selected>第1页</option>
            <option value="2">第2页</option>
            <option value="287">第287页</option>
            </select>
            <a href="https://www.gamemale.com/forum.php?mod=forumdisplay&amp;fid=150&amp;mobile=yes&amp;page=2">下一页</a>
            </div>
        """.trimIndent()
        val page = Site.parseForumPage(html)
        assertEquals(1, page.pageNo)
        assertEquals(287, page.pageCount)
        val p2 = page.pages.first { it.first == 2 }.second
        val p40 = page.pages.first { it.first == 40 }.second
        assertTrue(p2.contains("page=2"))
        assertTrue(p2.contains("mobile=yes"))
        assertTrue(p40.contains("page=40"))
        assertTrue(p40.contains("mobile=yes"))
    }

    @Test
    fun threadDetailParses() {
        val page = Site.parseThread(load("thread.html"))
        println("subject=${page.subject} posts=${page.posts.size} pages=${page.pageCount}")
        val op = page.posts.first()
        println("op author=${op.author} floor=${op.floor} blocks=${op.blocks.size} att=${op.attachments.size} rating=${op.rating}")
        assertTrue(page.posts.isNotEmpty())
        assertTrue(op.author.isNotBlank())
        assertTrue(op.blocks.isNotEmpty())
        assertTrue(op.attachments.isNotEmpty())
        assertTrue(page.pageCount >= 1)
    }
}
