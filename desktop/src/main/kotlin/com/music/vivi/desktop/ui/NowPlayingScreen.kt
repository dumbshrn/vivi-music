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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Subject
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.Lyrics
import com.music.vivi.desktop.data.RepeatMode
import com.music.vivi.desktop.data.Store

/**
 * Apple-style full-screen player, matching the app's default player: big shadowed
 * artwork, title + like, thin seek bar, three large transport buttons and a row of
 * circular actions, with an optional synced-lyrics pane.
 */
@Composable
fun NowPlayingScreen() {
    val track = Store.current ?: return
    val colors = MaterialTheme.colorScheme
    val showLyrics = Store.showLyrics
    var menuOpen by remember { mutableStateOf(false) }

    val tint = Store.artSeed ?: colors.primary

    Box(Modifier.fillMaxSize().background(colors.surface)) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to tint.copy(alpha = 0.75f),
                        0.45f to colors.surface.copy(alpha = 0.92f),
                        0.75f to colors.surface,
                        1f to colors.surface,
                    ),
                ),
        )
        Column(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { Store.showNowPlaying = false }) {
                    Icon(
                        Icons.Rounded.ArrowDownward,
                        contentDescription = "Close player",
                        tint = colors.onSurface,
                    )
                }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Now Playing", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                    Text(
                        track.album ?: "VIVI Music",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { Store.showQueue = !Store.showQueue }) {
                    Icon(Icons.Rounded.QueueMusic, contentDescription = "Queue", tint = colors.onSurface)
                }
            }

            Row(Modifier.fillMaxSize().padding(top = 12.dp)) {
                if (showLyrics) {
                    LyricsPane(Modifier.fillMaxSize())
                } else {
                Spacer(Modifier.weight(1f))
                Column(
                    Modifier.widthIn(max = 500.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.weight(1f))
                    CoverArt(
                        track.thumbnail,
                        320.dp,
                        RoundedCornerShape(12.dp),
                        modifier = Modifier.shadow(28.dp, RoundedCornerShape(12.dp)),
                    )
                    Spacer(Modifier.height(28.dp))

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                track.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface,
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
                                if (Store.isLiked(track)) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Like",
                                tint = colors.onSurface,
                            )
                        }
                        Box {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "More", tint = colors.onSurface)
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                TrackMenuItems(track, { menuOpen = false })
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Slider(
                        value = Store.positionMs.coerceAtLeast(0).toFloat(),
                        onValueChange = { Store.seekTo(it.toLong()) },
                        valueRange = 0f..(if (Store.durationMs > 0) Store.durationMs.toFloat() else 1f),
                        colors = SliderDefaults.colors(
                            thumbColor = colors.onSurface,
                            activeTrackColor = colors.onSurface,
                            inactiveTrackColor = colors.onSurface.copy(alpha = 0.22f),
                        ),
                    )
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            formatTimeMillis(Store.positionMs),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            formatTimeMillis(Store.durationMs),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        Surface(shape = CircleShape, color = colors.surfaceContainerHigh) {
                            IconButton(onClick = { Store.previous() }, modifier = Modifier.size(56.dp)) {
                                Icon(
                                    Icons.Rounded.SkipPrevious,
                                    contentDescription = "Previous",
                                    tint = colors.onSurface,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                        }
                        FilledIconButton(
                            onClick = { Store.toggle() },
                            shape = CircleShape,
                            colors =
                                IconButtonDefaults.filledIconButtonColors(
                                    containerColor = colors.onSurface,
                                    contentColor = colors.surface,
                                ),
                            modifier = Modifier.size(78.dp),
                        ) {
                            if (Store.buffering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(26.dp),
                                    strokeWidth = 3.dp,
                                    color = colors.surface,
                                )
                            } else {
                                Icon(
                                    if (Store.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = "Play",
                                    modifier = Modifier.size(40.dp),
                                )
                            }
                        }
                        Surface(shape = CircleShape, color = colors.surfaceContainerHigh) {
                            IconButton(onClick = { Store.next() }, modifier = Modifier.size(56.dp)) {
                                Icon(
                                    Icons.Rounded.SkipNext,
                                    contentDescription = "Next",
                                    tint = colors.onSurface,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        CircleAction(
                            icon = Icons.Rounded.Subject,
                            description = "Lyrics",
                            active = showLyrics,
                            onClick = { Store.showLyrics = !Store.showLyrics },
                        )
                        CircleAction(
                            icon = Icons.Rounded.Shuffle,
                            description = "Shuffle",
                            active = Store.shuffle,
                            onClick = { Store.toggleShuffle() },
                        )
                        CircleAction(
                            icon = if (Store.repeat == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                            description = "Repeat",
                            active = Store.repeat != RepeatMode.OFF,
                            onClick = { Store.cycleRepeat() },
                        )
                    }

                    Spacer(Modifier.height(18.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.VolumeUp, contentDescription = null, tint = colors.onSurfaceVariant)
                        Slider(
                            value = Store.volume,
                            onValueChange = { Store.changeVolume(it) },
                            modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = colors.onSurface,
                                activeTrackColor = colors.onSurface,
                                inactiveTrackColor = colors.onSurface.copy(alpha = 0.22f),
                            ),
                        )
                    }

                    Store.playbackError?.let { error ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 10.dp),
                        ) {
                            Text(
                                error,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                }

                Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CircleAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color =
            if (active) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
            Icon(
                icon,
                contentDescription = description,
                tint =
                    if (active) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/** Large, centered, fading lyrics like the app's lyrics mode. */
@Composable
fun LyricsPane(modifier: Modifier = Modifier) {
    val lyrics = Store.lyrics
    val track = Store.current
    val state = rememberLazyListState()

    val activeIndex =
        remember(lyrics, Store.positionMs) {
            val lines = lyrics?.lines.orEmpty()
            lines.indexOfLast { it.timeMs >= 0 && it.timeMs <= Store.positionMs + 120 }
        }

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) state.animateScrollToItem((activeIndex - 2).coerceAtLeast(0))
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
                Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Lyrics from " + lyrics.provider,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        when (lyrics.sync) {
                            Lyrics.Sync.WORD -> "Word sync"
                            Lyrics.Sync.LINE -> "Line sync"
                            Lyrics.Sync.NONE -> "Unsynced"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(lyrics.lines) { index, line ->
                        val active = index == activeIndex
                        Text(
                            text = line.text.ifBlank { " " },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                            textAlign = TextAlign.Start,
                            color =
                                when {
                                    active -> MaterialTheme.colorScheme.onSurface
                                    lyrics.sync == Lyrics.Sync.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
        modifier = Modifier.width(420.dp).height(540.dp),
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
                    itemsIndexed(queue, key = { index, track -> track.id + index }) { index, track ->
                        val isCurrent = Store.current?.id == track.id
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isCurrent) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    } else {
                                        Color.Transparent
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
