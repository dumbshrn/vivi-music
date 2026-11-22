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
        return runCatching {
            if (!Files.exists(file)) return PersistedState()
            json.decodeFromString<PersistedState>(Files.readString(file))
        }.getOrElse { PersistedState() }
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
