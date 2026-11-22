package com.music.vivi.desktop.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.Route
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.data.ThemePreference
import com.music.vivi.desktop.ui.screens.AlbumScreen
import com.music.vivi.desktop.ui.screens.ArtistScreen
import com.music.vivi.desktop.ui.screens.ExploreScreen
import com.music.vivi.desktop.ui.screens.HomeScreen
import com.music.vivi.desktop.ui.screens.LibraryScreen
import com.music.vivi.desktop.ui.screens.LocalPlaylistScreen
import com.music.vivi.desktop.ui.screens.PlaylistScreen
import com.music.vivi.desktop.ui.screens.SearchScreen
import com.music.vivi.desktop.ui.screens.SettingsScreen
import com.music.vivi.desktop.ui.theme.ViviTheme

@Composable
fun App() {
    val dark =
        when (Store.theme) {
            ThemePreference.SYSTEM -> isSystemInDarkTheme()
            ThemePreference.LIGHT -> false
            ThemePreference.DARK -> true
        }
    val seed = if (Store.dynamicColors) (Store.artSeed ?: Store.seed) else Store.seed

    ViviTheme(darkTheme = dark, seed = seed) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
            Row(Modifier.fillMaxSize().padding(bottom = 94.dp)) {
                Sidebar()
                Column(Modifier.fillMaxSize()) {
                    TopBar()
                    Box(Modifier.fillMaxSize()) {
                        ScreenHost()
                    }
                }
            }

            Box(Modifier.align(Alignment.BottomCenter)) {
                PlayerBar()
            }

            AnimatedVisibility(
                visible = Store.showNowPlaying,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                NowPlayingScreen()
            }

            AnimatedVisibility(
                visible = Store.showQueue,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.BottomEnd),
            ) {
                QueuePanel()
            }

            Store.toast?.let { message ->
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 112.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Sidebar() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.width(258.dp).fillMaxSize(),
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("VIVI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "Music",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            NavItem("Home", Icons.Rounded.Home, Route.Home)
            NavItem("Explore", Icons.Rounded.Explore, Route.Explore)
            NavItem("Search", Icons.Rounded.Search, Route.Search)
            NavItem("Library", Icons.Rounded.LibraryMusic, Route.Library)

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(Modifier.padding(horizontal = 8.dp))
            Row(
                Modifier.fillMaxWidth().padding(start = 18.dp, end = 6.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "PLAYLISTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                IconButton(onClick = {
                    val playlist = Store.createPlaylist("My playlist #${Store.playlists.size + 1}")
                    Store.navigate(Route.LocalPlaylist(playlist.id))
                }) {
                    Icon(Icons.Rounded.Add, contentDescription = "New playlist", modifier = Modifier.size(20.dp))
                }
            }

            LazyColumn(Modifier.weight(1f)) {
                items(Store.playlists, key = { it.id }) { playlist ->
                    NavigationDrawerItem(
                        label = {
                            Text(playlist.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        selected = Store.route == Route.LocalPlaylist(playlist.id),
                        onClick = { Store.navigate(Route.LocalPlaylist(playlist.id)) },
                        modifier = Modifier.padding(vertical = 1.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            unselectedContainerColor = Color.Transparent,
                        ),
                    )
                }
            }

            NavItem("Settings", Icons.Rounded.Settings, Route.Settings)
        }
    }
}

@Composable
private fun NavItem(label: String, icon: ImageVector, route: Route) {
    NavigationDrawerItem(
        label = { Text(label) },
        icon = { Icon(icon, contentDescription = null) },
        selected = Store.route::class == route::class,
        onClick = {
            if (Store.route != route) Store.navigate(route)
        },
        modifier = Modifier.padding(vertical = 2.dp),
        colors =
            NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unselectedContainerColor = Color.Transparent,
            ),
    )
}

@Composable
private fun TopBar() {
    val canGoBack = Store.routeStack.isNotEmpty()
    var searchQuery by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (canGoBack) {
            IconButton(onClick = { Store.back() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
        }
        Text(
            text = when (val route = Store.route) {
                Route.Home -> "Home"
                Route.Explore -> "Explore"
                Route.Search -> "Search"
                Route.Library -> "Library"
                Route.Settings -> "Settings"
                is Route.Album -> "Album"
                is Route.Artist -> "Artist"
                is Route.Playlist -> "Playlist"
                is Route.LocalPlaylist -> Store.localPlaylist(route.id)?.name ?: "Playlist"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.weight(1f))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search songs, albums, artists…") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            modifier = Modifier.width(360.dp),
        )
        Spacer(Modifier.width(8.dp))
        androidx.compose.material3.Button(
            onClick = {
                Store.searchQuery = searchQuery
                Store.navigate(Route.Search)
            },
            shape = CircleShape,
        ) {
            Text("Search")
        }
    }
}

@Composable
private fun ScreenHost() {
    val route = Store.route
    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 0.dp))) {
        when (route) {
            Route.Home -> HomeScreen()
            Route.Explore -> ExploreScreen()
            Route.Search -> SearchScreen(Store.searchQuery)
            Route.Library -> LibraryScreen()
            Route.Settings -> SettingsScreen()
            is Route.Album -> AlbumScreen(route.id)
            is Route.Artist -> ArtistScreen(route.id)
            is Route.Playlist -> PlaylistScreen(route.id)
            is Route.LocalPlaylist -> LocalPlaylistScreen(route.id)
        }
    }
}
