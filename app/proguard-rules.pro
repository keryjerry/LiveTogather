# Firestore creates these Kotlin data models reflectively from document fields.
-keep class com.devil.livetogather.UserProfile { *; }
-keep class com.devil.livetogather.MatchItem { *; }
-keep class com.devil.livetogather.ChatMessage { *; }
-keep class com.devil.livetogather.NotificationItem { *; }
-keep class com.devil.livetogather.SwipeRecord { *; }
-keep class com.devil.livetogather.GiftEvent { *; }
-keep class com.devil.livetogather.LiveEmojiReaction { *; }
-keep class com.devil.livetogather.IntimacyBond { *; }
-keep class com.devil.livetogather.RoomMember { *; }
-keep class com.devil.livetogather.LiveRoomComment { *; }
-keep class com.devil.livetogather.MicSeat { *; }
-keep class com.devil.livetogather.MomentItem { *; }
-keep class com.devil.livetogather.CommentItem { *; }
-keep class com.devil.livetogather.EarningTransaction { *; }

# Agora's native JNI_OnLoad resolves internal Java classes by their original names.
-keep class io.agora.** { *; }
-dontwarn com.google.devtools.build.android.desugar.runtime.ThrowableExtension
