package com.music.vivi.desktop.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.MediaCard
import com.music.vivi.desktop.data.PlayRecord
import com.music.vivi.desktop.data.Route
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.data.Track
import com.music.vivi.desktop.ui.CoverArt
import com.music.vivi.desktop.ui.NavigationTitle
import com.music.vivi.desktop.ui.TrackRow
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

private enum class StatPeriod(val label: String, val days: Long?) {
    TODAY("Today", 0L),
    WEEK("This week", 7L),
    MONTH("This month", 30L),
    YEAR("This year", 365L),
    ALL("All time", null),
}

private data class RankedTracks(val track: Track, val plays: Int)

/** Listening stats derived from local play records, mirroring the app's stats. */
@Composable
fun StatsScreen() {
    var period by remember { mutableStateOf(StatPeriod.MONTH) }
    val scope = rememberCoroutineScope()

    val records = Store.plays.toList()
    val filtered =
        remember(records, period) {
            val cutoff =
                period.days?.let { days ->
                    LocalDate.now().minusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
            if (cutoff == null) records else records.filter { it.playedAt >= cutoff }
        }

    val songs = remember(filtered) { rankSongs(filtered) }
    val artists = remember(filtered) { rankArtists(filtered) }
    val albums = remember(filtered) { rankAlbums(filtered) }
    val totalSeconds = filtered.sumOf { it.track.duration }
    val topTracks = songs.map { it.track }

    LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
        item {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                Text(
                    "Listening stats",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    filtered.size.toString() + " plays • " + formatLongDuration(totalSeconds) + " • " +
                        artists.size + " artists • " + albums.size + " albums",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatPeriod.entries.forEach { option ->
                        FilterChip(
                            selected = period == option,
                            onClick = { period = option },
                            label = { Text(option.label) },
                        )
                    }
                }
                if (topTracks.isNotEmpty()) {
                    Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(shape = CircleShape, onClick = { Store.play(topTracks) }) {
                            Icon(Icons.Rounded.PlayArrow, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Play top songs")
                        }
                        Button(shape = CircleShape, onClick = { Store.play(topTracks.shuffled()) }) {
                            Icon(Icons.Rounded.Shuffle, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Shuffle")
                        }
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(top = 90.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Listen to some music to build your stats.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            item { NavigationTitle(title = "Top songs") }
            itemsIndexed(songs, key = { _, entry -> "song-" + entry.track.id }) { index, entry ->
                Box(Modifier.padding(horizontal = 16.dp)) {
                    TrackRow(
                        track = entry.track,
                        index = index,
                        queue = topTracks,
                        showArtwork = true,
                        showAlbum = false,
                        scope = scope,
                    )
                }
            }

            item { NavigationTitle(title = "Top artists") }
            item { RankedArtists(artists) }

            item { NavigationTitle(title = "Top albums") }
            item { RankedAlbums(albums) }
        }
    }
}

@Composable
private fun RankedArtists(artists: List<Pair<String, Int>>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(artists.take(12), key = { it.first }) { entry ->
            val (name, plays) = entry
            val artwork = Store.history.firstOrNull { it.artists.firstOrNull() == name }?.thumbnail.orEmpty()
            Column(
                Modifier
                    .width(150.dp)
                    .clickable {
                        Store.searchQuery = name
                        Store.navigate(Route.Search)
                    }
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CoverArt(artwork, 140.dp, CircleShape)
                Spacer(Modifier.height(8.dp))
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    plays.toString() + " plays",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RankedAlbums(albums: List<Pair<MediaCard.Album, Int>>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(albums.take(12), key = { it.first.browseId }) { entry ->
            val (album, plays) = entry
            Column(
                Modifier
                    .width(150.dp)
                    .clickable { openCard(album) }
                    .padding(4.dp),
            ) {
                CoverArt(album.thumbnail, 140.dp, RoundedCornerShape(8.dp))
                Spacer(Modifier.height(8.dp))
                Text(
                    album.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    plays.toString() + " plays",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun rankSongs(records: List<PlayRecord>): List<RankedTracks> =
    records
        .groupBy { it.track.id }
        .values
        .sortedByDescending { it.size }
        .map { RankedTracks(it.first().track, it.size) }

private fun rankArtists(records: List<PlayRecord>): List<Pair<String, Int>> {
    val counts = mutableMapOf<String, Int>()
    records.forEach { record ->
        record.track.artists.forEach { artist -> counts[artist] = (counts[artist] ?: 0) + 1 }
    }
    return counts.entries.sortedByDescending { it.value }.map { it.key to it.value }
}

private fun rankAlbums(records: List<PlayRecord>): List<Pair<MediaCard.Album, Int>> =
    records
        .filter { it.track.albumId != null && it.track.album != null }
        .groupBy { it.track.albumId!! }
        .entries
        .sortedByDescending { it.value.size }
        .map { (albumId, values) ->
            val track = values.first().track
            MediaCard.Album(
                browseId = albumId,
                title = track.album.orEmpty(),
                thumbnail = track.thumbnail,
                artists = track.artistLine,
                year = null,
                playlistId = "",
            ) to values.size
        }

private fun formatLongDuration(seconds: Int): String {
    if (seconds <= 0) return "0 min"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        hours > 0 -> String.format(Locale.US, "%d h %d min", hours, minutes)
        minutes > 0 -> minutes.toString() + " min"
        else -> seconds.toString() + " s"
    }
}
