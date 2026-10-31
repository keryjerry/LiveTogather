package com.devil.livetogather

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class SocialUserItem(
    val uid: String = "",
    val name: String = "",
    val photo: String = "",
    val timestamp: Long = 0L,
    val isFollowing: Boolean = false
)

/**
 * Real-Time Following & Followers List Dialog.
 *
 * Implements:
 * 1. Shows user's real-time following list (`users/{uid}/following`) and followers list (`users/{uid}/followers`).
 * 2. Unfollow action with real-time count decrement.
 * 3. Follow Back / Follow action with real-time count increment.
 * 4. Strictly private to the user themselves (hidden when viewing other users' profiles).
 */
@Composable
fun SocialConnectionsDialog(
    initialTab: Int = 0, // 0 = Following, 1 = Followers
    currentUser: UserProfile,
    db: FirebaseFirestore,
    onDismiss: () -> Unit,
    onOpenUserChat: ((UserProfile) -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    val followingList = remember { mutableStateListOf<SocialUserItem>() }
    val followersList = remember { mutableStateListOf<SocialUserItem>() }
    var isLoadingFollowing by remember { mutableStateOf(true) }
    var isLoadingFollowers by remember { mutableStateOf(true) }

    // Real-Time Listeners for Following and Followers
    DisposableEffect(currentUser.uid) {
        if (currentUser.uid.isBlank()) return@DisposableEffect onDispose {}

        val followingSub = db.collection("users").document(currentUser.uid)
            .collection("following")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        val uId = doc.getString("hostUid") ?: doc.getString("uid") ?: doc.id
                        val name = doc.getString("hostName") ?: doc.getString("name") ?: "User"
                        val photo = doc.getString("hostPhoto") ?: doc.getString("photo") ?: ""
                        val time = doc.getLong("timestamp") ?: 0L
                        SocialUserItem(uid = uId, name = name, photo = photo, timestamp = time, isFollowing = true)
                    }
                    followingList.clear()
                    followingList.addAll(list)
                }
                isLoadingFollowing = false
            }

        val followersSub = db.collection("users").document(currentUser.uid)
            .collection("followers")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        val uId = doc.getString("uid") ?: doc.id
                        val name = doc.getString("name") ?: "User"
                        val photo = doc.getString("photo") ?: ""
                        val time = doc.getLong("timestamp") ?: 0L
                        SocialUserItem(uid = uId, name = name, photo = photo, timestamp = time, isFollowing = false)
                    }
                    followersList.clear()
                    followersList.addAll(list)
                }
                isLoadingFollowers = false
            }

        onDispose {
            followingSub.remove()
            followersSub.remove()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            color = Color(0xFF0F0B18)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = Color(0xFFFF4081).copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFFFF4081), modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (selectedTab == 0) "Following (${followingList.size})" else "Followers (${followersList.size})",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                // Tab Switcher (Following vs Followers)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1728))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Following (${followingList.size})", "Followers (${followersList.size})").forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Brush.horizontalGradient(
                                        listOf(Color(0xFFFF3366), Color(0xFFFF7700))
                                    ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                )
                                .clickable { selectedTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content List
                val currentList = if (selectedTab == 0) followingList else followersList
                val isLoading = if (selectedTab == 0) isLoadingFollowing else isLoadingFollowers

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFF4081), strokeWidth = 2.5.dp)
                    }
                } else if (currentList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Text(if (selectedTab == 0) "🌟" else "👥", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (selectedTab == 0) "You are not following anyone yet" else "No followers yet",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (selectedTab == 0) "Join live audio & video rooms to discover and follow awesome broadcasters!"
                                else "Start broadcasting or post moments to grow your LiveTogather fanbase!",
                                color = Color.Gray,
                                fontSize = 11.5.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(currentList, key = { it.uid }) { item ->
                            val isFollowingThisUser = followingList.any { it.uid == item.uid }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF191326),
                                border = BorderStroke(1.dp, Color(0xFF2C223E)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        AsyncImage(
                                            model = item.photo.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                            contentDescription = item.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .border(1.2.dp, Color(0xFFFFD700), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = item.name.ifBlank { "LiveTogather User" },
                                                color = Color.White,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "ID: ${item.uid.take(8)}",
                                                color = Color(0xFFFFD700),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    // Action Button
                                    if (selectedTab == 0) {
                                        // Unfollow Button
                                        OutlinedButton(
                                            onClick = {
                                                db.collection("users").document(currentUser.uid)
                                                    .collection("following").document(item.uid).delete()
                                                db.collection("users").document(item.uid)
                                                    .collection("followers").document(currentUser.uid).delete()
                                                db.collection("users").document(currentUser.uid)
                                                    .update("followingCount", FieldValue.increment(-1))
                                                db.collection("users").document(item.uid)
                                                    .update("followerCount", FieldValue.increment(-1))
                                                Toast.makeText(context, "Unfollowed ${item.name}", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color(0xFF6B7280)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Unfollow", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    } else {
                                        // Followers Tab: Follow Back / Following
                                        if (isFollowingThisUser) {
                                            OutlinedButton(
                                                onClick = {
                                                    db.collection("users").document(currentUser.uid)
                                                        .collection("following").document(item.uid).delete()
                                                    db.collection("users").document(item.uid)
                                                        .collection("followers").document(currentUser.uid).delete()
                                                    db.collection("users").document(currentUser.uid)
                                                        .update("followingCount", FieldValue.increment(-1))
                                                    db.collection("users").document(item.uid)
                                                        .update("followerCount", FieldValue.increment(-1))
                                                    Toast.makeText(context, "Unfollowed ${item.name}", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, Color(0xFF10B981)),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Following ✓", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    db.collection("users").document(currentUser.uid)
                                                        .collection("following").document(item.uid).set(
                                                            mapOf(
                                                                "hostUid" to item.uid,
                                                                "name" to item.name,
                                                                "photo" to item.photo,
                                                                "timestamp" to System.currentTimeMillis()
                                                            )
                                                        )
                                                    db.collection("users").document(item.uid)
                                                        .collection("followers").document(currentUser.uid).set(
                                                            mapOf(
                                                                "uid" to currentUser.uid,
                                                                "name" to currentUser.name,
                                                                "photo" to currentUser.photo,
                                                                "timestamp" to System.currentTimeMillis()
                                                            )
                                                        )
                                                    db.collection("users").document(currentUser.uid)
                                                        .update("followingCount", FieldValue.increment(1))
                                                    db.collection("users").document(item.uid)
                                                        .update("followerCount", FieldValue.increment(1))
                                                    Toast.makeText(context, "Now following ${item.name}! 🌟", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3366)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("+ Follow Back", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
