package com.gamemale.client

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
