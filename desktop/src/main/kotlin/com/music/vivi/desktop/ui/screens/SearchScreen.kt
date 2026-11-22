package com.music.vivi.desktop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.MediaCard
import com.music.vivi.desktop.data.Repository
import com.music.vivi.desktop.data.UiSection
import com.music.vivi.desktop.ui.SectionRow
import com.music.vivi.desktop.ui.TrackRow

private val suggestions = listOf("Blinding Lights", "Lo-fi beats", "Arijit Singh", "The Weeknd", "Hans Zimmer", "K-pop")

@Composable
fun SearchScreen(initialQuery: String) {
    var query by remember(initialQuery) { mutableStateOf(initialQuery) }
    var sections by remember { mutableStateOf<List<UiSection>?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var token by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(query, token) {
        if (query.isBlank()) {
            sections = null
            return@LaunchedEffect
        }
        loading = true
        error = null
        val result = Repository.searchAll(query)
        sections = result
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        if (query.isBlank()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Search, null, modifier = Modifier.height(56.dp))
                    Text(
                        "Search YouTube Music",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Text(
                        "Songs, albums, artists and playlists — ad-free.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    androidx.compose.foundation.layout.Row(
                        Modifier.padding(top = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        suggestions.forEach { suggestion ->
                            AssistChip(
                                onClick = {
                                    query = suggestion
                                    com.music.vivi.desktop.data.Store.searchQuery = suggestion
                                },
                                label = { Text(suggestion) },
                                shape = CircleShape,
                            )
                        }
                    }
                }
            }
            return@Column
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        error?.let {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            sections.orEmpty().forEach { section ->
                if (section.cards.all { it is MediaCard.SongCard }) {
                    item(key = "header-${section.title}") {
                        Text(
                            section.title,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp),
                        )
                    }
                    val tracks = section.cards.mapNotNull { (it as? MediaCard.SongCard)?.track }
                    itemsIndexed(tracks, key = { index, track -> "${track.id}-$index" }) { index, track ->
                        Box(Modifier.padding(horizontal = 20.dp)) {
                            TrackRow(
                                track = track,
                                index = index,
                                queue = tracks,
                                showArtwork = true,
                                scope = scope,
                            )
                        }
                    }
                } else {
                    item(key = "row-${section.title}") {
                        SectionRow(section) { card -> openCard(card) }
                    }
                }
            }
        }
    }
}
