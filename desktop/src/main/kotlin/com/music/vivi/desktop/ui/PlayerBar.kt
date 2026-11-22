package com.music.vivi.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.RepeatMode
import com.music.vivi.desktop.data.Store

@Composable
fun PlayerBar() {
    val track = Store.current
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(94.dp).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(Modifier.width(330.dp), verticalAlignment = Alignment.CenterVertically) {
                if (track != null) {
                    CoverArt(track.thumbnail, 56.dp, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = track.artistLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            tint =
                                if (Store.isLiked(track)) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                } else {
                    Text(
                        "Pick a song to start listening",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    IconButton(onClick = { Store.toggleShuffle() }) {
                        Icon(
                            Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle",
                            tint =
                                if (Store.shuffle) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                    IconButton(onClick = { Store.previous() }) {
                        Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous")
                    }
                    FilledIconButton(
                        onClick = { Store.toggle() },
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp),
                    ) {
                        if (Store.buffering) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Icon(
                                if (Store.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (Store.playing) "Pause" else "Play",
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }
                    IconButton(onClick = { Store.next() }) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = "Next")
                    }
                    IconButton(onClick = { Store.cycleRepeat() }) {
                        Icon(
                            if (Store.repeat == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                            contentDescription = "Repeat",
                            tint =
                                if (Store.repeat != RepeatMode.OFF) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatTimeMillis(Store.positionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Slider(
                        value = Store.positionMs.coerceAtLeast(0).toFloat(),
                        onValueChange = { Store.seekTo(it.toLong()) },
                        valueRange = 0f..(if (Store.durationMs > 0) Store.durationMs.toFloat() else 1f),
                        modifier = Modifier.weight(1f).height(24.dp).padding(horizontal = 12.dp),
                        colors =
                            SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                    )
                    Text(
                        formatTimeMillis(Store.durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                modifier = Modifier.width(280.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Icon(
                    when {
                        Store.volume <= 0.01f -> Icons.Rounded.VolumeOff
                        Store.volume < 0.5f -> Icons.Rounded.VolumeDown
                        else -> Icons.Rounded.VolumeUp
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Slider(
                    value = Store.volume,
                    onValueChange = { Store.changeVolume(it) },
                    modifier = Modifier.width(110.dp).padding(horizontal = 10.dp),
                    colors =
                        SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                )
                IconButton(onClick = { Store.showNowPlaying = true }) {
                    Icon(Icons.Rounded.Lyrics, contentDescription = "Lyrics")
                }
                IconButton(onClick = { Store.showQueue = !Store.showQueue }) {
                    Icon(
                        Icons.Rounded.QueueMusic,
                        contentDescription = "Queue",
                        tint =
                            if (Store.showQueue) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
                IconButton(onClick = { Store.showNowPlaying = true }) {
                    Icon(Icons.Rounded.ExpandLess, contentDescription = "Now playing")
                }
            }
        }
    }
}

@Composable
fun MiniCover(trackThumbnail: String?) {
    Box(Modifier.width(56.dp)) {
        CoverArt(trackThumbnail, 56.dp)
    }
}
