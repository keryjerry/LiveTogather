package com.devil.livetogather

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import java.util.Locale

// -------------------------------------------------------------
// APP UPDATE DATA MODEL
// -------------------------------------------------------------
data class AppUpdateInfo(
    val latestVersionCode: Int = 18,
    val latestVersionName: String = "1.0.17",
    val minSupportedVersionCode: Int = 0,
    val apkUrl: String = "",
    val releaseNotes: String = "",
    val isForceUpdate: Boolean = false
)

// -------------------------------------------------------------
// APP UPDATE MANAGER SINGLETON
// -------------------------------------------------------------
object AppUpdateManager {
    private const val TAG = "AppUpdateManager"
    private const val PREFS_NAME = "app_update_prefs"
    private const val KEY_DISMISSED_CODE = "dismissed_version_code"
    private const val KEY_DISMISSED_TIME = "dismissed_timestamp"
    private const val DISMISS_GRACE_PERIOD_MS = 12 * 60 * 60 * 1000L // 12 hours grace if dismissed

    /**
     * Checks Firestore `app_config/version` for updates.
     * Accurately compares latest_version_code against current installed versionCode.
     * If latest <= current, NO popup is ever triggered.
     */
    fun checkForUpdate(
        context: Context,
        onUpdateAvailable: (AppUpdateInfo) -> Unit,
        onNoUpdate: () -> Unit = {}
    ) {
        val currentCode = getCurrentVersionCode(context)
        val db = FirebaseFirestore.getInstance()

        db.collection("app_config").document("version")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error listening for app updates: ${error.message}")
                    onNoUpdate()
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val latestCode = (snapshot.getLong("latest_version_code")
                        ?: snapshot.getLong("latestVersionCode") ?: 1L).toInt()
                    val minSupportedCode = (snapshot.getLong("min_supported_version_code")
                        ?: snapshot.getLong("minSupportedVersionCode") ?: 0L).toInt()
                    val latestName = snapshot.getString("latest_version_name")
                        ?: snapshot.getString("latestVersionName") ?: "1.0.17"
                    val apkUrl = snapshot.getString("apk_url")
                        ?: snapshot.getString("apkUrl") ?: ""
                    val releaseNotes = snapshot.getString("release_notes")
                        ?: snapshot.getString("releaseNotes")
                        ?: "✨ Performance enhancements, bug fixes, and new features!"
                    val explicitForce = snapshot.getBoolean("is_force_update")
                        ?: snapshot.getBoolean("isForceUpdate") ?: false

                    val isForce = explicitForce || (minSupportedCode > 0 && currentCode < minSupportedCode)

                    Log.d(TAG, "Current Version: $currentCode, Latest: $latestCode, MinSupported: $minSupportedCode, IsForce: $isForce")

                    // Strictly trigger ONLY if latest is newer than current installed code
                    if (latestCode > currentCode && apkUrl.isNotBlank()) {
                        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        val lastDismissedCode = prefs.getInt(KEY_DISMISSED_CODE, -1)
                        val lastDismissedTime = prefs.getLong(KEY_DISMISSED_TIME, 0L)
                        val now = System.currentTimeMillis()
                        val isRecentlyDismissed = (lastDismissedCode == latestCode && (now - lastDismissedTime) < DISMISS_GRACE_PERIOD_MS)

                        // Force update ignores dismiss; optional update respects grace period to prevent harassment
                        if (!isForce && isRecentlyDismissed) {
                            Log.d(TAG, "Update $latestCode was dismissed recently. Suppressing popup.")
                            onNoUpdate()
                        } else {
                            val info = AppUpdateInfo(
                                latestVersionCode = latestCode,
                                latestVersionName = latestName,
                                minSupportedVersionCode = minSupportedCode,
                                apkUrl = apkUrl,
                                releaseNotes = releaseNotes,
                                isForceUpdate = isForce
                            )
                            onUpdateAvailable(info)
                        }
                    } else {
                        // Already on latest or newer version; clean up any old cached APKs
                        cleanupOldApks(context, currentCode)
                        onNoUpdate()
                    }
                } else {
                    onNoUpdate()
                }
            }
    }

    /**
     * Records dismissal in SharedPreferences so user is not harassed on every screen.
     */
    fun recordDismissal(context: Context, versionCode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_DISMISSED_CODE, versionCode)
            .putLong(KEY_DISMISSED_TIME, System.currentTimeMillis())
            .apply()
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
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.17"
        } catch (e: Exception) {
            "1.0.17"
        }
    }

    /**
     * Determines whether the app has permission to install unknown apps.
     */
    fun hasInstallPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Opens system Settings so user can enable "Install Unknown Apps" permission.
     */
    fun requestInstallPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open install permission settings: ${e.message}")
            }
        }
    }

    /**
     * Returns the persistent target APK file location for a given version.
     */
    fun getTargetApkFile(context: Context, versionCode: Int): File {
        val cacheDir = context.externalCacheDir ?: context.cacheDir
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return File(cacheDir, "LiveTogather_v${versionCode}.apk")
    }

    /**
     * Checks if the APK for this version is already downloaded and valid in cache.
     */
    fun isApkAlreadyDownloaded(context: Context, versionCode: Int): Boolean {
        val file = getTargetApkFile(context, versionCode)
        return file.exists() && file.length() > 500_000L // Valid if >= 500 KB
    }

    /**
     * Removes older version APK files to preserve user disk space.
     */
    fun cleanupOldApks(context: Context, keepVersionCode: Int) {
        try {
            val cacheDir = context.externalCacheDir ?: context.cacheDir
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("LiveTogather_v") && file.name.endsWith(".apk")) {
                    if (!file.name.contains("_v${keepVersionCode}.apk")) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error cleaning old APKs: ${e.message}")
        }
    }

    /**
     * Downloads APK with resume cache. If APK is already downloaded, skips download and directly opens installer!
     */
    fun downloadAndInstallApk(
        context: Context,
        updateInfo: AppUpdateInfo,
        onProgress: (progress: Float, downloadedBytes: Long, totalBytes: Long) -> Unit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val apkUrl = updateInfo.apkUrl.trim()
        val versionCode = updateInfo.latestVersionCode

        if (apkUrl.isBlank()) {
            onError("Invalid APK download URL")
            return
        }

        val targetFile = getTargetApkFile(context, versionCode)

        // 1. SMART CACHE CHECK: If APK already downloaded, DO NOT re-download!
        if (isApkAlreadyDownloaded(context, versionCode)) {
            Log.d(TAG, "APK already downloaded (${targetFile.length()} bytes). Launching installer immediately.")
            onProgress(1f, targetFile.length(), targetFile.length())
            onSuccess()
            installApk(context, targetFile)
            return
        }

        // 2. CHECK INSTALL PERMISSION BEFORE DOWNLOAD
        if (!hasInstallPermission(context)) {
            Toast.makeText(context, "Please allow 'Install unknown apps' permission to update", Toast.LENGTH_LONG).show()
            requestInstallPermission(context)
            onError("Please allow install permission and try again.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            var connection: HttpURLConnection? = null
            try {
                var currentUrl = apkUrl
                var redirectCount = 0
                val maxRedirects = 6

                while (redirectCount < maxRedirects) {
                    val url = URL(currentUrl)
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        connectTimeout = 25000
                        readTimeout = 90000
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", "LiveTogather-Updater/1.0.17")
                    }
                    connection.connect()

                    val responseCode = connection.responseCode
                    if (responseCode in listOf(HttpURLConnection.HTTP_MOVED_PERM, HttpURLConnection.HTTP_MOVED_TEMP, HttpURLConnection.HTTP_SEE_OTHER, 307, 308)) {
                        val redirectLocation = connection.getHeaderField("Location")
                        connection.disconnect()
                        if (!redirectLocation.isNullOrBlank()) {
                            currentUrl = redirectLocation
                            redirectCount++
                            continue
                        }
                    }
                    break
                }

                val nonNullConn = connection
                val finalCode = nonNullConn?.responseCode ?: -1
                if (nonNullConn == null || finalCode !in 200..299) {
                    withContext(Dispatchers.Main) {
                        onError("Server returned HTTP $finalCode")
                    }
                    return@launch
                }

                val totalLength = nonNullConn.contentLength.toLong()
                val tempFile = File(targetFile.parentFile, "LiveTogather_v${versionCode}.tmp")
                if (tempFile.exists()) {
                    tempFile.delete()
                }

                val input = nonNullConn.inputStream
                val output = FileOutputStream(tempFile)
                val buffer = ByteArray(16384)
                var downloadedTotal: Long = 0
                var count: Int

                while (input.read(buffer).also { count = it } != -1) {
                    downloadedTotal += count
                    output.write(buffer, 0, count)
                    if (totalLength > 0) {
                        val progress = downloadedTotal.toFloat() / totalLength.toFloat()
                        withContext(Dispatchers.Main) {
                            onProgress(progress, downloadedTotal, totalLength)
                        }
                    }
                }

                output.flush()
                output.close()
                input.close()

                if (tempFile.length() < 100_000L) {
                    tempFile.delete()
                    withContext(Dispatchers.Main) {
                        onError("Downloaded APK file is incomplete or invalid.")
                    }
                    return@launch
                }

                // Atomic rename to final APK file
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                tempFile.renameTo(targetFile)

                withContext(Dispatchers.Main) {
                    onSuccess()
                    installApk(context, targetFile)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Download failed. Please check network connection.")
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    /**
     * Launches system PackageInstaller safely with FileProvider.
     */
    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) {
            Toast.makeText(context, "APK file not found for installation", Toast.LENGTH_SHORT).show()
            return
        }

        if (!hasInstallPermission(context)) {
            Toast.makeText(context, "Please allow 'Install unknown apps' permission to update", Toast.LENGTH_LONG).show()
            requestInstallPermission(context)
            return
        }

        try {
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

// -------------------------------------------------------------
// PROFESSIONAL GALAXY GLOSSY ANIMATED UPDATE DIALOG (NO CARTOON)
// -------------------------------------------------------------
@Composable
fun AppUpdateDialog(
    updateInfo: AppUpdateInfo,
    downloadProgress: Float,
    downloadedBytes: Long = 0L,
    totalBytes: Long = 0L,
    isDownloading: Boolean,
    onUpdateClick: (AppUpdateInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isApkCached = remember(updateInfo.latestVersionCode) {
        AppUpdateManager.isApkAlreadyDownloaded(context, updateInfo.latestVersionCode)
    }

    // Subtle Infinite Cosmic Pulse for Icon & Border
    val infiniteTransition = rememberInfiniteTransition(label = "updateCosmicPulse")
    val cosmicPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cosmicPulse"
    )

    Dialog(
        onDismissRequest = {
            if (!updateInfo.isForceUpdate && !isDownloading) {
                AppUpdateManager.recordDismissal(context, updateInfo.latestVersionCode)
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !updateInfo.isForceUpdate && !isDownloading,
            dismissOnClickOutside = !updateInfo.isForceUpdate && !isDownloading,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Galaxy Glossy Dark Purple Dialog Container
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.3.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFFC084FC).copy(alpha = 0.65f),
                                Color(0xFF7C3AED).copy(alpha = 0.35f),
                                Color(0xFF3B0764).copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0xFF0F061F), // Deep Cosmic Purple Night
                                    Color(0xFF1B0B38), // Galactic Indigo
                                    Color(0xFF0D061A)  // Obsidian Space
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Header: Sleek Nebula Orb with Icon
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF8B5CF6).copy(alpha = 0.35f * cosmicPulse),
                                            Color(0xFF1B0B38),
                                            Color(0xFF0B061A)
                                        )
                                    )
                                )
                                .border(
                                    1.2.dp,
                                    Color(0xFFC084FC).copy(alpha = 0.5f),
                                    RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isApkCached) Icons.Default.CheckCircle else Icons.Default.RocketLaunch,
                                contentDescription = "App Update",
                                tint = Color(0xFFE9D5FF),
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title & Version Badge
                        Text(
                            text = if (isApkCached) "Update Ready to Install" else "New Update Available",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Version Pill Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF280C46).copy(alpha = 0.8f))
                                .border(0.8.dp, Color(0xFFC084FC).copy(alpha = 0.4f), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "v${updateInfo.latestVersionName}",
                                color = Color(0xFFE9D5FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•  Build ${updateInfo.latestVersionCode}",
                                color = Color(0xFFA855F7),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Critical Force Update Alert Pill
                        if (updateInfo.isForceUpdate) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFF1744).copy(alpha = 0.15f))
                                    .border(0.8.dp, Color(0xFFFF5252).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "REQUIRED CRITICAL UPDATE",
                                    color = Color(0xFFFF8A80),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // What's New Frosted Box
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF130924).copy(alpha = 0.85f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.8.dp, Color(0xFF3B1D66).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "WHAT'S NEW",
                                    color = Color(0xFFC084FC),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = updateInfo.releaseNotes.ifBlank { "✨ Performance improvements, smooth connection fixes, and luxury UI updates." },
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Download Progress or Action Controls
                        if (isDownloading) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Progress Bar with Cosmic Gradient
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFF1C0D33))
                                ) {
                                    val safeProgress = downloadProgress.coerceIn(0f, 1f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(safeProgress)
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        Color(0xFF7C3AED),
                                                        Color(0xFFA855F7),
                                                        Color(0xFFC084FC)
                                                    )
                                                )
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val percent = (downloadProgress.coerceIn(0f, 1f) * 100).toInt()
                                    Text(
                                        text = "Downloading: $percent%",
                                        color = Color(0xFFE9D5FF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    if (totalBytes > 0) {
                                        val downloadedMb = String.format(Locale.US, "%.1f", downloadedBytes / (1024f * 1024f))
                                        val totalMb = String.format(Locale.US, "%.1f", totalBytes / (1024f * 1024f))
                                        Text(
                                            text = "$downloadedMb / $totalMb MB",
                                            color = Color(0xFFA855F7),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        } else {
                            // Primary Action Button (Galaxy Violet Gradient)
                            Button(
                                onClick = { onUpdateClick(updateInfo) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF7C3AED),
                                                Color(0xFF9333EA),
                                                Color(0xFFA855F7)
                                            )
                                        ),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .border(
                                        1.dp,
                                        Color(0xFFC084FC).copy(alpha = 0.5f),
                                        RoundedCornerShape(14.dp)
                                    )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isApkCached) Icons.Default.CheckCircle else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isApkCached) "Install Update Now" else "Update Now",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }

                            // Dismiss / Later Button (Disabled if Force Update)
                            if (!updateInfo.isForceUpdate) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = {
                                        AppUpdateManager.recordDismissal(context, updateInfo.latestVersionCode)
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Remind Me Later",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
