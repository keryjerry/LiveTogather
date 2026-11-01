-keep class com.devil.livetogather.** { *; }
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Firestore models
-keepclassmembers class * {
    @com.google.firebase.firestore.** *;
}

# Agora native JNI
-keep class io.agora.** { *; }
-dontwarn com.google.devtools.build.android.desugar.runtime.ThrowableExtension
