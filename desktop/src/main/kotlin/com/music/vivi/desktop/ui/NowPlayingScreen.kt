package com.music.vivi.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.Lyrics
import com.music.vivi.desktop.data.RepeatMode
import com.music.vivi.desktop.data.Store

@Composable
fun NowPlayingScreen() {
    val track = Store.current ?: return
    val colors = MaterialTheme.colorScheme

    Surface(color = colors.surface) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.primaryContainer.copy(alpha = 0.55f),
                            colors.surface,
                            colors.surface,
                        ),
                    ),
                ),
        ) {
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { Store.showNowPlaying = false }) {
                        Icon(Icons.Rounded.ExpandMore, contentDescription = "Close")
                    }
                    Text(
                        "NOW PLAYING",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { Store.showQueue = !Store.showQueue }) {
                        Icon(Icons.Rounded.QueueMusic, contentDescription = "Queue")
                    }
                }

                Row(Modifier.fillMaxSize().padding(top = 8.dp)) {
                    Column(
                        Modifier.width(430.dp).fillMaxSize().padding(end = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CoverArt(track.thumbnail, 330.dp, RoundedCornerShape(26.dp))
                        Spacer(Modifier.height(26.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    track.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    track.artistLine,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            IconButton(onClick = {
                                val added = Store.toggleLike(track)
                                Store.notify(if (added) "Added to liked songs" else "Removed from liked songs")
                            }) {
                                Icon(
                                    Icons.Rounded.Favorite,
                                    contentDescription = "Like",
                                    tint = if (Store.isLiked(track)) colors.primary else colors.onSurfaceVariant,
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Slider(
                            value = Store.positionMs.coerceAtLeast(0).toFloat(),
                            onValueChange = { Store.seekTo(it.toLong()) },
                            valueRange = 0f..(if (Store.durationMs > 0) Store.durationMs.toFloat() else 1f),
                            colors =
                                SliderDefaults.colors(
                                    thumbColor = colors.primary,
                                    activeTrackColor = colors.primary,
                                    inactiveTrackColor = colors.surfaceVariant,
                                ),
                        )
                        Row(Modifier.fillMaxWidth()) {
                            Text(
                                formatTimeMillis(Store.positionMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                formatTimeMillis(Store.durationMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant,
                            )
                        }

                        Spacer(Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            IconButton(onClick = { Store.toggleShuffle() }) {
                                Icon(
                                    Icons.Rounded.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = if (Store.shuffle) colors.primary else colors.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = { Store.previous() }) {
                                Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(34.dp))
                            }
                            FilledIconButton(
                                onClick = { Store.toggle() },
                                shape = CircleShape,
                                modifier = Modifier.size(68.dp),
                            ) {
                                if (Store.buffering) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(26.dp),
                                        strokeWidth = 3.dp,
                                        color = colors.onPrimary,
                                    )
                                } else {
                                    Icon(
                                        if (Store.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                        contentDescription = "Play",
                                        modifier = Modifier.size(38.dp),
                                    )
                                }
                            }
                            IconButton(onClick = { Store.next() }) {
                                Icon(Icons.Rounded.SkipNext, contentDescription = "Next", modifier = Modifier.size(34.dp))
                            }
                            IconButton(onClick = { Store.cycleRepeat() }) {
                                Icon(
                                    if (Store.repeat == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                                    contentDescription = "Repeat",
                                    tint = if (Store.repeat != RepeatMode.OFF) colors.primary else colors.onSurfaceVariant,
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.VolumeUp, contentDescription = null, tint = colors.onSurfaceVariant)
                            Slider(
                                value = Store.volume,
                                onValueChange = { Store.changeVolume(it) },
                                modifier = Modifier.width(180.dp).padding(horizontal = 10.dp),
                                colors =
                                    SliderDefaults.colors(
                                        thumbColor = colors.primary,
                                        activeTrackColor = colors.primary,
                                        inactiveTrackColor = colors.surfaceVariant,
                                    ),
                            )
                        }
                    }

                    LyricsPane(Modifier.weight(1f).fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun LyricsPane(modifier: Modifier = Modifier) {
    val lyrics = Store.lyrics
    val track = Store.current
    val state = rememberLazyListState()

    val activeIndex =
        remember(lyrics, Store.positionMs) {
            val lines = lyrics?.lines.orEmpty()
            lines.indexOfLast { it.timeMs >= 0 && it.timeMs <= Store.positionMs + 120 }
        }

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            state.animateScrollToItem((activeIndex - 3).coerceAtLeast(0))
        }
    }

    Box(modifier, contentAlignment = Alignment.Center) {
        when {
            Store.lyricsLoading -> CircularProgressIndicator()
            lyrics == null || lyrics.lines.isEmpty() -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Lyrics", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    "No lyrics found for this track.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "LYRICS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(10.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Text(
                            "${lyrics.provider} • ${
                                when (lyrics.sync) {
                                    Lyrics.Sync.WORD -> "word sync"
                                    Lyrics.Sync.LINE -> "line sync"
                                    Lyrics.Sync.NONE -> "unsynced"
                                }
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    track?.album?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                LazyColumn(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(lyrics.lines) { index, line ->
                        val active = index == activeIndex
                        Text(
                            text = line.text,
                            style =
                                if (active) {
                                    MaterialTheme.typography.headlineSmall
                                } else {
                                    MaterialTheme.typography.titleMedium
                                },
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Start,
                            color =
                                when {
                                    active -> MaterialTheme.colorScheme.primary
                                    lyrics.sync == Lyrics.Sync.NONE -> MaterialTheme.colorScheme.onSurface
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (lyrics.sync != Lyrics.Sync.NONE) {
                                            Modifier
                                        } else {
                                            Modifier
                                        },
                                    ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QueuePanel() {
    val queue = Store.queue
    Surface(
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        shadowElevation = 16.dp,
        modifier = Modifier.width(420.dp).height(520.dp),
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Queue", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Text(
                    "${queue.size} tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                IconButton(onClick = { Store.showQueue = false }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close queue")
                }
            }
            if (queue.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Your queue is empty", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    itemsIndexed(queue, key = { index, track -> "${track.id}-$index" }) { index, track ->
                        val isCurrent = Store.current?.id == track.id
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isCurrent) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    } else {
                                        androidx.compose.ui.graphics.Color.Transparent
                                    },
                                    RoundedCornerShape(12.dp),
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = { Store.play(queue.toList(), index) }) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", modifier = Modifier.size(18.dp))
                            }
                            CoverArt(track.thumbnail, 40.dp, RoundedCornerShape(8.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    track.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                )
                                Text(
                                    track.artistLine,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            IconButton(onClick = { Store.removeFromQueue(index) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Remove", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
