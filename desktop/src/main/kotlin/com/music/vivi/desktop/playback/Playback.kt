package com.music.vivi.desktop.playback

import com.music.vivi.desktop.data.Store
import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration

/**
 * Desktop audio backend. JavaFX Media is bundled with the app (no external
 * runtime required) and decodes the AAC/MP4 audio stream used by VIVI.
 */
object Playback {
    private var player: MediaPlayer? = null

    fun load(url: String, volume: Float) {
        Platform.runLater {
            runCatching {
                player?.stop()
                player?.dispose()
                val media = Media(url)
                val created =
                    MediaPlayer(media).apply {
                        this.volume = volume.toDouble()
                        setOnReady {
                            Store.buffering = false
                            val total = totalDurationProperty().get()
                            if (total != null && !total.isUnknown && total.toMillis() > 0) {
                                Store.durationMs = total.toMillis().toLong()
                            }
                        }
                        setOnPlaying {
                            Store.playing = true
                            Store.buffering = false
                        }
                        setOnPaused { Store.playing = false }
                        setOnStopped { Store.playing = false }
                        setOnStalled { Store.buffering = true }
                        setOnEndOfMedia { Store.next(auto = true) }
                        setOnError {
                            Store.buffering = false
                            Store.playing = false
                            Store.playbackError = this.error?.message ?: "Playback error"
                            Store.notify("Playback error: ${this.error?.message ?: "unknown"}")
                        }
                        currentTimeProperty().addListener { _, _, time ->
                            if (time != null && !time.isUnknown) Store.positionMs = time.toMillis().toLong()
                        }
                        totalDurationProperty().addListener { _, _, duration ->
                            if (duration != null && !duration.isUnknown && duration.toMillis() > 0) {
                                Store.durationMs = duration.toMillis().toLong()
                            }
                        }
                        volumeProperty().addListener { _, _, value ->
                            val level = value?.toDouble() ?: return@addListener
                            if (kotlin.math.abs(Store.volume - level.toFloat()) > 0.01f) {
                                Store.volume = level.toFloat()
                            }
                        }
                    }
                player = created
            }.onFailure { error ->
                Store.buffering = false
                Store.playbackError = error.message
                Store.notify("Playback error: ${error.message}")
            }
        }
    }

    fun play() {
        Platform.runLater { player?.play() }
    }

    fun pause() {
        Platform.runLater { player?.pause() }
    }

    fun stop() {
        Platform.runLater {
            player?.stop()
            player?.dispose()
            player = null
        }
    }

    fun seek(milliseconds: Long) {
        Platform.runLater {
            player?.let { current ->
                current.seek(Duration.millis(milliseconds.toDouble().coerceAtLeast(0.0)))
            }
        }
    }

    fun setVolume(volume: Float) {
        Platform.runLater { player?.volume = volume.toDouble().coerceIn(0.0, 1.0) }
    }
}
