package com.music.vivi.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.HomeChip
import com.music.vivi.desktop.data.HomeFeed
import com.music.vivi.desktop.data.MediaCard
import com.music.vivi.desktop.data.MoodAndGenre
import com.music.vivi.desktop.data.Repository
import com.music.vivi.desktop.data.Route
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.data.Track
import com.music.vivi.desktop.data.UiSection
import com.music.vivi.desktop.ui.CoverArt
import com.music.vivi.desktop.ui.NavigationTitle
import com.music.vivi.desktop.ui.QuickPicksRow
import com.music.vivi.desktop.ui.SectionRow
import com.music.vivi.desktop.ui.SpeedDialTile
import com.music.vivi.desktop.ui.TrackRow
import java.time.LocalTime

@Composable
fun HomeScreen() {
    var feed by remember { mutableStateOf<HomeFeed?>(null) }
    var selectedChip by remember { mutableStateOf<HomeChip?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadToken by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reloadToken) {
        loading = true
        error = null
        Repository.home()
            .onSuccess { feed = it }
            .onFailure { error = it.message ?: "Could not load music" }
        loading = false
    }

    LaunchedEffect(selectedChip) {
        val chip = selectedChip ?: return@LaunchedEffect
        loading = true
        Repository.homeChip(chip.params)
            .onSuccess { feed = it }
            .onFailure { Store.notify("Could not load ${chip.title}") }
        loading = false
    }

    Box(Modifier.fillMaxSize()) {
        when {
            error != null && feed == null -> ErrorState(error!!) { reloadToken += 1 }
            feed == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            else -> {
                val data = feed ?: HomeFeed.Empty
                val quickPicksSection = data.sections.firstOrNull { it.title.contains("quick pick", ignoreCase = true) }
                val firstSongSection = data.sections.firstOrNull { section -> section.cards.all { it is MediaCard.SongCard } }
                val quickPicks =
                    quickPicksSection?.cards?.mapNotNull { (it as? MediaCard.SongCard)?.track }
                        ?: firstSongSection?.cards?.mapNotNull { (it as? MediaCard.SongCard)?.track }
                        ?: emptyList()
                val remainingSections =
                    data.sections.filterNot { section ->
                        section === quickPicksSection || (quickPicksSection == null && section === firstSongSection)
                    }
                val speedDial = speedDialCards()

                LazyColumn(
                    contentPadding = PaddingValues(top = 4.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    if (data.chips.isNotEmpty()) {
                        item {
                            ChipsRow(
                                chips = data.chips.filterNot { it.title.equals("Podcasts", ignoreCase = true) },
                                selected = selectedChip,
                                onSelect = { chip ->
                                    selectedChip = if (selectedChip?.title == chip.title) null else chip
                                    if (selectedChip == null) reloadToken += 1
                                },
                            )
                        }
                    }

                    if (speedDial.isNotEmpty()) {
                        item { NavigationTitle(title = "Speed dial") }
                        item { SpeedDialGrid(speedDial) }
                    }

                    if (quickPicks.isNotEmpty()) {
                        item {
                            NavigationTitle(
                                title = quickPicksSection?.title ?: "Quick picks",
                                onPlayAll = { Store.play(quickPicks) },
                            )
                        }
                        item { QuickPicksRow(quickPicks) }
                    }

                    items(remainingSections, key = { it.title + it.cards.size }) { section ->
                        if (section.cards.all { it is MediaCard.SongCard }) {
                            SongSection(section, scope)
                        } else {
                            SectionRow(section) { card -> openCard(card) }
                        }
                    }
                }

                if (loading) {
                    Box(
                        Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SongSection(section: UiSection, scope: kotlinx.coroutines.CoroutineScope) {
    val tracks = section.cards.mapNotNull { (it as? MediaCard.SongCard)?.track }
    if (tracks.isEmpty()) return
    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        NavigationTitle(
            title = section.title.ifBlank { "Songs" },
            label = section.label,
            onPlayAll = { Store.play(tracks) },
        )
        tracks.take(8).forEachIndexed { index, track ->
            Box(Modifier.padding(horizontal = 16.dp)) {
                TrackRow(
                    track = track,
                    index = index,
                    queue = tracks,
                    showArtwork = true,
                    showAlbum = false,
                    scope = scope,
                )
            }
        }
    }
}

@Composable
private fun ChipsRow(chips: List<HomeChip>, selected: HomeChip?, onSelect: (HomeChip) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        chips.distinctBy { it.title }.forEach { chip ->
            FilterChip(
                selected = selected?.title == chip.title,
                onClick = { onSelect(chip) },
                label = { Text(chip.title) },
                shape = RoundedCornerShape(if (selected?.title == chip.title) 20.dp else 8.dp),
                border = null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        }
    }
}

@Composable
private fun SpeedDialGrid(cards: List<MediaCard>) {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        val columns = (maxWidth / 150.dp).toInt().coerceIn(3, 8)
        val limited = cards.take(columns * 2)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            limited.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { card ->
                        SpeedDialTile(
                            card = card,
                            onClick = { openCard(card) },
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                        )
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

private fun speedDialCards(): List<MediaCard> {
    val albums =
        Store.history.mapNotNull { track ->
            val albumId = track.albumId ?: return@mapNotNull null
            MediaCard.Album(
                browseId = albumId,
                title = track.album ?: track.title,
                thumbnail = track.thumbnail,
                artists = track.artistLine,
                year = null,
                playlistId = "",
            )
        }.distinctBy { it.browseId }
    val playlists =
        Store.playlists.map { playlist ->
            MediaCard.Playlist(
                browseId = playlist.id,
                title = playlist.name,
                thumbnail = playlist.tracks.firstOrNull()?.thumbnail.orEmpty(),
                author = "${playlist.tracks.size} songs",
            )
        }
    val liked =
        Store.liked.take(2).map { track ->
            MediaCard.SongCard(browseId = track.id, title = track.title, thumbnail = track.thumbnail, track = track)
        }
    return (albums + playlists + liked).take(16)
}

@Composable
fun ExploreScreen() {
    var sections by remember { mutableStateOf<List<UiSection>?>(null) }
    var moods by remember { mutableStateOf<List<MoodAndGenre>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var token by remember { mutableStateOf(0) }

    LaunchedEffect(token) {
        sections = null
        error = null
        Repository.explore()
            .onSuccess { (newSections, newMoods) ->
                sections = newSections
                moods = newMoods
            }
            .onFailure { error = it.message ?: "Could not load explore" }
    }

    when {
        error != null -> ErrorState(error!!) { token += 1 }
        sections == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else -> {
            LazyColumn(contentPadding = PaddingValues(bottom = 28.dp)) {
                item {
                    Text(
                        "Explore",
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    )
                }
                if (moods.isNotEmpty()) {
                    item { NavigationTitle(title = "Moods & genres") }
                    item { MoodGrid(moods) }
                }
                items(sections.orEmpty(), key = { it.title }) { section ->
                    SectionRow(section) { card -> openCard(card) }
                }
            }
        }
    }
}

@Composable
private fun MoodGrid(moods: List<MoodAndGenre>) {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        val columns = (maxWidth / 210.dp).toInt().coerceIn(2, 5)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            moods.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { mood ->
                        val tint = mood.color?.let { Color(0xFF000000L or (it and 0xFFFFFFL)) }
                            ?: MaterialTheme.colorScheme.secondaryContainer
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = tint,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .clickable {
                                    Store.searchQuery = mood.title
                                    Store.navigate(Route.Search)
                                },
                        ) {
                            Box(Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                                Text(
                                    mood.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (tint.luminance() > 0.5f) Color.Black else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

private fun Color.luminance(): Float = 0.299f * red + 0.587f * green + 0.114f * blue

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Rounded.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Try again")
            }
        }
    }
}

fun openCard(card: MediaCard) {
    when (card) {
        is MediaCard.Album -> Store.navigate(Route.Album(card.browseId))
        is MediaCard.Artist ->
            if (card.browseId.isBlank()) {
                Store.searchQuery = card.title
                Store.navigate(Route.Search)
            } else {
                Store.navigate(Route.Artist(card.browseId))
            }
        is MediaCard.Playlist -> {
            if (card.browseId.startsWith("local-")) {
                Store.navigate(Route.LocalPlaylist(card.browseId))
            } else {
                Store.navigate(Route.Playlist(card.browseId))
            }
        }
        is MediaCard.SongCard -> Store.play(listOf(card.track))
    }
}
