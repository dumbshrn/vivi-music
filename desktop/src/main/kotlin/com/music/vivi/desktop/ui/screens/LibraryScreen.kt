package com.music.vivi.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.MediaCard
import com.music.vivi.desktop.data.Route
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.data.Track
import com.music.vivi.desktop.ui.CardTile
import com.music.vivi.desktop.ui.CoverArt
import com.music.vivi.desktop.ui.SongShelfRow
import com.music.vivi.desktop.ui.TrackRow

private enum class LibraryTab(val label: String) {
    ALL("All"),
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    PLAYLISTS("Playlists"),
}

private enum class SongSort(val label: String) {
    RECENT("Recently added"),
    TITLE("Title"),
    ARTIST("Artist"),
    DURATION("Duration"),
}

@Composable
fun LibraryScreen() {
    var tab by remember { mutableStateOf(LibraryTab.ALL) }
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
    val topPlayed = Store.plays.groupBy { it.track.id }.values.sortedByDescending { it.size }.take(50)
        .map { it.first().track }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.weight(1f).height(56.dp).clickable {
                    Store.searchQuery = ""
                    Store.navigate(Route.Search)
                },
            ) {
                Row(Modifier.padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Artists, Songs, Lyrics, and More",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
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
            if (tab == LibraryTab.SONGS && sortedSongs.isNotEmpty()) {
                Button(shape = CircleShape, onClick = { Store.play(sortedSongs) }) {
                    Icon(Icons.Rounded.PlayArrow, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Play all")
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            TabStrip(
                tabs = LibraryTab.entries.map { it.label },
                selected = tab.ordinal,
                onSelect = { tab = LibraryTab.entries[it] },
                modifier = Modifier.weight(1f),
            )
            Button(
                shape = CircleShape,
                onClick = {
                    val playlist = Store.createPlaylist("My playlist #" + (Store.playlists.size + 1))
                    Store.navigate(Route.LocalPlaylist(playlist.id))
                },
            ) {
                Icon(Icons.Rounded.Add, null)
                Spacer(Modifier.width(6.dp))
                Text("New playlist")
            }
        }

        when (tab) {
            LibraryTab.ALL -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(220.dp),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 28.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    item {
                        LibraryTile(
                            title = "Liked",
                            subtitle = songs.size.toString() + " songs",
                            icon = if (songs.isEmpty()) Icons.Rounded.FavoriteBorder else Icons.Rounded.Favorite,
                            onClick = { tab = LibraryTab.SONGS },
                        )
                    }
                    item {
                        LibraryTile(
                            title = "My top 50",
                            subtitle = topPlayed.size.toString() + " songs",
                            icon = Icons.AutoMirrored.Rounded.TrendingUp,
                            onClick = {
                                if (topPlayed.isNotEmpty()) Store.play(topPlayed) else Store.notify("Play something first")
                            },
                        )
                    }
                    item {
                        LibraryTile(
                            title = "History",
                            subtitle = Store.plays.size.toString() + " plays",
                            icon = Icons.Rounded.History,
                            onClick = { Store.navigate(Route.History) },
                        )
                    }
                    item {
                        LibraryTile(
                            title = "Playlists",
                            subtitle = Store.playlists.size.toString() + " playlists",
                            icon = Icons.Rounded.PlaylistPlay,
                            onClick = { tab = LibraryTab.PLAYLISTS },
                        )
                    }

                    if (artists.isNotEmpty()) {
                        item {
                            Column {
                                Text(
                                    "Artists",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    artists.take(4).forEach { artist ->
                                        ArtistCell(artist, Modifier.width(180.dp))
                                    }
                                }
                            }
                        }
                    }
                    if (Store.playlists.isNotEmpty()) {
                        item {
                            Column {
                                Text(
                                    "Playlists",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Store.playlists.take(4).forEach { playlist ->
                                        Column(
                                            Modifier.width(170.dp).clickable {
                                                Store.navigate(Route.LocalPlaylist(playlist.id))
                                            },
                                        ) {
                                            CoverArt(playlist.tracks.firstOrNull()?.thumbnail, 162.dp, RoundedCornerShape(10.dp))
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                playlist.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                playlist.tracks.size.toString() + " songs",
                                                style = MaterialTheme.typography.bodyMedium,
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

            LibraryTab.SONGS -> {
                if (sortedSongs.isEmpty()) {
                    EmptyLibrary("Songs you like will appear here.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        itemsIndexed(sortedSongs, key = { index, track -> track.id + index }) { index, track ->
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
                        columns = GridCells.Adaptive(170.dp),
                        contentPadding = PaddingValues(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(albums, key = { it.browseId }) { album ->
                            CardTile(album, onClick = { openCard(album) }, size = 170.dp)
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
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(190.dp),
                        contentPadding = PaddingValues(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        items(artists, key = { it.title }) { artist ->
                            ArtistCell(artist)
                        }
                    }
                }
            }

            LibraryTab.PLAYLISTS -> {
                if (Store.playlists.isEmpty()) {
                    EmptyLibrary("Playlists you create will appear here.")
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(180.dp),
                        contentPadding = PaddingValues(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(Store.playlists, key = { it.id }) { playlist ->
                            Column(Modifier.clickable { Store.navigate(Route.LocalPlaylist(playlist.id)) }) {
                                CoverArt(playlist.tracks.firstOrNull()?.thumbnail, 176.dp, RoundedCornerShape(10.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    playlist.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    playlist.tracks.size.toString() + " songs",
                                    style = MaterialTheme.typography.bodyMedium,
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

@Composable
private fun TabStrip(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(30.dp),
    ) {
        tabs.forEachIndexed { index, label ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSelect(index) }.padding(vertical = 10.dp),
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (index == selected) FontWeight.Bold else FontWeight.Normal,
                    color =
                        if (index == selected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier
                        .width(if (index == selected) 64.dp else 0.dp)
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun LibraryTile(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Column {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth().aspectRatio(1.05f).clickable(onClick = onClick),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    modifier = Modifier.size(64.dp),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
private fun ArtistCell(artist: MediaCard.Artist, modifier: Modifier = Modifier) {
    val count = Store.liked.count { it.artists.firstOrNull() == artist.title }
    Column(
        modifier = modifier
            .clickable { openCard(artist) }
            .padding(4.dp),
    ) {
        CoverArt(artist.thumbnail, 168.dp, CircleShape)
        Spacer(Modifier.height(10.dp))
        Text(
            artist.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            count.toString() + if (count == 1) " song" else " songs",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AlbumRow(album: MediaCard.Album, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 6.dp),
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
