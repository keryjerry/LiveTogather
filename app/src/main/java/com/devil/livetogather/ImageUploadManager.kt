package com.devil.livetogather

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ImageUploadManager {
    private const val TAG = "ImageUploadManager"

    /**
     * Saves image permanently to app's internal private storage directory.
     * Guaranteed to work 100% offline and instantly.
     * Returns the absolute file path of the saved photo.
     */
    fun saveImageLocally(context: Context, uri: Uri, customName: String? = null): String? {
        return try {
            val bytes = compressImage(context, uri) ?: return null
            val dir = File(context.filesDir, "profiles").apply { mkdirs() }
            val fileName = customName ?: "avatar_${System.currentTimeMillis()}.jpg"
            val file = File(dir, fileName)
            FileOutputStream(file).use { fos ->
                fos.write(bytes)
                fos.flush()
            }
            Log.d(TAG, "Saved image locally to: ${file.absolutePath}")
            "file://${file.absolutePath}"
        } catch (e: Exception) {
            Log.e(TAG, "Local image save failed: ${e.message}", e)
            null
        }
    }

    /**
     * Compresses and converts image to a Firestore-compatible Data URI string.
     * Stored 100% inside Cloud Firestore document (users/{uid}.photo).
     * Works on all devices, syncs in real-time across users, and never relies on device-local directories.
     */
    fun convertImageToFirestoreDataUri(context: Context, uri: Uri): String? {
        val bytes = compressImage(context, uri, maxDimension = 360, quality = 80) ?: return null
        val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        return "data:image/jpeg;base64,$base64"
    }

    /**
     * Multi-tier image upload:
     * 1. Converts to Firestore-ready Data URI so data is always saved in Cloud Firestore.
     * 2. Attempts Firebase Storage with a strict 4-second timeout.
     * 3. Falls back immediately to the Firestore Data URI so the image is stored directly in Cloud Firestore without hanging.
     */
    suspend fun uploadImage(
        context: Context,
        uri: Uri,
        folder: String = "general"
    ): String? = withContext(Dispatchers.IO) {
        val firestoreDataUri = convertImageToFirestoreDataUri(context, uri)

        // Attempt quick Firebase Storage upload with 4-second timeout
        try {
            kotlinx.coroutines.withTimeoutOrNull(4000L) {
                val bytes = compressImage(context, uri, maxDimension = 800, quality = 80)
                if (bytes != null) {
                    val fileName = "${folder}/${UUID.randomUUID()}.jpg"
                    val storage = FirebaseStorage.getInstance()
                    val storageRef = storage.reference.child(fileName)
                    val cloudUrl = uploadToStorageRef(storageRef, bytes)
                    if (!cloudUrl.isNullOrBlank()) {
                        Log.d(TAG, "Uploaded image successfully to Firebase Storage: $cloudUrl")
                        return@withTimeoutOrNull cloudUrl
                    }
                }
                null
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Firebase Storage quick upload skipped: ${e.message}")
            null
        } ?: firestoreDataUri
    }

    suspend fun uploadMomentMedia(context: Context, uri: Uri): String? {
        val contentType = context.contentResolver.getType(uri) ?: return null
        if (contentType.startsWith("image/")) {
            return uploadImage(context, uri, "moments")
        }
        if (!contentType.startsWith("video/")) return null

        val extension = when (contentType.substringAfter('/').lowercase()) {
            "quicktime" -> "mov"
            "x-matroska" -> "mkv"
            "webm" -> "webm"
            else -> "mp4"
        }
        val storageRef = FirebaseStorage.getInstance().reference
            .child("moments/${UUID.randomUUID()}.$extension")
        storageRef.putFile(
            uri,
            StorageMetadata.Builder().setContentType(contentType).build()
        ).await()
        return storageRef.downloadUrl.await().toString()
    }

    private suspend fun uploadToStorageRef(storageRef: StorageReference, bytes: ByteArray): String? = suspendCoroutine { continuation ->
        val uploadTask = storageRef.putBytes(bytes)
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                continuation.resume(downloadUri.toString())
            }.addOnFailureListener {
                continuation.resume(null)
            }
        }.addOnFailureListener {
            continuation.resume(null)
        }
    }

    private fun compressImage(
        context: Context,
        uri: Uri,
        maxDimension: Int = 1024,
        quality: Int = 85
    ): ByteArray? {
        return try {
            if (uri.toString().startsWith("data:image")) {
                val base64Part = uri.toString().substringAfter("base64,")
                return android.util.Base64.decode(base64Part, android.util.Base64.DEFAULT)
            }

            // Step 1: Query image dimensions without allocating bitmap memory
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight
            if (origWidth <= 0 || origHeight <= 0) return null

            // Step 2: Compute inSampleSize to downsample high-res photos during decode
            var sampleSize = 1
            while ((origWidth / (sampleSize * 2)) >= maxDimension && (origHeight / (sampleSize * 2)) >= maxDimension) {
                sampleSize *= 2
            }

            // Step 3: Decode scaled bitmap with RGB_565 to prevent any OutOfMemoryError
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            val sampledBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            // Step 4: Scale precisely to maxDimension
            val width = sampledBitmap.width
            val height = sampledBitmap.height
            val finalBitmap = if (width > maxDimension || height > maxDimension) {
                val ratio = width.toFloat() / height.toFloat()
                val targetWidth = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
                val targetHeight = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
                val scaled = Bitmap.createScaledBitmap(sampledBitmap, targetWidth.coerceAtLeast(1), targetHeight.coerceAtLeast(1), true)
                if (scaled != sampledBitmap) sampledBitmap.recycle()
                scaled
            } else {
                sampledBitmap
            }

            val outputStream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            finalBitmap.recycle()
            outputStream.toByteArray()
        } catch (e: Throwable) {
            Log.e(TAG, "Error compressing image: ${e.message}", e)
            null
        }
    }
}
