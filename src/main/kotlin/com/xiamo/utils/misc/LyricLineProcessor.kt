package com.xiamo.utils.misc




object LyricLineProcessor {


    fun parseLyricLine(text: String): List<LyricLine> {
        val list = mutableListOf<LyricLine>()
        val regex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")
        val s =text.replace("\\n","\n")
        s.lines().forEach { line ->
            val match = regex.matchEntire(line) ?: return@forEach
            val minutes  = match.groupValues[1]
            val seconds = match.groupValues[2]
            val timeMs = minutes.toLong() * 60_000 + (seconds.toLong() * 1000)
            val content = match.groupValues[4]

            list += LyricLine(content, timeMs)
        }

        return list.sortedBy { it.timeMs }
    }

    fun findCurrentIndex(lyrics: List<LyricLine>, timeMs: Long): Int {
        var index = 0
        lyrics.forEachIndexed { i, line ->
            if (line.timeMs <= timeMs) {
                index = i
            } else {
                return index
            }
        }
        return index
    }
}



data class LyricLine(
    val text: String,
    val timeMs : Long,
)