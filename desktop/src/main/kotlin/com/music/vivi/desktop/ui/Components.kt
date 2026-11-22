package com.music.vivi.desktop.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddToQueue
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import java.util.Locale

@Composable
fun CoverArt(url: String?, size: Dp, shape: Shape = RoundedCornerShape(6.dp), modifier: Modifier = Modifier) {
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
                modifier = Modifier.fillMaxSize(),
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

/** The app's `NavigationTitle`: bold primary title, optional label and play-all pill. */
@Composable
fun NavigationTitle(
    title: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    thumbnail: (@Composable () -> Unit)? = null,
    onPlayAll: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        thumbnail?.invoke()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            if (!label.isNullOrBlank()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (onPlayAll != null) {
            OutlinedButton(
                onClick = onPlayAll,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(26.dp),
            ) {
                Text("Play all", style = MaterialTheme.typography.labelSmall)
            }
        }
        if (onClick != null) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
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
    val songs = section.cards.mapNotNull { (it as? MediaCard.SongCard)?.track }
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        NavigationTitle(
            title = section.title.ifBlank { "Made for you" },
            label = section.label,
            thumbnail = section.thumbnail?.takeIf { it.isNotBlank() }?.let { url ->
                { CoverArt(url, 40.dp, RoundedCornerShape(8.dp)) }
            },
            onPlayAll = songs.takeIf { it.isNotEmpty() }?.let { tracks -> { Store.play(tracks) } },
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(section.cards, key = { "${it.browseId}-${it.title}" }) { card ->
                CardTile(card = card, onClick = { onOpenCard(card) })
            }
        }
    }
}

@Composable
fun CardTile(card: MediaCard, onClick: () -> Unit, size: Dp = 140.dp) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val shape = if (card is MediaCard.Artist) CircleShape else RoundedCornerShape(6.dp)

    Column(
        modifier = Modifier
            .width(size)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .hoverable(interaction)
            .padding(4.dp),
    ) {
        Box {
            CoverArt(card.thumbnail, size - 8.dp, shape)
            if (hovered) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 6.dp,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                ) {
                    Icon(
                        imageVector = if (card is MediaCard.Artist) Icons.Rounded.Person else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(8.dp).size(20.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
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

/** Home speed-dial tile: square art, scrim, bold caption and chevron. */
@Composable
fun SpeedDialTile(card: MediaCard, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = if (card is MediaCard.Artist) CircleShape else RoundedCornerShape(6.dp)
    Box(modifier.clip(shape).clickable(onClick = onClick)) {
        Box(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (card.thumbnail.isNotBlank()) {
                AsyncImage(
                    model = card.thumbnail,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.85f),
                        ),
                    ),
                ),
        )
        Row(
            Modifier.align(Alignment.BottomStart).padding(10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (card !is MediaCard.SongCard) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/** Horizontal, 4-row quick picks grid matching the app's `LazyHorizontalGrid`. */
@Composable
fun QuickPicksRow(tracks: List<Track>, modifier: Modifier = Modifier) {
    if (tracks.isEmpty()) return
    val columns = tracks.distinctBy { it.id }.chunked(4)
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(columns) { column ->
            Column(Modifier.width(330.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                column.forEach { track ->
                    val index = tracks.indexOf(track)
                    QuickPickRow(track = track, queue = tracks, index = index)
                }
            }
        }
    }
}

@Composable
private fun QuickPickRow(track: Track, queue: List<Track>, index: Int) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val isCurrent = Store.current?.id == track.id
    val background by animateColorAsState(
        when {
            isCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            hovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else -> Color.Transparent
        },
        label = "quickPickBackground",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .hoverable(interaction)
            .clickable { Store.play(queue, index.coerceAtLeast(0)) }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.Center) {
            CoverArt(track.thumbnail, 48.dp, RoundedCornerShape(6.dp))
            if (hovered && !isCurrent) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)).background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White)
                }
            }
        }
        Spacer(Modifier.width(10.dp))
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
        if (isCurrent) PlayingIndicator()
    }
}

/** Animated three-bar indicator for the playing track. */
@Composable
fun PlayingIndicator(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    val transition = rememberInfiniteTransition(label = "playing")
    val heights =
        listOf(120, 340, 220).mapIndexed { index, delay ->
            transition.animateFloat(
                initialValue = 0.35f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 420, delayMillis = delay),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "bar$index",
            )
        }
    Row(
        modifier = modifier.height(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        heights.forEach { height ->
            Box(
                Modifier
                    .width(3.dp)
                    .height((16 * height.value).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
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
            else -> Color.Transparent
        },
        label = "rowBackground",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .hoverable(interaction)
            .clickable { Store.play(queue, index) }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(34.dp), contentAlignment = Alignment.Center) {
            when {
                isCurrent && Store.playing -> PlayingIndicator()
                hovered -> IconButton(onClick = { if (isCurrent) Store.toggle() else Store.play(queue, index) }) {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                else -> Text(
                    text = (index + 1).toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (showArtwork) {
            CoverArt(track.thumbnail, 46.dp, RoundedCornerShape(6.dp))
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
                modifier = Modifier.width(200.dp),
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
                tint = if (liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    HorizontalDivider()
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
                    HorizontalDivider()
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
