package com.music.vivi.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.MediaCard
import com.music.vivi.desktop.data.Repository
import com.music.vivi.desktop.data.SearchCategory
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.data.Track
import com.music.vivi.desktop.data.UiSection
import com.music.vivi.desktop.ui.CardTile
import com.music.vivi.desktop.ui.NavigationTitle
import com.music.vivi.desktop.ui.SectionRow
import com.music.vivi.desktop.ui.TrackRow
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(initialQuery: String) {
    var query by remember { mutableStateOf(initialQuery) }
    var submitted by remember { mutableStateOf(initialQuery.trim()) }
    var category by remember { mutableStateOf(SearchCategory.ALL) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var results by remember { mutableStateOf<Map<SearchCategory, List<MediaCard>>>(emptyMap()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) {
            query = initialQuery
            submitted = initialQuery.trim()
        }
    }

    LaunchedEffect(query) {
        if (query.isBlank() || query == submitted) {
            suggestions = emptyList()
            return@LaunchedEffect
        }
        delay(220)
        suggestions = Repository.searchSuggestions(query)
    }

    LaunchedEffect(submitted, category) {
        val term = submitted
        if (term.isBlank()) return@LaunchedEffect
        loading = true
        error = null
        if (category == SearchCategory.ALL) {
            val sections = Repository.searchAll(term)
            results =
                sections.associate { section ->
                    val entry =
                        SearchCategory.entries.firstOrNull { it.label == section.title }
                            ?: SearchCategory.ALL
                    entry to section.cards
                } + (SearchCategory.ALL to sections.flatMap { it.cards })
        } else {
            val cards = Repository.search(term, category.filter!!)
            results = results + (category to cards)
        }
        loading = false
    }

    val submit: (String) -> Unit = { value ->
        val term = value.trim()
        if (term.isNotEmpty()) {
            query = term
            submitted = term
            Store.searchQuery = term
            Store.addSearch(term)
            suggestions = emptyList()
            results = emptyMap()
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f).widthIn(max = 720.dp).focusRequester(focusRequester),
                placeholder = { Text("Songs, albums, artists, playlists…") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            query = ""
                            submitted = ""
                            results = emptyMap()
                            Store.searchQuery = ""
                        }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = CircleShape,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { submit(query) }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
        }

        if (suggestions.isNotEmpty() && query != submitted) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp)) {
                suggestions.forEach { suggestion ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { submit(suggestion) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(suggestion, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            return@Column
        }

        if (submitted.isBlank()) {
            EmptySearch(onSearch = submit)
            return@Column
        }

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SearchCategory.entries.forEach { entry ->
                FilterChip(
                    selected = category == entry,
                    onClick = { category = entry },
                    label = { Text(entry.label) },
                    shape = CircleShape,
                    border = null,
                    colors =
                        androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                )
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
            else -> {
                val cards = results[category].orEmpty()
                if (cards.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No results for \"$submitted\"",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    when (category) {
                        SearchCategory.ALL -> AllResults(
                            cardsByCategory = results,
                            scope = scope,
                        )
                        SearchCategory.SONGS, SearchCategory.VIDEOS -> SongResults(cards, scope)
                        else -> GridResults(cards)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearch(onSearch: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (Store.searches.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Recent searches",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "Clear",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { Store.clearSearches() }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            Store.searches.forEach { term ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSearch(term) }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(term, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = { Store.removeSearch(term) }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text("Browse", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("Lo-fi beats", "Arijit Singh", "The Weeknd", "K-pop", "Hans Zimmer", "Workout")) { suggestion ->
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.clickable { onSearch(suggestion) },
                ) {
                    Text(
                        suggestion,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AllResults(cardsByCategory: Map<SearchCategory, List<MediaCard>>, scope: kotlinx.coroutines.CoroutineScope) {
    LazyColumn(contentPadding = PaddingValues(vertical = 6.dp)) {
        SearchCategory.entries.filter { it != SearchCategory.ALL }.forEach { entry ->
            val cards = cardsByCategory[entry].orEmpty()
            if (cards.isEmpty()) return@forEach
            item(key = "header-${entry.name}") {
                Text(
                    entry.label,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                )
            }
            if (entry == SearchCategory.SONGS) {
                val tracks = cards.mapNotNull { (it as? MediaCard.SongCard)?.track }
                itemsIndexed(tracks, key = { index, track -> "${track.id}-$index" }) { index, track ->
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
            } else {
                item(key = "row-${entry.name}") {
                    com.music.vivi.desktop.ui.SectionRow(
                        com.music.vivi.desktop.data.UiSection(entry.label, cards),
                    ) { card -> openCard(card) }
                }
            }
        }
    }
}

@Composable
private fun SongResults(cards: List<MediaCard>, scope: kotlinx.coroutines.CoroutineScope) {
    val tracks = cards.mapNotNull { (it as? MediaCard.SongCard)?.track }
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        itemsIndexed(tracks, key = { index, track -> "${track.id}-$index" }) { index, track ->
            TrackRow(
                track = track,
                index = index,
                queue = tracks,
                showArtwork = true,
                showAlbum = true,
                scope = scope,
            )
        }
    }
}

@Composable
private fun GridResults(cards: List<MediaCard>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(cards, key = { "${it.browseId}-${it.title}" }) { card ->
            CardTile(card = card, onClick = { openCard(card) }, size = 150.dp)
        }
    }
}
