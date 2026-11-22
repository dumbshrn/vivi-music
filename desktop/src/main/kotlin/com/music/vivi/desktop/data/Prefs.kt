package com.music.vivi.desktop.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/** Persists the library and settings as JSON in the user's home directory. */
object Prefs {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val scope = CoroutineScope(Dispatchers.IO)
    private val directory: Path = Paths.get(System.getProperty("user.home"), ".vivi-music")
    private val file: Path = directory.resolve("library.json")

    fun load(): PersistedState {
        val loaded =
            runCatching {
                if (!Files.exists(file)) return PersistedState()
                json.decodeFromString<PersistedState>(Files.readString(file))
            }.getOrElse { PersistedState() }

        // Version 1 shipped the old red seed with a light/system theme. Existing
        // installs are moved onto the app's warm black + gold look once.
        return if (loaded.styleVersion < 2) {
            loaded.copy(
                theme = ThemePreference.DARK.name,
                dynamicColors = false,
                seed = 0xFFC8B273,
                pureBlack = true,
                styleVersion = 2,
            )
        } else {
            loaded
        }
    }

    fun save(state: PersistedState) {
        scope.launch {
            runCatching {
                Files.createDirectories(directory)
                val temporary = directory.resolve("library.json.tmp")
                Files.writeString(temporary, json.encodeToString(state))
                Files.move(temporary, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    fun cacheDirectory(): Path {
        val path = directory.resolve("cache")
        Files.createDirectories(path)
        return path
    }
}
