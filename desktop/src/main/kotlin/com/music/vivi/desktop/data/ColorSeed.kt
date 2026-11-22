package com.music.vivi.desktop.data

import androidx.compose.ui.graphics.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import javax.imageio.ImageIO

/** Extracts a Material You seed color from album artwork. */
object ColorSeed {
    private val client: HttpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

    private val cache = java.util.concurrent.ConcurrentHashMap<String, Color?>()

    fun extract(artworkUrl: String?): Color? {
        if (artworkUrl.isNullOrBlank()) return null
        if (cache.containsKey(artworkUrl)) return cache[artworkUrl]
        val color = runCatching { download(artworkUrl) }.getOrNull()
        cache[artworkUrl] = color
        return color
    }

    private fun download(url: String): Color? {
        val request =
            HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "VIVI-Music-Desktop/1.0")
                .GET()
                .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        if (response.statusCode() !in 200..299) return null
        val image = ImageIO.read(ByteArrayInputStream(response.body())) ?: return null
        return dominantColor(image)
    }

    private fun dominantColor(image: BufferedImage): Color? {
        val buckets = HashMap<Int, DoubleArray>()
        val stepX = (image.width / 48).coerceAtLeast(1)
        val stepY = (image.height / 48).coerceAtLeast(1)

        for (y in 0 until image.height step stepY) {
            for (x in 0 until image.width step stepX) {
                val rgb = image.getRGB(x, y)
                val r = (rgb shr 16) and 0xFF
                val g = (rgb shr 8) and 0xFF
                val b = rgb and 0xFF
                val max = maxOf(r, g, b)
                val min = minOf(r, g, b)
                val saturation = if (max == 0) 0.0 else (max - min).toDouble() / max
                val luminance = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0
                if (luminance < 0.12 || luminance > 0.92 || saturation < 0.16) continue

                val key = ((r shr 4) shl 8) or ((g shr 4) shl 4) or (b shr 4)
                val weight = saturation * (1 - Math.abs(luminance - 0.5))
                val bucket = buckets.getOrPut(key) { DoubleArray(4) }
                bucket[0] += r * weight
                bucket[1] += g * weight
                bucket[2] += b * weight
                bucket[3] += weight
            }
        }

        val best = buckets.values.maxByOrNull { it[3] } ?: return null
        if (best[3] <= 0.0) return null
        return Color(
            red = (best[0] / best[3] / 255.0).toFloat().coerceIn(0f, 1f),
            green = (best[1] / best[3] / 255.0).toFloat().coerceIn(0f, 1f),
            blue = (best[2] / best[3] / 255.0).toFloat().coerceIn(0f, 1f),
        )
    }
}
