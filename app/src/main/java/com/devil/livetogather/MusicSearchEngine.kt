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

        try {
            val encoded = URLEncoder.encode(trimmed, "UTF-8")
            val urlString = "https://itunes.apple.com/search?term=$encoded&media=music&entity=song&limit=30"
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "LiveTogather-Android/1.0")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                parseItunesJson(jsonStr)
            } else {
                Log.w(TAG, "Search returned HTTP ${conn.responseCode}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search tracks for '$trimmed': ${e.message}")
            emptyList()
        }
    }

    private fun parseItunesJson(jsonStr: String): List<HangoutTrack> {
        val list = mutableListOf<HangoutTrack>()
        try {
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
                            id = trackId,
                            title = trackName,
                            artist = if (artistName.isNotBlank()) artistName else "Unknown Artist",
                            category = if (genre.isNotBlank()) genre else "Global",
                            url = previewUrl,
                            artworkUrl = artwork
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing search JSON: ${e.message}")
        }
        return list
    }
}
