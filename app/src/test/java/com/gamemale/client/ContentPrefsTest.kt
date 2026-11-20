package com.gamemale.client

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentPrefsTest {
    @Test
    fun hitIgnoresAsciiCase() {
        assertTrue(ContentPrefs.hit("Zephyxus 的新帖", listOf("zepHyxus")))
        assertTrue(ContentPrefs.hit("[资源] ZEPHYXUS 合集", listOf("Zephyxus")))
        assertFalse(ContentPrefs.hit("别的标题", listOf("Zephyxus")))
    }

    @Test
    fun hitSupportsRegex() {
        assertTrue(ContentPrefs.hit("[资源] 3D 模型", listOf("^\\[资源\\]")))
        assertTrue(ContentPrefs.hit("版本 1,2,3 更新", listOf("1,2,3")))
        assertTrue(ContentPrefs.hit("第 12 楼", listOf("第\\s*\\d+\\s*楼")))
    }

    @Test
    fun invalidRegexFallsBackToLiteralContains() {
        assertTrue(ContentPrefs.hit("a[b 标题", listOf("[")))
        assertFalse(ContentPrefs.hit("普通标题", listOf("[")))
    }

    @Test
    fun batchKeepsRegexPunctuationAndSplitsByLine() {
        val parsed = ContentPrefs.parseBatch("  Zephyxus  \nfoo{1,3}\n\nbar;baz")
        assertEquals(listOf("Zephyxus", "foo{1,3}", "bar;baz"), parsed)
    }
}
