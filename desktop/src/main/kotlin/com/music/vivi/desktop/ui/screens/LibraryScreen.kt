package com.music.vivi.desktop.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.MediaCard
import com.music.vivi.desktop.data.Route
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.data.Track
import com.music.vivi.desktop.ui.CardTile
import com.music.vivi.desktop.ui.CoverArt
import com.music.vivi.desktop.ui.NavigationTitle
import com.music.vivi.desktop.ui.TrackRow

private enum class LibraryTab(val label: String) {
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    PLAYLISTS("Playlists"),
    HISTORY("History"),
}

private enum class SongSort(val label: String) {
    RECENT("Recently added"),
    TITLE("Title"),
    ARTIST("Artist"),
    DURATION("Duration"),
}

@Composable
fun LibraryScreen() {
    var tab by remember { mutableStateOf(LibraryTab.SONGS) }
    var sort by remember { mutableStateOf(SongSort.RECENT) }
    var grid by remember { mutableStateOf(true) }
    var sortOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val songs = Store.liked.toList()
    val sortedSongs =
        when (sort) {
            SongSort.RECENT -> songs
            SongSort.TITLE -> songs.sortedBy { it.title.lowercase() }
            SongSort.ARTIST -> songs.sortedBy { it.artistLine.lowercase() }
            SongSort.DURATION -> songs.sortedByDescending { it.duration }
        }
    val albums = libraryAlbums(songs)
    val artists = libraryArtists(songs)

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Your library", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "${Store.liked.size} liked songs • ${Store.playlists.size} playlists",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (tab == LibraryTab.HISTORY && Store.history.isNotEmpty()) {
                IconButton(onClick = { Store.clearHistory() }) {
                    Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear history")
                }
                Spacer(Modifier.width(8.dp))
            }
            if (tab == LibraryTab.SONGS && sortedSongs.isNotEmpty()) {
                Button(onClick = { Store.play(sortedSongs) }, shape = CircleShape) {
                    Icon(Icons.Rounded.PlayArrow, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Play all")
                }
                Spacer(Modifier.width(8.dp))
            }
            if (tab == LibraryTab.ALBUMS || tab == LibraryTab.ARTISTS || tab == LibraryTab.PLAYLISTS) {
                IconButton(onClick = { grid = !grid }) {
                    Icon(
                        if (grid) Icons.Rounded.ViewList else Icons.Rounded.Apps,
                        contentDescription = "Change layout",
                    )
                }
            }
            if (tab == LibraryTab.SONGS) {
                Box {
                    IconButton(onClick = { sortOpen = true }) {
                        Icon(Icons.Rounded.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(expanded = sortOpen, onDismissRequest = { sortOpen = false }) {
                        SongSort.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    sort = option
                                    sortOpen = false
                                },
                            )
                        }
                    }
                }
            }
            Button(
                onClick = {
                    val playlist = Store.createPlaylist("My playlist #${Store.playlists.size + 1}")
                    Store.navigate(Route.LocalPlaylist(playlist.id))
                },
                shape = CircleShape,
            ) {
                Icon(Icons.Rounded.Add, null)
                Spacer(Modifier.width(6.dp))
                Text("New playlist")
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LibraryTab.entries.forEach { entry ->
                FilterChip(
                    selected = tab == entry,
                    onClick = { tab = entry },
                    label = { Text(entry.label) },
                    shape = if (tab == entry) CircleShape else RoundedCornerShape(8.dp),
                    border = null,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                )
            }
        }

        when (tab) {
            LibraryTab.SONGS -> {
                if (sortedSongs.isEmpty()) {
                    EmptyLibrary("Songs you like will appear here.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        itemsIndexed(sortedSongs, key = { index, track -> "${track.id}-$index" }) { index, track ->
                            TrackRow(
                                track = track,
                                index = index,
                                queue = sortedSongs,
                                showArtwork = true,
                                scope = scope,
                                onRemove = { Store.toggleLike(track) },
                            )
                        }
                    }
                }
            }

            LibraryTab.ALBUMS -> {
                if (albums.isEmpty()) {
                    EmptyLibrary("Albums you save will appear here.")
                } else if (grid) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        contentPadding = PaddingValues(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(albums, key = { it.browseId }) { album ->
                            CardTile(album, onClick = { openCard(album) }, size = 150.dp)
                        }
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(albums.size) { index ->
                            val album = albums[index]
                            AlbumRow(album) { openCard(album) }
                        }
                    }
                }
            }

            LibraryTab.ARTISTS -> {
                if (artists.isEmpty()) {
                    EmptyLibrary("Artists you listen to will appear here.")
                } else if (grid) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        contentPadding = PaddingValues(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(artists, key = { it.title }) { artist ->
                            CardTile(artist, onClick = { openCard(artist) }, size = 150.dp)
                        }
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(artists.size) { index ->
                            val artist = artists[index]
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { openCard(artist) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CoverArt(artist.thumbnail, 48.dp, CircleShape)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(artist.title, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "Artist",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            LibraryTab.HISTORY -> {
                val history = Store.history.toList()
                if (history.isEmpty()) {
                    EmptyLibrary("Nothing played yet.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        itemsIndexed(history, key = { index, track -> "${track.id}-$index" }) { index, track ->
                            TrackRow(
                                track = track,
                                index = index,
                                queue = history,
                                showArtwork = true,
                                scope = scope,
                            )
                        }
                    }
                }
            }

            LibraryTab.PLAYLISTS -> {
                if (Store.playlists.isEmpty()) {
                    EmptyLibrary("Playlists you create will appear here.")
                } else if (grid) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(170.dp),
                        contentPadding = PaddingValues(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(Store.playlists, key = { it.id }) { playlist ->
                            Column(
                                Modifier.clickable { Store.navigate(Route.LocalPlaylist(playlist.id)) },
                            ) {
                                CoverArt(
                                    playlist.tracks.firstOrNull()?.thumbnail,
                                    160.dp,
                                    RoundedCornerShape(8.dp),
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    playlist.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    "${playlist.tracks.size} songs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(Store.playlists.size) { index ->
                            val playlist = Store.playlists[index]
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { Store.navigate(Route.LocalPlaylist(playlist.id)) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CoverArt(playlist.tracks.firstOrNull()?.thumbnail, 48.dp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(playlist.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "${playlist.tracks.size} songs",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumRow(album: MediaCard.Album, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverArt(album.thumbnail, 48.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(album.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                album.artists,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyLibrary(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun libraryAlbums(songs: List<Track>): List<MediaCard.Album> =
    songs.mapNotNull { track ->
        val id = track.albumId ?: return@mapNotNull null
        MediaCard.Album(
            browseId = id,
            title = track.album ?: return@mapNotNull null,
            thumbnail = track.thumbnail,
            artists = track.artistLine,
            year = null,
            playlistId = "",
        )
    }.distinctBy { it.browseId }

private fun libraryArtists(songs: List<Track>): List<MediaCard.Artist> =
    songs.mapNotNull { track ->
        val name = track.artists.firstOrNull() ?: return@mapNotNull null
        MediaCard.Artist(browseId = "", title = name, thumbnail = track.thumbnail)
    }.distinctBy { it.title }
