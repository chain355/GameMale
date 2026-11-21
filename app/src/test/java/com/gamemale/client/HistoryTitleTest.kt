package com.gamemale.client

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryTitleTest {
    @Test
    fun rejectsPlaceholderAndErrorTitles() {
        assertFalse(HistoryTitle.usable(null))
        assertFalse(HistoryTitle.usable(""))
        assertFalse(HistoryTitle.usable(" 帖子 "))
        assertFalse(HistoryTitle.usable("网页无法打开"))
        assertFalse(HistoryTitle.usable("无法访问此网站"))
        assertFalse(HistoryTitle.usable("请稍候..."))
    }

    @Test
    fun acceptsRealThreadTitles() {
        assertTrue(HistoryTitle.usable("【自购】【三々御茶】fanbox 2023年合集"))
        assertTrue(HistoryTitle.usable("网页无法打开怎么办？"))
    }

    @Test
    fun resolveKeepsOldRealTitleWhenNewTitleIsPlaceholder() {
        assertEquals("真实标题", HistoryTitle.resolve("网页无法打开", "真实标题"))
        assertEquals("真实标题", HistoryTitle.resolve("帖子", "真实标题"))
    }

    @Test
    fun resolvePrefersNewRealTitle() {
        assertEquals("新标题", HistoryTitle.resolve("新标题", "旧标题"))
        assertEquals("新标题", HistoryTitle.resolve(" 新标题 ", "网页无法打开"))
    }

    @Test
    fun resolveLeavesBlankWhenBothAreBad() {
        assertEquals("", HistoryTitle.resolve("网页无法打开", "帖子"))
    }
}
