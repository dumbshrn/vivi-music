package com.music.vivi.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.BrowseSection
import com.music.vivi.desktop.data.ChartSection
import com.music.vivi.desktop.data.MediaCard
import com.music.vivi.desktop.data.MoodGenreGroup
import com.music.vivi.desktop.data.Repository
import com.music.vivi.desktop.data.Route
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.data.Track
import com.music.vivi.desktop.ui.CardTile
import com.music.vivi.desktop.ui.NavigationTitle
import com.music.vivi.desktop.ui.TrackRow

/** Charts: one 4-row horizontal shelf per chart, videos as a card carousel (like the app). */
@Composable
fun ChartsScreen() {
    var sections by remember { mutableStateOf<List<ChartSection>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var token by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(token) {
        sections = null
        error = null
        Repository.charts()
            .onSuccess { sections = it }
            .onFailure { error = it.message ?: "Could not load charts" }
    }

    when {
        error != null -> DiscoverError(error!!) { token += 1 }
        sections == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else -> {
            val shelfSections = sections.orEmpty().filterNot { it.title.equals("Top music videos", ignoreCase = true) }
            val videos = sections.orEmpty().firstOrNull { it.title.equals("Top music videos", ignoreCase = true) }

            LazyColumn(contentPadding = PaddingValues(bottom = 28.dp)) {
                item {
                    Text(
                        "Charts",
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    )
                }
                items(shelfSections, key = { it.title }) { section ->
                    val tracks = section.cards.mapNotNull { (it as? MediaCard.SongCard)?.track }.distinctBy { it.id }
                    if (tracks.isNotEmpty()) {
                        NavigationTitle(
                            title = section.title.ifBlank { "Charts" },
                            onPlayAll = { Store.play(tracks) },
                        )
                        ChartSongShelf(tracks, scope)
                    } else {
                        NavigationTitle(title = section.title)
                        CardCarousel(section.cards)
                    }
                }
                if (videos != null) {
                    item { NavigationTitle(title = "Top music videos") }
                    item { CardCarousel(videos.cards, size = 200.dp) }
                }
            }
        }
    }
}

@Composable
private fun ChartSongShelf(tracks: List<Track>, scope: kotlinx.coroutines.CoroutineScope) {
    val columns = tracks.chunked(4)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(columns) { column ->
            Column(Modifier.width(360.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                column.forEach { track ->
                    TrackRow(
                        track = track,
                        index = tracks.indexOf(track),
                        queue = tracks,
                        showArtwork = true,
                        showAlbum = false,
                        scope = scope,
                    )
                }
            }
        }
    }
}

@Composable
private fun CardCarousel(cards: List<MediaCard>, size: androidx.compose.ui.unit.Dp = 160.dp) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(cards, key = { "${it.browseId}-${it.title}" }) { card ->
            CardTile(card = card, onClick = { openCard(card) }, size = size)
        }
    }
}

/** Moods & genres browser: tinted buttons grouped by shelf, opening browse pages. */
@Composable
fun MoodGenresScreen() {
    var groups by remember { mutableStateOf<List<MoodGenreGroup>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var token by remember { mutableStateOf(0) }

    LaunchedEffect(token) {
        groups = null
        error = null
        Repository.moodGenres()
            .onSuccess { groups = it }
            .onFailure { error = it.message ?: "Could not load moods and genres" }
    }

    when {
        error != null -> DiscoverError(error!!) { token += 1 }
        groups == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else -> {
            LazyColumn(contentPadding = PaddingValues(bottom = 28.dp)) {
                for (group in groups.orEmpty()) {
                    item(key = "title-${group.title}") { NavigationTitle(title = group.title) }
                    item(key = "grid-${group.title}") { MoodButtonGrid(group.items) }
                }
            }
        }
    }
}

@Composable
private fun MoodButtonGrid(items: List<com.music.vivi.desktop.data.MoodAndGenre>) {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        val columns = (maxWidth / 240.dp).toInt().coerceIn(2, 4)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { mood ->
                        val tint =
                            mood.color?.let { Color(0xFF000000L or (it and 0xFFFFFFL)) }
                                ?: MaterialTheme.colorScheme.secondaryContainer
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = tint,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable {
                                    if (mood.browseId != null) {
                                        Store.navigate(Route.Browse(mood.browseId, mood.params, mood.title))
                                    } else {
                                        Store.searchQuery = mood.title
                                        Store.navigate(Route.Search)
                                    }
                                },
                        ) {
                            Box(Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                                Text(
                                    mood.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (tint.isLight()) Color.Black else Color.White,
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

private fun Color.isLight(): Boolean = 0.299f * red + 0.587f * green + 0.114f * blue > 0.55f

/** The full new-releases album grid. */
@Composable
fun NewReleasesScreen() {
    var albums by remember { mutableStateOf<List<MediaCard>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var token by remember { mutableStateOf(0) }

    LaunchedEffect(token) {
        albums = null
        error = null
        Repository.newReleases()
            .onSuccess { albums = it }
            .onFailure { error = it.message ?: "Could not load new releases" }
    }

    when {
        error != null -> DiscoverError(error!!) { token += 1 }
        albums == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else -> {
            Column(Modifier.fillMaxSize()) {
                Text(
                    "New releases",
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(170.dp),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(albums.orEmpty().distinctBy { it.browseId }, key = { it.browseId }) { album ->
                        CardTile(album, onClick = { openCard(album) }, size = 170.dp)
                    }
                }
            }
        }
    }
}

/** Any Innertube browse page (mood/genre destination, YouTube's own "browse" targets). */
@Composable
fun BrowseScreen(browseId: String, params: String?, title: String) {
    var pageTitle by remember { mutableStateOf(title) }
    var sections by remember { mutableStateOf<List<BrowseSection>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var token by remember { mutableStateOf(0) }

    LaunchedEffect(browseId, params, token) {
        sections = null
        error = null
        Repository.browse(browseId, params)
            .onSuccess { (newTitle, newSections) ->
                if (!newTitle.isNullOrBlank()) pageTitle = newTitle
                sections = newSections.filter { it.cards.isNotEmpty() }
            }
            .onFailure { error = it.message ?: "Could not load this page" }
    }

    when {
        error != null -> DiscoverError(error!!) { token += 1 }
        sections == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else -> {
            LazyColumn(contentPadding = PaddingValues(bottom = 28.dp)) {
                item {
                    Text(
                        pageTitle,
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    )
                }
                for (section in sections.orEmpty()) {
                    item(key = "title-" + section.title) {
                        if (section.title.isNotBlank()) NavigationTitle(title = section.title)
                    }
                    item(key = "grid-" + section.title + section.cards.size) {
                        BrowseCardGrid(section.cards)
                    }
                }
            }
        }
    }
}

@Composable
private fun BrowseCardGrid(cards: List<MediaCard>) {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)) {
        val columns = (maxWidth / 150.dp).toInt().coerceIn(2, 7)
        val size = ((maxWidth - 8.dp * (columns - 1)) / columns).coerceAtMost(190.dp)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            cards.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { card ->
                        CardTile(card = card, onClick = { openCard(card) }, size = size)
                    }
                    repeat(columns - row.size) { Spacer(Modifier.width(size)) }
                }
            }
        }
    }
}

@Composable
internal fun DiscoverError(message: String, onRetry: () -> Unit) {
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
