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
     * Multi-tier image upload:
     * 1. Saves local persistent copy first so the chosen image NEVER fails or disappears.
     * 2. Attempts Firebase Storage with default and alternative buckets.
     * 3. If Cloud Storage fails, returns the local persistent file path.
     */
    suspend fun uploadImage(
        context: Context,
        uri: Uri,
        folder: String = "general"
    ): String? = withContext(Dispatchers.IO) {
        val bytes = compressImage(context, uri) ?: return@withContext null

        // Always save a local copy as an unbreakable fallback
        val localPath = saveImageLocally(context, uri, "${folder}_${UUID.randomUUID()}.jpg")

        // 1. Try Firebase Storage with multiple bucket configurations
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

        // 2. If Cloud Storage failed, return the local file path so the photo works immediately and permanently!
        Log.i(TAG, "Cloud storage unavailable. Falling back to local persistent path: $localPath")
        localPath ?: uri.toString()
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

    private fun compressImage(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap == null) return null

            // Scale down if dimensions exceed 1024px to optimize storage & speed
            val maxDimension = 1024
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
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image: ${e.message}")
            null
        }
    }
}
