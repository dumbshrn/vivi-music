package com.music.vivi.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import com.music.vivi.desktop.data.LyricLine
import com.music.vivi.desktop.data.Lyrics
import com.music.vivi.desktop.data.Route
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.data.Track
import com.music.vivi.desktop.ui.App
import com.music.vivi.desktop.ui.screens.AlbumScreen
import com.music.vivi.desktop.ui.screens.ArtistScreen
import com.music.vivi.desktop.ui.theme.ViviTheme
import org.jetbrains.skia.EncodedImageFormat
import java.io.File

/**
 * Renders the real Compose UI off-screen to PNGs so the desktop layout can be
 * inspected without a display server. `gradle screenshots`.
 */
@OptIn(ExperimentalComposeUiApi::class)
private fun shot(
    name: String,
    width: Int = 1440,
    height: Int = 900,
    content: @Composable () -> Unit,
) {
    val scene = ImageComposeScene(width = width, height = height, density = Density(1f), content = content)
    var image = scene.render(0)
    // Pump a few frames so async content and animations settle.
    repeat(140) { frame ->
        Thread.sleep(40)
        image = scene.render(frame * 40_000_000L)
    }
    val bytes = image.encodeToData(EncodedImageFormat.PNG)?.bytes
    val target = File("build/screenshots/$name.png")
    target.parentFile.mkdirs()
    target.writeBytes(bytes ?: ByteArray(0))
    println("wrote ${target.absolutePath} (${target.length()} bytes)")
    scene.close()
}

@Composable
private fun Page(content: @Composable () -> Unit) {
    ViviTheme(darkTheme = true, seed = Store.seed, pureBlack = Store.pureBlack) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onSurface,
        ) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) { content() }
        }
    }
}

fun main() {
    System.setProperty("skiko.renderApi", "SOFTWARE")
    DesktopInit.initialize()

    val demo =
        Track(
            id = "J7p4bzqLvCw",
            title = "Blinding Lights",
            artists = listOf("The Weeknd"),
            album = "After Hours",
            duration = 200,
            thumbnail = "https://i.ytimg.com/vi/4NRXx6U8ABQ/hqdefault.jpg",
        )
    Store.queue.clear()
    Store.queue.add(demo)
    Store.index = 0
    Store.durationMs = 200_000
    Store.positionMs = 68_000

    shot("home") { Page { App() } }

    Store.navigate(Route.Explore)
    shot("explore") { Page { App() } }
    Store.back()

    Store.navigate(Route.Library)
    shot("library") { Page { App() } }
    Store.back()

    Store.navigate(Route.Search)
    Store.searchQuery = "the weeknd"
    shot("search") { Page { App() } }
    Store.back()

    Store.navigate(Route.Settings)
    shot("settings") { Page { App() } }
    Store.back()

    Store.navigate(Route.Account)
    shot("account") { Page { App() } }
    Store.back()

    Store.navigate(Route.History)
    shot("history") { Page { App() } }
    Store.back()

    Store.navigate(Route.Stats)
    shot("stats") { Page { App() } }
    Store.back()

    Store.navigate(Route.Charts)
    shot("charts") { Page { App() } }
    Store.back()

    Store.navigate(Route.MoodGenres)
    shot("moods") { Page { App() } }
    Store.back()

    Store.navigate(Route.NewReleases)
    shot("new-releases") { Page { App() } }
    Store.back()

    Store.showNowPlaying = true
    shot("now-playing") { Page { App() } }

    Store.lyrics =
        Lyrics(
            provider = "LrcLib",
            sync = Lyrics.Sync.LINE,
            lines =
                listOf(
                    "Yeah", "I've been on my own for long enough", "Maybe you can show me how to love, maybe",
                    "I'm going through withdrawals", "You don't even have to do too much", "You can turn me on with just a touch, baby",
                    "I look around and Sin City's cold and empty", "No one's around to judge me",
                ).mapIndexed { index, line -> LyricLine(timeMs = index * 4_000L, text = line) },
        )
    Store.showLyrics = true
    shot("lyrics") { Page { App() } }
    Store.showLyrics = false
    Store.lyrics = null
    Store.showNowPlaying = false

    Store.queue.addAll(
        listOf(
            demo.copy(id = "a", title = "Save Your Tears", duration = 215),
            demo.copy(id = "b", title = "Starboy", duration = 230),
            demo.copy(id = "c", title = "Die For You", duration = 260),
        ),
    )
    Store.showQueue = true
    shot("queue") { Page { App() } }
    Store.showQueue = false

    shot("album") { Page { AlbumScreen("MPREb_cC2AzCC686k") } }
    shot("artist") { Page { ArtistScreen("UClYV6hHlupm_S_ObS1W-DYw") } }

    println("done")
}
