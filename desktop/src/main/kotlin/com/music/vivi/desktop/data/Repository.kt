package com.music.vivi.desktop.data

import com.music.innertube.YouTube
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.music.innertube.models.YouTubeClient
import com.music.innertube.pages.HomePage
import com.music.kugou.KuGou
import com.music.lrclib.LrcLib
import com.music.unison.Unison
import com.music.vivi.betterlyrics.BetterLyrics
import com.music.youlyplus.YouLyPlus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Network facade over the app's own Innertube + lyrics provider modules. */
object Repository {
    private fun HomePage.toFeed(): HomeFeed {
        val sections =
            this.sections.mapNotNull { section ->
                val cards = section.items.mapNotNull { it.toCard() }
                if (cards.isEmpty()) return@mapNotNull null
                UiSection(
                    title = section.title,
                    cards = cards,
                    label = section.label,
                    thumbnail = section.thumbnail,
                )
            }
        val chips =
            this.chips.orEmpty().map { chip ->
                HomeChip(
                    title = chip.title,
                    browseId = chip.endpoint?.browseId,
                    params = chip.endpoint?.params,
                    deselectParams = chip.deselectEndPoint?.params,
                )
            }
        return HomeFeed(chips, sections)
    }

    suspend fun home(): Result<HomeFeed> = withContext(Dispatchers.IO) {
        YouTube.home().map { it.toFeed() }
    }

    /** Loads a home feed for a selected mood/genre chip. */
    suspend fun homeChip(params: String?): Result<HomeFeed> = withContext(Dispatchers.IO) {
        YouTube.home(params = params).map { it.toFeed() }
    }

    suspend fun explore(): Result<Pair<List<UiSection>, List<MoodAndGenre>>> = withContext(Dispatchers.IO) {
        YouTube.explore().map { page ->
            val albums = page.newReleaseAlbums.mapNotNull { it.toCard() }
            val sections =
                if (albums.isEmpty()) emptyList() else listOf(UiSection("New releases", albums))
            val moods =
                page.moodAndGenres.map { mood ->
                    MoodAndGenre(
                        title = mood.title,
                        browseId = mood.endpoint.browseId,
                        params = mood.endpoint.params,
                        color = mood.stripeColor,
                    )
                }
            sections to moods
        }
    }

    suspend fun search(query: String, filter: YouTube.SearchFilter): List<MediaCard> = withContext(Dispatchers.IO) {
        YouTube.search(query, filter).getOrNull()?.items?.mapNotNull { it.toCard() }.orEmpty()
    }

    suspend fun searchSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        YouTube.searchSuggestions(query).getOrNull()?.queries.orEmpty().take(8)
    }

    suspend fun searchAll(query: String): List<UiSection> = withContext(Dispatchers.IO) {
        SearchCategory.entries
            .filter { it.filter != null }
            .mapNotNull { category ->
                val filter = category.filter ?: return@mapNotNull null
                val cards = YouTube.search(query, filter).getOrNull()?.items?.mapNotNull { it.toCard() }.orEmpty()
                if (cards.isEmpty()) null else UiSection(category.label, cards)
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
            val topSongs =
                page.sections
                    .firstOrNull { it.title.contains("song", ignoreCase = true) || it.items.all { item -> item is com.music.innertube.models.SongItem } }
                    ?.items
                    ?.mapNotNull { (it as? com.music.innertube.models.SongItem)?.toTrack() }
                    .orEmpty()
            ArtistDetails(
                artist = page.artist,
                sections = sections.filterNot { it.title.contains("song", ignoreCase = true) },
                topSongs = topSongs,
                subscriberCount = page.subscriberCountText,
                monthlyListeners = page.monthlyListenerCount,
                description = page.description,
            )
        }
    }

    suspend fun playlist(playlistId: String): Result<PlaylistDetails> = withContext(Dispatchers.IO) {
        YouTube.playlist(playlistId).map { page ->
            PlaylistDetails(
                playlist = page.playlist,
                songs = page.songs.map { it.toTrack() },
                related = page.related?.mapNotNull { it.toCard() }.orEmpty(),
                relatedTitle = page.relatedTitle,
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

/** Search result tabs, mirroring the app's filter chips. */
enum class SearchCategory(val label: String, val filter: YouTube.SearchFilter?) {
    ALL("All", null),
    SONGS("Songs", YouTube.SearchFilter.FILTER_SONG),
    ALBUMS("Albums", YouTube.SearchFilter.FILTER_ALBUM),
    ARTISTS("Artists", YouTube.SearchFilter.FILTER_ARTIST),
    PLAYLISTS("Playlists", YouTube.SearchFilter.FILTER_FEATURED_PLAYLIST),
    VIDEOS("Videos", YouTube.SearchFilter.FILTER_VIDEO),
}
