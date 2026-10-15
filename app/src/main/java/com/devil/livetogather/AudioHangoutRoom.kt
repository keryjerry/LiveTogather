@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.devil.livetogather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import android.view.WindowManager
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

// -------------------------------------------------------------
// 1. CREATE CUSTOM ROOM DIALOG
// -------------------------------------------------------------
@Composable
fun CreateRoomDialog(
    currentUser: UserProfile,
    initialCategory: String = "free_tonight",
    onDismiss: () -> Unit,
    onCreate: (title: String, categoryId: String, themeUrl: String) -> Unit
) {
    var roomTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(if (initialCategory.isNotBlank()) initialCategory else CATEGORY_LIST.getOrNull(1)?.id ?: "free_tonight") }
    var selectedTheme by remember { mutableStateOf(ROOM_THEME_PRESETS[0].imageUrl) }

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎙️ Create Hangout Room",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Host your own live audio intent lounge",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Room Title Field
                Text("Room Title", color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = roomTitle,
                    onValueChange = { roomTitle = it },
                    placeholder = { Text("e.g. Saturday Rooftop Vibe Lounge", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrandCoral,
                        unfocusedBorderColor = BrandBorder,
                        focusedContainerColor = BrandDark,
                        unfocusedContainerColor = BrandDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Intent Category Selector
                Text("Select Intent Category", color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CATEGORY_LIST.filter { it.id != "all" }) { cat ->
                        val isSelected = selectedCategory == cat.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isSelected) Brush.horizontalGradient(listOf(BrandCoral, BrandOrange))
                                    else Brush.linearGradient(listOf(BrandDark, BrandDark))
                                )
                                .border(1.dp, if (isSelected) Color.Transparent else BrandBorder, RoundedCornerShape(50))
                                .clickable { selectedCategory = cat.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(cat.label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Theme Preset Selector
                Text("Choose Luxury Atmosphere Theme", color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(ROOM_THEME_PRESETS) { theme ->
                        val isSelected = selectedTheme == theme.imageUrl
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedTheme = theme.imageUrl }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp, 48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) BrandCoral else BrandBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                AsyncImage(
                                    model = theme.imageUrl,
                                    contentDescription = theme.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = theme.name,
                                color = if (isSelected) BrandCoral else Color.Gray,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(70.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Launch Room Button
                Button(
                    onClick = {
                        val finalTitle = roomTitle.trim().ifEmpty { "${currentUser.name}'s Hangout" }
                        onCreate(finalTitle, selectedCategory, selectedTheme)
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Launch Room As Host 👑", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. MUSIC VISUALIZER WAVE COMPONENT
// -------------------------------------------------------------
@Composable
fun MusicVisualizerWave(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = if (isPlaying) 22f else 6f,
        animationSpec = infiniteRepeatable(tween(380, easing = LinearEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = if (isPlaying) 8f else 6f,
        animationSpec = infiniteRepeatable(tween(290, easing = LinearEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = if (isPlaying) 26f else 6f,
        animationSpec = infiniteRepeatable(tween(440, easing = LinearEasing), RepeatMode.Reverse),
        label = "h3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = if (isPlaying) 6f else 6f,
        animationSpec = infiniteRepeatable(tween(330, easing = LinearEasing), RepeatMode.Reverse),
        label = "h4"
    )
    val h5 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = if (isPlaying) 24f else 6f,
        animationSpec = infiniteRepeatable(tween(480, easing = LinearEasing), RepeatMode.Reverse),
        label = "h5"
    )
    val h6 by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = if (isPlaying) 7f else 6f,
        animationSpec = infiniteRepeatable(tween(310, easing = LinearEasing), RepeatMode.Reverse),
        label = "h6"
    )
    val h7 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = if (isPlaying) 20f else 6f,
        animationSpec = infiniteRepeatable(tween(360, easing = LinearEasing), RepeatMode.Reverse),
        label = "h7"
    )

    val waveHeights = listOf(h1, h2, h3, h4, h5, h6, h7, h2, h5, h1, h6, h3, h7, h4)
    Row(
        modifier = modifier.height(28.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        waveHeights.forEachIndexed { idx, h ->
            val barBrush = if (isPlaying) {
                when (idx % 3) {
                    0 -> Brush.verticalGradient(listOf(BrandCoral, BrandGold))
                    1 -> Brush.verticalGradient(listOf(BrandGold, BrandOrange))
                    else -> Brush.verticalGradient(listOf(BrandCyan, BrandCoral))
                }
            } else {
                Brush.verticalGradient(listOf(Color.DarkGray, Color.Gray))
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(h.dp)
                    .clip(RoundedCornerShape(50))
                    .background(barBrush)
            )
        }
    }
}

// -------------------------------------------------------------
// 3. TRACK SELECTOR DIALOG (REAL ARTIST/SONG SEARCH & CURATED STREAMS)
// -------------------------------------------------------------
@Composable
fun TrackSelectorDialog(
    onDismiss: () -> Unit,
    onSelectTrack: (title: String, artist: String, category: String, url: String, artworkUrl: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val searchResults = remember { mutableStateListOf<HangoutTrack>() }
    var selectedGenre by remember { mutableStateOf("All") }
    val genreList = listOf("All", "Lo-Fi Chill", "Acoustic Romantic", "Cafe Jazz", "Party Beats", "EDM Club")

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Debounced search query handler
    LaunchedEffect(searchQuery) {
        val trimmed = searchQuery.trim()
        if (trimmed.isEmpty()) {
            searchResults.clear()
            isSearching = false
        } else {
            delay(400)
            isSearching = true
            val results = MusicSearchEngine.searchTracks(trimmed)
            searchResults.clear()
            searchResults.addAll(results)
            isSearching = false
        }
    }

    val filteredTracks = remember(selectedGenre) {
        if (selectedGenre == "All") CURATED_HANGOUT_TRACKS
        else CURATED_HANGOUT_TRACKS.filter { it.category == selectedGenre }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Local Audio Track"
            onSelectTrack(fileName, "Device Storage", "Local MP3", uri.toString(), "")
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BrandBorder, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎵 Global Music Engine", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Text(
                    "Search any artist or song worldwide. Real streams synced in real-time.",
                    color = Color.Gray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar for Any Artist / Song
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search artist or song (e.g. Arijit, Diljit, Drake)...", color = Color.Gray, fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = BrandGold, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrandGold,
                        unfocusedBorderColor = BrandBorder,
                        focusedContainerColor = BrandDark,
                        unfocusedContainerColor = BrandDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (searchQuery.isNotBlank()) {
                    // Search Results View
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSearching) "Searching streams..." else "Found ${searchResults.size} tracks:",
                            color = BrandGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isSearching) {
                            CircularProgressIndicator(color = BrandCoral, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (!isSearching && searchResults.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tracks found for \"$searchQuery\". Try another artist name.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults) { track ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BrandDark)
                                        .border(0.5.dp, BrandBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            onSelectTrack(track.title, track.artist, track.category, track.url, track.artworkUrl)
                                            onDismiss()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        if (track.artworkUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = track.artworkUrl,
                                                contentDescription = track.title,
                                                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(6.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(6.dp)).background(BrandCard),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = BrandGold, modifier = Modifier.size(20.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(track.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            val durLabel = if (track.durationSec > 0) " • ⏳ ${track.durationSec / 60}:${String.format("%02d", track.durationSec % 60)}" else ""
                                            Text("👤 ${track.artist} • 🏷️ ${track.category}$durLabel", color = Color.Gray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    val isTrackPlaying = AudioPlayerManager.isPlayingState.value && AudioPlayerManager.currentTrackUrlState.value == track.url
                                    Box(
                                        modifier = Modifier.size(32.dp).clip(CircleShape).background(if (isTrackPlaying) BrandGreen else BrandCoral),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isTrackPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isTrackPlaying) "Pause" else "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Default Curated Stations View
                    // Music Genre Filter Pills
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(genreList) { genre ->
                            val isSelected = selectedGenre == genre
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isSelected) Brush.horizontalGradient(listOf(BrandCoral, BrandOrange))
                                        else Brush.linearGradient(listOf(BrandDark, BrandDark))
                                    )
                                    .border(1.dp, if (isSelected) Color.Transparent else BrandBorder, RoundedCornerShape(50))
                                    .clickable { selectedGenre = genre }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(genre, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Pick Local File Button
                    OutlinedButton(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandCyan),
                        border = BorderStroke(1.dp, BrandCyan.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select Audio from Device (.mp3)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BrandBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Curated Stations (${filteredTracks.size}):", color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTracks) { track ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BrandDark)
                                    .clickable {
                                        onSelectTrack(track.title, track.artist, track.category, track.url, track.artworkUrl)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    if (track.artworkUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = track.artworkUrl,
                                            contentDescription = track.title,
                                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)).background(BrandCard),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = BrandGold, modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(track.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("👤 ${track.artist} • 🏷️ ${track.category}", color = Color.Gray, fontSize = 10.sp)
                                    }
                                }

                                val isTrackPlaying = AudioPlayerManager.isPlayingState.value && AudioPlayerManager.currentTrackUrlState.value == track.url
                                Box(
                                    modifier = Modifier.size(28.dp).clip(CircleShape).background(if (isTrackPlaying) BrandGreen else BrandCoral),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isTrackPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isTrackPlaying) "Pause" else "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
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

// -------------------------------------------------------------
// 4. SEND MESSAGE REQUEST DIALOG (FOR STANDARD / FREE USERS)
// -------------------------------------------------------------
@Composable
fun SendMessageRequestDialog(
    targetUser: RoomMember,
    onDismiss: () -> Unit,
    onSend: (message: String) -> Unit
) {
    var messageText by remember { mutableStateOf("Hey! Would love to connect and chat!") }
    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BrandBorder, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "💬 Message Request to ${targetUser.name}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Standard members can send a message request. Once accepted, 1-on-1 direct chat unlocks!",
                    color = Color.Gray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Write your message...", color = Color.Gray, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrandCoral,
                        unfocusedBorderColor = BrandBorder,
                        focusedContainerColor = BrandDark,
                        unfocusedContainerColor = BrandDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSend(messageText.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Send Request 🚀", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4B. ALL ROOM VIEWERS & MODERATION DIALOG
// -------------------------------------------------------------
@Composable
fun AllRoomViewersDialog(
    viewers: List<RoomMember>,
    currentUser: UserProfile,
    isHost: Boolean,
    onSelectMember: (RoomMember) -> Unit,
    onKickOut: (RoomMember) -> Unit,
    onBan: (RoomMember) -> Unit,
    onDismiss: () -> Unit
) {
    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .border(1.dp, BrandBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "👥 Active Viewers (${viewers.size})",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real-time listeners in this audio room",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (viewers.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active listeners yet.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewers, key = { it.uid }) { member ->
                            val isVip = member.tier.contains("VIP", true)
                            val isPlat = member.tier.contains("Platinum", true)
                            val isSelf = member.uid == currentUser.uid

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelf) BrandCardSubtle else BrandDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectMember(member)
                                        onDismiss()
                                    }
                                    .border(
                                        0.5.dp,
                                        if (isSelf) BrandCoral.copy(alpha = 0.5f) else BrandBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    1.5.dp,
                                                    if (isVip) BrandGold else if (isPlat) BrandCyan else BrandCoral,
                                                    CircleShape
                                                )
                                        ) {
                                            AsyncImage(
                                                model = member.photoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                                contentDescription = member.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = if (isSelf) "${member.name} (You)" else member.name,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (isVip) "★ VIP Member" else if (isPlat) "♦ Platinum" else "Listener",
                                                color = if (isVip) BrandGold else if (isPlat) BrandCyan else Color.Gray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = BrandCard,
                                            shape = RoundedCornerShape(50),
                                            border = BorderStroke(0.5.dp, BrandBorder),
                                            modifier = Modifier.clickable {
                                                onSelectMember(member)
                                                onDismiss()
                                            }
                                        ) {
                                            Text("Profile", color = BrandGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                        }

                                        if (isHost && !isSelf) {
                                            OutlinedButton(
                                                onClick = { onKickOut(member) },
                                                border = BorderStroke(1.dp, Color.Yellow),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                                                modifier = Modifier.height(24.dp)
                                            ) {
                                                Text("Kick", color = Color.Yellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { onBan(member) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                                                modifier = Modifier.height(24.dp)
                                            ) {
                                                Text("Ban", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. MEMBER PROFILE SHEET / DIALOG
// -------------------------------------------------------------
@Composable
fun RoomMemberProfileSheet(
    member: RoomMember,
    userProfile: UserProfile?,
    currentUser: UserProfile,
    isHost: Boolean,
    roomId: String,
    db: FirebaseFirestore,
    onDismiss: () -> Unit,
    onLeaveRoom: () -> Unit,
    onLike: () -> Unit,
    onDirectMessage: () -> Unit,
    onRequestMessage: () -> Unit,
    onKickOut: (RoomMember) -> Unit,
    onBan: (RoomMember) -> Unit,
    onBlock: (RoomMember) -> Unit,
    onTagMember: (String) -> Unit
) {
    val context = LocalContext.current
    val isVipOrPlatinum = currentUser.isVipOrPlatinum()
    val isMemberVip = member.tier.contains("VIP", ignoreCase = true)
    val isMemberPlat = member.tier.contains("Platinum", ignoreCase = true)
    val isSelf = member.uid == currentUser.uid

    var isFollowingMember by remember { mutableStateOf(false) }
    var memberFollowerCount by remember { mutableStateOf(userProfile?.followerCount ?: 0) }

    DisposableEffect(member.uid, currentUser.uid) {
        val targetUid = member.uid.ifEmpty { currentUser.uid }
        if (targetUid.isBlank()) {
            return@DisposableEffect onDispose {}
        }
        val countDoc = db.collection("users").document(targetUid)
            .collection("followers")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    memberFollowerCount = snapshot.size()
                }
            }
        val followDoc = if (!isSelf && member.uid.isNotBlank()) {
            db.collection("users").document(member.uid)
                .collection("followers").document(currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    isFollowingMember = snapshot != null && snapshot.exists()
                }
        } else null

        onDispose {
            countDoc.remove()
            followDoc?.remove()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isSelf) "Your Profile in Room" else "User Profile & Actions",
                        color = BrandCoral,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Member Photo with Tier Ring
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                if (isMemberVip) listOf(BrandGold, BrandCoral, BrandGold)
                                else if (isMemberPlat) listOf(BrandCyan, BrandCoral, BrandCyan)
                                else listOf(BrandCoral, BrandOrange)
                            ),
                            shape = CircleShape
                        )
                ) {
                    AsyncImage(
                        model = member.photoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                        contentDescription = member.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Name & Age
                val ageText = userProfile?.age?.let { ", $it" } ?: ""
                Text(
                    text = "${if (isSelf) "You (${member.name})" else member.name}$ageText",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                // Role & Tier Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (member.role == "host" || isHost && isSelf) {
                        Text(
                            text = "👑 Room Host",
                            color = BrandGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(BrandGold.copy(alpha = 0.2f), RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = if (isMemberVip) "VIP LEVEL ★" else if (isMemberPlat) "PLATINUM" else "Standard Member",
                        color = if (isMemberVip) BrandGold else if (isMemberPlat) BrandCyan else Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .border(0.5.dp, BrandBorder, RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                // City & Bio with Real Distance
                val locationStr = userProfile?.locationName?.ifBlank { userProfile.city } ?: userProfile?.city ?: "Kolkata"
                val distKm = if (userProfile != null && currentUser.latitude != 0.0 && userProfile.latitude != 0.0) {
                    calculateDistanceKm(currentUser.latitude, currentUser.longitude, userProfile.latitude, userProfile.longitude)
                } else 0
                val distText = if (distKm > 0) " • $distKm km away" else ""
                Text(
                    text = "📍 $locationStr$distText • 👥 $memberFollowerCount Followers",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                val bioText = userProfile?.bio?.ifEmpty { "Excited to meet and hangout with awesome people!" }
                    ?: "Excited to meet and hangout with awesome people!"
                Text(
                    text = "\"$bioText\"",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subscription Level Perks Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMemberVip) Color(0xFF2C2210) else if (isMemberPlat) Color(0xFF102830) else BrandCardSubtle
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isMemberVip) BrandGold.copy(alpha = 0.6f) else if (isMemberPlat) BrandCyan.copy(alpha = 0.6f) else BrandBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isMemberVip) "👑 VIP LEVEL BENEFITS" else if (isMemberPlat) "♦ PLATINUM BENEFITS" else "🌟 STANDARD BENEFITS",
                            color = if (isMemberVip) BrandGold else if (isMemberPlat) BrandCyan else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isMemberVip) "• Direct 1-on-1 Chats • Priority Stage Mic • Golden Halo Crown"
                            else if (isMemberPlat) "• Profile Boost • Cyan Halo Ring • Extended Reach"
                            else "• Message Requests • Public Room Audio & Chat Access",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 1. Follow / Unfollow interactive button (Always Available for All Profiles)
                Button(
                    onClick = {
                        val newFollow = !isFollowingMember
                        isFollowingMember = newFollow
                        if (newFollow) {
                            memberFollowerCount += 1
                            if (member.uid.isNotBlank() && member.uid != currentUser.uid) {
                                val record = FollowerRecord(
                                    uid = currentUser.uid,
                                    name = currentUser.name,
                                    photo = currentUser.photo,
                                    timestamp = System.currentTimeMillis()
                                )
                                db.collection("users").document(member.uid)
                                    .collection("followers").document(currentUser.uid).set(record)
                                db.collection("users").document(member.uid)
                                    .update("followerCount", FieldValue.increment(1))
                                db.collection("users").document(currentUser.uid)
                                    .update("followingCount", FieldValue.increment(1))
                                sendNotification(
                                    db = db,
                                    targetUserId = member.uid,
                                    sender = currentUser,
                                    type = "follow",
                                    title = "🌟 New Follower!",
                                    message = "${currentUser.name} started following you!"
                                )
                            } else {
                                db.collection("users").document(currentUser.uid)
                                    .update("followerCount", FieldValue.increment(1))
                            }
                            Toast.makeText(context, "🌟 Following ${member.name}! ($memberFollowerCount)", Toast.LENGTH_SHORT).show()
                        } else {
                            memberFollowerCount = (memberFollowerCount - 1).coerceAtLeast(0)
                            if (member.uid.isNotBlank() && member.uid != currentUser.uid) {
                                db.collection("users").document(member.uid)
                                    .collection("followers").document(currentUser.uid).delete()
                                db.collection("users").document(member.uid)
                                    .update("followerCount", FieldValue.increment(-1))
                                db.collection("users").document(currentUser.uid)
                                    .update("followingCount", FieldValue.increment(-1))
                            } else {
                                db.collection("users").document(currentUser.uid)
                                    .update("followerCount", FieldValue.increment(-1))
                            }
                            Toast.makeText(context, "Unfollowed ${member.name}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowingMember) BrandDark else BrandCoral
                    ),
                    border = BorderStroke(1.dp, if (isFollowingMember) BrandGold else Color.Transparent),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFollowingMember) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isFollowingMember) BrandGold else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFollowingMember) "✓ Following ($memberFollowerCount)" else "+ Follow ($memberFollowerCount)",
                        color = if (isFollowingMember) BrandGold else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!isSelf) {
                    // Action Buttons: Like & Chat
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onLike()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Like / Match", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onDismiss()
                                if (isVipOrPlatinum) {
                                    onDirectMessage()
                                } else {
                                    onRequestMessage()
                                }
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isVipOrPlatinum) BrandCyan else BrandCardSubtle
                            ),
                            shape = RoundedCornerShape(50),
                            border = if (!isVipOrPlatinum) BorderStroke(1.dp, BrandCoral.copy(alpha = 0.5f)) else null
                        ) {
                            Icon(
                                imageVector = if (isVipOrPlatinum) Icons.Default.Chat else Icons.Default.Send,
                                contentDescription = null,
                                tint = if (isVipOrPlatinum) Color.Black else BrandCoral,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isVipOrPlatinum) "Send Message" else "Msg Request",
                                color = if (isVipOrPlatinum) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            onTagMember(member.name)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, BrandCoral.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("@", color = BrandCoral, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tag @${member.name.substringBefore(' ')} in Room Chat", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (isHost) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("👑 HOST MODERATION CONTROLS", color = BrandGold, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            onKickOut(member)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("👢 Kick Out", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            onBan(member)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF210000)),
                                        border = BorderStroke(1.dp, Color.Red),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("🔨 Ban User", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            onBlock(member)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().height(32.dp)
                    ) {
                        Text("🚫 Block ${member.name.substringBefore(' ')}", color = Color.Gray, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        onDismiss()
                        onLeaveRoom()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🚪 Leave Room", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HELPER: BROADCAST LIVE NOTIFICATION TO ALL FOLLOWERS
// -------------------------------------------------------------
fun broadcastLiveNotificationToFollowers(
    db: FirebaseFirestore,
    hostId: String,
    hostName: String,
    hostPhoto: String,
    room: RoomItem
) {
    if (hostId.isBlank()) return
    db.collection("users").document(hostId).collection("followers").get()
        .addOnSuccessListener { followersSnapshot ->
            if (followersSnapshot != null && !followersSnapshot.isEmpty) {
                for (doc in followersSnapshot.documents) {
                    val followerId = doc.getString("uid") ?: doc.id
                    if (followerId.isNotBlank() && followerId != hostId) {
                        val notifId = UUID.randomUUID().toString()
                        val notif = NotificationItem(
                            id = notifId,
                            targetUserId = followerId,
                            senderId = hostId,
                            senderName = hostName,
                            senderPhoto = hostPhoto,
                            title = "🔴 $hostName is LIVE NOW!",
                            message = "$hostName just went LIVE in '${room.title}'! Tap to join.",
                            type = "host_live",
                            timestamp = System.currentTimeMillis(),
                            isRead = false
                        )
                        db.collection("users").document(followerId)
                            .collection("notifications").document(notifId).set(notif)
                        db.collection("notifications").document(notifId).set(notif)
                    }
                }
            }
        }
}

// -------------------------------------------------------------
// ROOM SETTINGS & CONTROLS DIALOG (PRIVACY, SCHEDULE, THEME, MODERATION)
// -------------------------------------------------------------
@Composable
fun RoomSettingsDialog(
    room: RoomItem,
    isHost: Boolean,
    activeMembers: List<RoomMember>,
    db: FirebaseFirestore,
    onDismiss: () -> Unit,
    onUpdateTheme: (String) -> Unit,
    onKickMember: ((RoomMember) -> Unit)? = null,
    onBanMember: ((RoomMember) -> Unit)? = null
) {
    val context = LocalContext.current
    var scheduledTimeText by remember { mutableStateOf(room.scheduledTime) }
    var selectedPrivacy by remember { mutableStateOf(room.privacy) }
    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val coroutineScope = rememberCoroutineScope()
    var isUploadingTheme by remember { mutableStateOf(false) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingTheme = true
            coroutineScope.launch {
                val themeUrl = ImageUploadManager.uploadImage(context, uri, "rooms") ?: uri.toString()
                onUpdateTheme(themeUrl)
                db.collection("rooms").document(room.id).update("banner_image_url", themeUrl)
                Toast.makeText(context, "Theme updated from gallery!", Toast.LENGTH_SHORT).show()
                isUploadingTheme = false
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .border(1.dp, BrandBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚙️ Room Settings & Controls", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. LIVE / OFFLINE BROADCAST STATUS
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BrandCardSubtle),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🔴 Live Broadcast Status", color = BrandCoral, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            db.collection("rooms").document(room.id).update(
                                                mapOf(
                                                    "isHostLive" to true,
                                                    "statusMessage" to "LIVE NOW"
                                                )
                                            )
                                            // Broadcast live notification to all followers
                                            broadcastLiveNotificationToFollowers(
                                                db = db,
                                                hostId = room.hostId,
                                                hostName = room.hostName,
                                                hostPhoto = room.hostPhotoUrl,
                                                room = room
                                            )
                                            Toast.makeText(context, "Room is now LIVE! Followers notified.", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (room.isHostLive) BrandGreen else BrandDark
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("🔴 Go Live", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            db.collection("rooms").document(room.id).update(
                                                mapOf(
                                                    "isHostLive" to false,
                                                    "statusMessage" to "Host will be live soon"
                                                )
                                            )
                                            Toast.makeText(context, "Room status set to 'Host will be live soon'", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!room.isHostLive) BrandGold else BrandDark
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("⏳ Set Offline", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!room.isHostLive) Color.Black else Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // 2. SCHEDULE TIMING
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BrandCardSubtle),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("⏰ Live Timing Schedule", color = BrandGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Notify users when you will be live next (e.g. Tonight 9:00 PM)", color = Color.Gray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = scheduledTimeText,
                                        onValueChange = { scheduledTimeText = it },
                                        placeholder = { Text("e.g. Tonight 9:30 PM", color = Color.Gray, fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = BrandGold,
                                            unfocusedBorderColor = BrandBorder,
                                            focusedContainerColor = BrandDark,
                                            unfocusedContainerColor = BrandDark
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            db.collection("rooms").document(room.id).update("scheduledTime", scheduledTimeText.trim())
                                            Toast.makeText(context, "Schedule timing saved!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Save", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 3. ROOM PRIVACY CONTROLS
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BrandCardSubtle),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🔒 Room Access & Privacy", color = BrandCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                listOf(
                                    "public" to "🌐 Public (Open to everyone)",
                                    "vip_only" to "⭐ VIP & Platinum Only",
                                    "private" to "🔒 Invite Only"
                                ).forEach { (key, label) ->
                                    val isSelected = selectedPrivacy == key
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) BrandCyan.copy(alpha = 0.15f) else Color.Transparent)
                                            .clickable {
                                                selectedPrivacy = key
                                                db.collection("rooms").document(room.id).update("privacy", key)
                                                Toast.makeText(context, "Privacy set to: $label", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(vertical = 8.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                selectedPrivacy = key
                                                db.collection("rooms").document(room.id).update("privacy", key)
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = BrandCyan)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(label, color = if (isSelected) Color.White else Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }

                    // 4. ROOM THEME CUSTOMIZER (LOCAL STORAGE + PRESETS)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BrandCardSubtle),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🎨 Room Theme & Background", color = BrandOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Button(
                                        onClick = { imagePickerLauncher.launch("image/*") },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                                        shape = RoundedCornerShape(50),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pick from Phone", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(ROOM_THEME_PRESETS) { preset ->
                                        val isCurrent = room.banner_image_url == preset.imageUrl
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable {
                                                onUpdateTheme(preset.imageUrl)
                                                db.collection("rooms").document(room.id).update("banner_image_url", preset.imageUrl)
                                                Toast.makeText(context, "Theme set to: ${preset.name}", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            AsyncImage(
                                                model = preset.imageUrl,
                                                contentDescription = preset.name,
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .border(
                                                        2.dp,
                                                        if (isCurrent) BrandCoral else Color.Transparent,
                                                        RoundedCornerShape(10.dp)
                                                    ),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = preset.name.take(10),
                                                color = if (isCurrent) BrandCoral else Color.Gray,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. MEMBER MODERATION (KICK & BAN)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BrandCardSubtle),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🛡️ Room Member Moderation", color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Manage active members in your room", color = Color.Gray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(10.dp))

                                val nonHostMembers = activeMembers.filter { it.uid != room.hostId }
                                if (nonHostMembers.isEmpty()) {
                                    Text("No other members currently in this room.", color = Color.Gray, fontSize = 11.sp)
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        nonHostMembers.forEach { member ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(BrandDark, RoundedCornerShape(10.dp))
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    AsyncImage(
                                                        model = member.photoUrl,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(32.dp).clip(CircleShape),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(member.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        Text(member.tier, color = BrandGold, fontSize = 9.sp)
                                                    }
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    // Kick Button
                                                    OutlinedButton(
                                                        onClick = {
                                                            onKickMember?.invoke(member)
                                                            db.collection("rooms").document(room.id)
                                                                .collection("members").document(member.uid).delete()
                                                            db.collection("rooms").document(room.id)
                                                                .update("kickedUserIds", FieldValue.arrayUnion(member.uid))
                                                            Toast.makeText(context, "${member.name} kicked out.", Toast.LENGTH_SHORT).show()
                                                        },
                                                        border = BorderStroke(1.dp, Color.Yellow),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(26.dp)
                                                    ) {
                                                        Text("Kick", color = Color.Yellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }

                                                    // Ban Button
                                                    Button(
                                                        onClick = {
                                                            onBanMember?.invoke(member)
                                                            db.collection("rooms").document(room.id)
                                                                .collection("members").document(member.uid).delete()
                                                            db.collection("rooms").document(room.id)
                                                                .update("bannedUserIds", FieldValue.arrayUnion(member.uid))
                                                            Toast.makeText(context, "${member.name} banned.", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(26.dp)
                                                    ) {
                                                        Text("Ban", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. CLEAR ROOM CHAT (HOST ONLY)
                    if (isHost) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BrandCardSubtle),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("🗑️ Clear Room Chat", color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Permanently delete all live messages from this room in real-time.", color = Color.Gray, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            db.collection("rooms").document(room.id)
                                                .collection("live_comments")
                                                .get()
                                                .addOnSuccessListener { snapshot ->
                                                    val batch = db.batch()
                                                    for (doc in snapshot.documents) {
                                                        batch.delete(doc.reference)
                                                    }
                                                    batch.commit().addOnSuccessListener {
                                                        Toast.makeText(context, "Room chat cleared successfully!", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.85f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Clear All Messages", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. ADVANCED AUDIO HANGOUT ROOM MAIN SCREEN / DIALOG
// -------------------------------------------------------------
@Composable
fun AudioHangoutRoomDialog(
    initialRoom: RoomItem,
    currentUser: UserProfile,
    allProfiles: List<UserProfile>,
    db: FirebaseFirestore,
    onDismiss: () -> Unit,
    onDirectChat: (UserProfile) -> Unit
) {
    val context = LocalContext.current

    // Live Room State from Firestore
    var room by remember { mutableStateOf(initialRoom) }
    val activeMembers = remember { mutableStateListOf<RoomMember>() }

    // Modals
    var showTrackSelector by remember { mutableStateOf(false) }
    var showRoomSettingsDialog by remember { mutableStateOf(false) }
    var showAllViewersDialog by remember { mutableStateOf(false) }
    var isFollowingHost by remember { mutableStateOf(false) }
    var showFollowCelebrationDialog by remember { mutableStateOf(false) }
    var hostFollowerCount by remember { mutableStateOf(0) }
    var selectedMemberForSheet by remember { mutableStateOf<RoomMember?>(null) }
    var selectedMemberForRequest by remember { mutableStateOf<RoomMember?>(null) }
    var entranceNotificationUser by remember { mutableStateOf<String?>(null) }
    val knownMemberIds = remember { mutableStateListOf<String>() }

    // 5-Mic Live Stage States
    val micSeats = remember { mutableStateListOf<MicSeat>() }
    var selectedSeatForAdmin by remember { mutableStateOf<MicSeat?>(null) }
    var showInviteListenersDialog by remember { mutableStateOf<MicSeat?>(null) }
    var selectedOwnSeatForControl by remember { mutableStateOf<MicSeat?>(null) }
    var isLocalTalking by remember { mutableStateOf(false) }
    var pendingSeatToJoin by remember { mutableStateOf<MicSeat?>(null) }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingSeatToJoin?.let { seat ->
                joinMicSeat(db, room.id, seat.seatIndex, currentUser, micSeats)
                Toast.makeText(context, "You are now on Mic ${seat.seatIndex + 1}! 🎙️", Toast.LENGTH_SHORT).show()
                pendingSeatToJoin = null
            }
        } else {
            Toast.makeText(context, "Microphone permission is required to speak on stage", Toast.LENGTH_SHORT).show()
            pendingSeatToJoin = null
        }
    }

    LaunchedEffect(activeMembers.size) {
        if (knownMemberIds.isEmpty()) {
            knownMemberIds.addAll(activeMembers.map { it.uid })
        } else {
            val newArrivals = activeMembers.filter { !knownMemberIds.contains(it.uid) && it.uid != currentUser.uid }
            if (newArrivals.isNotEmpty()) {
                val newest = newArrivals.last()
                entranceNotificationUser = newest.name
                knownMemberIds.addAll(newArrivals.map { it.uid })
                delay(3500)
                entranceNotificationUser = null
            }
        }
    }

    val isUserHost = currentUser.uid == room.hostId || (room.hostId.isEmpty() && activeMembers.firstOrNull()?.uid == currentUser.uid)
    val isMuted by AudioPlayerManager.isMutedState
    val localPlaying by AudioPlayerManager.isPlayingState

    // Kick & Ban Auto-Dismissal Enforcement
    LaunchedEffect(room.bannedUserIds, room.kickedUserIds) {
        if (room.bannedUserIds.contains(currentUser.uid)) {
            Toast.makeText(context, "You have been banned from this room by the host.", Toast.LENGTH_LONG).show()
            onDismiss()
        } else if (room.kickedUserIds.contains(currentUser.uid)) {
            Toast.makeText(context, "You have been kicked out of this room.", Toast.LENGTH_LONG).show()
            onDismiss()
        }
    }

    val currentMember = remember(currentUser.uid, room.id, isUserHost) {
        RoomMember(
            uid = currentUser.uid,
            name = currentUser.name,
            photoUrl = currentUser.photo,
            joinedAt = System.currentTimeMillis(),
            role = if (isUserHost) "host" else "listener",
            tier = if (currentUser.isVipOrPlatinum()) {
                if (currentUser.plan.contains("VIP", ignoreCase = true)) "VIP" else "Platinum"
            } else "Standard"
        )
    }

    // Immediately guarantee currentUser is present locally in activeMembers
    LaunchedEffect(currentMember) {
        if (activeMembers.none { it.uid == currentUser.uid }) {
            activeMembers.add(currentMember)
        }
    }

    // Ensure current user is registered in rooms/{roomId}/members in Firestore and deleted on exit
    DisposableEffect(room.id, currentUser.uid) {
        db.collection("rooms").document(room.id)
            .collection("members").document(currentUser.uid).set(currentMember)

        onDispose {
            db.collection("rooms").document(room.id)
                .collection("members").document(currentUser.uid).delete()
        }
    }

    val targetHostId = room.hostId.ifEmpty { activeMembers.firstOrNull { it.role == "host" }?.uid ?: if (isUserHost) currentUser.uid else "" }
    val hostMember = activeMembers.find { it.uid == targetHostId || it.role == "host" }
    val hostFromProfiles = allProfiles.find { it.uid == targetHostId }
    val hostName = room.hostName.ifBlank { hostMember?.name ?: hostFromProfiles?.name ?: if (isUserHost) currentUser.name else "Host" }
    val hostPhoto = room.hostPhotoUrl.ifBlank { "" }
        .ifEmpty { hostMember?.photoUrl.orEmpty() }
        .ifEmpty { hostFromProfiles?.photo.orEmpty() }
        .ifEmpty { if (isUserHost) currentUser.photo else "" }
        .ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80" }

    val filteredActiveAudience = activeMembers.filter {
        it.uid != targetHostId && it.uid !in room.kickedUserIds && it.uid !in room.bannedUserIds
    }
    val displayAudience = filteredActiveAudience
    val totalDisplayCount = (if (hostName.isNotBlank() || targetHostId.isNotBlank()) 1 else 0) + displayAudience.size

    // Synchronize host photo and name to room document if not yet saved in Firestore
    LaunchedEffect(room.id, hostPhoto, isUserHost) {
        if (room.hostPhotoUrl.isBlank() && hostPhoto.isNotBlank() && isUserHost) {
            db.collection("rooms").document(room.id).update(
                mapOf(
                    "hostPhotoUrl" to hostPhoto,
                    "hostName" to hostName
                )
            )
        }
    }

    // Host Follower Real-Time Listener
    DisposableEffect(targetHostId, currentUser.uid) {
        if (targetHostId.isBlank() || targetHostId == currentUser.uid) {
            return@DisposableEffect onDispose {}
        }
        val followDoc = db.collection("users").document(targetHostId)
            .collection("followers").document(currentUser.uid)
            .addSnapshotListener { snapshot, _ ->
                isFollowingHost = snapshot != null && snapshot.exists()
            }
        val countListener = db.collection("users").document(targetHostId)
            .collection("followers")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    hostFollowerCount = snapshot.size()
                }
            }
        onDispose {
            followDoc.remove()
            countListener.remove()
        }
    }

    // 1. Real-Time Room Metadata Listener (Rooms/{roomId})
    DisposableEffect(room.id) {
        val roomListener = db.collection("rooms").document(room.id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    snapshot.toObject(RoomItem::class.java)?.let { updated ->
                        room = updated

                        // Audio Sync: If room has track and is playing
                        if (updated.isPlaying && updated.nowPlayingUrl.isNotBlank()) {
                            if (AudioPlayerManager.currentTrackUrlState.value != updated.nowPlayingUrl || !AudioPlayerManager.isPlayingState.value) {
                                AudioPlayerManager.play(context, updated.nowPlayingUrl, updated.nowPlayingTitle)
                            }
                        } else if (!updated.isPlaying && AudioPlayerManager.isPlayingState.value) {
                            AudioPlayerManager.pause()
                        }
                    }
                }
            }

        onDispose {
            roomListener.remove()
        }
    }

    // 2. Real-Time Members Presence Listener (Rooms/{roomId}/members)
    DisposableEffect(room.id) {
        val membersListener = db.collection("rooms").document(room.id)
            .collection("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(RoomMember::class.java) }
                    activeMembers.clear()
                    activeMembers.addAll(list)
                    if (activeMembers.none { it.uid == currentUser.uid }) {
                        activeMembers.add(currentMember)
                    }
                }
            }

        onDispose {
            membersListener.remove()
        }
    }

    var commentInputText by remember { mutableStateOf("") }
    val liveComments = remember { mutableStateListOf<LiveRoomComment>() }

    // 3. Real-Time Live Comments Listener (Rooms/{roomId}/live_comments)
    DisposableEffect(room.id) {
        val commentsListener = db.collection("rooms").document(room.id)
            .collection("live_comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limitToLast(25)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(LiveRoomComment::class.java) }
                    liveComments.clear()
                    liveComments.addAll(list)
                }
            }
        onDispose {
            commentsListener.remove()
        }
    }

    // 4. Real-Time 5-Mic Seats Listener (rooms/{roomId}/mic_seats)
    DisposableEffect(room.id, isUserHost, targetHostId, hostName, hostPhoto) {
        val micSeatsRef = db.collection("rooms").document(room.id).collection("mic_seats")
        val micListener = micSeatsRef.orderBy("seatIndex", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    if (snapshot.isEmpty) {
                        // Seed default 5 seats in Firestore
                        val defaultSeats = listOf(
                            MicSeat(seatIndex = 0, label = "Host 👑", uid = targetHostId, name = hostName, photo = hostPhoto, role = "host"),
                            MicSeat(seatIndex = 1, label = "Mic 2", uid = "", name = "", photo = "", role = "speaker"),
                            MicSeat(seatIndex = 2, label = "Mic 3", uid = "", name = "", photo = "", role = "speaker"),
                            MicSeat(seatIndex = 3, label = "Mic 4", uid = "", name = "", photo = "", role = "speaker"),
                            MicSeat(seatIndex = 4, label = "Mic 5", uid = "", name = "", photo = "", role = "speaker")
                        )
                        defaultSeats.forEach { s ->
                            micSeatsRef.document(s.seatIndex.toString()).set(s)
                        }
                    } else {
                        val seats = snapshot.documents.mapNotNull { doc ->
                            val s = doc.toObject(MicSeat::class.java)
                            if (s != null) {
                                val rawMuted = doc.getBoolean("isMuted") ?: doc.getBoolean("muted") ?: s.isMuted
                                val rawLocked = doc.getBoolean("isLocked") ?: doc.getBoolean("locked") ?: s.isLocked
                                val rawTalking = doc.getBoolean("isTalking") ?: doc.getBoolean("talking") ?: s.isTalking
                                s.copy(isMuted = rawMuted, isLocked = rawLocked, isTalking = rawTalking)
                            } else null
                        }
                        val fullList = (0..4).map { idx ->
                            val existing = seats.find { it.seatIndex == idx }
                            if (existing != null) {
                                if (idx == 0 && existing.uid.isEmpty() && targetHostId.isNotEmpty()) {
                                    existing.copy(uid = targetHostId, name = hostName, photo = hostPhoto, role = "host", label = "Host 👑")
                                } else {
                                    existing
                                }
                            } else {
                                if (idx == 0) {
                                    MicSeat(seatIndex = 0, label = "Host 👑", uid = targetHostId, name = hostName, photo = hostPhoto, role = "host")
                                } else {
                                    MicSeat(seatIndex = idx, label = "Mic ${idx + 1}", uid = "", name = "", photo = "", role = "speaker")
                                }
                            }
                        }
                        micSeats.clear()
                        micSeats.addAll(fullList)
                    }
                }
            }

        onDispose {
            micListener.remove()
            val mySeat = micSeats.find { it.uid == currentUser.uid && it.role != "host" }
            if (mySeat != null) {
                micSeatsRef.document(mySeat.seatIndex.toString()).set(
                    MicSeat(
                        seatIndex = mySeat.seatIndex,
                        label = "Mic ${mySeat.seatIndex + 1}",
                        uid = "",
                        name = "",
                        photo = "",
                        isMuted = false,
                        isLocked = mySeat.isLocked,
                        isTalking = false,
                        role = "speaker"
                    )
                )
            }
        }
    }

    // Host Seat 0 Auto-Assignment
    LaunchedEffect(isUserHost, targetHostId, micSeats.size) {
        if (isUserHost && micSeats.isNotEmpty()) {
            val seat0 = micSeats.firstOrNull { it.seatIndex == 0 }
            if (seat0 != null && (seat0.uid != currentUser.uid || seat0.name != currentUser.name || seat0.photo != currentUser.photo)) {
                db.collection("rooms").document(room.id).collection("mic_seats").document("0")
                    .set(
                        MicSeat(
                            seatIndex = 0,
                            label = "Host 👑",
                            uid = currentUser.uid,
                            name = currentUser.name,
                            photo = currentUser.photo,
                            isMuted = seat0.isMuted,
                            isLocked = false,
                            isTalking = false,
                            role = "host"
                        )
                    )
            }
        }
    }

    // AudioRecord amplitude speech capture when current user is seated and unmuted
    val myCurrentMicSeat = micSeats.find { it.uid == currentUser.uid }
    val isCurrentUserActiveSpeaker = myCurrentMicSeat != null && !myCurrentMicSeat.isMuted

    LaunchedEffect(isCurrentUserActiveSpeaker) {
        if (!isCurrentUserActiveSpeaker) {
            isLocalTalking = false
            return@LaunchedEffect
        }

        val hasAudioPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!hasAudioPerm) {
            isLocalTalking = false
            return@LaunchedEffect
        }

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBuf = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(2048)
            var audioRecord: AudioRecord? = null

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    minBuf
                )
                if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                    audioRecord.release()
                    return@withContext
                }
                audioRecord.startRecording()
                val pcmBuffer = ShortArray(1024)

                while (isActive && isCurrentUserActiveSpeaker) {
                    val read = audioRecord.read(pcmBuffer, 0, pcmBuffer.size)
                    if (read > 0) {
                        var maxAmp = 0
                        for (i in 0 until read) {
                            val v = Math.abs(pcmBuffer[i].toInt())
                            if (v > maxAmp) maxAmp = v
                        }
                        val talking = maxAmp > 1200
                        if (isLocalTalking != talking) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                isLocalTalking = talking
                            }
                        }
                    }
                    kotlinx.coroutines.delay(120)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (e: Exception) {}
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    isLocalTalking = false
                }
            }
        }
    }

    // Sync speech detection to Firestore for remote participants
    LaunchedEffect(isLocalTalking, myCurrentMicSeat?.seatIndex) {
        val seatIdx = myCurrentMicSeat?.seatIndex ?: return@LaunchedEffect
        try {
            db.collection("rooms").document(room.id).collection("mic_seats").document(seatIdx.toString())
                .update("isTalking", isLocalTalking)
        } catch (e: Exception) {}
    }

    // Host pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "hostPulse")
    val hostBorderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val view = LocalView.current
        SideEffect {
            var parent = view.parent
            while (parent != null && parent !is android.app.Dialog && parent !is DialogWindowProvider) {
                parent = parent.parent
            }
            (parent as? DialogWindowProvider)?.window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
        }

        Scaffold(
            containerColor = BrandDark,
            topBar = {
                // Compact Luxury Room Navigation Bar (Optimizes vertical space)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(BrandCard.copy(alpha = 0.85f), CircleShape)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${room.title} ${room.emoji}",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(BrandGreen)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${activeMembers.size} Active in Room",
                                        color = BrandGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isUserHost) {
                                IconButton(
                                    onClick = { showRoomSettingsDialog = true },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(BrandCard.copy(alpha = 0.85f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Room Settings",
                                        tint = BrandGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            // Leave / Close Room Button
                            Button(
                                onClick = {
                                    if (isUserHost) {
                                        db.collection("rooms").document(room.id).update(
                                            mapOf(
                                                "isHostLive" to false,
                                                "statusMessage" to "Host will be live soon",
                                                "isPlaying" to false
                                            )
                                        )
                                    }
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.85f)),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(if (isUserHost) "Close" else "Leave", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Public Room Chat & Audio Controls Bar
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. SELF MIC BUTTON (Bottom Left Corner)
                        val mySeat = micSeats.find { it.uid == currentUser.uid }
                        val isSelfOnStage = mySeat != null
                        val isSelfMicMuted = mySeat?.isMuted ?: true

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelfMicMuted) Color(0xFF281518)
                                    else if (isLocalTalking) BrandGreen
                                    else BrandCard
                                )
                                .border(
                                    1.5.dp,
                                    if (isSelfMicMuted) Color(0xFFFF5252).copy(alpha = 0.85f)
                                    else if (isLocalTalking) BrandGreen
                                    else BrandCoral.copy(alpha = 0.85f),
                                    CircleShape
                                )
                                .clickable {
                                    if (isSelfOnStage) {
                                        val s = mySeat!!
                                        val newMute = !s.isMuted
                                        val idx = micSeats.indexOfFirst { it.seatIndex == s.seatIndex }
                                        if (idx != -1) {
                                            micSeats[idx] = micSeats[idx].copy(isMuted = newMute)
                                        }
                                        toggleMuteMicSeat(db, room.id, s.seatIndex, s.isMuted)
                                        Toast.makeText(context, if (newMute) "Microphone Muted 🔇" else "Microphone Live 🎙️", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val openSeat = micSeats.firstOrNull { it.seatIndex > 0 && it.uid.isEmpty() && !it.isLocked }
                                        if (openSeat != null) {
                                            val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                            if (hasPerm) {
                                                joinMicSeat(db, room.id, openSeat.seatIndex, currentUser, micSeats)
                                                Toast.makeText(context, "You took Mic ${openSeat.seatIndex + 1}! Live now 🎙️", Toast.LENGTH_SHORT).show()
                                            } else {
                                                pendingSeatToJoin = openSeat
                                                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        } else if (isUserHost) {
                                            val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                            if (hasPerm) {
                                                joinMicSeat(db, room.id, 0, currentUser, micSeats)
                                                Toast.makeText(context, "Host Mic Active 🎙️", Toast.LENGTH_SHORT).show()
                                            } else {
                                                pendingSeatToJoin = MicSeat(seatIndex = 0, label = "Host 👑", role = "host")
                                                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        } else {
                                            Toast.makeText(context, "All stage microphones are currently occupied", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSelfMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Self Mic",
                                tint = if (isSelfMicMuted) Color(0xFFFF8A80) else Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // 2. ROOM VOLUME BUTTON (Next to Mic in Bottom Left)
                        val isRoomAudioMuted by AudioPlayerManager.isMutedState
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isRoomAudioMuted) Color(0xFF251A1D)
                                    else BrandCard
                                )
                                .border(
                                    1.5.dp,
                                    if (isRoomAudioMuted) Color.Red.copy(alpha = 0.8f) else BrandCyan.copy(alpha = 0.8f),
                                    CircleShape
                                )
                                .clickable {
                                    val newMute = AudioPlayerManager.toggleMute()
                                    Toast.makeText(context, if (newMute) "Room Audio Muted 🔇" else "Room Audio On 🔊", Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRoomAudioMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Room Volume",
                                tint = if (isRoomAudioMuted) Color(0xFFFF8A80) else BrandCyan,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // 3. COMMENT BOX (Adjusted width with weight(1f), no logic changes)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .border(1.dp, BrandBorder.copy(alpha = 0.8f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = commentInputText,
                                onValueChange = { commentInputText = it },
                                placeholder = { Text("Say something...", color = Color.Gray, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (commentInputText.isNotBlank()) {
                                        val msg = commentInputText.trim()
                                        val commentDoc = LiveRoomComment(
                                            id = UUID.randomUUID().toString(),
                                            roomId = room.id,
                                            userId = currentUser.uid,
                                            userName = currentUser.name,
                                            userPhoto = currentUser.photo,
                                            tier = if (currentUser.isVipOrPlatinum()) {
                                                if (currentUser.plan.contains("VIP", true)) "VIP" else "Platinum"
                                            } else "Standard",
                                            message = msg,
                                            timestamp = System.currentTimeMillis()
                                        )
                                        db.collection("rooms").document(room.id)
                                            .collection("live_comments").document(commentDoc.id)
                                            .set(commentDoc)
                                        commentInputText = ""
                                    }
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                singleLine = true
                            )

                            IconButton(
                                onClick = {
                                    if (commentInputText.isNotBlank()) {
                                        val msg = commentInputText.trim()
                                        val commentDoc = LiveRoomComment(
                                            id = UUID.randomUUID().toString(),
                                            roomId = room.id,
                                            userId = currentUser.uid,
                                            userName = currentUser.name,
                                            userPhoto = currentUser.photo,
                                            tier = if (currentUser.isVipOrPlatinum()) {
                                                if (currentUser.plan.contains("VIP", true)) "VIP" else "Platinum"
                                            } else "Standard",
                                            message = msg,
                                            timestamp = System.currentTimeMillis()
                                        )
                                        db.collection("rooms").document(room.id)
                                            .collection("live_comments").document(commentDoc.id)
                                            .set(commentDoc)
                                        commentInputText = ""
                                    }
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier.size(30.dp).background(BrandCoral, CircleShape)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Ambient Luxury Wallpaper in Background (Full Room Ambiance)
                AsyncImage(
                    model = room.banner_image_url.ifEmpty { "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3" },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.22f
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    // 0. LIVE STREAMING ENTRANCE MARQUEE / BANNER
                    AnimatedVisibility(
                        visible = entranceNotificationUser != null,
                        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                        exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(50))
                                .background(Brush.horizontalGradient(listOf(BrandCoral.copy(alpha = 0.95f), BrandGold.copy(alpha = 0.95f))))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(50))
                                .padding(horizontal = 14.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✨", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${entranceNotificationUser ?: ""} joined the live room! Welcome! 👋",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // ---------------------------------------------------------
                    // 1. HOST SHOWCASE (TOP-MIDDLE WITH GLOWING CORAL RING & BADGE)
                    // ---------------------------------------------------------
                    val hostMemberObj = hostMember ?: RoomMember(
                        uid = targetHostId.ifEmpty { "host" },
                        name = hostName,
                        photoUrl = hostPhoto,
                        role = "host"
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandCard.copy(alpha = 0.92f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BrandBorder, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Host Avatar (Clickable to open host profile!)
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        selectedMemberForSheet = hostMemberObj
                                    }
                                    .border(
                                        width = 2.5.dp,
                                        brush = Brush.sweepGradient(
                                            listOf(
                                                BrandCoral.copy(alpha = hostBorderAlpha),
                                                BrandGold.copy(alpha = hostBorderAlpha),
                                                BrandCoral.copy(alpha = hostBorderAlpha)
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            ) {
                                AsyncImage(
                                    model = hostPhoto.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                    contentDescription = "Host",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedMemberForSheet = hostMemberObj
                                    }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "👑 Host: $hostName",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = if (room.isHostLive) "🔴 LIVE NOW"
                                    else if (room.scheduledTime.isNotBlank()) "⏳ Live at ${room.scheduledTime}"
                                    else "⏳ Host will be live soon",
                                    color = if (room.isHostLive) BrandGreen else BrandGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = "👥 $hostFollowerCount Followers • Tap for profile",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Dedicated Follow Action Button (Always Visible in All Rooms)
                            Button(
                                onClick = {
                                    val newFollowing = !isFollowingHost
                                    isFollowingHost = newFollowing
                                    if (newFollowing) {
                                        hostFollowerCount += 1
                                        if (targetHostId.isNotBlank() && targetHostId != currentUser.uid) {
                                            val record = FollowerRecord(
                                                uid = currentUser.uid,
                                                name = currentUser.name,
                                                photo = currentUser.photo,
                                                timestamp = System.currentTimeMillis()
                                            )
                                            db.collection("users").document(targetHostId)
                                                .collection("followers").document(currentUser.uid).set(record)
                                            db.collection("users").document(targetHostId)
                                                .update("followerCount", FieldValue.increment(1))
                                            db.collection("users").document(currentUser.uid)
                                                .update("followingCount", FieldValue.increment(1))

                                            sendNotification(
                                                db = db,
                                                targetUserId = targetHostId,
                                                sender = currentUser,
                                                type = "follow",
                                                title = "🌟 New Follower!",
                                                message = "${currentUser.name} started following you!"
                                            )
                                        } else {
                                            db.collection("users").document(currentUser.uid)
                                                .update("followerCount", FieldValue.increment(1))
                                        }
                                        Toast.makeText(context, "🌟 Following $hostName! ($hostFollowerCount)", Toast.LENGTH_SHORT).show()
                                        showFollowCelebrationDialog = true
                                    } else {
                                        hostFollowerCount = (hostFollowerCount - 1).coerceAtLeast(0)
                                        if (targetHostId.isNotBlank() && targetHostId != currentUser.uid) {
                                            db.collection("users").document(targetHostId)
                                                .collection("followers").document(currentUser.uid).delete()
                                            db.collection("users").document(targetHostId)
                                                .update("followerCount", FieldValue.increment(-1))
                                            db.collection("users").document(currentUser.uid)
                                                .update("followingCount", FieldValue.increment(-1))
                                        } else {
                                            db.collection("users").document(currentUser.uid)
                                                .update("followerCount", FieldValue.increment(-1))
                                        }
                                        Toast.makeText(context, "Unfollowed $hostName", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowingHost) BrandDark else BrandCoral
                                ),
                                border = if (isFollowingHost) BorderStroke(1.dp, BrandGold) else null,
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = if (isFollowingHost) "✓ Following ($hostFollowerCount)" else "+ Follow ($hostFollowerCount)",
                                    color = if (isFollowingHost) BrandGold else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ---------------------------------------------------------
                    // 2. FULL EQUALIZER MUSIC BOX WITH ALBUM ART BACKGROUND & LIVE TIMER
                    // ---------------------------------------------------------
                    val currentArtwork = room.nowPlayingArtworkUrl.ifEmpty { AudioPlayerManager.currentTrackArtworkUrlState.value }
                    val isPlayingNow = AudioPlayerManager.isPlayingState.value
                    val isBuffering by AudioPlayerManager.isBufferingState
                    val formattedTime by AudioPlayerManager.formattedTimeState
                    val currentPosMs by AudioPlayerManager.currentPositionMsState
                    val totalDurationMs by AudioPlayerManager.durationMsState

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    if (isPlayingNow) listOf(BrandCoral, BrandGold, BrandOrange)
                                    else listOf(BrandBorder, BrandBorder)
                                ),
                                shape = RoundedCornerShape(18.dp)
                            )
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // 1. Full Thumbnail in Transparency as Background
                            if (currentArtwork.isNotBlank()) {
                                AsyncImage(
                                    model = currentArtwork,
                                    contentDescription = null,
                                    modifier = Modifier.matchParentSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // 2. Translucent Dark Gradient Overlay
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.72f),
                                                Color(0xFF140F12).copy(alpha = 0.88f)
                                            )
                                        )
                                    )
                            )

                            // 3. Main Content Inside Full Equalizer Box
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                // Top Row: Thumbnail + Track Title/Artist + Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Small Thumbnail with Live Ring
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(
                                                1.dp,
                                                if (isPlayingNow) BrandCoral else BrandBorder,
                                                RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (currentArtwork.isNotBlank()) {
                                            AsyncImage(
                                                model = currentArtwork,
                                                contentDescription = "Artwork",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = BrandGold, modifier = Modifier.size(20.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (room.nowPlayingTitle.isNotBlank()) room.nowPlayingTitle else "Lounge Music Idle",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isBuffering) "⏳ Buffering audio stream..."
                                            else if (room.nowPlayingArtist.isNotBlank()) room.nowPlayingArtist
                                            else if (isPlayingNow) "Playing Live across Room"
                                            else "Select a track to play",
                                            color = if (isPlayingNow) BrandGold else Color.Gray,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Select Song Button
                                    IconButton(
                                        onClick = { showTrackSelector = true },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                            .border(1.dp, BrandBorder, CircleShape)
                                    ) {
                                        Icon(Icons.Default.QueueMusic, contentDescription = "Select Song", tint = BrandCyan, modifier = Modifier.size(16.dp))
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Responsive Play/Pause Toggle
                                    IconButton(
                                        onClick = {
                                            val willPlay = !isPlayingNow
                                            if (willPlay) {
                                                if (AudioPlayerManager.currentTrackUrlState.value.isNotBlank()) {
                                                    AudioPlayerManager.resume(context)
                                                } else {
                                                    val defaultUrl = room.nowPlayingUrl.ifEmpty { CURATED_HANGOUT_TRACKS[0].url }
                                                    val defaultTitle = room.nowPlayingTitle.ifEmpty { CURATED_HANGOUT_TRACKS[0].title }
                                                    val defaultArt = room.nowPlayingArtworkUrl.ifEmpty { CURATED_HANGOUT_TRACKS[0].artworkUrl }
                                                    AudioPlayerManager.play(context, defaultUrl, defaultTitle, defaultArt)
                                                }
                                            } else {
                                                AudioPlayerManager.pause()
                                            }
                                            room = room.copy(isPlaying = willPlay)
                                            db.collection("rooms").document(room.id).set(
                                                mapOf(
                                                    "isPlaying" to willPlay,
                                                    "nowPlayingTitle" to AudioPlayerManager.currentTrackTitleState.value.ifEmpty { room.nowPlayingTitle },
                                                    "nowPlayingArtist" to room.nowPlayingArtist,
                                                    "nowPlayingUrl" to AudioPlayerManager.currentTrackUrlState.value.ifEmpty { room.nowPlayingUrl },
                                                    "nowPlayingArtworkUrl" to AudioPlayerManager.currentTrackArtworkUrlState.value.ifEmpty { room.nowPlayingArtworkUrl }
                                                ),
                                                SetOptions.merge()
                                            )
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isPlayingNow) Brush.horizontalGradient(listOf(BrandCoral, BrandOrange))
                                                else Brush.horizontalGradient(listOf(BrandDark, Color(0xFF281E22)))
                                            )
                                            .border(1.dp, if (isPlayingNow) BrandGold else BrandBorder, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingNow) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlayingNow) "Pause" else "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // FULL EQUALIZER VISUALIZER WAVE (Spans card width)
                                MusicVisualizerWave(
                                    isPlaying = isPlayingNow,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(24.dp)
                                        .padding(vertical = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Live Playback Progress Bar + Song Duration Timer
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val progress = if (totalDurationMs > 0) (currentPosMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f) else 0f

                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = BrandCoral,
                                        trackColor = Color.White.copy(alpha = 0.15f)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = formattedTime,
                                        color = if (isPlayingNow) BrandGold else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ---------------------------------------------------------
                    // 2.5 REAL 5-MIC LIVE AUDIO STAGE (DIRECTLY BENEATH EQUALIZER)
                    // ---------------------------------------------------------
                    FiveMicStageSection(
                        seats = if (micSeats.isNotEmpty()) micSeats else listOf(
                            MicSeat(seatIndex = 0, label = "Host 👑", uid = targetHostId, name = hostName, photo = hostPhoto, role = "host"),
                            MicSeat(seatIndex = 1, label = "Mic 2", uid = "", name = "", photo = "", role = "speaker"),
                            MicSeat(seatIndex = 2, label = "Mic 3", uid = "", name = "", photo = "", role = "speaker"),
                            MicSeat(seatIndex = 3, label = "Mic 4", uid = "", name = "", photo = "", role = "speaker"),
                            MicSeat(seatIndex = 4, label = "Mic 5", uid = "", name = "", photo = "", role = "speaker")
                        ),
                        currentUserId = currentUser.uid,
                        isHost = isUserHost,
                        isLocalTalking = isLocalTalking,
                        onSeatClick = { seat ->
                            if (isUserHost) {
                                selectedSeatForAdmin = seat
                            } else {
                                if (seat.uid == currentUser.uid) {
                                    selectedOwnSeatForControl = seat
                                } else if (seat.uid.isNotEmpty()) {
                                    selectedMemberForSheet = RoomMember(
                                        uid = seat.uid,
                                        name = seat.name,
                                        photoUrl = seat.photo,
                                        role = seat.role
                                    )
                                } else if (seat.isLocked) {
                                    Toast.makeText(context, "This microphone is locked by the host 🔒", Toast.LENGTH_SHORT).show()
                                } else {
                                    val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                    if (hasPerm) {
                                        joinMicSeat(db, room.id, seat.seatIndex, currentUser, micSeats)
                                        Toast.makeText(context, "You are now live on Mic ${seat.seatIndex + 1}! 🎙️", Toast.LENGTH_SHORT).show()
                                    } else {
                                        pendingSeatToJoin = seat
                                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                        },
                        onQuickToggleMute = {
                            myCurrentMicSeat?.let { seat ->
                                toggleMuteMicSeat(db, room.id, seat.seatIndex, seat.isMuted)
                            }
                        },
                        onQuickLeaveStage = {
                            myCurrentMicSeat?.let { seat ->
                                if (seat.seatIndex != 0) {
                                    vacateMicSeat(db, room.id, seat.seatIndex, seat.isLocked)
                                    Toast.makeText(context, "You stepped down from the stage", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ---------------------------------------------------------
                    // 3. LIVE STREAMING AUDIENCE STRIP (REAL-TIME, NO DUMMY DATA)
                    // ---------------------------------------------------------
                    val audienceListState = rememberLazyListState()

                    // Auto-sliding Carousel for audience when members > 3
                    LaunchedEffect(displayAudience.size) {
                        if (displayAudience.size > 3) {
                            while (true) {
                                delay(2800)
                                val current = audienceListState.firstVisibleItemIndex
                                val next = (current + 1) % displayAudience.size
                                audienceListState.animateScrollToItem(next)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(BrandGreen.copy(alpha = 0.12f))
                                .border(1.dp, BrandGreen.copy(alpha = 0.35f), RoundedCornerShape(50))
                                .clickable { showAllViewersDialog = true }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(BrandGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "👁️ $totalDisplayCount Live in Room",
                                color = BrandGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "View all",
                                tint = BrandGreen,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = "Tap avatar to view profile",
                            color = Color.Gray,
                            fontSize = 9.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (displayAudience.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎧", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "No listeners yet • Invite friends to join your hangout!",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        val avatarSize = if (displayAudience.size > 4) 46.dp else 52.dp
                        // Horizontal Audience Strip with Responsive Sizing & Auto-Sliding
                        LazyRow(
                            state = audienceListState,
                            horizontalArrangement = Arrangement.spacedBy(if (displayAudience.size > 4) 10.dp else 14.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            items(displayAudience, key = { it.uid }) { member ->
                                val isMemberVip = member.tier.contains("VIP", ignoreCase = true)
                                val isMemberPlat = member.tier.contains("Platinum", ignoreCase = true)
                                val isSelf = member.uid == currentUser.uid

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedMemberForSheet = member
                                        }
                                        .padding(2.dp)
                                ) {
                                    Box(contentAlignment = Alignment.BottomEnd) {
                                        Box(
                                            modifier = Modifier
                                                .size(avatarSize)
                                                .clip(CircleShape)
                                                .border(
                                                    width = 2.dp,
                                                    brush = Brush.sweepGradient(
                                                        if (isMemberVip) listOf(BrandGold, BrandCoral, BrandGold)
                                                        else if (isMemberPlat) listOf(BrandCyan, BrandCoral, BrandCyan)
                                                        else listOf(BrandCoral, BrandOrange)
                                                    ),
                                                    shape = CircleShape
                                                )
                                        ) {
                                            AsyncImage(
                                                model = member.photoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                                contentDescription = member.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(BrandGreen)
                                                .border(1.5.dp, BrandDark, CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = if (isSelf) "You" else member.name.substringBefore(' '),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.width(if (displayAudience.size > 4) 48.dp else 56.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = if (isMemberVip) "VIP" else if (isMemberPlat) "PLAT" else "USER",
                                        color = if (isMemberVip) BrandGold else if (isMemberPlat) BrandCyan else Color.Gray,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // ---------------------------------------------------------
                    // 4. LIVE TRANSPARENT COMMENTS STREAM (FLOATING OVER WALLPAPER)
                    // ---------------------------------------------------------
                    val chatListState = rememberLazyListState()
                    LaunchedEffect(liveComments.size) {
                        if (liveComments.isNotEmpty()) {
                            chatListState.animateScrollToItem(liveComments.size - 1)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (liveComments.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "💬 Live Room Chat\nBe the first to say hello to the host and room!",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                state = chatListState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(liveComments) { comment ->
                                    val isVip = comment.tier.contains("VIP", true)
                                    val isPlat = comment.tier.contains("Platinum", true)
                                    val nameColor = if (isVip) BrandGold else if (isPlat) BrandCyan else BrandCoral

                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color.Black.copy(alpha = 0.55f))
                                            .border(0.5.dp, if (isVip) BrandGold.copy(alpha = 0.6f) else BrandBorder, RoundedCornerShape(50))
                                            .clickable {
                                                selectedMemberForSheet = RoomMember(
                                                    uid = comment.userId,
                                                    name = comment.userName,
                                                    photoUrl = comment.userPhoto,
                                                    tier = comment.tier
                                                )
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = comment.userPhoto.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${comment.userName}: ",
                                            color = nameColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = comment.message,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SUB-DIALOGS ---

        // 1. Host Track Selector Dialog
        if (showTrackSelector) {
            TrackSelectorDialog(
                onDismiss = { showTrackSelector = false },
                onSelectTrack = { title, artist, category, url, artworkUrl ->
                    db.collection("rooms").document(room.id).set(
                        mapOf(
                            "currentPlayingTrack" to title,
                            "nowPlayingTitle" to title,
                            "nowPlayingArtist" to artist,
                            "nowPlayingUrl" to url,
                            "nowPlayingArtworkUrl" to artworkUrl,
                            "isPlaying" to true,
                            "playbackPosition" to 0L
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                    room = room.copy(
                        nowPlayingTitle = title,
                        nowPlayingArtist = artist,
                        nowPlayingUrl = url,
                        nowPlayingArtworkUrl = artworkUrl,
                        isPlaying = true
                    )
                    AudioPlayerManager.play(context, url, title, artworkUrl)
                    Toast.makeText(context, "Streaming: $title by $artist", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 2. Member Profile Sheet Dialog
        if (selectedMemberForSheet != null) {
            val member = selectedMemberForSheet!!
            val profile = allProfiles.find { it.uid == member.uid } ?: UserProfile(
                uid = member.uid,
                name = member.name,
                photo = member.photoUrl,
                age = 23,
                city = "Kolkata",
                bio = "Music enthusiast and love deep conversations! 🎧",
                plan = if (member.tier.contains("VIP", true)) "VIP LEVEL ★" else if (member.tier.contains("Platinum", true)) "PLATINUM" else "Standard"
            )

            RoomMemberProfileSheet(
                member = member,
                userProfile = profile,
                currentUser = currentUser,
                isHost = isUserHost,
                roomId = room.id,
                db = db,
                onDismiss = { selectedMemberForSheet = null },
                onLeaveRoom = {
                    selectedMemberForSheet = null
                    onDismiss()
                },
                onLike = {
                    recordSwipe(db, currentUser.uid, member.uid, "like")
                    checkAndHandleMutualMatch(db, currentUser, profile, onMatch = {
                        Toast.makeText(context, "🎉 It's a Match with ${profile.name}!", Toast.LENGTH_LONG).show()
                    })
                    sendNotification(
                        db = db,
                        targetUserId = member.uid,
                        sender = currentUser,
                        type = "like",
                        title = "❤️ New Room Like!",
                        message = "${currentUser.name} liked you in ${room.title}"
                    )
                    Toast.makeText(context, "Liked ${member.name}!", Toast.LENGTH_SHORT).show()
                },
                onDirectMessage = {
                    val partner = profile ?: UserProfile(uid = member.uid, name = member.name, photo = member.photoUrl)
                    onDirectChat(partner)
                },
                onRequestMessage = {
                    selectedMemberForRequest = member
                },
                onKickOut = { targetMember ->
                    room = room.copy(kickedUserIds = room.kickedUserIds + targetMember.uid)
                    db.collection("rooms").document(room.id)
                        .collection("members").document(targetMember.uid).delete()
                    db.collection("rooms").document(room.id)
                        .update("kickedUserIds", FieldValue.arrayUnion(targetMember.uid))
                    Toast.makeText(context, "${targetMember.name} kicked out of room.", Toast.LENGTH_SHORT).show()
                    selectedMemberForSheet = null
                },
                onBan = { targetMember ->
                    room = room.copy(bannedUserIds = room.bannedUserIds + targetMember.uid)
                    db.collection("rooms").document(room.id)
                        .collection("members").document(targetMember.uid).delete()
                    db.collection("rooms").document(room.id)
                        .update("bannedUserIds", FieldValue.arrayUnion(targetMember.uid))
                    Toast.makeText(context, "${targetMember.name} permanently banned from room.", Toast.LENGTH_SHORT).show()
                    selectedMemberForSheet = null
                },
                onBlock = { targetMember ->
                    db.collection("users").document(currentUser.uid)
                        .update("blockedUserIds", FieldValue.arrayUnion(targetMember.uid))
                    Toast.makeText(context, "Blocked ${targetMember.name}.", Toast.LENGTH_SHORT).show()
                    selectedMemberForSheet = null
                },
                onTagMember = { memberName ->
                    commentInputText = "@$memberName "
                    selectedMemberForSheet = null
                }
            )
        }

        // 3. Send Message Request Dialog (Standard/Free user flow)
        if (selectedMemberForRequest != null) {
            val target = selectedMemberForRequest!!
            SendMessageRequestDialog(
                targetUser = target,
                onDismiss = { selectedMemberForRequest = null },
                onSend = { note ->
                    val reqId = UUID.randomUUID().toString()
                    val request = MessageRequest(
                        requestId = reqId,
                        senderId = currentUser.uid,
                        senderName = currentUser.name,
                        senderPhoto = currentUser.photo,
                        targetId = target.uid,
                        targetName = target.name,
                        message = note,
                        status = "pending",
                        timestamp = System.currentTimeMillis()
                    )
                    db.collection("message_requests").document(reqId).set(request)
                    sendNotification(
                        db = db,
                        targetUserId = target.uid,
                        sender = currentUser,
                        type = "message",
                        title = "💬 Message Request Received",
                        message = "${currentUser.name}: \"$note\""
                    )
                    selectedMemberForRequest = null
                    Toast.makeText(context, "Message request sent to ${target.name}!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 4. Room Settings & Controls Dialog (Host controls: Privacy, Schedule, Theme, Kick/Ban)
        if (showRoomSettingsDialog) {
            RoomSettingsDialog(
                room = room,
                isHost = isUserHost,
                activeMembers = activeMembers,
                db = db,
                onDismiss = { showRoomSettingsDialog = false },
                onUpdateTheme = { newThemeUrl ->
                    room = room.copy(banner_image_url = newThemeUrl)
                },
                onKickMember = { kicked ->
                    room = room.copy(kickedUserIds = room.kickedUserIds + kicked.uid)
                },
                onBanMember = { banned ->
                    room = room.copy(bannedUserIds = room.bannedUserIds + banned.uid)
                }
            )
        }

        // 4B. All Room Viewers & Moderation Dialog
        if (showAllViewersDialog) {
            AllRoomViewersDialog(
                viewers = displayAudience,
                currentUser = currentUser,
                isHost = isUserHost,
                onSelectMember = { member ->
                    selectedMemberForSheet = member
                    showAllViewersDialog = false
                },
                onKickOut = { targetMember ->
                    room = room.copy(kickedUserIds = room.kickedUserIds + targetMember.uid)
                    db.collection("rooms").document(room.id)
                        .collection("members").document(targetMember.uid).delete()
                    db.collection("rooms").document(room.id)
                        .update("kickedUserIds", FieldValue.arrayUnion(targetMember.uid))
                    Toast.makeText(context, "${targetMember.name} kicked out of room.", Toast.LENGTH_SHORT).show()
                },
                onBan = { targetMember ->
                    room = room.copy(bannedUserIds = room.bannedUserIds + targetMember.uid)
                    db.collection("rooms").document(room.id)
                        .collection("members").document(targetMember.uid).delete()
                    db.collection("rooms").document(room.id)
                        .update("bannedUserIds", FieldValue.arrayUnion(targetMember.uid))
                    Toast.makeText(context, "${targetMember.name} permanently banned from room.", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showAllViewersDialog = false }
            )
        }

        // 5. Celebratory Welcome Follower Dialog
        if (showFollowCelebrationDialog) {
            Dialog(
                onDismissRequest = { showFollowCelebrationDialog = false }
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉", fontSize = 46.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Thank You for Following!",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You are now following $hostName. You will receive an instant real-time notification whenever $hostName goes LIVE!",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { showFollowCelebrationDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Text("Awesome! 🚀", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 7. Host Seat Admin Dialog
        if (selectedSeatForAdmin != null) {
            val seat = selectedSeatForAdmin!!
            HostSeatAdminDialog(
                seat = seat,
                currentUserId = currentUser.uid,
                onDismiss = { selectedSeatForAdmin = null },
                onToggleMute = {
                    toggleMuteMicSeat(db, room.id, seat.seatIndex, seat.isMuted)
                    selectedSeatForAdmin = null
                },
                onToggleLock = {
                    toggleLockMicSeat(db, room.id, seat.seatIndex, seat.isLocked)
                    selectedSeatForAdmin = null
                },
                onKickToAudience = {
                    vacateMicSeat(db, room.id, seat.seatIndex, seat.isLocked)
                    Toast.makeText(context, "Removed ${seat.name} from stage", Toast.LENGTH_SHORT).show()
                    selectedSeatForAdmin = null
                },
                onInviteListener = {
                    showInviteListenersDialog = seat
                    selectedSeatForAdmin = null
                },
                onViewProfile = {
                    selectedMemberForSheet = RoomMember(
                        uid = seat.uid,
                        name = seat.name,
                        photoUrl = seat.photo,
                        role = seat.role
                    )
                    selectedSeatForAdmin = null
                }
            )
        }

        // 8. Invite Listener Dialog
        if (showInviteListenersDialog != null) {
            val seat = showInviteListenersDialog!!
            InviteListenerDialog(
                seat = seat,
                availableAudience = displayAudience.filter { aud -> micSeats.none { it.uid == aud.uid } },
                onDismiss = { showInviteListenersDialog = null },
                onSelectMember = { member ->
                    db.collection("rooms").document(room.id).collection("mic_seats").document(seat.seatIndex.toString())
                        .set(
                            MicSeat(
                                seatIndex = seat.seatIndex,
                                label = if (seat.seatIndex == 0) "Host 👑" else "Mic ${seat.seatIndex + 1}",
                                uid = member.uid,
                                name = member.name,
                                photo = member.photoUrl,
                                isMuted = false,
                                isLocked = false,
                                isTalking = false,
                                role = "speaker"
                            )
                        )
                    Toast.makeText(context, "Invited ${member.name} to Mic ${seat.seatIndex + 1}! 🎙️", Toast.LENGTH_SHORT).show()
                    showInviteListenersDialog = null
                }
            )
        }

        // 9. Speaker Own Seat Control Dialog
        if (selectedOwnSeatForControl != null) {
            val seat = selectedOwnSeatForControl!!
            SpeakerSeatControlDialog(
                seat = seat,
                isTalking = isLocalTalking,
                onDismiss = { selectedOwnSeatForControl = null },
                onToggleMute = {
                    toggleMuteMicSeat(db, room.id, seat.seatIndex, seat.isMuted)
                    selectedOwnSeatForControl = null
                },
                onLeaveStage = {
                    if (seat.seatIndex != 0) {
                        vacateMicSeat(db, room.id, seat.seatIndex, seat.isLocked)
                        Toast.makeText(context, "You stepped down from the stage", Toast.LENGTH_SHORT).show()
                    }
                    selectedOwnSeatForControl = null
                }
            )
        }
    }
}

// -------------------------------------------------------------
// 7. CATEGORY SUB-ROOM CARD
// -------------------------------------------------------------
@Composable
fun CategorySubRoomCard(
    room: RoomItem,
    activeCount: Int,
    onJoin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, BrandBorder, RoundedCornerShape(18.dp))
    ) {
        AsyncImage(
            model = room.banner_image_url.ifEmpty { "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3" },
            contentDescription = room.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Live status & Host chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Pill: "🔴 LIVE" or "⏳ Host will be live soon"
                if (room.isHostLive) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(BrandCoral.copy(alpha = 0.85f))
                            .border(0.5.dp, BrandGold, RoundedCornerShape(50))
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(0.5.dp, BrandGold.copy(alpha = 0.6f), RoundedCornerShape(50))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val statusText = if (room.scheduledTime.isNotBlank()) "⏳ ${room.scheduledTime}" else "⏳ Live soon"
                        Text(statusText, color = BrandGold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (room.hostName.isNotBlank()) {
                    Text(
                        text = "👑 ${room.hostName}",
                        color = BrandGold,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(50))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                Text(
                    text = "${room.title} ${room.emoji}",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Equalizer Wave: only animated/bouncing if room is LIVE and playing!
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MusicVisualizerWave(isPlaying = room.isHostLive && room.isPlaying)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (room.isHostLive && room.isPlaying && room.nowPlayingTitle.isNotBlank()) {
                            "🎶 ${room.nowPlayingTitle.take(14)}..."
                        } else if (room.isHostLive) {
                            "🔴 Room is Live"
                        } else {
                            "⏳ Host will be live soon"
                        },
                        color = if (room.isHostLive) BrandGreen else Color.LightGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onJoin,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.fillMaxWidth().height(32.dp)
                ) {
                    Text("Join Room", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 8. CATEGORY HUB / LOBBY DIALOG (ISOLATED INTENT SUB-ROOMS)
// -------------------------------------------------------------
@Composable
fun CategoryHubLobbyDialog(
    category: IntentCategory,
    categoryBannerUrl: String,
    allRooms: List<RoomItem>,
    allUsers: List<UserProfile>,
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onCreateRoom: () -> Unit,
    onJoinRoom: (RoomItem) -> Unit
) {
    // Rooms belonging to this category
    val categoryRooms = remember(allRooms, category.id) {
        allRooms.filter { it.category == category.id || it.id == category.id }
    }

    // Active members in this category across users
    val activeCategoryCount = remember(allUsers, category.id) {
        allUsers.count { it.activeIntent == category.id || it.current_room.contains(category.id) }
    }

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Scaffold(
            containerColor = BrandDark,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    AsyncImage(
                        model = categoryBannerUrl.ifEmpty { "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3" },
                        contentDescription = category.label,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.4f), BrandDark)
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = category.label,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "🟢 $activeCategoryCount Active in Category Lobby",
                                    color = BrandGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // "+ Create Room" Button inside Lobby
                        Button(
                            onClick = onCreateRoom,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Create Room", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Active Sub-Rooms (${categoryRooms.size})",
                            color = BrandGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Browse or host rooms dedicated exclusively to this intent.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (categoryRooms.isEmpty()) {
                    // Show default category lounge + invitation card
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            // Official category room card
                            val defaultRoom = RoomItem(
                                id = category.id,
                                title = category.label.substringAfter(' '),
                                emoji = category.label.take(2).trim(),
                                banner_image_url = categoryBannerUrl,
                                category = category.id
                            )
                            CategorySubRoomCard(
                                room = defaultRoom,
                                activeCount = activeCategoryCount,
                                onJoin = { onJoinRoom(defaultRoom) }
                            )
                        }

                        item {
                            // Host prompt card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BrandCardSubtle),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .border(1.dp, BrandCoral.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                    .clickable { onCreateRoom() }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Host a New Room",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Be the Host 👑 with music & live chat!",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(categoryRooms) { room ->
                            val activeCount = allUsers.count { it.current_room == room.id }
                            CategorySubRoomCard(
                                room = room,
                                activeCount = activeCount,
                                onJoin = { onJoinRoom(room) }
                            )
                        }

                        item {
                            // Host another room card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BrandCardSubtle),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .border(1.dp, BrandBorder, RoundedCornerShape(18.dp))
                                    .clickable { onCreateRoom() }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "+ Create Sub-Room",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Host another hangout lounge",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
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

// -------------------------------------------------------------
// 8. 5-MIC LIVE AUDIO STAGE COMPONENTS & HELPERS
// -------------------------------------------------------------

fun joinMicSeat(
    db: FirebaseFirestore,
    roomId: String,
    seatIndex: Int,
    user: UserProfile,
    allSeats: List<MicSeat>
) {
    val prevSeat = allSeats.find { it.uid == user.uid && it.seatIndex != seatIndex }
    if (prevSeat != null) {
        db.collection("rooms").document(roomId).collection("mic_seats").document(prevSeat.seatIndex.toString())
            .set(
                MicSeat(
                    seatIndex = prevSeat.seatIndex,
                    label = if (prevSeat.seatIndex == 0) "Host 👑" else "Mic ${prevSeat.seatIndex + 1}",
                    uid = "",
                    name = "",
                    photo = "",
                    isMuted = false,
                    isLocked = prevSeat.isLocked,
                    isTalking = false,
                    role = if (prevSeat.seatIndex == 0) "host" else "speaker"
                )
            )
    }

    db.collection("rooms").document(roomId).collection("mic_seats").document(seatIndex.toString())
        .set(
            MicSeat(
                seatIndex = seatIndex,
                label = if (seatIndex == 0) "Host 👑" else "Mic ${seatIndex + 1}",
                uid = user.uid,
                name = user.name,
                photo = user.photo,
                isMuted = false,
                isLocked = false,
                isTalking = false,
                role = if (seatIndex == 0) "host" else "speaker"
            )
        )
}

fun vacateMicSeat(
    db: FirebaseFirestore,
    roomId: String,
    seatIndex: Int,
    isLocked: Boolean = false
) {
    db.collection("rooms").document(roomId).collection("mic_seats").document(seatIndex.toString())
        .set(
            MicSeat(
                seatIndex = seatIndex,
                label = if (seatIndex == 0) "Host 👑" else "Mic ${seatIndex + 1}",
                uid = "",
                name = "",
                photo = "",
                isMuted = false,
                isLocked = isLocked,
                isTalking = false,
                role = if (seatIndex == 0) "host" else "speaker"
            )
        )
}

fun toggleMuteMicSeat(
    db: FirebaseFirestore,
    roomId: String,
    seatIndex: Int,
    currentMuted: Boolean
) {
    val newMute = !currentMuted
    db.collection("rooms").document(roomId).collection("mic_seats").document(seatIndex.toString())
        .set(
            mapOf(
                "isMuted" to newMute,
                "muted" to newMute
            ),
            SetOptions.merge()
        )
}

fun toggleLockMicSeat(
    db: FirebaseFirestore,
    roomId: String,
    seatIndex: Int,
    currentLocked: Boolean
) {
    val newLock = !currentLocked
    db.collection("rooms").document(roomId).collection("mic_seats").document(seatIndex.toString())
        .set(
            mapOf(
                "isLocked" to newLock,
                "locked" to newLock
            ),
            SetOptions.merge()
        )
}

@Composable
fun FiveMicStageSection(
    seats: List<MicSeat>,
    currentUserId: String,
    isHost: Boolean,
    isLocalTalking: Boolean,
    onSeatClick: (MicSeat) -> Unit,
    onQuickToggleMute: () -> Unit,
    onQuickLeaveStage: () -> Unit
) {
    val mySeat = seats.find { it.uid == currentUserId }

    Card(
        colors = CardDefaults.cardColors(containerColor = BrandCard.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BrandBorder.copy(alpha = 0.9f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Stage Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Mic Stage",
                        tint = BrandCoral,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE AUDIO STAGE",
                        color = BrandCoral,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = " • 5 Seats",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                if (isHost) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(BrandGold.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "👑 Host Admin",
                            color = BrandGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (mySeat != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(BrandGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🎙️ On Stage",
                            color = BrandGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Tap open mic to speak",
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                }
            }

            // Quick Controller Strip when Current User is On Stage
            if (mySeat != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (mySeat.isMuted) Color(0xFF2C1619) else Color(0xFF14291B))
                        .border(
                            1.dp,
                            if (mySeat.isMuted) Color(0xFFFF5252).copy(alpha = 0.5f) else BrandGreen.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (mySeat.isMuted) Color.Red else if (isLocalTalking) BrandGreen else Color(0xFF4CAF50))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (mySeat.isMuted) "Mic Muted 🔇" else if (isLocalTalking) "Speaking... 🟢" else "Live on Stage 🎙️",
                            color = if (mySeat.isMuted) Color(0xFFFF8A80) else BrandGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Quick Mute / Unmute
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (mySeat.isMuted) BrandCoral else Color.White.copy(alpha = 0.15f))
                                .clickable { onQuickToggleMute() }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (mySeat.isMuted) "🎙️ Unmute" else "🔇 Mute",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Step Down button (for non-host speakers)
                        if (mySeat.seatIndex != 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.Red.copy(alpha = 0.25f))
                                    .clickable { onQuickLeaveStage() }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "🚪 Leave Mic",
                                    color = Color(0xFFFF8A80),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5 Seats in Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0..4) {
                    val seat = seats.find { it.seatIndex == i } ?: MicSeat(
                        seatIndex = i,
                        label = if (i == 0) "Host 👑" else "Mic ${i + 1}",
                        uid = "",
                        name = "",
                        photo = "",
                        role = if (i == 0) "host" else "speaker"
                    )
                    MicSeatView(
                        seat = seat,
                        isCurrentSelf = seat.uid.isNotEmpty() && seat.uid == currentUserId,
                        isLocalTalking = isLocalTalking,
                        isHost = isHost,
                        onClick = { onSeatClick(seat) }
                    )
                }
            }
        }
    }
}

@Composable
fun MicSeatView(
    seat: MicSeat,
    isCurrentSelf: Boolean,
    isLocalTalking: Boolean,
    isHost: Boolean,
    onClick: () -> Unit
) {
    val isSpeaking = if (isCurrentSelf) (isLocalTalking && !seat.isMuted) else (seat.isTalking && !seat.isMuted)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_${seat.seatIndex}")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale_${seat.seatIndex}"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier.size(46.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing speech ring animation
            if (isSpeaking) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                        .clip(CircleShape)
                        .background(BrandGreen.copy(alpha = 0.35f))
                        .border(1.5.dp, BrandGreen, CircleShape)
                )
            }

            if (seat.uid.isNotEmpty()) {
                // Occupied Avatar
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (isSpeaking) 2.dp else 1.5.dp,
                            color = if (isSpeaking) BrandGreen else if (seat.seatIndex == 0) BrandGold else BrandCoral,
                            shape = CircleShape
                        )
                ) {
                    AsyncImage(
                        model = seat.photo.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                        contentDescription = seat.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Crown overlay for Seat 0 (Host)
                if (seat.seatIndex == 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-3).dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(BrandDark)
                            .border(1.dp, BrandGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑", fontSize = 8.sp)
                    }
                }

                // Mic Mute / Unmute Status Badge at bottom-right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (seat.isMuted) Color(0xFFD32F2F) else BrandGreen)
                        .border(1.dp, BrandDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (seat.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(9.dp)
                    )
                }
            } else if (seat.isLocked) {
                // Locked Seat
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.12f))
                        .border(1.dp, Color.Red.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.Red.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                // Empty & Unlocked Seat
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Take Mic",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(BrandCoral),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Label / Subtitle
        if (seat.uid.isNotEmpty()) {
            Text(
                text = if (isCurrentSelf) "You" else seat.name.substringBefore(' ').take(7),
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(48.dp)
            )
            Text(
                text = if (seat.seatIndex == 0) "Host" else "Mic ${seat.seatIndex + 1}",
                color = if (seat.seatIndex == 0) BrandGold else BrandCoral,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        } else if (seat.isLocked) {
            Text(
                text = "Mic ${seat.seatIndex + 1}",
                color = Color.Gray,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Locked",
                color = Color.Red.copy(alpha = 0.7f),
                fontSize = 8.sp,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "Mic ${seat.seatIndex + 1}",
                color = Color.Gray,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Join +",
                color = BrandGreen,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HostSeatAdminDialog(
    seat: MicSeat,
    currentUserId: String,
    onDismiss: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleLock: () -> Unit,
    onKickToAudience: () -> Unit,
    onInviteListener: () -> Unit,
    onViewProfile: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161214)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BrandBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "👑 Stage Seat ${seat.seatIndex + 1} Admin Controls",
                    color = BrandGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (seat.uid.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(10.dp)
                    ) {
                        AsyncImage(
                            model = seat.photo.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                            contentDescription = seat.name,
                            modifier = Modifier.size(44.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = seat.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (seat.seatIndex == 0) "Room Host 👑" else "Speaker (Mic ${seat.seatIndex + 1})",
                                color = if (seat.seatIndex == 0) BrandGold else BrandCoral,
                                fontSize = 11.sp
                            )
                            Text(
                                text = if (seat.isMuted) "Status: Muted 🔇" else "Status: Live 🎙️",
                                color = if (seat.isMuted) Color.Red else BrandGreen,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mute / Unmute Button
                    Button(
                        onClick = onToggleMute,
                        colors = ButtonDefaults.buttonColors(containerColor = if (seat.isMuted) BrandGreen else Color(0xFF424242)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text(
                            text = if (seat.isMuted) "🎙️ Unmute Speaker" else "🔇 Mute Speaker",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // If not host self: Remove from stage
                    if (seat.seatIndex != 0 && seat.uid != currentUserId) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onKickToAudience,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Text(
                                text = "🚫 Remove from Stage (To Audience)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onViewProfile,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BrandBorder),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Text("👤 View User Profile", color = Color.White, fontSize = 12.sp)
                        }
                    }
                } else {
                    // Empty seat controls
                    Text(
                        text = "This microphone seat is currently empty.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onInviteListener,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text("📨 Invite Listener to this Mic", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onToggleLock,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (seat.isLocked) BrandGreen else Color.Red),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text(
                            text = if (seat.isLocked) "🔓 Unlock Seat for Everyone" else "🔒 Lock Seat (Keep Empty)",
                            color = if (seat.isLocked) BrandGreen else Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun InviteListenerDialog(
    seat: MicSeat,
    availableAudience: List<RoomMember>,
    onDismiss: () -> Unit,
    onSelectMember: (RoomMember) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161214)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BrandBorder),
            modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "📨 Invite Audience to Mic ${seat.seatIndex + 1}",
                    color = BrandCoral,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select a listener to invite to this microphone seat:",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (availableAudience.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false).padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No listeners available in audience right now.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableAudience, key = { it.uid }) { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .clickable { onSelectMember(member) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = member.photoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                    contentDescription = member.name,
                                    modifier = Modifier.size(38.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = member.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = member.tier,
                                        color = if (member.tier.contains("VIP", true)) BrandGold else Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(BrandCoral)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Invite 🎙️", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SpeakerSeatControlDialog(
    seat: MicSeat,
    isTalking: Boolean,
    onDismiss: () -> Unit,
    onToggleMute: () -> Unit,
    onLeaveStage: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161214)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BrandBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎙️ Your Microphone (Mic ${seat.seatIndex + 1})",
                    color = BrandCoral,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (seat.isMuted) "Your microphone is currently MUTED 🔇" else if (isTalking) "You are currently SPEAKING 🟢" else "Your microphone is LIVE 🎙️",
                    color = if (seat.isMuted) Color(0xFFFF8A80) else BrandGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onToggleMute,
                    colors = ButtonDefaults.buttonColors(containerColor = if (seat.isMuted) BrandGreen else Color(0xFF424242)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text(
                        text = if (seat.isMuted) "🎙️ Unmute Microphone" else "🔇 Mute Microphone",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (seat.seatIndex != 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onLeaveStage,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text(
                            text = "🚪 Step Down / Leave Stage",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}


