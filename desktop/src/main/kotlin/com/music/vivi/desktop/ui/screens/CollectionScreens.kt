package com.music.vivi.desktop.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.music.vivi.desktop.data.AlbumDetails
import com.music.vivi.desktop.data.ArtistDetails
import com.music.vivi.desktop.data.PlaylistDetails
import com.music.vivi.desktop.data.Repository
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.data.Track
import com.music.vivi.desktop.ui.CoverArt
import com.music.vivi.desktop.ui.SectionRow
import com.music.vivi.desktop.ui.TrackRow

@Composable
private fun CollectionHeader(
    artwork: String,
    title: String,
    subtitle: String,
    tracks: List<Track>,
    round: Boolean = false,
    extra: @Composable () -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 20.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        CoverArt(
            artwork,
            216.dp,
            if (round) CircleShape else RoundedCornerShape(20.dp),
        )
        Spacer(Modifier.width(26.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { if (tracks.isNotEmpty()) Store.play(tracks) },
                    shape = CircleShape,
                    enabled = tracks.isNotEmpty(),
                ) {
                    Icon(Icons.Rounded.PlayArrow, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Play")
                }
                FilledTonalButton(
                    onClick = {
                        if (tracks.isNotEmpty()) {
                            Store.shuffle = true
                            Store.play(tracks.shuffled())
                        }
                    },
                    shape = CircleShape,
                    enabled = tracks.isNotEmpty(),
                ) {
                    Icon(Icons.Rounded.Shuffle, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Shuffle")
                }
                extra()
            }
        }
    }
}

@Composable
private fun LoadingOrError(loading: Boolean, error: String?, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (loading) {
            CircularProgressIndicator()
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error ?: "Nothing here", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(10.dp))
                Button(onClick = onRetry) { Text("Retry") }
            }
        }
    }
}

@Composable
fun AlbumScreen(browseId: String) {
    var details by remember(browseId) { mutableStateOf<AlbumDetails?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var token by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(browseId, token) {
        loading = true
        error = null
        Repository.album(browseId)
            .onSuccess { details = it }
            .onFailure { error = it.message }
        loading = false
    }

    val album = details
    if (album == null) {
        LoadingOrError(loading, error) { token += 1 }
        return
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            CollectionHeader(
                artwork = album.album.thumbnail,
                title = album.album.title,
                subtitle =
                    listOfNotNull(
                        album.album.artists?.joinToString(", ") { it.name }?.ifBlank { null },
                        album.album.year?.toString(),
                        "${album.songs.size} songs",
                    ).joinToString(" • "),
                tracks = album.songs,
            )
        }
        itemsIndexed(album.songs, key = { index, track -> "${track.id}-$index" }) { index, track ->
            Box(Modifier.padding(horizontal = 20.dp)) {
                TrackRow(
                    track = track,
                    index = index,
                    queue = album.songs,
                    showAlbum = false,
                    scope = scope,
                )
            }
        }
    }
}

@Composable
fun ArtistScreen(browseId: String) {
    var details by remember(browseId) { mutableStateOf<ArtistDetails?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var token by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(browseId, token) {
        loading = true
        error = null
        Repository.artist(browseId)
            .onSuccess { details = it }
            .onFailure { error = it.message }
        loading = false
    }

    val artist = details
    if (artist == null) {
        LoadingOrError(loading, error) { token += 1 }
        return
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            CollectionHeader(
                artwork = artist.artist.thumbnail.orEmpty(),
                title = artist.artist.title,
                subtitle =
                    listOfNotNull(artist.monthlyListeners, artist.subscriberCount)
                        .joinToString(" • ")
                        .ifBlank { "Artist" },
                tracks = artist.topSongs,
                round = true,
            )
        }
        if (artist.topSongs.isNotEmpty()) {
            item {
                Text(
                    "Top songs",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp),
                )
            }
            itemsIndexed(artist.topSongs, key = { index, track -> "${track.id}-$index" }) { index, track ->
                Box(Modifier.padding(horizontal = 20.dp)) {
                    TrackRow(
                        track = track,
                        index = index,
                        queue = artist.topSongs,
                        showArtwork = true,
                        showAlbum = false,
                        scope = scope,
                    )
                }
            }
        }
        items(artist.sections.size) { index ->
            SectionRow(artist.sections[index]) { card -> openCard(card) }
        }
    }
}

@Composable
fun PlaylistScreen(playlistId: String) {
    var details by remember(playlistId) { mutableStateOf<PlaylistDetails?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var token by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(playlistId, token) {
        loading = true
        error = null
        Repository.playlist(playlistId)
            .onSuccess { details = it }
            .onFailure { error = it.message }
        loading = false
    }

    val playlist = details
    if (playlist == null) {
        LoadingOrError(loading, error) { token += 1 }
        return
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            CollectionHeader(
                artwork = playlist.playlist.thumbnail.orEmpty(),
                title = playlist.playlist.title,
                subtitle =
                    listOfNotNull(
                        playlist.playlist.author?.name,
                        "${playlist.songs.size} songs",
                    ).joinToString(" • "),
                tracks = playlist.songs,
            )
        }
        itemsIndexed(playlist.songs, key = { index, track -> "${track.id}-$index" }) { index, track ->
            Box(Modifier.padding(horizontal = 20.dp)) {
                TrackRow(
                    track = track,
                    index = index,
                    queue = playlist.songs,
                    showArtwork = true,
                    showAlbum = false,
                    scope = scope,
                )
            }
        }
        items(playlist.related.size) { index ->
            SectionRow(com.music.vivi.desktop.data.UiSection("More like this", playlist.related)) { card -> openCard(card) }
        }
    }
}

@Composable
fun LocalPlaylistScreen(playlistId: String) {
    val playlist = Store.playlists.firstOrNull { it.id == playlistId }
    val scope = rememberCoroutineScope()
    var renaming by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(playlist?.name.orEmpty()) }
    var confirmingDelete by remember { mutableStateOf(false) }

    if (playlist == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Playlist not found") }
        return
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            CollectionHeader(
                artwork = playlist.tracks.firstOrNull()?.thumbnail.orEmpty(),
                title = playlist.name,
                subtitle = "${playlist.tracks.size} songs",
                tracks = playlist.tracks,
                extra = {
                    IconButton(onClick = { renaming = true; name = playlist.name }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Rename")
                    }
                    IconButton(onClick = { confirmingDelete = true }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                    }
                },
            )
        }
        if (playlist.tracks.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Add songs from any track menu.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        itemsIndexed(playlist.tracks, key = { index, track -> "${track.id}-$index" }) { index, track ->
            Box(Modifier.padding(horizontal = 20.dp)) {
                TrackRow(
                    track = track,
                    index = index,
                    queue = playlist.tracks,
                    showArtwork = true,
                    showAlbum = false,
                    scope = scope,
                    onRemove = { Store.removeFromPlaylist(playlist.id, track.id) },
                )
            }
        }
    }

    if (renaming) {
        AlertDialog(
            onDismissRequest = { renaming = false },
            title = { Text("Rename playlist") },
            text = { OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = {
                    Store.renamePlaylist(playlist.id, name)
                    renaming = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { renaming = false }) { Text("Cancel") } },
        )
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("Delete playlist?") },
            text = { Text("${playlist.name} will be removed from your library.") },
            confirmButton = {
                TextButton(onClick = {
                    Store.deletePlaylist(playlist.id)
                    confirmingDelete = false
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmingDelete = false }) { Text("Cancel") } },
        )
    }
}
