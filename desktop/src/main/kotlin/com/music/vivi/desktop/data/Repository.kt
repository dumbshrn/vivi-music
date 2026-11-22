package com.music.vivi.desktop.data

import com.music.innertube.YouTube
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.music.innertube.models.YouTubeClient
import com.music.kugou.KuGou
import com.music.lrclib.LrcLib
import com.music.unison.Unison
import com.music.vivi.betterlyrics.BetterLyrics
import com.music.youlyplus.YouLyPlus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Network facade over the app's own Innertube + lyrics provider modules. */
object Repository {
    private fun List<YTItem>.toSections(): List<UiSection> {
        val cards = mapNotNull { it.toCard() }
        if (cards.isEmpty()) return emptyList()
        return listOf(UiSection("", cards))
    }

    suspend fun home(): Result<List<UiSection>> = withContext(Dispatchers.IO) {
        YouTube.home().map { page ->
            page.sections.mapNotNull { section ->
                val cards = section.items.mapNotNull { it.toCard() }
                if (cards.isEmpty()) null else UiSection(section.title, cards)
            }
        }
    }

    suspend fun explore(): Result<List<UiSection>> = withContext(Dispatchers.IO) {
        YouTube.explore().map { page ->
            val sections = mutableListOf<UiSection>()
            val albums = page.newReleaseAlbums.mapNotNull { it.toCard() }
            if (albums.isNotEmpty()) sections += UiSection("New releases", albums)
            if (page.moodAndGenres.isNotEmpty()) {
                sections +=
                    UiSection(
                        "Moods & genres",
                        page.moodAndGenres.map { mood ->
                            MediaCard.Playlist(
                                browseId = mood.endpoint.browseId ?: "",
                                title = mood.title,
                                thumbnail = "",
                                author = "Mood & genre",
                            )
                        },
                    )
            }
            sections
        }
    }

    suspend fun search(query: String, filter: YouTube.SearchFilter): List<MediaCard> = withContext(Dispatchers.IO) {
        YouTube.search(query, filter).getOrNull()?.items?.mapNotNull { it.toCard() }.orEmpty()
    }

    suspend fun searchAll(query: String): List<UiSection> = withContext(Dispatchers.IO) {
        val groups =
            listOf(
                "Songs" to YouTube.SearchFilter.FILTER_SONG,
                "Albums" to YouTube.SearchFilter.FILTER_ALBUM,
                "Artists" to YouTube.SearchFilter.FILTER_ARTIST,
                "Playlists" to YouTube.SearchFilter.FILTER_FEATURED_PLAYLIST,
                "Videos" to YouTube.SearchFilter.FILTER_VIDEO,
            )
        groups.mapNotNull { (title, filter) ->
            val cards =
                YouTube.search(query, filter).getOrNull()?.items?.mapNotNull { it.toCard() }.orEmpty()
            if (cards.isEmpty()) null else UiSection(title, cards)
        }
    }

    suspend fun album(browseId: String): Result<AlbumDetails> = withContext(Dispatchers.IO) {
        YouTube.album(browseId).map { page ->
            AlbumDetails(page.album, page.songs.map { it.toTrack() }, page.description)
        }
    }

    suspend fun artist(browseId: String): Result<ArtistDetails> = withContext(Dispatchers.IO) {
        YouTube.artist(browseId).map { page ->
            val sections =
                page.sections.mapNotNull { section ->
                    val cards = section.items.mapNotNull { it.toCard() }
                    if (cards.isEmpty()) null else UiSection(section.title, cards)
                }
            ArtistDetails(
                artist = page.artist,
                sections = sections,
                topSongs = page.sections.firstOrNull()?.items?.mapNotNull { (it as? com.music.innertube.models.SongItem)?.toTrack() }.orEmpty(),
                subscriberCount = page.subscriberCountText,
                monthlyListeners = page.monthlyListenerCount,
            )
        }
    }

    suspend fun playlist(playlistId: String): Result<PlaylistDetails> = withContext(Dispatchers.IO) {
        YouTube.playlist(playlistId).map { page ->
            PlaylistDetails(
                playlist = page.playlist,
                songs = page.songs.map { it.toTrack() },
                related = page.related?.mapNotNull { it.toCard() }.orEmpty(),
            )
        }
    }

    suspend fun radio(videoId: String): Result<List<Track>> = withContext(Dispatchers.IO) {
        YouTube
            .next(WatchEndpoint(videoId = videoId))
            .map { page -> page.items.map { it.toTrack() } }
    }

    suspend fun queueFor(endpoint: WatchEndpoint): Result<List<Track>> = withContext(Dispatchers.IO) {
        YouTube.next(endpoint).map { page -> page.items.map { it.toTrack() } }
    }

    /**
     * Resolves a direct audio stream. Prefers AAC in MP4 because the desktop
     * audio backend decodes it everywhere, and falls back to the highest
     * bitrate audio format otherwise.
     */
    suspend fun streamUrl(videoId: String, quality: AudioQuality): Result<String> = withContext(Dispatchers.IO) {
        val clients = listOf(YouTubeClient.WEB_REMIX, YouTubeClient.ANDROID_VR_1_65_10, YouTubeClient.IOS)
        var lastError: Throwable? = null

        for (client in clients) {
            val response = YouTube.player(videoId = videoId, client = client).getOrNull()
            if (response == null) {
                lastError = IllegalStateException("Could not reach YouTube")
                continue
            }
            val formats =
                response.streamingData?.adaptiveFormats.orEmpty()
                    .filter { it.isAudio && !it.url.isNullOrBlank() }
            if (formats.isEmpty()) {
                lastError = IllegalStateException(response.playabilityStatus.reason ?: "No audio stream available")
                continue
            }
            val sorted = formats.sortedByDescending { it.bitrate }
            val candidates =
                when (quality) {
                    AudioQuality.HIGH -> sorted
                    AudioQuality.NORMAL -> sorted.drop(sorted.size / 3)
                    AudioQuality.LOW -> sorted.takeLast(2)
                }
            val chosen = candidates.firstOrNull { it.mimeType.contains("mp4") } ?: candidates.firstOrNull()
            val url = chosen?.url
            if (url != null) return@withContext Result.success(url)
        }

        Result.failure(lastError ?: IllegalStateException("This track cannot be streamed"))
    }

    private class LyricsProvider(val name: String, val fetch: suspend () -> Result<String>)

    suspend fun lyrics(track: Track, providers: List<String>): Lyrics = withContext(Dispatchers.IO) {
        val artist = track.artists.joinToString(", ")
        val primaryArtist = track.artists.firstOrNull() ?: ""

        val attempts =
            listOf(
                LyricsProvider("LrcLib") {
                    LrcLib.getLyrics(track.title, primaryArtist, track.duration, track.album)
                },
                LyricsProvider("Unison") {
                    Unison.getLyrics(
                        title = track.title,
                        artist = artist,
                        duration = track.duration,
                        album = track.album,
                        videoId = track.id,
                    )
                },
                LyricsProvider("Better Lyrics") {
                    BetterLyrics.getLyrics(track.title, primaryArtist, track.duration, track.album)
                },
                LyricsProvider("YouLyPlus") {
                    YouLyPlus.getLyrics(
                        title = track.title,
                        artist = artist,
                        duration = track.duration,
                        album = track.album,
                        id = track.id,
                    )
                },
                LyricsProvider("KuGou") {
                    KuGou.getLyrics(track.title, primaryArtist, track.duration, track.album)
                },
            ).sortedBy { provider ->
                val index = providers.indexOf(provider.name)
                if (index < 0) providers.size + 1 else index
            }

        for (provider in attempts) {
            val text = runCatching { provider.fetch() }.getOrNull()?.getOrNull()
            if (text.isNullOrBlank()) continue
            val parsed = LyricsParser.parse(text)
            if (parsed.lines.isEmpty()) continue
            return@withContext parsed.copy(provider = provider.name)
        }

        Lyrics(provider = "", sync = Lyrics.Sync.NONE, lines = emptyList(), plain = true)
    }
}
