package com.devil.livetogather

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore

val BrandDark = Color(0xFF0B090A)
val BrandCard = Color(0xFF161214)
val BrandCoral = Color(0xFFFF5E62)
val BrandOrange = Color(0xFFFF9966)
val BrandBorder = Color(0xFF2A2226)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiveTogatherApp()
        }
    }
}

@Composable
fun LiveTogatherApp() {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    var activeUserId by remember { mutableStateOf("USER_A") }
    var currentScreen by remember { mutableStateOf("discover") }
    var selectedCategory by remember { mutableStateOf("all") }
    var matchedPartner by remember { mutableStateOf<UserProfile?>(null) }
    var activeChatPartner by remember { mutableStateOf<UserProfile?>(null) }

    val userMap = remember {
        mutableStateMapOf(
            "USER_A" to UserProfile(
                uid = "USER_A",
                name = "Dilshad Warsi",
                age = 24,
                city = "Kolkata",
                photo = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=400&q=80",
                activeIntent = "free_tonight",
                hobbies = "Travelling, Music, Late-night drives",
                plan = "VIP"
            ),
            "USER_B" to UserProfile(
                uid = "USER_B",
                name = "Maya",
                age = 24,
                city = "Kolkata",
                photo = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
                activeIntent = "free_tonight",
                bio = "Love late-night coffee and spontaneous road trips 🚗",
                hobbies = "Dancing, Movies, Photography",
                plan = "Platinum"
            )
        )
    }

    val swipes = remember { mutableStateListOf<SwipeRecord>() }
    val matches = remember { mutableStateListOf<MatchItem>() }
    val messages = remember { mutableStateMapOf<String, MutableList<ChatMessage>>() }
    val moments = remember {
        mutableStateListOf(
            MomentItem(
                id = "M1",
                userId = "USER_B",
                userName = "Maya",
                userPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
                mediaUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=600&q=80",
                caption = "Heading to Park Street cafe tonight! ☕🥂",
                likes = 3
            )
        )
    }

    val currentUser = userMap[activeUserId] ?: UserProfile()
    val otherUser = userMap.values.firstOrNull { it.uid != activeUserId }

    Box(modifier = Modifier.fillMaxSize().background(BrandDark)) {
        Scaffold(
            containerColor = BrandDark,
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().background(BrandCard).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "LiveTogather",
                        color = BrandCoral,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )

                    Button(
                        onClick = {
                            activeUserId = if (activeUserId == "USER_A") "USER_B" else "USER_A"
                            Toast.makeText(context, "Switched to ${userMap[activeUserId]?.name}", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBorder),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Default.SwitchAccount, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = currentUser.name.split(" ")[0], color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            bottomBar = {
                NavigationBar(containerColor = BrandCard) {
                    NavigationBarItem(
                        selected = currentScreen == "discover",
                        onClick = { currentScreen = "discover" },
                        icon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = "Discover", tint = if (currentScreen == "discover") BrandCoral else Color.Gray) },
                        label = { Text("Discover", fontSize = 10.sp, color = if (currentScreen == "discover") BrandCoral else Color.Gray) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "likes",
                        onClick = { currentScreen = "likes" },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Likes", tint = if (currentScreen == "likes") BrandCoral else Color.Gray) },
                        label = { Text("Likes", fontSize = 10.sp, color = if (currentScreen == "likes") BrandCoral else Color.Gray) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "chats",
                        onClick = { currentScreen = "chats" },
                        icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Chats", tint = if (currentScreen == "chats") BrandCoral else Color.Gray) },
                        label = { Text("Chats", fontSize = 10.sp, color = if (currentScreen == "chats") BrandCoral else Color.Gray) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "moments",
                        onClick = { currentScreen = "moments" },
                        icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Moments", tint = if (currentScreen == "moments") BrandCoral else Color.Gray) },
                        label = { Text("Moments", fontSize = 10.sp, color = if (currentScreen == "moments") BrandCoral else Color.Gray) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "profile",
                        onClick = { currentScreen = "profile" },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile", tint = if (currentScreen == "profile") BrandCoral else Color.Gray) },
                        label = { Text("Profile", fontSize = 10.sp, color = if (currentScreen == "profile") BrandCoral else Color.Gray) }
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                when (currentScreen) {
                    "discover" -> {
                        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(CATEGORY_LIST) { cat ->
                                    val isSelected = selectedCategory == cat.id
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (isSelected) Brush.horizontalGradient(listOf(BrandCoral, BrandOrange)) else Brush.linearGradient(listOf(BrandCard, BrandCard)))
                                            .border(1.dp, if (isSelected) Color.Transparent else BrandBorder, RoundedCornerShape(50))
                                            .clickable { selectedCategory = cat.id }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = cat.label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (otherUser != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(24.dp))
                                        .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
                                ) {
                                    AsyncImage(
                                        model = otherUser.photo,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f)),
                                                    startY = 400f
                                                )
                                            )
                                    )
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(16.dp)
                                    ) {
                                        Text(text = "🍷 ${otherUser.activeIntent.replace('_', ' ').uppercase()}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(BrandCoral, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "${otherUser.name}, ${otherUser.age}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                        Text(text = otherUser.bio, color = Color.LightGray, fontSize = 12.sp)
                                        Text(text = "📍 ${otherUser.city} • 2.5 km away", color = BrandOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            IconButton(
                                                onClick = { Toast.makeText(context, "Passed", Toast.LENGTH_SHORT).show() },
                                                modifier = Modifier.size(50.dp).background(Color.Black.copy(alpha = 0.7f), CircleShape).border(1.dp, Color.Red, CircleShape)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red)
                                            }

                                            IconButton(
                                                onClick = {
                                                    swipes.add(SwipeRecord(fromUserId = currentUser.uid, toUserId = otherUser.uid, action = "like"))
                                                    val mutual = swipes.any { it.fromUserId == otherUser.uid && it.toUserId == currentUser.uid && it.action == "like" }
                                                    if (mutual) {
                                                        val matchId = listOf(currentUser.uid, otherUser.uid).sorted().joinToString("_")
                                                        if (matches.none { it.matchId == matchId }) {
                                                            matches.add(MatchItem(matchId = matchId, users = listOf(currentUser.uid, otherUser.uid), matchedIntent = currentUser.activeIntent))
                                                        }
                                                        matchedPartner = otherUser
                                                    } else {
                                                        Toast.makeText(context, "Like recorded! Waiting for match.", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(54.dp).background(Brush.horizontalGradient(listOf(BrandCoral, BrandOrange)), CircleShape)
                                            ) {
                                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "likes" -> {
                        val inboundLikes = swipes.filter { it.toUserId == currentUser.uid && it.action == "like" }
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Text("Who Liked You", color = BrandCoral, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (inboundLikes.isEmpty()) {
                                Text("No incoming likes yet.", color = Color.Gray, fontSize = 12.sp)
                            } else {
                                inboundLikes.forEach { swipe ->
                                    val liker = userMap[swipe.fromUserId]
                                    if (liker != null) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = BrandCard)
                                        ) {
                                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                AsyncImage(model = liker.photo, contentDescription = null, modifier = Modifier.size(50.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("${liker.name}, ${liker.age}", color = Color.White, fontWeight = FontWeight.Bold)
                                                    Text("Intent: ${liker.activeIntent}", color = BrandOrange, fontSize = 11.sp)
                                                }
                                                Button(
                                                    onClick = {
                                                        swipes.add(SwipeRecord(fromUserId = currentUser.uid, toUserId = liker.uid, action = "like"))
                                                        val matchId = listOf(currentUser.uid, liker.uid).sorted().joinToString("_")
                                                        matches.add(MatchItem(matchId = matchId, users = listOf(currentUser.uid, liker.uid), matchedIntent = currentUser.activeIntent))
                                                        matchedPartner = liker
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCoral)
                                                ) {
                                                    Text("Match Back", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "chats" -> {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Text("Matches & Inbox", color = BrandCoral, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (matches.isEmpty()) {
                                Text("No matches yet. Like back to start chatting!", color = Color.Gray, fontSize = 12.sp)
                            } else {
                                matches.forEach { m ->
                                    val pId = m.users.firstOrNull { it != currentUser.uid }
                                    val partner = userMap[pId]
                                    if (partner != null) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                                activeChatPartner = partner
                                            },
                                            colors = CardDefaults.cardColors(containerColor = BrandCard)
                                        ) {
                                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                AsyncImage(model = partner.photo, contentDescription = null, modifier = Modifier.size(44.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(partner.name, color = Color.White, fontWeight = FontWeight.Bold)
                                                    Text(m.lastMessage, color = Color.Gray, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "moments" -> {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Moments Feed", color = BrandCoral, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = {
                                        moments.add(
                                            0,
                                            MomentItem(
                                                id = "M${moments.size + 1}",
                                                userId = currentUser.uid,
                                                userName = currentUser.name,
                                                userPhoto = currentUser.photo,
                                                mediaUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=600&q=80",
                                                caption = "Late night drive vibes!",
                                                likes = 0
                                            )
                                        )
                                        Toast.makeText(context, "Moment Shared!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCoral)
                                ) {
                                    Text("+ Post Moment", fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(moments) { item ->
                                    Card(colors = CardDefaults.cardColors(containerColor = BrandCard)) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                AsyncImage(model = item.userPhoto, contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(item.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(item.caption, color = Color.White, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            AsyncImage(model = item.mediaUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "profile" -> {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Text("User Profile & Membership", color = BrandCoral, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(model = currentUser.photo, contentDescription = null, modifier = Modifier.size(64.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(currentUser.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text("${currentUser.city} • ${currentUser.age} yrs", color = Color.LightGray, fontSize = 12.sp)
                                    Text("Plan: ${currentUser.plan}", color = BrandOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = BrandCard), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Active Intent Status", color = BrandCoral, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("🍷 ${currentUser.activeIntent.replace('_', ' ').uppercase()}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Hobbies: ${currentUser.hobbies}", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (matchedPartner != null) {
            AlertDialog(
                onDismissRequest = { matchedPartner = null },
                containerColor = BrandCard,
                title = {
                    Text("IT'S A MATCH! 🎉", color = BrandCoral, fontWeight = FontWeight.Black, fontSize = 20.sp)
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("You and ${matchedPartner?.name} liked each other!", color = Color.White, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.Center) {
                            AsyncImage(model = currentUser.photo, contentDescription = null, modifier = Modifier.size(54.dp).clip(CircleShape).border(2.dp, BrandCoral, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            AsyncImage(model = matchedPartner?.photo, contentDescription = null, modifier = Modifier.size(54.dp).clip(CircleShape).border(2.dp, BrandOrange, CircleShape))
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val p = matchedPartner
                            matchedPartner = null
                            activeChatPartner = p
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral)
                    ) {
                        Text("Send Instant Message")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { matchedPartner = null }) {
                        Text("Keep Exploring", color = Color.Gray)
                    }
                }
            )
        }

        if (activeChatPartner != null) {
            val partner = activeChatPartner!!
            val matchKey = listOf(currentUser.uid, partner.uid).sorted().joinToString("_")
            var chatInput by remember { mutableStateOf("") }
            val currentChatList = messages.getOrPut(matchKey) { mutableStateListOf() }

            Box(modifier = Modifier.fillMaxSize().background(BrandDark)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(BrandCard).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { activeChatPartner = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        AsyncImage(model = partner.photo, contentDescription = null, modifier = Modifier.size(36.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(partner.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Plan: ${partner.activeIntent}", color = BrandOrange, fontSize = 10.sp)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentChatList) { msg ->
                            val isMe = msg.senderId == currentUser.uid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isMe) BrandCoral else BrandCard)
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(msg.text, color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().background(BrandCard).padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Type a message...", color = Color.Gray, fontSize = 12.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    currentChatList.add(ChatMessage(senderId = currentUser.uid, text = chatInput))
                                    chatInput = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = BrandCoral)
                        }
                    }
                }
            }
        }
    }
}