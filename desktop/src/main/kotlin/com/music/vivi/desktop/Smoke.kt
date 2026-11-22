package com.music.vivi.desktop

import com.music.vivi.desktop.data.AudioQuality
import com.music.vivi.desktop.data.MediaCard
import com.music.vivi.desktop.data.Repository
import kotlinx.coroutines.runBlocking

/**
 * Headless verification of the ported data layer (no UI, no audio): run with
 * `gradle smoke`.
 */
fun main() =
    runBlocking {
        DesktopInit.initialize()
        println("== HOME ==")
        Repository.home()
            .onSuccess { sections ->
                sections.take(6).forEach { section ->
                    println("• ${section.title} (${section.cards.size}) -> ${section.cards.firstOrNull()?.title}")
                }
            }
            .onFailure { println("HOME FAILED: ${it.message}") }

        println("== SEARCH ==")
        var firstSong: String? = null
        Repository.searchAll("blinding lights").forEach { section ->
            println("• ${section.title} (${section.cards.size}) -> ${section.cards.take(2).map { it.title }}")
            if (firstSong == null) {
                firstSong = section.cards.filterIsInstance<MediaCard.SongCard>().firstOrNull()?.track?.id
            }
        }

        println("== ARTIST ==")
        Repository.artist("UClYV6hHlupm_S_ObS1W-DYw")
            .onSuccess { artist ->
                println("${artist.artist.title} • ${artist.subscriberCount} • topSongs=${artist.topSongs.size} • sections=${artist.sections.size}")
            }
            .onFailure { println("ARTIST FAILED: ${it.message}") }

        println("== ALBUM ==")
        Repository.album("MPREb_cC2AzCC686k")
            .onSuccess { album -> println("${album.album.title} • ${album.songs.size} songs • first=${album.songs.firstOrNull()?.title}") }
            .onFailure { println("ALBUM FAILED: ${it.message}") }

        println("== LYRICS (LrcLib) ==")
        val lyrics =
            Repository.lyrics(
                com.music.vivi.desktop.data.Track(
                    id = "J7p4bzqLvCw",
                    title = "Blinding Lights",
                    artists = listOf("The Weeknd"),
                    album = "After Hours",
                    duration = 200,
                ),
                listOf("LrcLib", "Better Lyrics", "YouLyPlus", "Unison"),
            )
        println("provider=${lyrics.provider} sync=${lyrics.sync} lines=${lyrics.lines.size} first=${lyrics.lines.firstOrNull()?.text}")

        println("== STREAM ==")
        firstSong?.let { id ->
            Repository.streamUrl(id, AudioQuality.HIGH)
                .onSuccess { url -> println("stream url: ${url.take(90)}… (${url.length} chars)") }
                .onFailure { println("STREAM FAILED: ${it.message}") }
        }
        println("== DONE ==")
    }
