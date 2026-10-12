package com.devil.livetogather

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object MusicSearchEngine {
    private const val TAG = "MusicSearchEngine"

    suspend fun searchTracks(query: String): List<HangoutTrack> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext emptyList()

        val encoded = URLEncoder.encode(trimmed, "UTF-8")

        // 1. Primary: Search Audius for 100% FULL-LENGTH streaming tracks (3 to 6+ minutes)
        val fullLengthTracks = searchAudiusTracks(encoded)
        if (fullLengthTracks.isNotEmpty()) {
            return@withContext fullLengthTracks
        }

        // 2. Fallback: Query iTunes search
        return@withContext searchItunesTracks(encoded)
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
