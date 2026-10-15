package com.devil.livetogather

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

private val DarkBackground = Color(0xFF0B090A)
private val CardBackground = Color(0xFF161214)
private val CoralAccent = Color(0xFFFF5E62)
private val OrangeAccent = Color(0xFFFF9966)
private val GoldAccent = Color(0xFFFFD700)
private val BorderColor = Color(0xFF2A2226)

class ProfileSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageLoader = coil.ImageLoader.Builder(this)
            .components {
                add(DataUriFetcher.Factory(this@ProfileSetupActivity))
                add(DataUriFetcher.StringFactory(this@ProfileSetupActivity))
            }
            .build()
        coil.Coil.setImageLoader(imageLoader)

        setContent {
            ProfileSetupScreen(
                onComplete = {
                    val intent = Intent(this@ProfileSetupActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val prefs = remember { context.getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    val currentUid = auth.currentUser?.uid ?: prefs.getString("user_uid", "user_${UUID.randomUUID().toString().take(8)}") ?: ""
    val deviceId = remember {
        try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    val initialName = auth.currentUser?.displayName ?: prefs.getString("profile_name", "") ?: ""
    val initialPhoto = auth.currentUser?.photoUrl?.toString() ?: prefs.getString("profile_photo", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80") ?: ""

    var name by remember { mutableStateOf(initialName) }
    var ageText by remember { mutableStateOf(prefs.getInt("profile_age", 24).toString()) }
    var gender by remember { mutableStateOf(prefs.getString("profile_gender", "Woman") ?: "Woman") }
    var city by remember { mutableStateOf(prefs.getString("profile_city", "Kolkata") ?: "Kolkata") }
    var bio by remember { mutableStateOf(prefs.getString("profile_bio", "Love late-night coffee and spontaneous road trips 🚗") ?: "") }
    var interestedIn by remember { mutableStateOf(prefs.getString("profile_interested_in", "Everyone") ?: "Everyone") }
    var photoUrl by remember { mutableStateOf(initialPhoto) }
    var localAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var isUploadingPhoto by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            localAvatarUri = uri
            photoUrl = uri.toString()
            isUploadingPhoto = true
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val firestoreDataUri = ImageUploadManager.convertImageToFirestoreDataUri(context, uri)
                    if (!firestoreDataUri.isNullOrBlank()) {
                        withContext(Dispatchers.Main) {
                            photoUrl = firestoreDataUri
                            Toast.makeText(context, "Photo uploaded! 📸", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to compress image, using original", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        isUploadingPhoto = false
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Complete Your Profile",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Step 2: Setup your public identity",
                            color = CoralAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackground,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar Selector with Camera Badge
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(3.dp, Brush.horizontalGradient(listOf(CoralAccent, OrangeAccent)), CircleShape)
                        .padding(4.dp)
                ) {
                    AsyncImage(
                        model = localAvatarUri ?: photoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80" },
                        contentDescription = "Profile Photo",
                        imageLoader = coil.Coil.imageLoader(context),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                if (isUploadingPhoto) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CoralAccent, modifier = Modifier.size(32.dp))
                    }
                }

                IconButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd)
                        .background(CoralAccent, CircleShape)
                        .border(2.dp, DarkBackground, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change Photo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "Tap camera to upload a real photo",
                color = Color.LightGray,
                fontSize = 11.sp
            )

            // Setup Form Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        placeholder = { Text("e.g. Maya") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralAccent,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = CoralAccent,
                            unfocusedLabelColor = Color.Gray
                        )
                    )

                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("Age (18+)") },
                        placeholder = { Text("24") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralAccent,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = CoralAccent,
                            unfocusedLabelColor = Color.Gray
                        )
                    )

                    // Gender Selection
                    Column {
                        Text("I am a:", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Woman", "Man", "Non-binary").forEach { opt ->
                                val selected = gender == opt
                                Surface(
                                    color = if (selected) CoralAccent.copy(alpha = 0.2f) else DarkBackground,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (selected) CoralAccent else BorderColor),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { gender = opt }
                                ) {
                                    Text(
                                        text = opt,
                                        color = if (selected) CoralAccent else Color.LightGray,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Interested In
                    Column {
                        Text("Looking to meet:", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Women", "Men", "Everyone").forEach { opt ->
                                val selected = interestedIn == opt
                                Surface(
                                    color = if (selected) OrangeAccent.copy(alpha = 0.2f) else DarkBackground,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (selected) OrangeAccent else BorderColor),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { interestedIn = opt }
                                ) {
                                    Text(
                                        text = opt,
                                        color = if (selected) OrangeAccent else Color.LightGray,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City / Neighborhood") },
                        placeholder = { Text("Kolkata") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralAccent,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = CoralAccent,
                            unfocusedLabelColor = Color.Gray
                        )
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("About You (Bio)") },
                        placeholder = { Text("Love late night drives & deep conversations...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralAccent,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = CoralAccent,
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                }
            }

            // Complete Setup Button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val ageVal = ageText.toIntOrNull() ?: 24
                    if (ageVal < 18) {
                        Toast.makeText(context, "You must be 18+ to use LiveTogether", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (isUploadingPhoto) {
                        Toast.makeText(context, "Optimizing photo, please wait a moment...", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSaving = true
                    val effectiveUid = auth.currentUser?.uid
                        ?: prefs.getString("user_uid", null)
                        ?: currentUid.ifBlank { "user_${UUID.randomUUID().toString().take(8)}" }

                    val profile = UserProfile(
                        uid = effectiveUid,
                        deviceId = deviceId,
                        name = name.trim(),
                        age = ageVal,
                        gender = gender,
                        city = city.trim(),
                        photo = photoUrl,
                        bio = bio.trim(),
                        interestedIn = interestedIn,
                        isLocationSet = true,
                        locationName = city.trim(),
                        plan = "Free",
                        dailySwipeCount = 0,
                        lastSwipeDate = "",
                        hostTrialStartDate = System.currentTimeMillis()
                    )

                    // 1. Immediately persist to SharedPreferences (instant & offline safe)
                    saveProfileToPrefs(prefs, profile)
                    prefs.edit().putString("user_uid", effectiveUid).apply()

                    // 2. Persist to Firebase Firestore asynchronously without locking the UI
                    try {
                        db.collection("users").document(effectiveUid)
                            .set(profile, SetOptions.merge())
                            .addOnSuccessListener {
                                android.util.Log.d("ProfileSetup", "Firestore profile updated successfully")
                            }
                            .addOnFailureListener { e ->
                                android.util.Log.w("ProfileSetup", "Firestore sync postponed or failed: ${e.message}")
                            }
                    } catch (e: Exception) {
                        android.util.Log.e("ProfileSetup", "Firestore write error: ${e.message}")
                    }

                    // 3. Immediately dismiss loading, show welcome Toast, and proceed to MainActivity
                    Toast.makeText(context, "Welcome to LiveTogether, ${profile.name}! 🎉", Toast.LENGTH_SHORT).show()
                    isSaving = false
                    onComplete()
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CoralAccent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Complete Profile & Enter LiveTogether ➔",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
