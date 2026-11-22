package com.music.vivi.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.MediaCard
import com.music.vivi.desktop.data.Repository
import com.music.vivi.desktop.data.Route
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.data.Track
import com.music.vivi.desktop.data.UiSection
import com.music.vivi.desktop.ui.CoverArt
import com.music.vivi.desktop.ui.SectionRow
import com.music.vivi.desktop.ui.TrackRow
import java.time.LocalTime

@Composable
fun HomeScreen() {
    SectionsPage(loader = { Repository.home() }, greeting = true)
}

@Composable
fun ExploreScreen() {
    SectionsPage(loader = { Repository.explore() }, greeting = false)
}

@Composable
private fun SectionsPage(
    loader: suspend () -> Result<List<UiSection>>,
    greeting: Boolean,
) {
    var sections by remember { mutableStateOf<List<UiSection>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadToken by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reloadToken) {
        sections = null
        error = null
        loader()
            .onSuccess { sections = it }
            .onFailure { error = it.message ?: "Could not load music" }
    }

    val cards = sections?.flatMap { it.cards }.orEmpty()
    val songs = cards.mapNotNull { (it as? MediaCard.SongCard)?.track }
    val heroArt = cards.firstOrNull()?.thumbnail.orEmpty()

    when {
        error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error!!, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { reloadToken += 1 }) {
                        Icon(Icons.Rounded.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Try again")
                    }
                }
            }
        }

        sections == null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        else -> {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item {
                    HeroBanner(
                        greeting = greeting,
                        artwork = heroArt,
                        onPlay = { if (songs.isNotEmpty()) Store.play(songs) },
                        onShuffle = {
                            if (songs.isNotEmpty()) {
                                Store.play(songs.shuffled())
                                Store.shuffle = true
                            }
                        },
                    )
                }
                items(sections.orEmpty().size) { index ->
                    val section = sections.orEmpty()[index]
                    if (section.cards.all { it is MediaCard.SongCard }) {
                        Column(Modifier.padding(horizontal = 28.dp, vertical = 8.dp)) {
                            Text(
                                section.title,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                            val tracks = section.cards.mapNotNull { (it as? MediaCard.SongCard)?.track }
                            tracks.take(6).forEachIndexed { trackIndex, track ->
                                TrackRow(
                                    track = track,
                                    index = trackIndex,
                                    queue = tracks,
                                    showArtwork = true,
                                    scope = scope,
                                )
                            }
                        }
                    } else {
                        SectionRow(section) { card -> openCard(card) }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroBanner(
    greeting: Boolean,
    artwork: String,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val hour = remember { LocalTime.now().hour }
    val greetingText =
        when {
            !greeting -> "Explore new music"
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 14.dp)
            .height(196.dp)
            .background(
                brush =
                    Brush.linearGradient(
                        listOf(colors.primaryContainer, colors.tertiaryContainer, colors.secondaryContainer),
                    ),
                shape = RoundedCornerShape(26.dp),
            ),
    ) {
        Row(
            Modifier.fillMaxSize().padding(26.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    greetingText,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onPrimaryContainer,
                )
                Text(
                    "VIVI Music",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.onPrimaryContainer,
                )
                Text(
                    "Your music, your aesthetic.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onPrimaryContainer.copy(alpha = 0.75f),
                )
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onPlay, shape = CircleShape) {
                        Icon(Icons.Rounded.PlayArrow, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Play")
                    }
                    FilledTonalButton(onClick = onShuffle, shape = CircleShape) {
                        Icon(Icons.Rounded.Shuffle, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Shuffle")
                    }
                }
            }
            if (artwork.isNotBlank()) {
                Surface(shape = RoundedCornerShape(22.dp), shadowElevation = 12.dp) {
                    CoverArt(artwork, 148.dp, RoundedCornerShape(22.dp))
                }
            }
        }
    }
}

fun openCard(card: MediaCard) {
    when (card) {
        is MediaCard.Album -> Store.navigate(Route.Album(card.browseId))
        is MediaCard.Artist -> Store.navigate(Route.Artist(card.browseId))
        is MediaCard.Playlist -> Store.navigate(Route.Playlist(card.browseId))
        is MediaCard.SongCard -> Store.play(listOf(card.track))
    }
}

@Suppress("unused")
private fun List<Track>.totalMinutes(): Int = sumOf { it.duration } / 60
