package com.gamemale.client

/**
 * 把帖子标题拆成可加入词库的候选词。
 *
 * 优先取【】、[]、（）里的标签；剩余标题按空格和标点切分。
 * 很长的日文片段再按常见助词切一次，避免整句只能整体加入词库。
 */
object TitleKeywords {
    private val bracket = Regex("【([^】]*)】|\\[([^\\]]*)\\]|（([^）]*)）|\\(([^)]*)\\)")
    private val separator = Regex("[\\s:：;；,，、|｜/／]+")
    private val jpParticle = Regex("(?<=.)(が|は|も)(?=.)")
    private val edgeMarks = " \t\r\n-—–·•*"

    private const val MAX = 16

    fun split(title: String): List<String> {
        val raw = title.trim()
        if (raw.isEmpty()) return emptyList()
        val out = LinkedHashMap<String, String>()

        fun add(value: String) {
            val token = value.trim().trim(*edgeMarks.toCharArray()).trim()
            if (!keep(token)) return
            val key = token.lowercase()
            if (!out.containsKey(key)) out[key] = token
        }

        // 1. 方括号/圆括号里的标签最像关键词，先收进来
        bracket.findAll(raw).forEach { match ->
            match.groupValues.drop(1).firstOrNull { it.isNotBlank() }?.let(::add)
        }

        // 2. 去掉括号内容后，按空格、冒号、逗号、竖线等切分
        val rest = raw.replace(bracket, " ")
        separator.split(rest).forEach { part ->
            val token = part.trim().trim(*edgeMarks.toCharArray()).trim()
            if (!keep(token)) return@forEach
            // 很长的日文句子按助词断成两段；中文标题通常不含这些假名，不会被误切
            val pieces = if (token.length > 12 && token.any { it in 'ぁ'..'ん' || it in 'ァ'..'ン' }) {
                jpParticle.split(token).map { it.trim() }.filter { keep(it) }
            } else {
                emptyList()
            }
            if (pieces.size >= 2) pieces.forEach(::add) else add(token)
        }

        if (out.size <= MAX) return out.values.toList()
        return out.values.take(MAX)
    }

    private fun keep(token: String): Boolean {
        if (token.isEmpty()) return false
        if (token.length == 1 && token[0].isDigit()) return false
        if (token.all { it.isWhitespace() || it in "，。！？!?、；;：:·•-—–_/\\|" }) return false
        return true
    }
}
