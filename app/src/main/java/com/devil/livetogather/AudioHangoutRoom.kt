@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.devil.livetogather

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.google.firebase.firestore.Query
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
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
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = isKeyboardOpen) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = !isKeyboardOpen)
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
    
    val height1 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = if (isPlaying) 22f else 6f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val height2 by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = if (isPlaying) 8f else 6f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val height3 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = if (isPlaying) 24f else 6f,
        animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse),
        label = "h3"
    )
    val height4 by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = if (isPlaying) 7f else 6f,
        animationSpec = infiniteRepeatable(tween(300, easing = LinearEasing), RepeatMode.Reverse),
        label = "h4"
    )

    Row(
        modifier = modifier.height(26.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(height1, height2, height3, height4).forEach { h ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isPlaying) Brush.verticalGradient(listOf(BrandCoral, BrandGold))
                        else Brush.verticalGradient(listOf(Color.Gray, Color.DarkGray))
                    )
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
    onSelectTrack: (title: String, artist: String, category: String, url: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val searchResults = remember { mutableStateListOf<HangoutTrack>() }
    var selectedGenre by remember { mutableStateOf("All") }
    val genreList = listOf("All", "Lo-Fi Chill", "Acoustic Romantic", "Cafe Jazz", "Party Beats", "EDM Club")

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Prevent dialog from falling back / dismissing when closing keyboard
    BackHandler(enabled = isKeyboardOpen) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

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
            onSelectTrack(fileName, "Device Storage", "Local MP3", uri.toString())
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = !isKeyboardOpen)
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
                                            onSelectTrack(track.title, track.artist, track.category, track.url)
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
                                            Text("👤 ${track.artist} • 🏷️ ${track.category}", color = Color.Gray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Box(
                                        modifier = Modifier.size(32.dp).clip(CircleShape).background(BrandCoral),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(18.dp))
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
                                        onSelectTrack(track.title, track.artist, track.category, track.url)
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

                                Box(
                                    modifier = Modifier.size(28.dp).clip(CircleShape).background(BrandCoral),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(16.dp))
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
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = isKeyboardOpen) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isKeyboardOpen
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
// 5. MEMBER PROFILE SHEET / DIALOG
// -------------------------------------------------------------
@Composable
fun RoomMemberProfileSheet(
    member: RoomMember,
    userProfile: UserProfile?,
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onLike: () -> Unit,
    onDirectMessage: () -> Unit,
    onRequestMessage: () -> Unit
) {
    val isVipOrPlatinum = currentUser.isVipOrPlatinum()
    val isMemberVip = member.tier.contains("VIP", ignoreCase = true)
    val isMemberPlat = member.tier.contains("Platinum", ignoreCase = true)

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
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Member Photo with Tier Ring
                Box(
                    modifier = Modifier
                        .size(100.dp)
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

                Spacer(modifier = Modifier.height(10.dp))

                // Name & Age
                val ageText = userProfile?.age?.let { ", $it" } ?: ""
                Text(
                    text = "${member.name}$ageText",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Role & Tier Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (member.role == "host") {
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

                // City & Intent
                val cityText = userProfile?.city ?: "Kolkata"
                Text(
                    text = "📍 $cityText",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )

                // Bio
                val bioText = userProfile?.bio?.ifEmpty { "Excited to meet and hangout with awesome people!" }
                    ?: "Excited to meet and hangout with awesome people!"
                Text(
                    text = "\"$bioText\"",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Send Like / Match Request
                    Button(
                        onClick = {
                            onLike()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Like / Match", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Direct Message (VIP) or Request (Standard)
                    Button(
                        onClick = {
                            onDismiss()
                            if (isVipOrPlatinum) {
                                onDirectMessage()
                            } else {
                                onRequestMessage()
                            }
                        },
                        modifier = Modifier.weight(1f).height(42.dp),
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
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isVipOrPlatinum) "Direct Chat" else "Msg Request",
                            color = if (isVipOrPlatinum) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
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
    var selectedMemberForSheet by remember { mutableStateOf<RoomMember?>(null) }
    var selectedMemberForRequest by remember { mutableStateOf<RoomMember?>(null) }

    val isUserHost = currentUser.uid == room.hostId || (room.hostId.isEmpty() && activeMembers.firstOrNull()?.uid == currentUser.uid)
    val isMuted by AudioPlayerManager.isMutedState
    val localPlaying by AudioPlayerManager.isPlayingState

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

    // Prevent full room dialog from exiting when dismissing soft keyboard
    BackHandler(enabled = isKeyboardOpen) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isKeyboardOpen
        )
    ) {
        Scaffold(
            containerColor = BrandDark,
            topBar = {
                // Luxury Room Banner Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    AsyncImage(
                        model = room.banner_image_url.ifEmpty { "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3" },
                        contentDescription = null,
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
                            .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                    text = "${room.title} ${room.emoji}",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                // Real-Time Viewers Count Chip (Pure Firestore count)
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
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Leave Room Button
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Leave", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // ---------------------------------------------------------
                // HOST SHOWCASE (TOP-MIDDLE WITH GLOWING CORAL RING & BADGE)
                // ---------------------------------------------------------
                val hostMember = activeMembers.find { it.uid == room.hostId }
                val hostName = room.hostName.ifEmpty { hostMember?.name ?: currentUser.name }
                val hostPhoto = room.hostPhotoUrl.ifEmpty { hostMember?.photoUrl ?: currentUser.photo }

                Card(
                    colors = CardDefaults.cardColors(containerColor = BrandCard),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrandBorder, RoundedCornerShape(18.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Host Avatar with Animated Glowing Coral Ring
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 3.dp,
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

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "👑 Host: $hostName",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isUserHost) "YOU" else "LIVE",
                                    color = BrandGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier
                                        .background(BrandGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }

                            Text(
                                text = if (room.isCustom) "Custom Hangout Room" else "Intent Lounge • ${room.title}",
                                color = BrandOrange,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ---------------------------------------------------------
                // ROOM MUSIC / AUDIO ENGINE BAR & CONTROLS
                // ---------------------------------------------------------
                Card(
                    colors = CardDefaults.cardColors(containerColor = BrandCardSubtle),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrandBorder, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Visualizer Wave & Song Info
                        val isActuallyPlaying = localPlaying && room.isPlaying
                        val isBuffering by AudioPlayerManager.isBufferingState

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            MusicVisualizerWave(isPlaying = isActuallyPlaying)

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = if (room.nowPlayingTitle.isNotBlank()) "🎶 ${room.nowPlayingTitle}" else "🔇 Lounge Audio Idle",
                                    color = if (isActuallyPlaying) BrandGold else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isBuffering) "⏳ Buffering audio stream..."
                                    else if (isActuallyPlaying) "🔊 Playing live across room"
                                    else if (room.isPlaying) "Connecting stream..."
                                    else "Host can select a soundtrack",
                                    color = if (isActuallyPlaying) BrandGreen else Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Host vs Listener Controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isUserHost) {
                                // Track Selector
                                IconButton(
                                    onClick = { showTrackSelector = true },
                                    modifier = Modifier.size(34.dp).background(BrandDark, CircleShape)
                                ) {
                                    Icon(Icons.Default.MusicNote, contentDescription = "Change Track", tint = BrandCyan, modifier = Modifier.size(18.dp))
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Play / Pause Toggle
                                IconButton(
                                    onClick = {
                                        val nextPlaying = !room.isPlaying
                                        db.collection("rooms").document(room.id)
                                            .update("isPlaying", nextPlaying)
                                        if (nextPlaying) {
                                            if (room.nowPlayingUrl.isBlank()) {
                                                val defaultTrack = CURATED_HANGOUT_TRACKS[0]
                                                db.collection("rooms").document(room.id).update(
                                                    mapOf(
                                                        "nowPlayingTitle" to defaultTrack.title,
                                                        "nowPlayingUrl" to defaultTrack.url,
                                                        "isPlaying" to true
                                                    )
                                                )
                                            } else {
                                                AudioPlayerManager.resume()
                                            }
                                        } else {
                                            AudioPlayerManager.pause()
                                        }
                                    },
                                    modifier = Modifier.size(34.dp).background(BrandCoral, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (room.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                // Listener Mute/Unmute Toggle
                                IconButton(
                                    onClick = { AudioPlayerManager.toggleMute() },
                                    modifier = Modifier.size(34.dp).background(BrandDark, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                        contentDescription = "Mute Toggle",
                                        tint = if (isMuted) Color.Gray else BrandGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ---------------------------------------------------------
                // LIVE MEMBERS GRID (REAL-TIME FIRESTORE LISTENER)
                // ---------------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Active Members (${activeMembers.size})",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap avatar to interact",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (activeMembers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Connecting to room presence...",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(activeMembers) { member ->
                            val isMemberVip = member.tier.contains("VIP", ignoreCase = true)
                            val isMemberPlat = member.tier.contains("Platinum", ignoreCase = true)
                            val isSelf = member.uid == currentUser.uid

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelf) BrandCardSubtle else BrandCard
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!isSelf) {
                                            selectedMemberForSheet = member
                                        }
                                    }
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelf) BrandCoral.copy(alpha = 0.5f) else BrandBorder,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Avatar with Online Dot
                                    Box(contentAlignment = Alignment.BottomEnd) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
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

                                        // Online indicator dot
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(BrandGreen)
                                                .border(1.dp, BrandDark, CircleShape)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = if (isSelf) "You (${member.name})" else member.name,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Role / Tier chip
                                    if (member.role == "host") {
                                        Text(
                                            text = "👑 Host",
                                            color = BrandGold,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Text(
                                            text = if (isMemberVip) "VIP ★" else if (isMemberPlat) "PLATINUM" else "Listener",
                                            color = if (isMemberVip) BrandGold else if (isMemberPlat) BrandCyan else Color.Gray,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ---------------------------------------------------------
                // LIVE STREAMING FLOWING COMMENTS OVERLAY & PUBLIC CHAT BAR
                // ---------------------------------------------------------
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    // Flowing Comments Overlay (Latest 4 comments)
                    if (liveComments.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(liveComments.takeLast(4)) { comment ->
                                val isVip = comment.tier.contains("VIP", true)
                                val isPlat = comment.tier.contains("Platinum", true)
                                val nameColor = if (isVip) BrandGold else if (isPlat) BrandCyan else BrandCoral

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .border(0.5.dp, BrandBorder, RoundedCornerShape(50))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = comment.userPhoto.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp).clip(CircleShape),
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
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Public Room Chat Bar (Open for all members)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(BrandCard)
                            .border(1.dp, BrandBorder, RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentInputText,
                            onValueChange = { commentInputText = it },
                            placeholder = { Text("Say something in room...", color = Color.Gray, fontSize = 11.sp) },
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
                            modifier = Modifier.size(32.dp).background(BrandCoral, CircleShape)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
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
                onSelectTrack = { title, artist, category, url ->
                    db.collection("rooms").document(room.id).update(
                        mapOf(
                            "currentPlayingTrack" to title,
                            "nowPlayingTitle" to title,
                            "nowPlayingArtist" to artist,
                            "nowPlayingUrl" to url,
                            "isPlaying" to true,
                            "playbackPosition" to 0L
                        )
                    )
                    AudioPlayerManager.play(context, url, title)
                    Toast.makeText(context, "Streaming: $title by $artist", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 2. Member Profile Sheet Dialog
        if (selectedMemberForSheet != null) {
            val member = selectedMemberForSheet!!
            val profile = allProfiles.find { it.uid == member.uid }

            RoomMemberProfileSheet(
                member = member,
                userProfile = profile,
                currentUser = currentUser,
                onDismiss = { selectedMemberForSheet = null },
                onLike = {
                    recordSwipe(db, currentUser.uid, member.uid, "like")
                    profile?.let { partner ->
                        checkAndHandleMutualMatch(db, currentUser, partner, onMatch = {})
                    }
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
                        colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.9f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Live active count & Host chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(0.5.dp, BrandGreen.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(BrandGreen))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("$activeCount Active", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Live Audio Lounge 🎙️",
                    color = BrandOrange,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

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
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = isKeyboardOpen) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isKeyboardOpen
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

