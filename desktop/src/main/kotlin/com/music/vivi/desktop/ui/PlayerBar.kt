package com.music.vivi.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.Store

/** Floating mini player, tinted by the current artwork like the app's Apple mini player. */
@Composable
fun PlayerBar() {
    val track = Store.current
    val tint = Store.artSeed ?: MaterialTheme.colorScheme.primary

    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth().clickable { if (track != null) Store.showNowPlaying = true },
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                tint.copy(alpha = 0.55f),
                                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                            ),
                        ),
                    ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (track == null) {
                        Text(
                            "Pick a song to start listening",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.weight(1f))
                    } else {
                        CoverArt(track.thumbnail, 56.dp, RoundedCornerShape(10.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                track.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                track.artistLine,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.75f),
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
                                        Color.White.copy(alpha = 0.75f)
                                    },
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { Store.previous() }) {
                            Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = Color.White)
                        }
                        Box(
                            Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { Store.toggle() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                if (Store.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                        IconButton(onClick = { Store.next() }) {
                            Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = Color.White)
                        }
                        Spacer(Modifier.width(6.dp))
                        IconButton(onClick = { Store.showQueue = !Store.showQueue }) {
                            Icon(
                                Icons.Rounded.QueueMusic,
                                contentDescription = "Queue",
                                tint =
                                    if (Store.showQueue) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        Color.White.copy(alpha = 0.75f)
                                    },
                            )
                        }
                        IconButton(onClick = { Store.showNowPlaying = true }) {
                            Icon(Icons.Rounded.ExpandLess, contentDescription = "Open player", tint = Color.White)
                        }
                    }

                    if (track == null) {
                        Text(
                            "Search or explore to find something",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (track != null) {
                    // Thin progress line hugging the bottom of the mini player.
                    Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().height(2.dp)) {
                        Box(
                            Modifier
                                .fillMaxWidth(
                                    if (Store.durationMs > 0) {
                                        (Store.positionMs.toFloat() / Store.durationMs).coerceIn(0f, 1f)
                                    } else {
                                        0f
                                    },
                                )
                                .height(2.dp)
                                .background(Color.White.copy(alpha = 0.8f)),
                        )
                    }
                }
            }
        }
    }
}
