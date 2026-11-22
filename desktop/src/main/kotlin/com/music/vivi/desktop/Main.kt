package com.music.vivi.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.music.vivi.desktop.ui.App
import javafx.application.Platform

fun main() {
    DesktopInit.initialize()
    runCatching { Platform.startup {} }
    runCatching { System.setProperty("prism.lcdtext", "false") }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "VIVI Music",
            state = rememberWindowState(width = 1440.dp, height = 920.dp),
        ) {
            App()
        }
    }
}
