package com.music.vivi.desktop

import com.music.innertube.YouTube
import com.music.innertube.models.YouTubeLocale
import kotlinx.coroutines.runBlocking

/**
 * Mirrors the Android app's startup wiring: YouTube Music needs a locale and
 * visitor data before browse/search requests are accepted.
 */
object DesktopInit {
    private var initialized = false

    fun initialize() {
        if (initialized) return
        initialized = true

        YouTube.locale = YouTubeLocale(gl = "US", hl = "en")
        runBlocking {
            runCatching {
                val visitor = YouTube.visitorData().getOrNull()
                if (!visitor.isNullOrBlank()) YouTube.visitorData = visitor
            }
        }
    }
}
