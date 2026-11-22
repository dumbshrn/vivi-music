package com.music.vivi.desktop.ui.screens

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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.music.vivi.desktop.data.Store
import java.awt.Desktop
import java.net.URI

/**
 * Google sign-in. The desktop client cannot embed Google's OAuth WebView flow, so
 * it mirrors what every desktop YouTube client does: sign in inside a browser and
 * paste the resulting cookie header once. It is stored locally and sent to YouTube.
 */
@Composable
fun AccountScreen() {
    var cookieText by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)) {
                        Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                if (Store.signedIn) Icons.Rounded.CheckCircle else Icons.Rounded.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.width(18.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (Store.signedIn) "Signed in to YouTube Music" else "Not signed in",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        if (Store.signedIn) {
                            "Liked songs, subscriptions and your playlists sync with YouTube."
                        } else {
                            "Sign in to sync your library, liked songs and playlists."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (Store.signedIn) {
                    OutlinedButton(onClick = { Store.signOut() }, shape = CircleShape) { Text("Sign out") }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("How to sign in", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Step("1", "Open YouTube Music in your browser and sign in with your Google account.")
                Step("2", "Open the browser's developer tools (F12) → Network, then reload the page.")
                Step("3", "Select any request to music.youtube.com, copy the whole value of its Cookie request header.")
                Step("4", "Paste it below. It stays on this computer, in your local library file.")

                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        shape = CircleShape,
                        onClick = {
                            runCatching {
                                Desktop.getDesktop().browse(URI("https://music.youtube.com"))
                            }
                        },
                    ) {
                        Icon(Icons.Rounded.OpenInNew, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Open YouTube Music")
                    }
                }

                OutlinedTextField(
                    value = cookieText,
                    onValueChange = { cookieText = it },
                    label = { Text("Cookie header") },
                    placeholder = { Text("SID=…; HSID=…; SSID=…; SAPISID=…") },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        shape = CircleShape,
                        enabled = cookieText.isNotBlank(),
                        onClick = {
                            Store.signIn(cookieText)
                            if (Store.signedIn) {
                                Store.notify("Signed in to YouTube Music")
                                cookieText = ""
                                Store.back()
                            } else {
                                Store.notify("That cookie does not contain a SAPISID value")
                            }
                        },
                    ) {
                        Text("Sign in")
                    }
                    OutlinedButton(
                        shape = CircleShape,
                        onClick = { cookieText = "" },
                    ) {
                        Text("Clear")
                    }
                }
            }
        }

        Text(
            "Signing in also improves playback: YouTube serves account-aware audio streams to signed-in clients.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun Step(number: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                Text(
                    number,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}
