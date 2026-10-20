package com.devil.livetogather

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import java.util.Locale

object AudioPlayerManager {
    private const val TAG = "AudioPlayerManager"
    private var mediaPlayer: MediaPlayer? = null
    private var tickerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    val isPlayingState = mutableStateOf(false)
    val isBufferingState = mutableStateOf(false)
    val isMutedState = mutableStateOf(false)
    val currentTrackTitleState = mutableStateOf("")
    val currentTrackUrlState = mutableStateOf("")
    val currentTrackArtworkUrlState = mutableStateOf("")
    val currentPositionMsState = mutableStateOf(0)
    val durationMsState = mutableStateOf(0)
    val formattedTimeState = mutableStateOf("00:00 / 00:00")

    fun play(context: Context, urlOrUri: String, title: String, artworkUrl: String = "", onCompletion: () -> Unit = {}) {
        if (urlOrUri.isBlank()) return

        // If already playing this track, don't restart
        if (currentTrackUrlState.value == urlOrUri && isPlayingState.value && mediaPlayer?.isPlaying == true) {
            return
        }

        try {
            stop()
            isBufferingState.value = true
            currentTrackTitleState.value = title
            currentTrackUrlState.value = urlOrUri
            currentTrackArtworkUrlState.value = artworkUrl

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                if (urlOrUri.startsWith("content://") || urlOrUri.startsWith("file://")) {
                    setDataSource(context, Uri.parse(urlOrUri))
                } else {
                    setDataSource(urlOrUri)
                }

                isLooping = true

                setOnPreparedListener { mp ->
                    isBufferingState.value = false
                    try {
                        val vol = if (isMutedState.value) 0f else 1f
                        mp.setVolume(vol, vol)
                        mp.start()
                        isPlayingState.value = true
                        durationMsState.value = mp.duration
                        startTicker()
                        Log.d(TAG, "Playback started successfully: $title, duration=${mp.duration}ms")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting playback: ${e.message}")
                        isPlayingState.value = false
                    }
                }

                setOnCompletionListener {
                    try {
                        it.seekTo(0)
                        it.start()
                        isPlayingState.value = true
                        startTicker()
                    } catch (e: Exception) {
                        isPlayingState.value = false
                        isBufferingState.value = false
                        stopTicker()
                        onCompletion()
                    }
                }

                setOnErrorListener { _, what, extra ->
                    Log.w(TAG, "MediaPlayer error what=$what extra=$extra for $urlOrUri. Falling back to default track.")
                    isPlayingState.value = false
                    isBufferingState.value = false
                    stopTicker()
                    if (urlOrUri != CURATED_HANGOUT_TRACKS[0].url) {
                        scope.launch {
                            play(context, CURATED_HANGOUT_TRACKS[0].url, CURATED_HANGOUT_TRACKS[0].title, CURATED_HANGOUT_TRACKS[0].artworkUrl)
                        }
                    }
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MediaPlayer for $urlOrUri: ${e.message}")
            isPlayingState.value = false
            isBufferingState.value = false
            stopTicker()
            if (urlOrUri != CURATED_HANGOUT_TRACKS[0].url) {
                scope.launch {
                    play(context, CURATED_HANGOUT_TRACKS[0].url, CURATED_HANGOUT_TRACKS[0].title, CURATED_HANGOUT_TRACKS[0].artworkUrl)
                }
            }
        }
    }

    private fun startTicker() {
        stopTicker()
        tickerJob = scope.launch {
            while (isActive && isPlayingState.value) {
                try {
                    val pos = mediaPlayer?.currentPosition ?: 0
                    val dur = mediaPlayer?.duration ?: 0
                    currentPositionMsState.value = pos
                    if (dur > 0) durationMsState.value = dur
                    formattedTimeState.value = "${formatMs(pos)} / ${formatMs(dur)}"
                } catch (e: Exception) {
                    // Ignored on player state transitions
                }
                delay(500L)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    fun formatMs(ms: Int): String {
        if (ms <= 0) return "00:00"
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format(Locale.getDefault(), "%02d:%02d", min, sec)
    }

    fun seekTo(ms: Int) {
        try {
            mediaPlayer?.seekTo(ms)
            currentPositionMsState.value = ms
        } catch (e: Exception) {
            Log.w(TAG, "Seek failed: ${e.message}")
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
            isPlayingState.value = false
            isBufferingState.value = false
            stopTicker()
        } catch (e: Exception) {
            Log.e(TAG, "Pause failed: ${e.message}")
        }
    }

    fun resume(context: Context? = null) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer?.start()
                isPlayingState.value = true
                isBufferingState.value = false
                startTicker()
            } else if (context != null && currentTrackUrlState.value.isNotBlank()) {
                play(context, currentTrackUrlState.value, currentTrackTitleState.value, currentTrackArtworkUrlState.value)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Resume failed: ${e.message}")
        }
    }

    fun togglePlayPause(context: Context, urlOrUri: String, title: String, artworkUrl: String = ""): Boolean {
        return if (isPlayingState.value) {
            pause()
            false
        } else {
            if (mediaPlayer != null && currentTrackUrlState.value == urlOrUri) {
                resume(context)
            } else {
                play(context, urlOrUri, title, artworkUrl)
            }
            true
        }
    }

    fun stop() {
        try {
            stopTicker()
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Stop error: ${e.message}")
        } finally {
            mediaPlayer = null
            isPlayingState.value = false
            isBufferingState.value = false
            currentTrackTitleState.value = ""
            currentTrackUrlState.value = ""
            currentTrackArtworkUrlState.value = ""
            currentPositionMsState.value = 0
            durationMsState.value = 0
            formattedTimeState.value = "00:00 / 00:00"
        }
    }

    val volumeLevelState = mutableStateOf(1.0f)

    fun setVolumeLevel(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        volumeLevelState.value = clamped
        isMutedState.value = clamped == 0f
        try {
            mediaPlayer?.setVolume(clamped, clamped)
        } catch (e: Exception) {
            Log.w(TAG, "Set volume error: ${e.message}")
        }
    }

    fun toggleMute(): Boolean {
        val newMute = !isMutedState.value
        isMutedState.value = newMute
        val vol = if (newMute) 0f else volumeLevelState.value.coerceAtLeast(0.5f)
        try {
            mediaPlayer?.setVolume(vol, vol)
        } catch (e: Exception) {
            Log.w(TAG, "Mute toggle error: ${e.message}")
        }
        return newMute
    }
}
