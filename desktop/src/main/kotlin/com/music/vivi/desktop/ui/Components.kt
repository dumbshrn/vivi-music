package com.music.vivi.desktop.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddToQueue
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Radio
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.music.vivi.desktop.data.MediaCard
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.data.Track
import com.music.vivi.desktop.data.UiSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun CoverArt(url: String?, size: Dp, shape: Shape = RoundedCornerShape(12.dp), modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
            )
        } else {
            Icon(
                Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size / 3),
            )
        }
    }
}

@Composable
fun SectionRow(
    section: UiSection,
    onOpenCard: (MediaCard) -> Unit,
) {
    if (section.cards.isEmpty()) return
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp),
        )
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(section.cards, key = { it.browseId + it.title }) { card ->
                CardTile(card = card, onClick = { onOpenCard(card) })
            }
        }
    }
}

@Composable
fun CardTile(card: MediaCard, onClick: () -> Unit, size: Dp = 172.dp) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val shape = if (card is MediaCard.Artist) CircleShape else RoundedCornerShape(18.dp)

    Column(
        modifier = Modifier
            .width(size)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .hoverable(interaction)
            .padding(6.dp),
    ) {
        Box {
            CoverArt(card.thumbnail, size - 12.dp, shape)
            androidx.compose.animation.AnimatedVisibility(
                visible = hovered,
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 6.dp,
                ) {
                    Icon(
                        imageVector = if (card is MediaCard.Artist) Icons.Rounded.Person else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(10.dp).size(22.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = card.title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        val subtitle =
            when (card) {
                is MediaCard.Album -> listOfNotNull(card.artists.ifBlank { null }, card.year?.toString()).joinToString(" • ")
                is MediaCard.Playlist -> card.author
                is MediaCard.SongCard -> card.track.artistLine
                is MediaCard.Artist -> "Artist"
            }
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun TrackRow(
    track: Track,
    index: Int,
    queue: List<Track>,
    showArtwork: Boolean = false,
    showAlbum: Boolean = true,
    scope: CoroutineScope,
    onRemove: (() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val isCurrent = Store.current?.id == track.id
    val liked = Store.isLiked(track)
    var menuOpen by remember { mutableStateOf(false) }

    val background by animateColorAsState(
        when {
            isCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            hovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else -> androidx.compose.ui.graphics.Color.Transparent
        },
        label = "rowBackground",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .hoverable(interaction)
            .clickable { Store.play(queue, index) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(34.dp), contentAlignment = Alignment.Center) {
            if (hovered || isCurrent) {
                IconButton(onClick = { if (isCurrent) Store.toggle() else Store.play(queue, index) }) {
                    Icon(
                        if (isCurrent && Store.playing) Icons.Rounded.PlaylistPlay else Icons.Rounded.PlayArrow,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                Text(
                    text = (index + 1).toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (showArtwork) {
            CoverArt(track.thumbnail, 46.dp, RoundedCornerShape(10.dp))
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
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
        if (showAlbum) {
            Text(
                text = track.album.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(220.dp),
            )
        }
        Text(
            text = formatDuration(track.duration),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(52.dp),
        )
        IconButton(onClick = {
            val added = Store.toggleLike(track)
            Store.notify(if (added) "Added to liked songs" else "Removed from liked songs")
        }) {
            Icon(
                Icons.Rounded.Favorite,
                contentDescription = "Like",
                tint =
                    if (liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Play next") },
                    leadingIcon = { Icon(Icons.Rounded.PlayArrow, null) },
                    onClick = {
                        menuOpen = false
                        Store.playNext(track)
                    },
                )
                DropdownMenuItem(
                    text = { Text("Add to queue") },
                    leadingIcon = { Icon(Icons.Rounded.AddToQueue, null) },
                    onClick = {
                        menuOpen = false
                        Store.addToQueue(track)
                    },
                )
                DropdownMenuItem(
                    text = { Text("Start radio") },
                    leadingIcon = { Icon(Icons.Rounded.Radio, null) },
                    onClick = {
                        menuOpen = false
                        Store.startRadio(track)
                    },
                )
                DropdownMenuItem(
                    text = { Text("New playlist with this song") },
                    leadingIcon = { Icon(Icons.Rounded.PlaylistAdd, null) },
                    onClick = {
                        menuOpen = false
                        val playlist = Store.createPlaylist("New playlist", listOf(track))
                        Store.notify("Created ${playlist.name}")
                    },
                )
                if (Store.playlists.isNotEmpty()) {
                    androidx.compose.material3.HorizontalDivider()
                    Store.playlists.take(5).forEach { playlist ->
                        DropdownMenuItem(
                            text = { Text(playlist.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingIcon = { Icon(Icons.Rounded.Add, null) },
                            onClick = {
                                menuOpen = false
                                Store.addToPlaylist(playlist.id, listOf(track))
                                Store.notify("Added to ${playlist.name}")
                            },
                        )
                    }
                }
                if (onRemove != null) {
                    androidx.compose.material3.HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Remove") },
                        leadingIcon = { Icon(Icons.Rounded.Check, null) },
                        onClick = {
                            menuOpen = false
                            onRemove()
                        },
                    )
                }
            }
        }
    }
}

fun formatDuration(seconds: Int): String {
    if (seconds <= 0) return ""
    val minutes = seconds / 60
    val remaining = seconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, remaining)
}

fun formatTimeMillis(milliseconds: Long): String {
    if (milliseconds <= 0) return "0:00"
    val total = milliseconds / 1000
    val minutes = total / 60
    val seconds = total % 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes % 60, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}
