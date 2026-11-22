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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.PlayRecord
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.ui.NavigationTitle
import com.music.vivi.desktop.ui.TrackRow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private data class HistoryGroup(val label: String, val records: List<PlayRecord>)

/**
 * History with the app's date grouping, free-text filter and shuffle action.
 * Remote (account) history needs a Google sign-in, which the desktop client
 * does not have, so only local history is listed.
 */
@Composable
fun HistoryScreen() {
    var query by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val plays = Store.plays.toList()
    val records =
        remember(plays, query) {
            plays.filter { record ->
                query.isBlank() ||
                    record.track.title.contains(query, ignoreCase = true) ||
                    record.track.artistLine.contains(query, ignoreCase = true) ||
                    record.track.album.orEmpty().contains(query, ignoreCase = true)
            }
        }

    val groups = remember(records) { groupByDate(records) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search history") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = CircleShape,
                modifier = Modifier.width(360.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "${records.size} plays",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            if (records.isNotEmpty()) {
                Button(shape = CircleShape, onClick = { Store.play(records.map { it.track }.shuffled()) }) {
                    Icon(Icons.Rounded.Shuffle, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Shuffle")
                }
                Spacer(Modifier.width(6.dp))
                IconButton(onClick = { Store.clearHistory() }) {
                    Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear history")
                }
            }
        }

        if (groups.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (query.isBlank()) "Nothing played yet." else "No history matches \"$query\".",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                for (group in groups) {
                    item(key = "header-${group.label}") {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(top = 6.dp),
                        ) {
                            NavigationTitle(title = group.label)
                        }
                    }
                    val tracks = group.records.map { it.track }
                    itemsIndexed(group.records, key = { index, record -> "${record.playedAt}-$index" }) { index, record ->
                        TrackRow(
                            track = record.track,
                            index = index,
                            queue = tracks,
                            showArtwork = true,
                            showAlbum = false,
                            scope = scope,
                            onRemove = { Store.removeHistoryEntry(record.playedAt) },
                        )
                    }
                }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}

private fun groupByDate(records: List<PlayRecord>): List<HistoryGroup> {
    val today = LocalDate.now()
    val zone = ZoneId.systemDefault()

    return records
        .groupBy { record ->
            val date = Instant.ofEpochMilli(record.playedAt).atZone(zone).toLocalDate()
            val days = ChronoUnit.DAYS.between(date, today)
            when {
                days <= 0L -> "Today"
                days == 1L -> "Yesterday"
                days <= 7L -> "This week"
                days <= 14L -> "Last week"
                else -> date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            }
        }
        .entries
        .sortedByDescending { entry -> entry.value.maxOf { it.playedAt } }
        .map { HistoryGroup(it.key, it.value) }
}
