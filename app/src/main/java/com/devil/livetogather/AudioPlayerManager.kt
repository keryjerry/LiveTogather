package com.devil.livetogather

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf

object AudioPlayerManager {
    private const val TAG = "AudioPlayerManager"
    private var mediaPlayer: MediaPlayer? = null

    val isPlayingState = mutableStateOf(false)
    val isBufferingState = mutableStateOf(false)
    val isMutedState = mutableStateOf(false)
    val currentTrackTitleState = mutableStateOf("")
    val currentTrackUrlState = mutableStateOf("")

    fun play(context: Context, urlOrUri: String, title: String, onCompletion: () -> Unit = {}) {
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

                setOnPreparedListener { mp ->
                    isBufferingState.value = false
                    try {
                        val vol = if (isMutedState.value) 0f else 1f
                        mp.setVolume(vol, vol)
                        mp.start()
                        isPlayingState.value = true
                        Log.d(TAG, "Playback started successfully: $title")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting playback: ${e.message}")
                        isPlayingState.value = false
                    }
                }

                setOnCompletionListener {
                    isPlayingState.value = false
                    isBufferingState.value = false
                    onCompletion()
                }

                setOnErrorListener { _, what, extra ->
                    Log.w(TAG, "MediaPlayer error what=$what extra=$extra for $urlOrUri")
                    isPlayingState.value = false
                    isBufferingState.value = false
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MediaPlayer for $urlOrUri: ${e.message}")
            isPlayingState.value = false
            isBufferingState.value = false
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
            isPlayingState.value = false
            isBufferingState.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Pause failed: ${e.message}")
        }
    }

    fun resume() {
        try {
            mediaPlayer?.start()
            isPlayingState.value = true
            isBufferingState.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Resume failed: ${e.message}")
        }
    }

    fun stop() {
        try {
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
        }
    }

    fun toggleMute(): Boolean {
        val newMute = !isMutedState.value
        isMutedState.value = newMute
        val vol = if (newMute) 0f else 1f
        try {
            mediaPlayer?.setVolume(vol, vol)
        } catch (e: Exception) {
            Log.w(TAG, "Mute toggle error: ${e.message}")
        }
        return newMute
    }
}
