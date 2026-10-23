package com.devil.livetogather.agora

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration

object AgoraVideoManager {
    private const val TAG = "AgoraVideoManager"

    const val AGORA_APP_ID = "740cdcaef70f4dd7b121f44a47d6d461"
    const val AGORA_PRIMARY_CERTIFICATE = "0a70ee3143aa47c7aeb3c1148574e079"

    private var rtcEngine: RtcEngine? = null
    private var currentChannel: String? = null
    var isMuted: Boolean = false
        private set
    var isCameraOff: Boolean = false
        private set

    var onLocalTalkingStateChanged: ((Boolean) -> Unit)? = null

    interface VideoEventListener {
        fun onUserJoined(uid: Int) {}
        fun onUserOffline(uid: Int, reason: Int) {}
        fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {}
        fun onAudioVolumeIndication(speakers: Array<out IRtcEngineEventHandler.AudioVolumeInfo>?, totalVolume: Int) {}
        fun onError(err: Int) {}
    }

    private var eventListener: VideoEventListener? = null

    fun setEventListener(listener: VideoEventListener?) {
        this.eventListener = listener
    }

    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var currentUid: Int = 0

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.d(TAG, "Joined Agora video channel: $channel, localUid: $uid, elapsed: $elapsed")
            rtcEngine?.setEnableSpeakerphone(true)
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d(TAG, "Remote user joined Agora video channel: $uid")
            mainHandler.post {
                eventListener?.onUserJoined(uid)
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d(TAG, "Remote user went offline: $uid, reason: $reason")
            mainHandler.post {
                eventListener?.onUserOffline(uid, reason)
            }
        }

        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
            Log.d(TAG, "First remote video decoded for uid: $uid, size: ${width}x${height}")
            mainHandler.post {
                eventListener?.onFirstRemoteVideoDecoded(uid, width, height, elapsed)
                eventListener?.onUserJoined(uid)
            }
        }

        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            Log.d(TAG, "onRemoteVideoStateChanged: uid=$uid, state=$state, reason=$reason")
            if (state == Constants.REMOTE_VIDEO_STATE_STARTING || state == Constants.REMOTE_VIDEO_STATE_DECODING) {
                mainHandler.post {
                    eventListener?.onUserJoined(uid)
                }
            }
        }

        override fun onAudioVolumeIndication(
            speakers: Array<out AudioVolumeInfo>?,
            totalVolume: Int
        ) {
            mainHandler.post {
                eventListener?.onAudioVolumeIndication(speakers, totalVolume)
                if (speakers != null) {
                    for (speaker in speakers) {
                        if (speaker.uid == 0) {
                            val talking = speaker.volume > 15 && !isMuted
                            onLocalTalkingStateChanged?.invoke(talking)
                        }
                    }
                }
            }
        }

        override fun onError(err: Int) {
            Log.e(TAG, "Agora Video RTC Error: $err")
            mainHandler.post {
                eventListener?.onError(err)
            }
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
                mAudioScenario = Constants.AUDIO_SCENARIO_GAME_STREAMING
            }
            rtcEngine = RtcEngine.create(config)

            rtcEngine?.apply {
                setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
                enableVideo()
                enableAudio()
                enableLocalAudio(true)
                enableLocalVideo(true)
                setDefaultAudioRoutetoSpeakerphone(true)
                setEnableSpeakerphone(true)
                adjustRecordingSignalVolume(100)
                adjustPlaybackSignalVolume(100)
                enableAudioVolumeIndication(200, 3, true)

                // High Definition Mobile 720p/1080p Video Configuration (smooth 30fps)
                val videoConfig = VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_1280x720,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
                )
                setVideoEncoderConfiguration(videoConfig)
            }
            Log.d(TAG, "Agora RtcEngine initialized successfully with Video + Audio")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Agora RtcEngine: ${e.message}", e)
        }
    }

    fun setupLocalVideo(view: android.view.View) {
        try {
            if (view is SurfaceView) {
                view.setZOrderMediaOverlay(true)
                view.setZOrderOnTop(false)
            }
            val canvas = VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, 0)
            canvas.mirrorMode = Constants.VIDEO_MIRROR_MODE_AUTO
            rtcEngine?.setupLocalVideo(canvas)
            rtcEngine?.startPreview()
            Log.d(TAG, "Local video preview started on view: ${view.javaClass.simpleName}")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up local video: ${e.message}")
        }
    }

    fun setupRemoteVideo(view: android.view.View, uid: Int) {
        try {
            if (view is SurfaceView) {
                view.setZOrderMediaOverlay(true)
                view.setZOrderOnTop(false)
            }
            val canvas = VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, uid)
            canvas.mirrorMode = Constants.VIDEO_MIRROR_MODE_AUTO
            rtcEngine?.setupRemoteVideo(canvas)
            Log.d(TAG, "Remote video configured for uid $uid on view: ${view.javaClass.simpleName}")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up remote video for uid $uid: ${e.message}")
        }
    }

    fun joinRoom(
        context: Context,
        roomId: String,
        uid: Int,
        isBroadcaster: Boolean = true,
        startMuted: Boolean = false
    ) {
        init(context)
        val engine = rtcEngine ?: return

        currentUid = uid

        if (currentChannel == roomId) {
            Log.d(TAG, "Already in video channel $roomId")
            setBroadcasterRole(isBroadcaster)
            setMuted(startMuted)
            return
        }

        if (currentChannel != null) {
            leaveRoom()
        }

        currentChannel = roomId
        isMuted = startMuted
        isCameraOff = false

        val options = ChannelMediaOptions().apply {
            channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            clientRoleType = if (isBroadcaster) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
            autoSubscribeAudio = true
            autoSubscribeVideo = true
            publishCameraTrack = isBroadcaster
            publishMicrophoneTrack = isBroadcaster && !startMuted
        }

        // Always build token with ROLE_PUBLISHER so any room participant has instant authorization when promoted
        val tokenBuilder = RtcTokenBuilder2()
        val token = try {
            tokenBuilder.buildTokenWithUid(
                AGORA_APP_ID,
                AGORA_PRIMARY_CERTIFICATE,
                roomId,
                uid,
                RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                86400,
                86400
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating Agora Video RTC token: ${e.message}")
            ""
        }

        val res = engine.joinChannel(token, roomId, uid, options)
        Log.d(TAG, "joinRoom result: $res for channel: $roomId, uid: $uid, isBroadcaster: $isBroadcaster, tokenLength: ${token.length}")
    }

    fun setBroadcasterRole(isBroadcaster: Boolean) {
        val engine = rtcEngine ?: return
        val role = if (isBroadcaster) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
        engine.setClientRole(role)
        val options = ChannelMediaOptions().apply {
            publishCameraTrack = isBroadcaster && !isCameraOff
            publishMicrophoneTrack = isBroadcaster && !isMuted
        }
        engine.updateChannelMediaOptions(options)
        if (isBroadcaster) {
            engine.enableVideo()
            engine.enableLocalVideo(true)
            engine.muteLocalVideoStream(false)
            engine.muteLocalAudioStream(false)
            engine.startPreview()

            val ch = currentChannel
            if (!ch.isNullOrBlank() && currentUid > 0) {
                try {
                    val tokenBuilder = RtcTokenBuilder2()
                    val token = tokenBuilder.buildTokenWithUid(
                        AGORA_APP_ID,
                        AGORA_PRIMARY_CERTIFICATE,
                        ch,
                        currentUid,
                        RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                        86400,
                        86400
                    )
                    engine.renewToken(token)
                    Log.d(TAG, "Renewed publisher token for Agora video channel")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed renewing token: ${e.message}")
                }
            }
        }
        Log.d(TAG, "Agora client role updated to: $role")
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        val engine = rtcEngine ?: return
        engine.muteLocalAudioStream(muted)
        val options = ChannelMediaOptions().apply {
            publishMicrophoneTrack = !muted
        }
        engine.updateChannelMediaOptions(options)
        Log.d(TAG, "Agora local audio mute set to: $muted")
    }

    fun setCameraOff(off: Boolean) {
        isCameraOff = off
        val engine = rtcEngine ?: return
        engine.muteLocalVideoStream(off)
        val options = ChannelMediaOptions().apply {
            publishCameraTrack = !off
        }
        engine.updateChannelMediaOptions(options)
        Log.d(TAG, "Agora local video off set to: $off")
    }

    fun switchCamera() {
        val engine = rtcEngine ?: return
        engine.switchCamera()
        Log.d(TAG, "Agora camera switched front/back")
    }

    fun leaveRoom() {
        try {
            rtcEngine?.stopPreview()
            rtcEngine?.leaveChannel()
            currentChannel = null
            Log.d(TAG, "Left Agora video channel")
        } catch (e: Exception) {
            Log.e(TAG, "Error leaving Agora video channel: ${e.message}")
        }
    }

    fun destroy() {
        try {
            leaveRoom()
            RtcEngine.destroy()
            rtcEngine = null
            Log.d(TAG, "Agora Video RtcEngine destroyed")
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying Agora Video RtcEngine: ${e.message}")
        }
    }
}
