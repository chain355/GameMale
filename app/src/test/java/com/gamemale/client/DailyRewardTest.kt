package com.gamemale.client

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DailyRewardTest {

    @Test
    fun signReadsCoinHiddenField() {
        val html = """<ul class="countqian cl"><li><h4>积分奖励</h4>
            <input type="hidden" class="hidnum" id="lxreward" value="3">
        </li></ul>"""
        assertEquals("3 枚金币", DailyReward.sign(html))
    }

    @Test
    fun signIgnoresNonNumericValue() {
        assertNull(DailyReward.sign("""<input id="lxreward" value=""> """))
    }

    @Test
    fun awardUsesLatestRecordAndNormalizesText() {
        val html = """
            <div id="myaward">
              <table>
                <tr><td>2026-11-21 00:33:22 用了 (2 金币 ) 抽到了III 等卡片</td></tr>
                <tr><td>积分奖励：5 血液</td></tr>
                <tr><td>2026-11-20 21:45:05 用了 (2 金币 ) 抽到了II 等卡片</td></tr>
                <tr><td>积分奖励：10 血液</td></tr>
              </table>
            </div>
        """.trimIndent()
        assertEquals(
            "2026-11-21 00:33:22 用了 2 金币抽到 III 等卡片，积分奖励：5 血液",
            DailyReward.latestAward(html)
        )
    }

    @Test
    fun awardSupportsMultipleRewardCredits() {
        val html = """<p>2026-11-14 21:45:05 用了 （2 金币） 抽到 Ⅲ 等卡片 积分奖励：10 血液 2 金币</p>"""
        assertEquals(
            "2026-11-14 21:45:05 用了 2 金币抽到 Ⅲ 等卡片，积分奖励：10 血液 2 金币",
            DailyReward.latestAward(html)
        )
    }

    @Test
    fun awardWithoutTimestampReturnsNull() {
        assertNull(DailyReward.latestAward("""<div id="myaward">暂无抽奖记录</div>"""))
    }
}
