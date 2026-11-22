package com.music.vivi.desktop.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.Route
import com.music.vivi.desktop.data.Store
import com.music.vivi.desktop.data.ThemePreference
import com.music.vivi.desktop.ui.screens.AccountScreen
import com.music.vivi.desktop.ui.screens.AlbumScreen
import com.music.vivi.desktop.ui.screens.ArtistScreen
import com.music.vivi.desktop.ui.screens.BrowseScreen
import com.music.vivi.desktop.ui.screens.ChartsScreen
import com.music.vivi.desktop.ui.screens.ExploreScreen
import com.music.vivi.desktop.ui.screens.HistoryScreen
import com.music.vivi.desktop.ui.screens.HomeScreen
import com.music.vivi.desktop.ui.screens.LibraryScreen
import com.music.vivi.desktop.ui.screens.LocalPlaylistScreen
import com.music.vivi.desktop.ui.screens.MoodGenresScreen
import com.music.vivi.desktop.ui.screens.NewReleasesScreen
import com.music.vivi.desktop.ui.screens.PlaylistScreen
import com.music.vivi.desktop.ui.screens.SearchScreen
import com.music.vivi.desktop.ui.screens.SettingsScreen
import com.music.vivi.desktop.ui.screens.StatsScreen
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

    ViviTheme(darkTheme = dark, seed = seed, pureBlack = Store.pureBlack) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onSurface,
        ) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxSize().padding(bottom = 104.dp)) {
                    AppRail()
                    Column(Modifier.fillMaxSize()) {
                        TopBar()
                        Box(Modifier.fillMaxSize()) { ScreenHost() }
                    }
                }

                Box(Modifier.align(Alignment.BottomCenter)) { PlayerBar() }

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
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp),
                        shape = CircleShape,
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
}

/** The app's navigation rail: circular icon wells with labels underneath. */
@Composable
private fun AppRail() {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest, modifier = Modifier.width(92.dp).fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.GraphicEq,
                    contentDescription = "VIVI Music",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.height(22.dp))

            RailItem("Home", Icons.Rounded.Home, Route.Home)
            RailItem("Explore", Icons.Rounded.Explore, Route.Explore)
            RailItem("Search", Icons.Rounded.Search, Route.Search)
            RailItem("Library", Icons.Rounded.LibraryMusic, Route.Library)

            Spacer(Modifier.weight(1f))
            RailItem("Settings", Icons.Rounded.Settings, Route.Settings)
        }
    }
}

@Composable
private fun RailItem(label: String, icon: ImageVector, route: Route) {
    val selected = Store.route::class == route::class
    val container by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainer,
        label = "railContainer",
    )
    val content by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "railContent",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .width(76.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { if (Store.route != route) Store.navigate(route) }
            .padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(CircleShape).background(container),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TopBar() {
    val canGoBack = Store.routeStack.isNotEmpty()
    val title =
        when (val route = Store.route) {
            Route.Home -> "Music"
            Route.Explore -> "Explore"
            Route.Search -> "Search"
            Route.Library -> "Library"
            Route.Settings -> "Settings"
            Route.History -> "History"
            Route.Stats -> "Listening stats"
            Route.Charts -> "Charts"
            Route.MoodGenres -> "Mood and Genres"
            Route.NewReleases -> "New releases"
            Route.Account -> "Account"
            is Route.Browse -> route.title
            is Route.Album -> "Album"
            is Route.Artist -> "Artist"
            is Route.Playlist -> "Playlist"
            is Route.LocalPlaylist -> Store.localPlaylist(route.id)?.name ?: "Playlist"
        }

    Row(
        modifier = Modifier.fillMaxWidth().height(84.dp).padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (canGoBack) {
            IconButton(onClick = { Store.back() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(4.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(21.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.weight(1f))

        TopBarAction(Icons.Rounded.History, "History") { Store.navigate(Route.History) }
        Spacer(Modifier.width(8.dp))
        TopBarAction(Icons.Rounded.Equalizer, "Listening stats") { Store.navigate(Route.Stats) }
        Spacer(Modifier.width(8.dp))
        TopBarAction(Icons.Rounded.Search, "Search") { Store.navigate(Route.Search) }
        Spacer(Modifier.width(8.dp))
        TopBarAction(
            Icons.Rounded.Person,
            if (Store.signedIn) "Account (signed in)" else "Sign in",
            tinted = Store.signedIn,
        ) { Store.navigate(Route.Account) }
    }
}

@Composable
private fun TopBarAction(icon: ImageVector, description: String, tinted: Boolean = false, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color =
            if (tinted) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
            Icon(
                icon,
                contentDescription = description,
                tint =
                    if (tinted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                modifier = Modifier.size(21.dp),
            )
        }
    }
}

@Composable
private fun ScreenHost() {
    val route = Store.route
    Box(Modifier.fillMaxSize()) {
        when (route) {
            Route.Home -> HomeScreen()
            Route.Explore -> ExploreScreen()
            Route.Search -> SearchScreen(Store.searchQuery)
            Route.Library -> LibraryScreen()
            Route.Settings -> SettingsScreen()
            Route.History -> HistoryScreen()
            Route.Stats -> StatsScreen()
            Route.Charts -> ChartsScreen()
            Route.MoodGenres -> MoodGenresScreen()
            Route.NewReleases -> NewReleasesScreen()
            Route.Account -> AccountScreen()
            is Route.Browse -> BrowseScreen(route.browseId, route.params, route.title)
            is Route.Album -> AlbumScreen(route.id)
            is Route.Artist -> ArtistScreen(route.id)
            is Route.Playlist -> PlaylistScreen(route.id)
            is Route.LocalPlaylist -> LocalPlaylistScreen(route.id)
        }
    }
}
