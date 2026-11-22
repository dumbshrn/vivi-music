package com.music.vivi.desktop.data

import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.YTItem
import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val id: String,
    val title: String,
    val artists: List<String>,
    val album: String? = null,
    val albumId: String? = null,
    val duration: Int = 0,
    val thumbnail: String = "",
) {
    val artistLine: String get() = artists.joinToString(", ")
}

fun SongItem.toTrack(): Track =
    Track(
        id = id,
        title = title,
        artists = artists.map { it.name }.ifEmpty { listOf("Unknown artist") },
        album = album?.name,
        albumId = album?.id,
        duration = duration ?: 0,
        thumbnail = thumbnail,
    )

fun YTItem.toTrackOrNull(): Track? = (this as? SongItem)?.toTrack()

/** A card-friendly projection of any YouTube Music item. */
@Serializable
sealed class MediaCard {
    abstract val browseId: String
    abstract val title: String
    abstract val thumbnail: String

    @Serializable
    data class Album(
        override val browseId: String,
        override val title: String,
        override val thumbnail: String,
        val artists: String,
        val year: Int? = null,
        val playlistId: String,
    ) : MediaCard()

    @Serializable
    data class Artist(
        override val browseId: String,
        override val title: String,
        override val thumbnail: String,
    ) : MediaCard()

    @Serializable
    data class Playlist(
        override val browseId: String,
        override val title: String,
        override val thumbnail: String,
        val author: String,
    ) : MediaCard()

    @Serializable
    data class SongCard(
        override val browseId: String,
        override val title: String,
        override val thumbnail: String,
        val track: Track,
    ) : MediaCard()
}

fun YTItem.toCard(): MediaCard? =
    when (this) {
        is AlbumItem ->
            MediaCard.Album(
                browseId = browseId,
                title = title,
                thumbnail = thumbnail,
                artists = artists?.joinToString(", ") { it.name } ?: "",
                year = year,
                playlistId = playlistId,
            )

        is ArtistItem ->
            MediaCard.Artist(browseId = id, title = title, thumbnail = thumbnail ?: "")

        is PlaylistItem ->
            MediaCard.Playlist(browseId = id, title = title, thumbnail = thumbnail ?: "", author = author?.name ?: "")

        is SongItem ->
            MediaCard.SongCard(browseId = id, title = title, thumbnail = thumbnail, track = toTrack())

        else -> null
    }

data class LyricLine(val timeMs: Long, val text: String, val words: List<LyricWord> = emptyList())

data class LyricWord(val timeMs: Long, val text: String)

data class Lyrics(
    val provider: String,
    val sync: Sync,
    val lines: List<LyricLine>,
    val plain: Boolean = false,
) {
    enum class Sync { WORD, LINE, NONE }
}

data class UiSection(val title: String, val cards: List<MediaCard>)

data class AlbumDetails(val album: AlbumItem, val songs: List<Track>, val description: String?)

data class ArtistDetails(
    val artist: ArtistItem,
    val sections: List<UiSection>,
    val topSongs: List<Track>,
    val subscriberCount: String?,
    val monthlyListeners: String?,
)

data class PlaylistDetails(val playlist: PlaylistItem, val songs: List<Track>, val related: List<MediaCard>)

sealed interface Route {
    data object Home : Route
    data object Explore : Route
    data object Search : Route
    data object Library : Route
    data object Settings : Route
    data class Album(val id: String) : Route
    data class Artist(val id: String) : Route
    data class Playlist(val id: String) : Route
    data class LocalPlaylist(val id: String) : Route
}

@Serializable
data class LocalPlaylist(
    val id: String,
    val name: String,
    val createdAt: Long,
    val tracks: List<Track>,
)

enum class RepeatMode { OFF, ALL, ONE }

enum class ThemePreference { SYSTEM, LIGHT, DARK }

enum class AudioQuality { LOW, NORMAL, HIGH }

@Serializable
data class PersistedState(
    val liked: List<Track> = emptyList(),
    val playlists: List<LocalPlaylist> = emptyList(),
    val history: List<Track> = emptyList(),
    val volume: Float = 0.85f,
    val theme: String = ThemePreference.SYSTEM.name,
    val dynamicColors: Boolean = true,
    val seed: Long = 0xFFED5564,
    val quality: String = AudioQuality.HIGH.name,
    val providers: List<String> = listOf("Musixmatch", "LrcLib", "Unison", "Better Lyrics", "YouLyPlus"),
    val radio: Boolean = true,
)
