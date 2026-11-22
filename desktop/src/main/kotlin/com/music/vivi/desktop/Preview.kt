package com.music.vivi.desktop

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import com.music.vivi.desktop.data.Route
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.data.ThemePreference
import com.music.vivi.desktop.ui.App
import com.music.vivi.desktop.ui.screens.AlbumScreen
import com.music.vivi.desktop.ui.screens.LibraryScreen
import com.music.vivi.desktop.ui.screens.SearchScreen
import com.music.vivi.desktop.ui.screens.SettingsScreen
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

fun main() {
    System.setProperty("skiko.renderApi", "SOFTWARE")
    DesktopInit.initialize()

    shot("now-playing") { ViviTheme(darkTheme = true) { App() } }
    shot("light") { ViviTheme(darkTheme = false) { App() } }

    Store.navigate(Route.Search)
    Store.searchQuery = "the weeknd"
    shot("search") { ViviTheme(darkTheme = true) { App() } }

    Store.navigate(Route.Library)
    shot("library") { ViviTheme(darkTheme = true) { App() } }

    Store.navigate(Route.Settings)
    shot("settings") { ViviTheme(darkTheme = true) { App() } }

    println("done")
}
