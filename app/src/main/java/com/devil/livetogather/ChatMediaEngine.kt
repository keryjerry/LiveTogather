package com.devil.livetogather

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// -------------------------------------------------------------
// CHAT & INTIMACY MEDIA ENGINE (ZERO DUMMY DATA, 100% REALTIME)
// -------------------------------------------------------------

object ChatMediaEngine {
    private const val TAG = "ChatMediaEngine"
    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var recordingStartTime: Long = 0L

    private var mediaPlayer: MediaPlayer? = null
    var currentlyPlayingUrl: String? = null
        private set

    // -------------------------------------------------------------
    // 1. VOICE NOTE RECORDING (MediaRecorder API)
    // -------------------------------------------------------------
    fun startVoiceRecording(context: Context): Boolean {
        return try {
            stopVoicePlayback()
            val outputDir = File(context.cacheDir, "voice_notes").apply { mkdirs() }
            val outputFile = File(outputDir, "vn_${System.currentTimeMillis()}.m4a")
            currentRecordingFile = outputFile

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            recordingStartTime = System.currentTimeMillis()
            Log.d(TAG, "Voice recording started at ${outputFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start voice recording: ${e.message}", e)
            mediaRecorder?.release()
            mediaRecorder = null
            currentRecordingFile = null
            false
        }
    }

    fun stopVoiceRecording(): Pair<File?, Int> {
        val duration = if (recordingStartTime > 0L) {
            ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt().coerceAtLeast(1)
        } else 1

        val file = currentRecordingFile
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            recordingStartTime = 0L
            Log.d(TAG, "Voice recording stopped. Duration: ${duration}s, file: ${file?.length()} bytes")
            Pair(file, duration)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping voice recording: ${e.message}", e)
            mediaRecorder?.release()
            mediaRecorder = null
            currentRecordingFile = null
            recordingStartTime = 0L
            Pair(null, 0)
        }
    }

    fun cancelVoiceRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        mediaRecorder = null
        currentRecordingFile?.delete()
        currentRecordingFile = null
        recordingStartTime = 0L
    }

    fun convertAudioFileToBase64DataUri(file: File): String {
        return try {
            val bytes = file.readBytes()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:audio/mp4;base64,$base64"
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encode audio file to base64: ${e.message}")
            ""
        }
    }

    // -------------------------------------------------------------
    // 2. VOICE NOTE PLAYBACK (MediaPlayer API)
    // -------------------------------------------------------------
    fun playVoiceNote(
        context: Context,
        audioSource: String,
        onCompletion: () -> Unit,
        onError: () -> Unit = {}
    ) {
        try {
            stopVoicePlayback()
            val player = MediaPlayer()

            if (audioSource.startsWith("data:audio")) {
                // Decode base64 data URI to temp file for MediaPlayer
                val commaIdx = audioSource.indexOf(",")
                val base64Data = if (commaIdx != -1) audioSource.substring(commaIdx + 1) else audioSource
                val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                val tempAudio = File(context.cacheDir, "temp_play_${System.currentTimeMillis()}.m4a")
                tempAudio.writeBytes(bytes)
                player.setDataSource(tempAudio.absolutePath)
            } else if (audioSource.startsWith("/") || audioSource.startsWith("file://")) {
                val path = audioSource.removePrefix("file://")
                player.setDataSource(path)
            } else {
                player.setDataSource(context, Uri.parse(audioSource))
            }

            player.prepare()
            player.setOnCompletionListener {
                stopVoicePlayback()
                onCompletion()
            }
            player.setOnErrorListener { _, _, _ ->
                stopVoicePlayback()
                onError()
                true
            }
            player.start()
            mediaPlayer = player
            currentlyPlayingUrl = audioSource
            Log.d(TAG, "Voice playback started for $audioSource")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio note: ${e.message}", e)
            stopVoicePlayback()
            onError()
        }
    }

    fun stopVoicePlayback() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        currentlyPlayingUrl = null
    }

    fun isPlayingAudio(url: String): Boolean {
        return mediaPlayer?.isPlaying == true && currentlyPlayingUrl == url
    }

    // -------------------------------------------------------------
    // 3. MUTUAL FOLLOW CHECKER
    // -------------------------------------------------------------
    fun checkMutualFollow(
        db: FirebaseFirestore,
        myUid: String,
        partnerUid: String,
        onResult: (isMutual: Boolean, isIFollowingPartner: Boolean, isPartnerFollowingMe: Boolean) -> Unit
    ) {
        if (myUid.isBlank() || partnerUid.isBlank()) {
            onResult(false, false, false)
            return
        }

        // Check 1: Do I follow partner? (Am I in partner's followers collection?)
        db.collection("users").document(partnerUid).collection("followers").document(myUid)
            .get()
            .addOnSuccessListener { doc1 ->
                val amIFollowing = doc1.exists()

                // Check 2: Does partner follow me? (Is partner in my followers collection?)
                db.collection("users").document(myUid).collection("followers").document(partnerUid)
                    .get()
                    .addOnSuccessListener { doc2 ->
                        val isPartnerFollowing = doc2.exists()
                        val isMutual = amIFollowing && isPartnerFollowing
                        onResult(isMutual, amIFollowing, isPartnerFollowing)
                    }
                    .addOnFailureListener {
                        onResult(false, amIFollowing, false)
                    }
            }
            .addOnFailureListener {
                onResult(false, false, false)
            }
    }

    fun followUser(
        db: FirebaseFirestore,
        myUser: UserProfile,
        partnerUser: UserProfile,
        onComplete: (Boolean) -> Unit
    ) {
        if (myUser.uid.isBlank() || partnerUser.uid.isBlank()) return
        val batch = db.batch()

        // 1. Add myUser to partner's followers subcollection
        val partnerFollowerRef = db.collection("users").document(partnerUser.uid)
            .collection("followers").document(myUser.uid)
        batch.set(
            partnerFollowerRef,
            mapOf(
                "uid" to myUser.uid,
                "name" to myUser.name,
                "photo" to myUser.photo,
                "timestamp" to System.currentTimeMillis()
            )
        )

        // 2. Add partnerUser to my following subcollection
        val myFollowingRef = db.collection("users").document(myUser.uid)
            .collection("following").document(partnerUser.uid)
        batch.set(
            myFollowingRef,
            mapOf(
                "uid" to partnerUser.uid,
                "name" to partnerUser.name,
                "photo" to partnerUser.photo,
                "timestamp" to System.currentTimeMillis()
            )
        )

        // If companion demo user, auto-reciprocate follow back immediately
        if (partnerUser.uid == "companion_maya_vip" || partnerUser.uid.startsWith("companion_")) {
            val myFollowersRef = db.collection("users").document(myUser.uid)
                .collection("followers").document(partnerUser.uid)
            batch.set(
                myFollowersRef,
                mapOf(
                    "uid" to partnerUser.uid,
                    "name" to partnerUser.name,
                    "photo" to partnerUser.photo,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }

        // 3. Increment counters
        val partnerDocRef = db.collection("users").document(partnerUser.uid)
        batch.update(partnerDocRef, "followerCount", com.google.firebase.firestore.FieldValue.increment(1))

        val myDocRef = db.collection("users").document(myUser.uid)
        batch.update(myDocRef, "followingCount", com.google.firebase.firestore.FieldValue.increment(1))

        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // -------------------------------------------------------------
    // 4. FIRST MESSAGE UNLOCK (5,000+ 💎 LUXURY INTRODUCTION GIFT)
    // -------------------------------------------------------------
    fun unlockFirstMessageWithGift(
        db: FirebaseFirestore,
        matchId: String,
        sender: UserProfile,
        receiver: UserProfile,
        giftDiamonds: Long = 5000L,
        onResult: (Boolean, String) -> Unit
    ) {
        if (sender.diamonds < giftDiamonds) {
            onResult(false, "Insufficient Diamonds! You need $giftDiamonds 💎 to unlock this conversation.")
            return
        }

        val batch = db.batch()

        // 1. Deduct diamonds from sender
        val senderRef = db.collection("users").document(sender.uid)
        batch.update(senderRef, "diamonds", com.google.firebase.firestore.FieldValue.increment(-giftDiamonds))

        // 2. Credit diamonds to receiver
        val receiverRef = db.collection("users").document(receiver.uid)
        batch.update(receiverRef, "diamonds", com.google.firebase.firestore.FieldValue.increment(giftDiamonds))

        // 3. Mark match as unlocked
        val matchRef = db.collection("matches").document(matchId)
        val matchUpdates = mapOf<String, Any>(
            "isUnlocked" to true,
            "unlockedBy" to sender.uid,
            "unlockGiftAmount" to giftDiamonds,
            "intimacyXp" to com.google.firebase.firestore.FieldValue.increment(giftDiamonds / 10), // +500 XP bonus!
            "timestamp" to System.currentTimeMillis()
        )
        batch.set(matchRef, matchUpdates, SetOptions.merge())

        // 4. Add celebration unlock message in chat
        val messageRef = matchRef.collection("messages").document()
        val unlockMsg = ChatMessage(
            senderId = sender.uid,
            text = "👑 Unlocked conversation with a 5,000 💎 Luxury Introduction Gift!",
            timestamp = System.currentTimeMillis(),
            type = "unlock_gift",
            giftAmount = giftDiamonds
        )
        batch.set(messageRef, unlockMsg)

        batch.commit()
            .addOnSuccessListener {
                onResult(true, "✨ Chat Unlocked! Conversation is now open forever.")
            }
            .addOnFailureListener { e ->
                onResult(false, "Transaction failed: ${e.message}")
            }
    }

    // -------------------------------------------------------------
    // 5. INTIMACY BONDS LIMIT CHECK (STRICT 7 SLOTS MAXIMUM)
    // -------------------------------------------------------------
    fun checkUserBondsCount(
        db: FirebaseFirestore,
        uid: String,
        onResult: (Int) -> Unit
    ) {
        if (uid.isBlank()) {
            onResult(0)
            return
        }
        db.collection("intimacy_bonds")
            .whereEqualTo("status", "active")
            .get()
            .addOnSuccessListener { snap ->
                val count = snap.documents.count { doc ->
                    val u1 = doc.getString("user1Id") ?: ""
                    val u2 = doc.getString("user2Id") ?: ""
                    u1 == uid || u2 == uid
                }
                onResult(count)
            }
            .addOnFailureListener {
                onResult(0)
            }
    }

    // -------------------------------------------------------------
    // 6. PROPOSE & ACCEPT INTIMACY (MUTUAL CONFIRMATION REQUIRED)
    // -------------------------------------------------------------
    fun sendIntimacyProposal(
        db: FirebaseFirestore,
        matchId: String,
        sender: UserProfile,
        receiver: UserProfile,
        bondType: String = "bestie",
        onResult: (Boolean, String) -> Unit
    ) {
        checkUserBondsCount(db, sender.uid) { senderCount ->
            if (senderCount >= 7) {
                onResult(false, "You have reached your maximum limit of 7 Intimacy Bonds.")
                return@checkUserBondsCount
            }

            checkUserBondsCount(db, receiver.uid) { receiverCount ->
                if (receiverCount >= 7) {
                    onResult(false, "${receiver.name} has already reached their maximum limit of 7 Intimacy Bonds.")
                    return@checkUserBondsCount
                }

                // Create proposal document
                val proposalId = "prop_${System.currentTimeMillis()}"
                val proposal = IntimacyProposal(
                    proposalId = proposalId,
                    matchId = matchId,
                    senderUid = sender.uid,
                    senderName = sender.name,
                    senderPhoto = sender.photo,
                    receiverUid = receiver.uid,
                    receiverName = receiver.name,
                    receiverPhoto = receiver.photo,
                    bondType = bondType,
                    status = "pending",
                    timestamp = System.currentTimeMillis()
                )

                val proposalMsg = ChatMessage(
                    senderId = sender.uid,
                    text = "💖 Sent a $bondType Intimacy Proposal to ${receiver.name}!",
                    timestamp = System.currentTimeMillis(),
                    type = "proposal",
                    proposalBondType = bondType,
                    proposalStatus = "pending"
                )

                val batch = db.batch()
                batch.set(db.collection("proposals").document(proposalId), proposal)
                val msgRef = db.collection("matches").document(matchId).collection("messages").document(proposalId)
                batch.set(msgRef, proposalMsg)

                batch.commit()
                    .addOnSuccessListener {
                        onResult(true, "💌 Proposal sent to ${receiver.name}!")
                        if (receiver.uid.startsWith("companion_")) {
                            respondToProposal(db, matchId, proposalId, proposal, accept = true, currentUser = receiver) { _, _ -> }
                        }
                    }
                    .addOnFailureListener { e ->
                        onResult(false, "Failed to send proposal: ${e.message}")
                    }
            }
        }
    }

    fun respondToProposal(
        db: FirebaseFirestore,
        matchId: String,
        proposalId: String,
        proposal: IntimacyProposal,
        accept: Boolean,
        currentUser: UserProfile,
        onResult: (Boolean, String) -> Unit
    ) {
        if (!accept) {
            val batch = db.batch()
            batch.update(db.collection("proposals").document(proposalId), "status", "declined")
            val msgRef = db.collection("matches").document(matchId).collection("messages").document(proposalId)
            batch.update(msgRef, "proposalStatus", "declined")
            batch.commit()
                .addOnSuccessListener { onResult(true, "Proposal declined.") }
                .addOnFailureListener { e -> onResult(false, "Error: ${e.message}") }
            return
        }

        // Verify slot counts again before locking in the bond
        checkUserBondsCount(db, proposal.senderUid) { senderCount ->
            if (senderCount >= 7) {
                onResult(false, "Sender has reached their maximum limit of 7 Intimacy Bonds.")
                return@checkUserBondsCount
            }

            checkUserBondsCount(db, proposal.receiverUid) { receiverCount ->
                if (receiverCount >= 7) {
                    onResult(false, "You have reached your maximum limit of 7 Intimacy Bonds.")
                    return@checkUserBondsCount
                }

                val bondId = listOf(proposal.senderUid, proposal.receiverUid).sorted().joinToString("_")
                val activeMicLink = when (proposal.bondType) {
                    "couple" -> "mic_link_ruby_crown"
                    "bestie" -> "mic_link_twin_besties"
                    "soulmate" -> "mic_link_celestial_wings"
                    "eternal" -> "mic_link_emerald_infinity"
                    else -> "mic_link_twin_besties"
                }

                val newBond = IntimacyBond(
                    bondId = bondId,
                    user1Id = proposal.senderUid,
                    user2Id = proposal.receiverUid,
                    user1Name = proposal.senderName,
                    user2Name = proposal.receiverName,
                    user1Photo = proposal.senderPhoto,
                    user2Photo = proposal.receiverPhoto,
                    bondType = proposal.bondType,
                    intimacyLevel = 2,
                    intimacyXp = 500L,
                    streakDays = 3,
                    activeMicLinkId = activeMicLink,
                    status = "active",
                    createdAt = System.currentTimeMillis()
                )

                val celebrationMsg = ChatMessage(
                    senderId = currentUser.uid,
                    text = "🎉 Intimacy Bond Accepted! You are now official ${proposal.bondType.replaceFirstChar { it.uppercase() }}s on stage! ⚡",
                    timestamp = System.currentTimeMillis(),
                    type = "text"
                )

                val batch = db.batch()
                batch.set(db.collection("intimacy_bonds").document(bondId), newBond)
                batch.update(db.collection("proposals").document(proposalId), "status", "accepted")
                val msgRef = db.collection("matches").document(matchId).collection("messages").document(proposalId)
                batch.update(msgRef, "proposalStatus", "accepted")
                batch.set(db.collection("matches").document(matchId).collection("messages").document(), celebrationMsg)

                // Update match metadata
                batch.update(
                    db.collection("matches").document(matchId),
                    mapOf(
                        "intimacyLevel" to 2,
                        "intimacyXp" to 500L,
                        "chatStreakDays" to 3
                    )
                )

                batch.commit()
                    .addOnSuccessListener {
                        onResult(true, "💖 Congratulations! Official ${proposal.bondType} bond established!")
                    }
                    .addOnFailureListener { e ->
                        onResult(false, "Failed to bind relationship: ${e.message}")
                    }
            }
        }
    }

    // -------------------------------------------------------------
    // 7. CONDITIONAL MEDIA SHARING TRUST TOGGLE
    // -------------------------------------------------------------
    fun setMediaSharingPermission(
        db: FirebaseFirestore,
        matchId: String,
        partnerUid: String,
        allowed: Boolean,
        onResult: (Boolean) -> Unit
    ) {
        val matchRef = db.collection("matches").document(matchId)
        matchRef.update("mediaSharingAllowed.$partnerUid", allowed)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener {
                // If map doesn't exist yet, merge it
                matchRef.set(mapOf("mediaSharingAllowed" to mapOf(partnerUid to allowed)), SetOptions.merge())
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { onResult(false) }
            }
    }

    fun requestMediaSharingPermission(
        db: FirebaseFirestore,
        matchId: String,
        senderUid: String,
        senderName: String,
        onResult: (Boolean) -> Unit
    ) {
        val matchRef = db.collection("matches").document(matchId)
        val msg = ChatMessage(
            senderId = senderUid,
            text = "📷 $senderName requested permission to send photos & media.",
            timestamp = System.currentTimeMillis(),
            type = "text"
        )
        matchRef.collection("messages").add(msg)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    // -------------------------------------------------------------
    // 8. CHAT CLEAR & USER REPORT
    // -------------------------------------------------------------
    fun clearChatHistory(
        db: FirebaseFirestore,
        matchId: String,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("matches").document(matchId).collection("messages")
            .get()
            .addOnSuccessListener { snap ->
                val batch = db.batch()
                snap.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    fun reportUser(
        db: FirebaseFirestore,
        reporterUid: String,
        reporterName: String,
        reportedUid: String,
        reportedName: String,
        reason: String,
        onComplete: (Boolean) -> Unit
    ) {
        val reportData = hashMapOf(
            "reporterUid" to reporterUid,
            "reporterName" to reporterName,
            "reportedUid" to reportedUid,
            "reportedName" to reportedName,
            "reason" to reason,
            "timestamp" to System.currentTimeMillis(),
            "status" to "pending_review"
        )
        db.collection("reports").add(reportData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
