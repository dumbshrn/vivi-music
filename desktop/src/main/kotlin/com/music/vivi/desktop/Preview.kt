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
import com.music.vivi.desktop.data.Route
import com.music.vivi.desktop.data.Store
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
    repeat(120) { frame ->
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
    ViviTheme(darkTheme = true) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) { content() }
    }
}

fun main() {
    System.setProperty("skiko.renderApi", "SOFTWARE")
    DesktopInit.initialize()

    shot("home") { Page { App() } }
    shot("home-light") { ViviTheme(darkTheme = false) { App() } }

    shot("album") { Page { AlbumScreen("MPREb_cC2AzCC686k") } }
    shot("artist") { Page { ArtistScreen("UClYV6hHlupm_S_ObS1W-DYw") } }

    Store.navigate(Route.Search)
    Store.searchQuery = "the weeknd"
    shot("search") { Page { App() } }
    Store.back()

    Store.queue.clear()
    Store.queue.add(
        com.music.vivi.desktop.data.Track(
            id = "J7p4bzqLvCw",
            title = "Blinding Lights",
            artists = listOf("The Weeknd"),
            album = "After Hours",
            duration = 200,
            thumbnail = "https://lh3.googleusercontent.com/0u4N8hF0Vf3jR8m0Zl7c1L8Oq3w_KkO7n3d2f5B8Y1s=w544-h544-l90-rj",
        ),
    )
    Store.index = 0
    Store.durationMs = 200_000
    Store.showNowPlaying = true
    shot("now-playing") { Page { App() } }
    Store.showNowPlaying = false

    Store.navigate(Route.Library)
    shot("library") { Page { App() } }

    Store.navigate(Route.Settings)
    shot("settings") { Page { App() } }

    println("done")
}
