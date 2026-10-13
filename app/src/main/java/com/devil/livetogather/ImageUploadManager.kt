package com.devil.livetogather

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object ImageUploadManager {
    private const val TAG = "ImageUploadManager"

    suspend fun uploadImage(
        context: Context,
        uri: Uri,
        folder: String = "general"
    ): String? = withContext(Dispatchers.IO) {
        try {
            val bytes = compressImage(context, uri) ?: return@withContext null

            val fileName = "${folder}/${UUID.randomUUID()}.jpg"
            val storageRef = FirebaseStorage.getInstance().reference.child(fileName)

            val downloadUrl = suspendCoroutine<String> { continuation ->
                val uploadTask = storageRef.putBytes(bytes)
                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        continuation.resume(downloadUri.toString())
                    }.addOnFailureListener { err ->
                        continuation.resumeWithException(err)
                    }
                }.addOnFailureListener { err ->
                    continuation.resumeWithException(err)
                }
            }

            Log.d(TAG, "Uploaded image successfully to Firebase Storage: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Image upload failed: ${e.message}", e)
            null
        }
    }

    private fun compressImage(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap == null) return null

            // Scale down if dimensions exceed 1280px to optimize storage & speed
            val maxDimension = 1280
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
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 82, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image: ${e.message}")
            null
        }
    }
}
