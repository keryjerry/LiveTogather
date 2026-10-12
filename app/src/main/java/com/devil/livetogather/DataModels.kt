package com.devil.livetogather

data class IntentCategory(val id: String, val label: String)

val CATEGORY_LIST = listOf(
    IntentCategory("all", "🔥 All Profiles"),
    IntentCategory("free_tonight", "🍷 Free Tonight"),
    IntentCategory("free_tomorrow", "🌙 Free Tomorrow"),
    IntentCategory("free_weekend", "🎉 Free in Weekend"),
    IntentCategory("ready_travel", "🧳 Ready for Travelling"),
    IntentCategory("travel_partner", "✈️ Travel Partner"),
    IntentCategory("hangout_partner", "🤝 Hangout Partner"),
    IntentCategory("free_party", "🥳 Free for Party"),
    IntentCategory("join_party", "🍸 Join Party"),
    IntentCategory("going_club", "🕺 Going to Club"),
    IntentCategory("plan_trip", "🗺️ Lets Plan Holiday Trip"),
    IntentCategory("lets_movie", "🍿 Lets Go For Movie"),
    IntentCategory("lets_cafe", "☕ Lets Go Cafe"),
    IntentCategory("lets_dinner", "🍽️ Lets Go For Dinner/Lunch")
)

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val age: Int = 24,
    val city: String = "Kolkata",
    val photo: String = "",
    val bio: String = "",
    val activeIntent: String = "free_tonight",
    val hobbies: String = "",
    val plan: String = "VIP"
)

data class SwipeRecord(
    val fromUserId: String = "",
    val toUserId: String = "",
    val action: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class MatchItem(
    val matchId: String = "",
    val users: List<String> = emptyList(),
    val matchedIntent: String = "",
    val lastMessage: String = "It's a Match! Say hello."
)

data class ChatMessage(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class MomentItem(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhoto: String = "",
    val mediaUrl: String = "",
    val caption: String = "",
    val likes: Int = 0
)
