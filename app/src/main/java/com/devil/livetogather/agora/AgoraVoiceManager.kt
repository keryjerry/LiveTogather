package com.devil.livetogather.agora

import android.content.Context
import android.util.Log
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig

object AgoraVoiceManager {
    private const val TAG = "AgoraVoiceManager"

    const val AGORA_APP_ID = "740cdcaef70f4dd7b121f44a47d6d461"
    const val AGORA_PRIMARY_CERTIFICATE = "0a70ee3143aa47c7aeb3c1148574e079"

    private const val TOKEN_EXPIRATION_SECONDS = 86400

    private var rtcEngine: RtcEngine? = null
    private var currentChannel: String? = null
    private var isMuted: Boolean = false

    interface VoiceEventListener {
        fun onUserJoined(uid: Int) {}
        fun onUserOffline(uid: Int, reason: Int) {}
        fun onAudioVolumeIndication(speakers: Array<out IRtcEngineEventHandler.AudioVolumeInfo>?, totalVolume: Int) {}
        fun onError(err: Int) {}
    }

    private var eventListener: VoiceEventListener? = null

    fun setEventListener(listener: VoiceEventListener?) {
        this.eventListener = listener
    }

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.d(TAG, "Successfully joined Agora voice channel: " + channel + ", uid: " + uid + ", elapsed: " + elapsed)
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d(TAG, "Remote user joined Agora channel: " + uid)
            eventListener?.onUserJoined(uid)
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d(TAG, "Remote user went offline: " + uid + ", reason: " + reason)
            eventListener?.onUserOffline(uid, reason)
        }

        override fun onAudioVolumeIndication(
            speakers: Array<out AudioVolumeInfo>?,
            totalVolume: Int
        ) {
            eventListener?.onAudioVolumeIndication(speakers, totalVolume)
        }

        override fun onError(err: Int) {
            Log.e(TAG, "Agora RTC Error: " + err)
            eventListener?.onError(err)
        }
    }

    @Synchronized
    fun init(context: Context) {
        if (rtcEngine != null) return
        try {
            val config = RtcEngineConfig().apply {
                mContext = context.applicationContext
                mAppId = AGORA_APP_ID
                mEventHandler = rtcEventHandler
                mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            }
            rtcEngine = RtcEngine.create(config)

            rtcEngine?.apply {
                setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
                setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
                enableAudio()
                enableAudioVolumeIndication(200, 3, true)
                setEnableSpeakerphone(true)
            }
            Log.d(TAG, "Agora RtcEngine initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Agora RtcEngine: " + e.message, e)
        }
    }

    fun joinRoom(
        context: Context,
        roomId: String,
        uid: Int,
        startMuted: Boolean = false
    ) {
        init(context)
        val engine = rtcEngine ?: return

        if (currentChannel != null && currentChannel != roomId) {
            leaveRoom()
        }

        try {
            val tokenBuilder = RtcTokenBuilder2()
            val token = tokenBuilder.buildTokenWithUid(
                AGORA_APP_ID,
                AGORA_PRIMARY_CERTIFICATE,
                roomId,
                uid,
                RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                TOKEN_EXPIRATION_SECONDS,
                TOKEN_EXPIRATION_SECONDS
            )

            engine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
            engine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
            engine.enableAudio()
            engine.setEnableSpeakerphone(true)

            val result = engine.joinChannel(token, roomId, "", uid)
            Log.d(TAG, "joinChannel result for room '" + roomId + "' with uid " + uid + ": " + result)
            currentChannel = roomId

            muteLocalAudio(startMuted)
        } catch (e: Exception) {
            Log.e(TAG, "Error joining Agora room '" + roomId + "': " + e.message, e)
        }
    }

    fun muteLocalAudio(mute: Boolean) {
        isMuted = mute
        rtcEngine?.muteLocalAudioStream(mute)
        Log.d(TAG, "Local microphone muted: " + mute)
    }

    fun isLocalAudioMuted(): Boolean = isMuted

    fun leaveRoom() {
        try {
            rtcEngine?.leaveChannel()
            Log.d(TAG, "Left Agora voice channel: " + currentChannel)
        } catch (e: Exception) {
            Log.e(TAG, "Error leaving channel: " + e.message, e)
        } finally {
            currentChannel = null
        }
    }

    fun destroy() {
        try {
            leaveRoom()
            RtcEngine.destroy()
            rtcEngine = null
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying Agora engine: " + e.message, e)
        }
    }
}
