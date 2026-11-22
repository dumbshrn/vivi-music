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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.music.vivi.desktop.data.Route
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.data.Track
import com.music.vivi.desktop.ui.CoverArt
import com.music.vivi.desktop.ui.TrackRow

private enum class LibraryTab(val label: String) { LIKED("Liked songs"), PLAYLISTS("Playlists"), HISTORY("History") }

@Composable
fun LibraryScreen() {
    var tab by remember { mutableStateOf(LibraryTab.LIKED) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp),
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
            if (Store.liked.isNotEmpty() && tab == LibraryTab.LIKED) {
                Button(
                    onClick = { Store.play(Store.liked.toList()) },
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Icon(Icons.Rounded.PlayArrow, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Play all")
                }
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = {
                    val playlist = Store.createPlaylist("My playlist #${Store.playlists.size + 1}")
                    Store.navigate(Route.LocalPlaylist(playlist.id))
                },
                shape = RoundedCornerShape(24.dp),
            ) {
                Icon(Icons.Rounded.Add, null)
                Spacer(Modifier.width(6.dp))
                Text("New playlist")
            }
        }

        TabRow(selectedTabIndex = tab.ordinal, modifier = Modifier.padding(horizontal = 20.dp)) {
            LibraryTab.entries.forEach { entry ->
                Tab(
                    selected = tab == entry,
                    onClick = { tab = entry },
                    text = { Text(entry.label) },
                )
            }
        }

        val tracks: List<Track> =
            when (tab) {
                LibraryTab.LIKED -> Store.liked.toList()
                LibraryTab.HISTORY -> Store.history.toList()
                LibraryTab.PLAYLISTS -> emptyList()
            }

        when (tab) {
            LibraryTab.PLAYLISTS -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(180.dp),
                    contentPadding = PaddingValues(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(Store.playlists, key = { it.id }) { playlist ->
                        Column(
                            Modifier.clickable { Store.navigate(Route.LocalPlaylist(playlist.id)) },
                        ) {
                            CoverArt(
                                playlist.tracks.firstOrNull()?.thumbnail,
                                172.dp,
                                RoundedCornerShape(18.dp),
                            )
                            Spacer(Modifier.height(8.dp))
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
            }

            else -> {
                if (tracks.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (tab == LibraryTab.LIKED) "Songs you like will appear here." else "Nothing played yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (tab == LibraryTab.HISTORY) {
                            item {
                                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(Modifier.weight(1f))
                                    IconButton(onClick = { Store.history.clear() }) {
                                        Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear history")
                                    }
                                }
                            }
                        }
                        itemsIndexed(tracks, key = { index, track -> "${track.id}-$index" }) { index, track ->
                            TrackRow(
                                track = track,
                                index = index,
                                queue = tracks,
                                showArtwork = true,
                                scope = scope,
                                onRemove =
                                    if (tab == LibraryTab.LIKED) {
                                        { Store.toggleLike(track) }
                                    } else {
                                        { Store.history.remove(track) }
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
}
