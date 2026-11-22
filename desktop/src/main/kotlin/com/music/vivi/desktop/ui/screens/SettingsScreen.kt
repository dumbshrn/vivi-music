package com.music.vivi.desktop.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.FolderOpen
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
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.AudioQuality
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.data.ThemePreference
import java.awt.Desktop
import java.nio.file.Paths

private val seedPresets =
    listOf(
        "Vivi red" to Color(0xFFED5564),
        "Purple" to Color(0xFF7C4DFF),
        "Ocean" to Color(0xFF1E88E5),
        "Leaf" to Color(0xFF2E7D32),
        "Sunset" to Color(0xFFFF7043),
        "Monochrome" to Color(0xFF000000),
    )

@Composable
fun SettingsScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SettingsCard("Appearance") {
            Text("Theme", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
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

            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Material You from album art", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Colors shift to match what is playing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = Store.dynamicColors, onCheckedChange = { Store.applyDynamicColors(it) })
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Pure black", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "OLED-friendly true black surfaces in dark mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = Store.pureBlack, onCheckedChange = { Store.togglePureBlack() })
            }

            Text("Seed color", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 16.dp))
            Row(
                Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                seedPresets.forEach { (name, color) ->
                    Surface(
                        shape = CircleShape,
                        color = color,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.Transparent),
                        onClick = { Store.setSeed(color) },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (Store.seed == color) {
                                Text("✓", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    if (Store.dynamicColors) "Overridden while a track plays" else "Used for the whole app",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SettingsCard("Playback") {
            Text("Streaming quality", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
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
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Autoplay radio", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Keep the music going with a station based on the last track",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = Store.radio, onCheckedChange = { Store.toggleRadio() })
            }
        }

        SettingsCard("Lyrics providers") {
            Text(
                "Synced lyrics are fetched in this order; line and word-synced results win.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            Store.providers.forEachIndexed { index, provider ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${index + 1}. $provider", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
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

        SettingsCard("Data") {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Library folder", style = MaterialTheme.typography.titleSmall)
                    Text(
                        Paths.get(System.getProperty("user.home"), ".vivi-music").toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = {
                    runCatching {
                        Desktop.getDesktop().open(Paths.get(System.getProperty("user.home"), ".vivi-music").toFile())
                    }
                }) {
                    Icon(Icons.Rounded.FolderOpen, contentDescription = "Open folder")
                }
            }
            Text(
                "Everything (liked songs, playlists, history, settings) is stored locally. No account, no telemetry.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        SettingsCard("About") {
            Text("VIVI Music for Windows", style = MaterialTheme.typography.titleMedium)
            Text(
                "Desktop port of the VIVI Android client — Material You, synced lyrics, ad-free YouTube Music streaming.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "Version 1.0.0 • GPL-3.0 • Not affiliated with YouTube or Google",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}
