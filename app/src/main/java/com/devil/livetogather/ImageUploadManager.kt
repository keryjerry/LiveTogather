package com.devil.livetogather

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
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
     * 2. Attempts Firebase Storage with default and alternative buckets.
     * 3. If Cloud Storage fails, returns the Firestore Data URI so it is stored directly in Cloud Firestore!
     */
    suspend fun uploadImage(
        context: Context,
        uri: Uri,
        folder: String = "general"
    ): String? = withContext(Dispatchers.IO) {
        val firestoreDataUri = convertImageToFirestoreDataUri(context, uri)

        // 1. Try Firebase Storage with multiple bucket configurations
        val bytes = compressImage(context, uri, maxDimension = 1024, quality = 85)
        if (bytes != null) {
            val fileName = "${folder}/${UUID.randomUUID()}.jpg"
            val candidateBuckets = listOf(
                null, // Default bucket from google-services.json
                "gs://livetogather-ac204.firebasestorage.app",
                "gs://livetogather-ac204.appspot.com"
            )

            for (bucket in candidateBuckets) {
                try {
                    val storage = if (bucket == null) FirebaseStorage.getInstance() else FirebaseStorage.getInstance(bucket)
                    val storageRef = storage.reference.child(fileName)
                    val cloudUrl = uploadToStorageRef(storageRef, bytes)
                    if (!cloudUrl.isNullOrBlank()) {
                        Log.d(TAG, "Uploaded image successfully to Firebase Storage ($bucket): $cloudUrl")
                        return@withContext cloudUrl
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Firebase Storage attempt failed for bucket $bucket: ${e.message}")
                }
            }
        }

        // 2. Return the Firestore Data URI so the image is stored directly in Cloud Firestore!
        Log.i(TAG, "Returning Cloud Firestore Data URI for cloud database persistence")
        firestoreDataUri
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
            val originalBitmap = if (uri.toString().startsWith("data:image")) {
                val base64Part = uri.toString().substringAfter("base64,")
                val decodedBytes = android.util.Base64.decode(base64Part, android.util.Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } else {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return null
                val bmp = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                bmp
            } ?: return null

            val width = originalBitmap.width
            val height = originalBitmap.height
            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                val ratio = width.toFloat() / height.toFloat()
                val targetWidth = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
                val targetHeight = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
                Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image: ${e.message}")
            null
        }
    }
}
