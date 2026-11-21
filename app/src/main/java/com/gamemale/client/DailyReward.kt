package com.gamemale.client

import org.jsoup.Jsoup

/** 每日任务奖励解析：签到只发金币，抽卡取「我的抽奖」最新一条。 */
object DailyReward {

    /** 签到页的“积分奖励”是隐藏域 #lxreward，返回“3 枚金币”这样的文案。 */
    fun sign(html: String?): String? {
        if (html.isNullOrBlank()) return null
        val value = runCatching {
            Jsoup.parse(html).selectFirst("#lxreward")?.attr("value")?.trim()
        }.getOrNull()
        return value?.takeIf { it.matches(Regex("\\d+")) }?.let { "$it 枚金币" }
    }

    /**
     * 抽奖页「我的抽奖」最新一条记录：
     * “2026-11-14 21:45:05 用了 (2 金币) 抽到了 III 等卡片 / 积分奖励：10 血液”
     */
    fun latestAward(html: String?): String? {
        if (html.isNullOrBlank()) return null
        val text = runCatching { Jsoup.parse(html).text() }.getOrNull() ?: return null
        val flat = text.replace(Regex("\\s+"), " ").trim()
        val timeRe = Regex("\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}")
        val first = timeRe.find(flat) ?: return null
        val next = timeRe.find(flat, first.range.last + 1)
        val end = next?.range?.first ?: minOf(flat.length, first.range.first + 240)
        val body = flat.substring(first.range.first, end).trim()
        val m = Regex(
            "(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})\\s*用了\\s*[（(]?\\s*(\\d+)\\s*金币\\s*[)）]?\\s*抽到(?:了)?\\s*(.*?)\\s*积分奖励\\s*[:：]\\s*([0-9]+\\s*[^\\d\\s]+(?:\\s+[0-9]+\\s*[^\\d\\s]+)*)"
        ).find(body)
        if (m != null) {
            return "${m.groupValues[1]} 用了 ${m.groupValues[2]} 金币抽到 ${m.groupValues[3].trim()}，积分奖励：${m.groupValues[4].trim()}"
        }
        return body.takeIf { it.contains("抽到") || it.contains("积分奖励") }?.take(180)
    }
}
