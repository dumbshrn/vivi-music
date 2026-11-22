package com.music.vivi.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Subject
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.AudioQuality
import com.music.vivi.desktop.data.Route
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.data.ThemePreference
import java.awt.Desktop
import java.nio.file.Paths

private val seedPresets = listOf(
    "Gold" to Color(0xFFC8B273),
    "Rose" to Color(0xFFED5564),
    "Lilac" to Color(0xFF9C86E8),
    "Ocean" to Color(0xFF4F8FE8),
    "Leaf" to Color(0xFF4F9E5A),
    "Mono" to Color(0xFF000000),
)

@Composable
fun SettingsScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 8.dp),
    ) {
        Spacer(Modifier.height(6.dp))

        SettingsGroup {
            SettingsRow(
                icon = Icons.Rounded.Person,
                title = if (Store.signedIn) "Account" else "Sign in",
                subtitle = if (Store.signedIn) "Signed in to YouTube Music" else "Sync your library and playlists",
                onClick = { Store.navigate(Route.Account) },
            )
            SettingsRow(
                icon = Icons.Rounded.LibraryMusic,
                title = "Listening summary",
                subtitle = "Your top songs, artists and albums",
                onClick = { Store.navigate(Route.Stats) },
            )
            SettingsRow(
                icon = Icons.Rounded.CheckCircle,
                title = "History",
                subtitle = Store.plays.size.toString() + " plays",
                onClick = { Store.navigate(Route.History) },
            )
        }

        SettingsGroup {
            Row(Modifier.fillMaxWidth().padding(bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Theme, colors and player style",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemePreference.entries.forEach { preference ->
                    FilterChip(
                        selected = Store.theme == preference,
                        onClick = { Store.applyTheme(preference) },
                        label = {
                            Text(
                                when (preference) {
                                    ThemePreference.SYSTEM -> "System"
                                    ThemePreference.LIGHT -> "Light"
                                    ThemePreference.DARK -> "Dark"
                                },
                            )
                        },
                    )
                }
            }
            SettingSwitch(
                title = "Material You from album art",
                subtitle = "Colors shift to match what is playing",
                checked = Store.dynamicColors,
                onChange = { Store.applyDynamicColors(it) },
            )
            SettingSwitch(
                title = "Pure black",
                subtitle = "OLED-friendly true black surfaces",
                checked = Store.pureBlack,
                onChange = { Store.togglePureBlack() },
            )
            Text(
                "Seed color",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 14.dp),
            )
            Row(
                Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                seedPresets.forEach { (_, color) ->
                    Surface(
                        shape = CircleShape,
                        color = color,
                        modifier = Modifier.size(34.dp).clickable { Store.setSeed(color) },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (Store.seed == color) {
                                Text("✓", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }

        SettingsGroup {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Player and audio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Audio quality and autoplay",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text("Streaming quality", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 14.dp))
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AudioQuality.entries.forEach { quality ->
                    FilterChip(
                        selected = Store.quality == quality,
                        onClick = { Store.applyQuality(quality) },
                        label = {
                            Text(
                                when (quality) {
                                    AudioQuality.LOW -> "Data saver"
                                    AudioQuality.NORMAL -> "Normal"
                                    AudioQuality.HIGH -> "High"
                                },
                            )
                        },
                    )
                }
            }
            SettingSwitch(
                title = "Autoplay radio",
                subtitle = "Keep the music going with a station based on the last track",
                checked = Store.radio,
                onChange = { Store.toggleRadio() },
            )
        }

        SettingsGroup {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Subject, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Lyrics providers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Tried in this order; line and word-synced results win",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Store.providers.forEachIndexed { index, provider ->
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        (index + 1).toString() + ". " + provider,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    IconButton(
                        enabled = index > 0,
                        onClick = {
                            val list = Store.providers.toMutableList()
                            val item = list.removeAt(index)
                            list.add(index - 1, item)
                            Store.setProviders(list)
                        },
                    ) {
                        Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move up", modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        enabled = index < Store.providers.lastIndex,
                        onClick = {
                            val list = Store.providers.toMutableList()
                            val item = list.removeAt(index)
                            list.add(index + 1, item)
                            Store.setProviders(list)
                        },
                    ) {
                        Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move down", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        SettingsGroup {
            SettingsRow(
                icon = Icons.Rounded.Storage,
                title = "Storage",
                subtitle = Paths.get(System.getProperty("user.home"), ".vivi-music").toString(),
                trailingIcon = Icons.Rounded.FolderOpen,
                onClick = {
                    runCatching {
                        Desktop.getDesktop().open(
                            Paths.get(System.getProperty("user.home"), ".vivi-music").toFile(),
                        )
                    }
                },
            )
            Text(
                "Liked songs, playlists, history and settings live on this computer only.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        SettingsGroup {
            SettingsRow(
                icon = Icons.Rounded.Info,
                title = "About",
                subtitle = "VIVI Music for Windows • GPL-3.0 • Not affiliated with YouTube or Google",
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
    ) {
        Column(Modifier.padding(20.dp)) { content() }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailingIcon != null) {
            Icon(trailingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        } else if (onClick != null) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
