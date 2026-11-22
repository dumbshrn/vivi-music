package com.music.vivi.desktop.data

/** Parses LRC (including enhanced word-synced LRC) and TTML lyrics. */
object LyricsParser {
    private val timeTag = Regex("""\[(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?]""")
    private val wordTag = Regex("""<(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?>([^<]*)""")

    fun parse(raw: String): Lyrics {
        val text = raw.trim()
        if (text.isEmpty()) return Lyrics("", Lyrics.Sync.NONE, emptyList(), true)
        if (text.startsWith("<tt", ignoreCase = true) || (text.contains("<p ") && text.contains("begin="))) {
            val lines = parseTtml(text)
            return Lyrics(
                provider = "",
                sync = if (lines.any { it.words.isNotEmpty() }) Lyrics.Sync.WORD else Lyrics.Sync.LINE,
                lines = lines,
            )
        }
        if (timeTag.containsMatchIn(text)) {
            val lines = parseLrc(text)
            return Lyrics(
                provider = "",
                sync = if (lines.any { it.words.size > 1 }) Lyrics.Sync.WORD else Lyrics.Sync.LINE,
                lines = lines,
            )
        }
        return Lyrics(
            provider = "",
            sync = Lyrics.Sync.NONE,
            lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }.map { LyricLine(-1, it) },
            plain = true,
        )
    }

    private fun toMillis(minutes: String, seconds: String, fraction: String?): Long {
        val ms =
            when (fraction?.length) {
                1 -> fraction.toLong() * 100
                2 -> fraction.toLong() * 10
                3 -> fraction.toLong()
                else -> 0
            }
        return minutes.toLong() * 60_000 + seconds.toLong() * 1_000 + ms
    }

    private fun parseLrc(text: String): List<LyricLine> {
        val result = mutableListOf<LyricLine>()
        for (rawLine in text.lines()) {
            val stamps = timeTag.findAll(rawLine).toList()
            if (stamps.isEmpty()) continue
            val body = rawLine.replace(timeTag, "").trim()
            if (body.isEmpty()) continue
            val words =
                wordTag.findAll(body)
                    .map {
                        LyricWord(
                            timeMs = toMillis(it.groupValues[1], it.groupValues[2], it.groupValues[3].ifBlank { null }),
                            text = it.groupValues[4].trim(),
                        )
                    }
                    .filter { it.text.isNotEmpty() }
                    .toList()
            val lineText = body.replace(wordTag, "").trim().ifEmpty { words.joinToString(" ") { it.text } }
            val start =
                toMillis(
                    stamps.first().groupValues[1],
                    stamps.first().groupValues[2],
                    stamps.first().groupValues[3].ifBlank { null },
                )
            result += LyricLine(start, lineText, if (words.size > 1) words else emptyList())
        }
        return result.sortedBy { it.timeMs }
    }

    private fun parseTtml(ttml: String): List<LyricLine> {
        val paragraphs = Regex("""<p\b[^>]*>[\s\S]*?</p>""").findAll(ttml).toList()
        val result = mutableListOf<LyricLine>()
        for (paragraph in paragraphs) {
            val block = paragraph.value
            val begin = Regex("""begin="([^"]+)"""").find(block)?.groupValues?.get(1) ?: continue
            val words =
                Regex("""<span\b[^>]*begin="([^"]+)"[^>]*>([\s\S]*?)</span>""")
                    .findAll(block)
                    .map { match ->
                        LyricWord(secondsToMillis(match.groupValues[1]), stripTags(match.groupValues[2]).trim())
                    }
                    .filter { it.text.isNotEmpty() }
                    .toList()
            val plainText = stripTags(block.replace(Regex("""<span\b[^>]*>[\s\S]*?</span>"""), "")).trim()
            val lineText = plainText.ifEmpty { words.joinToString(" ") { it.text } }
            if (lineText.isEmpty()) continue
            result += LyricLine(secondsToMillis(begin), lineText, if (words.size > 1) words else emptyList())
        }
        return result.sortedBy { it.timeMs }
    }

    private fun secondsToMillis(value: String): Long {
        val clean = value.trim().removeSuffix("s")
        return when {
            clean.contains(":") -> {
                val parts = clean.split(":")
                when (parts.size) {
                    3 -> ((parts[0].toDouble() * 3600) + (parts[1].toDouble() * 60) + parts[2].toDouble()).times(1000).toLong()
                    2 -> ((parts[0].toDouble() * 60) + parts[1].toDouble()).times(1000).toLong()
                    else -> (parts[0].toDoubleOrNull() ?: 0.0).times(1000).toLong()
                }
            }
            else -> ((clean.toDoubleOrNull() ?: 0.0) * 1000).toLong()
        }
    }

    private fun stripTags(value: String): String =
        value
            .replace(Regex("<[^>]+>"), "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .replace(Regex("\\s+"), " ")
}
