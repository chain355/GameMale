package com.gamemale.client

import org.junit.Assert.assertEquals
import org.junit.Test

class TitleKeywordsTest {
    @Test
    fun splitTagAndJapaneseTitle() {
        val title = "【AI汉化】【西音寺エリザベス】fanbox 2022-10-12 既刊限定：いろんな大きさが師匠を超えちゃいました"
        assertEquals(
            listOf(
                "AI汉化",
                "西音寺エリザベス",
                "fanbox",
                "2022-10-12",
                "既刊限定",
                "いろんな大きさ",
                "師匠を超えちゃいました"
            ),
            TitleKeywords.split(title)
        )
    }

    @Test
    fun splitDedupesRepeatedTagsIgnoringCase() {
        assertEquals(
            listOf("AI汉化", "自购", "fanbox", "新作"),
            TitleKeywords.split("【AI汉化】【自购】【自购】【ai汉化】fanbox 新作")
        )
    }

    @Test
    fun splitLatinAndChineseRunsWithoutSpaces() {
        assertEquals(
            listOf("zephyxus", "我爱你"),
            TitleKeywords.split("zephyxus我爱你")
        )
        assertEquals(
            listOf("zephyxus", "我爱你", "AI汉化"),
            TitleKeywords.split("zephyxus我爱你 AI汉化")
        )
    }

    @Test
    fun splitMixedBracketTagButKeepShortAcronym() {
        assertEquals(
            listOf("zephyxus", "我爱你", "AI汉化"),
            TitleKeywords.split("【zephyxus我爱你】【AI汉化】")
        )
    }

    @Test
    fun splitKeepsShortAcronymWithChineseSuffix() {
        assertEquals(listOf("AI汉化", "4K修复"), TitleKeywords.split("【AI汉化】4K修复"))
    }

    @Test
    fun splitKeepsBracketWordsAndDropsBlankTitle() {
        assertEquals(emptyList<String>(), TitleKeywords.split("   "))
        assertEquals(listOf("自购", "三々御茶"), TitleKeywords.split("【自购】【三々御茶】"))
    }
}
