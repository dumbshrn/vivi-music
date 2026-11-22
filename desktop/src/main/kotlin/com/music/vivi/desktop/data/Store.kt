package com.music.vivi.desktop.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.music.vivi.desktop.playback.Playback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application-wide player + library state. Mirrors the responsibility split of
 * the Android app (PlayerConnection / database repositories / preferences) in a
 * compact desktop form.
 */
object Store {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val persisted = Prefs.load()

    var route by mutableStateOf<Route>(Route.Home)
        private set
    var searchQuery by mutableStateOf("")
    val routeStack = mutableStateListOf<Route>()

    val liked = mutableStateListOf<Track>().apply { addAll(persisted.liked) }
    val playlists = mutableStateListOf<LocalPlaylist>().apply { addAll(persisted.playlists) }
    val history = mutableStateListOf<Track>().apply { addAll(persisted.history) }

    var volume by mutableStateOf(persisted.volume)
    var theme by mutableStateOf(runCatching { ThemePreference.valueOf(persisted.theme) }.getOrDefault(ThemePreference.SYSTEM))
    var dynamicColors by mutableStateOf(persisted.dynamicColors)
    var seed by mutableStateOf(Color(persisted.seed))
    var quality by mutableStateOf(runCatching { AudioQuality.valueOf(persisted.quality) }.getOrDefault(AudioQuality.HIGH))
    val providers = mutableStateListOf<String>().apply { addAll(persisted.providers) }
    var radio by mutableStateOf(persisted.radio)
    var artSeed by mutableStateOf<Color?>(null)
    var pureBlack by mutableStateOf(false)

    val queue = mutableStateListOf<Track>()
    var index by mutableStateOf(-1)
    var playing by mutableStateOf(false)
    var buffering by mutableStateOf(false)
    var positionMs by mutableStateOf(0L)
    var durationMs by mutableStateOf(0L)
    var shuffle by mutableStateOf(false)
    var repeat by mutableStateOf(RepeatMode.OFF)
    var showNowPlaying by mutableStateOf(false)
    var showQueue by mutableStateOf(false)
    var playbackError by mutableStateOf<String?>(null)

    var lyrics by mutableStateOf<Lyrics?>(null)
    var lyricsLoading by mutableStateOf(false)
    private var lyricsTrackId: String? = null

    var toast by mutableStateOf<String?>(null)
    private var toastToken = 0L

    val current: Track? get() = queue.getOrNull(index)

    fun navigate(next: Route) {
        routeStack.add(route)
        route = next
    }

    fun back() {
        route = routeStack.removeLastOrNull() ?: Route.Home
    }

    fun notify(message: String) {
        toast = message
        toastToken = System.currentTimeMillis()
        val token = toastToken
        scope.launch {
            kotlinx.coroutines.delay(2600)
            if (toastToken == token) toast = null
        }
    }

    fun save() {
        Prefs.save(
            PersistedState(
                liked = liked.toList(),
                playlists = playlists.toList(),
                history = history.toList(),
                volume = volume,
                theme = theme.name,
                dynamicColors = dynamicColors,
                seed = seed.value.toLong(),
                quality = quality.name,
                providers = providers.toList(),
                radio = radio,
            ),
        )
    }

    // ─── Library ────────────────────────────────────────────────────────────

    fun isLiked(track: Track): Boolean = liked.any { it.id == track.id }

    fun toggleLike(track: Track): Boolean {
        val existing = liked.indexOfFirst { it.id == track.id }
        return if (existing >= 0) {
            liked.removeAt(existing)
            save()
            false
        } else {
            liked.add(0, track)
            save()
            true
        }
    }

    fun addHistory(track: Track) {
        val existing = history.indexOfFirst { it.id == track.id }
        if (existing >= 0) history.removeAt(existing)
        history.add(0, track)
        while (history.size > 200) history.removeAt(history.lastIndex)
        save()
    }

    fun createPlaylist(name: String, tracks: List<Track> = emptyList()): LocalPlaylist {
        val playlist =
            LocalPlaylist(
                id = "local-${System.currentTimeMillis()}-${(1000..9999).random()}",
                name = name.ifBlank { "New playlist" }.trim(),
                createdAt = System.currentTimeMillis(),
                tracks = tracks.distinctBy { it.id },
            )
        playlists.add(0, playlist)
        save()
        return playlist
    }

    fun deletePlaylist(id: String) {
        playlists.removeAll { it.id == id }
        save()
        if (route == Route.LocalPlaylist(id)) back()
    }

    fun renamePlaylist(id: String, name: String) {
        val index = playlists.indexOfFirst { it.id == id }
        if (index < 0) return
        playlists[index] = playlists[index].copy(name = name.trim().ifBlank { playlists[index].name })
        save()
    }

    fun addToPlaylist(id: String, tracks: List<Track>) {
        val index = playlists.indexOfFirst { it.id == id }
        if (index < 0) return
        val playlist = playlists[index]
        playlists[index] = playlist.copy(tracks = (playlist.tracks + tracks).distinctBy { it.id })
        save()
    }

    fun removeFromPlaylist(id: String, trackId: String) {
        val index = playlists.indexOfFirst { it.id == id }
        if (index < 0) return
        val playlist = playlists[index]
        playlists[index] = playlist.copy(tracks = playlist.tracks.filterNot { it.id == trackId })
        save()
    }

    fun localPlaylist(id: String): LocalPlaylist? = playlists.firstOrNull { it.id == id }

    // ─── Playback ───────────────────────────────────────────────────────────

    fun play(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        queue.clear()
        queue.addAll(tracks)
        index = startIndex.coerceIn(0, tracks.lastIndex)
        loadCurrent()
    }

    fun playNext(track: Track) {
        if (queue.isEmpty()) {
            play(listOf(track))
            return
        }
        queue.add(index + 1, track)
        notify("Playing next")
    }

    fun addToQueue(track: Track) {
        if (queue.isEmpty()) {
            play(listOf(track))
            return
        }
        queue.add(track)
        notify("Added to queue")
    }

    fun removeFromQueue(position: Int) {
        if (position !in queue.indices) return
        queue.removeAt(position)
        when {
            position < index -> index -= 1
            position == index -> {
                index = index.coerceAtMost(queue.lastIndex)
                if (queue.isEmpty()) stop() else loadCurrent()
            }
        }
    }

    private fun loadCurrent() {
        val track = current ?: return
        buffering = true
        playbackError = null
        positionMs = 0
        durationMs = track.duration * 1000L
        addHistory(track)
        loadLyrics(track)
        loadSeed(track)
        scope.launch {
            Repository.streamUrl(track.id, quality)
                .onSuccess { url ->
                    Playback.load(url, volume)
                    Playback.play()
                    buffering = false
                }
                .onFailure { error ->
                    buffering = false
                    playbackError = error.message ?: "Playback failed"
                    playing = false
                    notify("Could not play ${track.title}")
                }
        }
    }

    fun toggle() {
        if (current == null) return
        if (playing) {
            Playback.pause()
            playing = false
        } else {
            Playback.play()
            playing = true
        }
    }

    fun next(auto: Boolean = false) {
        if (queue.isEmpty()) return
        if (repeat == RepeatMode.ONE && auto) {
            seekTo(0)
            Playback.play()
            return
        }
        var nextIndex = index + 1
        if (shuffle && queue.size > 1) {
            do nextIndex = (queue.indices).random() while (nextIndex == index)
        }
        if (nextIndex > queue.lastIndex) {
            if (repeat == RepeatMode.ALL) {
                nextIndex = 0
            } else {
                val track = current
                if (radio && track != null) {
                    radioFrom(track)
                    return
                }
                stop()
                return
            }
        }
        index = nextIndex
        loadCurrent()
    }

    private fun radioFrom(track: Track) {
        buffering = true
        scope.launch {
            Repository.radio(track.id)
                .onSuccess { tracks ->
                    val fresh = tracks.filterNot { candidate -> queue.any { it.id == candidate.id } }
                    if (fresh.isEmpty()) {
                        stop()
                        return@onSuccess
                    }
                    queue.addAll(fresh)
                    index = queue.lastIndex - fresh.size + 1
                    loadCurrent()
                }
                .onFailure {
                    buffering = false
                    stop()
                }
        }
    }

    fun previous() {
        if (positionMs > 4000) {
            seekTo(0)
            return
        }
        if (index > 0) {
            index -= 1
            loadCurrent()
        } else {
            seekTo(0)
        }
    }

    fun seekTo(ms: Long) {
        Playback.seek(ms)
        positionMs = ms
    }

    fun changeVolume(value: Float) {
        volume = value.coerceIn(0f, 1f)
        Playback.setVolume(volume)
        save()
    }

    fun applyQuality(value: AudioQuality) {
        quality = value
        save()
    }

    fun stop() {
        Playback.stop()
        playing = false
        buffering = false
        positionMs = 0
    }

    fun startRadio(track: Track) {
        scope.launch {
            Repository.radio(track.id)
                .onSuccess { tracks -> play(listOf(track) + tracks.filterNot { it.id == track.id }) }
                .onFailure { notify("No radio station found") }
        }
    }

    fun toggleShuffle() {
        shuffle = !shuffle
    }

    fun cycleRepeat() {
        repeat =
            when (repeat) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
    }

    private fun loadLyrics(track: Track) {
        if (lyricsTrackId == track.id && lyrics != null) return
        lyricsTrackId = track.id
        lyrics = null
        lyricsLoading = true
        scope.launch {
            val result = runCatching { Repository.lyrics(track, providers.toList()) }.getOrNull()
            if (lyricsTrackId == track.id) {
                lyrics = result
                lyricsLoading = false
            }
        }
    }

    private fun loadSeed(track: Track) {
        if (!dynamicColors) {
            artSeed = null
            return
        }
        scope.launch {
            val color = ColorSeed.extract(track.thumbnail)
            if (current?.id == track.id) artSeed = color
        }
    }

    fun applyDynamicColors(enabled: Boolean) {
        dynamicColors = enabled
        save()
        if (!enabled) artSeed = null else current?.let { loadSeed(it) }
    }

    fun setSeed(color: Color, fromTrack: Boolean = false) {
        seed = color
        save()
    }

    fun applyTheme(preference: ThemePreference) {
        theme = preference
        save()
    }

    fun toggleRadio() {
        radio = !radio
        save()
    }

    fun setProviders(list: List<String>) {
        providers.clear()
        providers.addAll(list)
        save()
    }
}
