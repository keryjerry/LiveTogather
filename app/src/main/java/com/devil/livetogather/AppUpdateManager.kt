package com.devil.livetogather

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val latestVersionCode: Int = 1,
    val latestVersionName: String = "1.0.0",
    val apkUrl: String = "",
    val releaseNotes: String = "",
    val isForceUpdate: Boolean = false
)

object AppUpdateManager {
    private const val TAG = "AppUpdateManager"

    fun checkForUpdate(
        context: Context,
        onUpdateAvailable: (AppUpdateInfo) -> Unit,
        onNoUpdate: () -> Unit = {}
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("app_config").document("version")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && snapshot.exists()) {
                    val latestCode = (snapshot.getLong("latest_version_code")
                        ?: snapshot.getLong("latestVersionCode") ?: 1L).toInt()
                    val latestName = snapshot.getString("latest_version_name")
                        ?: snapshot.getString("latestVersionName") ?: "1.0.0"
                    val apkUrl = snapshot.getString("apk_url")
                        ?: snapshot.getString("apkUrl") ?: ""
                    val releaseNotes = snapshot.getString("release_notes")
                        ?: snapshot.getString("releaseNotes")
                        ?: "✨ Performance enhancements, new themes, and mic fixes!"
                    val isForce = snapshot.getBoolean("is_force_update")
                        ?: snapshot.getBoolean("isForceUpdate") ?: false

                    val currentCode = getCurrentVersionCode(context)
                    Log.d(TAG, "Current Version Code: $currentCode, Latest Version Code: $latestCode")

                    if (latestCode > currentCode && apkUrl.isNotBlank()) {
                        val info = AppUpdateInfo(
                            latestVersionCode = latestCode,
                            latestVersionName = latestName,
                            apkUrl = apkUrl,
                            releaseNotes = releaseNotes,
                            isForceUpdate = isForce
                        )
                        onUpdateAvailable(info)
                    } else {
                        onNoUpdate()
                    }
                } else {
                    onNoUpdate()
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error checking for app updates: ${e.message}")
                onNoUpdate()
            }
    }

    fun getCurrentVersionCode(context: Context): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionCode
            }
        } catch (e: Exception) {
            1
        }
    }

    fun getCurrentVersionName(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    fun downloadAndInstallApk(
        context: Context,
        apkUrl: String,
        onProgress: (Float) -> Unit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (apkUrl.isBlank()) {
            onError("Invalid APK download URL")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(apkUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.instanceFollowRedirects = true
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    withContext(Dispatchers.Main) {
                        onError("Server returned HTTP $responseCode")
                    }
                    return@launch
                }

                val fileLength = connection.contentLength
                val cacheDir = context.externalCacheDir ?: context.cacheDir
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }

                // Delete any stale/corrupted APK files before writing
                try {
                    cacheDir.listFiles()?.forEach { file ->
                        if (file.name.startsWith("LiveTogather") || file.name.endsWith(".apk")) {
                            file.delete()
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error cleaning stale APK files: ${e.message}")
                }

                val destinationFile = File(cacheDir, "LiveTogather_update_${System.currentTimeMillis()}.apk")
                if (destinationFile.exists()) {
                    destinationFile.delete()
                }

                val input = connection.inputStream
                val output = FileOutputStream(destinationFile)
                val buffer = ByteArray(8192)
                var total: Long = 0
                var count: Int

                while (input.read(buffer).also { count = it } != -1) {
                    total += count
                    output.write(buffer, 0, count)
                    if (fileLength > 0) {
                        val progress = total.toFloat() / fileLength.toFloat()
                        withContext(Dispatchers.Main) {
                            onProgress(progress)
                        }
                    }
                }

                output.flush()
                output.close()
                input.close()

                withContext(Dispatchers.Main) {
                    onSuccess()
                    installApk(context, destinationFile)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Download failed")
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) {
            Toast.makeText(context, "APK file not found for installation", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    Toast.makeText(context, "Please allow 'Install unknown apps' permission to update", Toast.LENGTH_LONG).show()
                    val permissionIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(permissionIntent)
                    return
                }
            }

            val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            val resInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                context.grantUriPermission(resolveInfo.activityInfo.packageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start APK installer: ${e.message}", e)
            Toast.makeText(context, "Failed to launch installer: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun AppUpdateDialog(
    updateInfo: AppUpdateInfo,
    downloadProgress: Float,
    isDownloading: Boolean,
    onUpdateClick: (AppUpdateInfo) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            if (!updateInfo.isForceUpdate && !isDownloading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !updateInfo.isForceUpdate && !isDownloading,
            dismissOnClickOutside = !updateInfo.isForceUpdate && !isDownloading
        )
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, BrandGold, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(BrandGold.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = "Update",
                        tint = BrandGold,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "New Update Available! 🚀",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Version ${updateInfo.latestVersionName} (Build ${updateInfo.latestVersionCode}) is ready.",
                    color = BrandGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = BrandCardSubtle),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "What's New:",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = updateInfo.releaseNotes.ifBlank { "✨ Performance improvements, bug fixes, and new features!" },
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (isDownloading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (downloadProgress >= 0f) {
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                color = BrandGold,
                                trackColor = BrandCardSubtle,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Downloading update: ${(downloadProgress * 100).toInt()}%",
                                color = BrandGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            CircularProgressIndicator(color = BrandGold, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Connecting to server...", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = { onUpdateClick(updateInfo) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "Update Now 📥",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!updateInfo.isForceUpdate) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Later", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
