package com.devil.livetogather

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object MusicSearchEngine {
    private const val TAG = "MusicSearchEngine"
    private const val SAAVN_DES_KEY = "38346591"

    suspend fun searchTracks(query: String): List<HangoutTrack> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext emptyList()

        val encoded = URLEncoder.encode(trimmed, "UTF-8")

        // 1. Primary: Search JioSaavn for 100% FULL-LENGTH streaming tracks (3 to 6+ minutes)
        val saavnTracks = searchJioSaavnTracks(encoded)
        if (saavnTracks.isNotEmpty()) {
            return@withContext saavnTracks
        }

        // 2. Secondary: Search Audius for full-length tracks
        val fullLengthTracks = searchAudiusTracks(encoded)
        if (fullLengthTracks.isNotEmpty()) {
            return@withContext fullLengthTracks
        }

        // 3. Fallback: Query iTunes search
        return@withContext searchItunesTracks(encoded)
    }

    private fun searchJioSaavnTracks(encodedQuery: String): List<HangoutTrack> {
        val list = mutableListOf<HangoutTrack>()
        try {
            val urlString = "https://www.jiosaavn.com/api.php?__call=search.getResults&_format=json&n=25&p=1&_marker=0&ctx=android&q=$encodedQuery"
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonStr)
                val results = root.optJSONArray("results") ?: return emptyList()

                for (i in 0 until results.length()) {
                    val item = results.optJSONObject(i) ?: continue
                    val songId = item.optString("id", i.toString()).trim()
                    val songTitle = cleanHtml(item.optString("song", item.optString("title", "")).trim())
                    val singer = cleanHtml(item.optString("primary_artists", item.optString("singers", "Artist")).trim())
                    val durationSec = item.optString("duration", "0").toIntOrNull() ?: 0
                    val rawImg = item.optString("image", "").trim()
                    val highResImg = if (rawImg.contains("150x150")) rawImg.replace("150x150", "500x500") else rawImg

                    val encryptedUrl = item.optString("encrypted_media_url", "").trim()
                    val streamUrl = if (encryptedUrl.isNotBlank()) {
                        decryptMediaUrl(encryptedUrl)
                    } else null

                    val finalUrl = streamUrl
                        ?: item.optString("media_preview_url", "").trim().takeIf { it.isNotBlank() }
                        ?: item.optString("vlink", "").trim().takeIf { it.isNotBlank() }

                    if (songTitle.isNotBlank() && !finalUrl.isNullOrBlank()) {
                        list.add(
                            HangoutTrack(
                                id = "saavn_$songId",
                                title = songTitle,
                                artist = singer.ifEmpty { "Artist" },
                                category = item.optString("language", "Full Track").replaceFirstChar { it.uppercase() },
                                url = finalUrl,
                                artworkUrl = highResImg,
                                durationSec = if (durationSec > 0) durationSec else 210
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "JioSaavn search failed: ${e.message}")
        }
        return list
    }

    private fun decryptMediaUrl(encryptedUrl: String): String? {
        return try {
            val keyBytes = SAAVN_DES_KEY.toByteArray(Charsets.UTF_8)
            val keySpec = SecretKeySpec(keyBytes, "DES")
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.decode(encryptedUrl, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            val decryptedUrl = String(decryptedBytes, Charsets.UTF_8).trim()
            if (decryptedUrl.contains("_96.mp4")) {
                decryptedUrl.replace("_96.mp4", "_160.mp4")
            } else {
                decryptedUrl
            }
        } catch (e: Exception) {
            Log.e(TAG, "DES decryption failed: ${e.message}")
            null
        }
    }

    private fun cleanHtml(text: String): String {
        return text.replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }

    private fun searchAudiusTracks(encodedQuery: String): List<HangoutTrack> {
        val list = mutableListOf<HangoutTrack>()
        try {
            val urlString = "https://discoveryprovider.audius.co/v1/tracks/search?query=$encodedQuery&app_name=LiveTogather"
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "LiveTogather-Android/1.0")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonStr)
                val data = root.optJSONArray("data") ?: return emptyList()

                for (i in 0 until data.length()) {
                    val item = data.optJSONObject(i) ?: continue
                    val trackId = item.optString("id", "").trim()
                    val title = item.optString("title", "").trim()
                    val userObj = item.optJSONObject("user")
                    val artist = userObj?.optString("name", "Unknown Artist")?.trim() ?: "Unknown Artist"
                    val durationSec = item.optInt("duration", 0)
                    val genre = item.optString("genre", "Full Music").trim()
                    val artworkObj = item.optJSONObject("artwork")
                    val artwork = artworkObj?.optString("480x480", "")
                        ?: artworkObj?.optString("150x150", "")
                        ?: ""

                    if (trackId.isNotBlank() && title.isNotBlank()) {
                        val streamUrl = "https://discoveryprovider.audius.co/v1/tracks/$trackId/stream?app_name=LiveTogather"
                        list.add(
                            HangoutTrack(
                                id = "audius_$trackId",
                                title = title,
                                artist = artist,
                                category = if (genre.isNotBlank()) genre else "Full Length",
                                url = streamUrl,
                                artworkUrl = artwork,
                                durationSec = durationSec
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Audius search failed: ${e.message}")
        }
        return list
    }

    private fun searchItunesTracks(encodedQuery: String): List<HangoutTrack> {
        val list = mutableListOf<HangoutTrack>()
        try {
            val urlString = "https://itunes.apple.com/search?term=$encodedQuery&media=music&entity=song&limit=30"
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "LiveTogather-Android/1.0")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonStr)
                val results = root.optJSONArray("results") ?: return emptyList()

                for (i in 0 until results.length()) {
                    val item = results.optJSONObject(i) ?: continue
                    val trackName = item.optString("trackName", "").trim()
                    val artistName = item.optString("artistName", "").trim()
                    val previewUrl = item.optString("previewUrl", "").trim()
                    val genre = item.optString("primaryGenreName", "Music").trim()
                    val artwork = item.optString("artworkUrl100", "").trim()
                    val trackId = item.optLong("trackId", i.toLong()).toString()

                    if (previewUrl.isNotBlank() && trackName.isNotBlank()) {
                        list.add(
                            HangoutTrack(
                                id = "itunes_$trackId",
                                title = trackName,
                                artist = if (artistName.isNotBlank()) artistName else "Unknown Artist",
                                category = if (genre.isNotBlank()) genre else "Global",
                                url = previewUrl,
                                artworkUrl = artwork,
                                durationSec = 30
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "iTunes fallback search failed: ${e.message}")
        }
        return list
    }
}
