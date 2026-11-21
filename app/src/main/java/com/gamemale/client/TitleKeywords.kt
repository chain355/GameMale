package com.gamemale.client

/**
 * 把帖子标题拆成可加入词库的候选词。
 *
 * 优先取【】、[]、（）里的标签；剩余标题按空格和标点切分。
 * 中英文直接相连时按字符类型断开，例如「zephyxus我爱你」拆成「zephyxus」「我爱你」；
 * 但「AI汉化」「4K修复」这类 1~3 位缩写与中文组合保留整体。
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

        // 1. 方括号/圆括号里的标签最像关键词，先收进来；混排标签也拆开
        bracket.findAll(raw).forEach { match ->
            match.groupValues.drop(1).firstOrNull { it.isNotBlank() }
                ?.let(::splitMixed)
                ?.forEach(::add)
        }

        // 2. 去掉括号内容后，按空格、冒号、逗号、竖线等切分
        val rest = raw.replace(bracket, " ")
        separator.split(rest).forEach { part ->
            val token = part.trim().trim(*edgeMarks.toCharArray()).trim()
            if (!keep(token)) return@forEach
            splitMixed(token).forEach { piece ->
                // 很长的日文句子按助词断成两段；中文标题通常不含这些假名，不会被误切
                val pieces = if (piece.length > 12 && piece.any { it in 'ぁ'..'ん' || it in 'ァ'..'ン' }) {
                    jpParticle.split(piece).map { it.trim() }.filter { keep(it) }
                } else {
                    emptyList()
                }
                if (pieces.size >= 2) pieces.forEach(::add) else add(piece)
            }
        }

        if (out.size <= MAX) return out.values.toList()
        return out.values.take(MAX)
    }

    /**
     * 中英文直接相连的片段按字符类型断开。
     * 只断「英文 / 数字」与「中日文」的交界；交界处英文侧不超过 3 个字符时视为缩写，保留整体。
     */
    private fun splitMixed(token: String): List<String> {
        if (token.length < 2) return listOf(token)
        val cuts = ArrayList<Int>()
        for (i in 1 until token.length) {
            val left = token[i - 1]
            val right = token[i]
            val crosses = (isAsciiWord(left) && isCjk(right)) || (isCjk(left) && isAsciiWord(right))
            if (!crosses) continue
            val asciiRun = if (isAsciiWord(left)) {
                var start = i - 1
                while (start > 0 && isAsciiWord(token[start - 1])) start--
                i - start
            } else {
                var end = i
                while (end < token.length && isAsciiWord(token[end])) end++
                end - i
            }
            if (asciiRun > 3) cuts.add(i)
        }
        if (cuts.isEmpty()) return listOf(token)
        val pieces = ArrayList<String>()
        var from = 0
        cuts.forEach { cut ->
            pieces.add(token.substring(from, cut))
            from = cut
        }
        pieces.add(token.substring(from))
        return pieces.map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun isAsciiWord(c: Char): Boolean =
        c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' || c == '-' || c == '_' || c == '.' || c == '\''

    private fun isCjk(c: Char): Boolean =
        c in '\u3040'..'\u30FF' || c in '\u3400'..'\u9FFF' ||
            c in '\uF900'..'\uFAFF' || c in '\uAC00'..'\uD7AF'

    private fun keep(token: String): Boolean {
        if (token.isEmpty()) return false
        if (token.length == 1 && token[0].isDigit()) return false
        if (token.all { it.isWhitespace() || it in "，。！？!?、；;：:·•-—–_/\\|" }) return false
        return true
    }
}
