# LiveTogather Permanent Release Build & Deployment Rules

Whenever requested to generate a release build or signed AAB bundle for LiveTogather, strictly adhere to the following rules:

1. **AUTO-INCREMENT VERSION:**
   - Always check the current `versionCode` and `versionName` in `app/build.gradle.kts`.
   - Automatically increment `versionCode` to the next sequential integer (e.g. 29 -> 30) and update `versionName` accordingly (e.g. "1.0.28" -> "1.0.29") before building, preventing version collision errors in Google Play Console.

2. **OFFICIAL SIGNATURE & GOOGLE SERVICES:**
   - Always sign release builds using the official release keystore (`livetogather-release.jks`, alias: `livetogather-key`, SHA-1: `B4:8F:28:50:04:20:A2:B4:BC:7C:76:26:7C:51:8F:3F:12:60:55:97`).
   - Retain and preserve the active `app/google-services.json` containing the registered release SHA-1 client ID to ensure Google Sign-In operates without DEVELOPER_ERROR (10).

3. **STRICT CODE INTEGRITY:**
   - Never modify, overwrite, delete, or alter any unrelated business logic, Kotlin/Java code, UI screens, Jetpack Compose layouts, XML resources, audio/VIP features, or Gradle dependencies.
   - Only modify what is strictly required for the version increment and signing configuration.

4. **IMMEDIATE OUTPUT & CONVENIENCE COPIES:**
   - Immediately provide the exact absolute file path of the generated signed release AAB.
   - Copy the final `.aab` to `C:\Users\user\Downloads\Hindi Romantic\app-release.aab` and `C:\Users\user\Downloads\app-release.aab` for immediate Play Console upload.

5. **PERMANENT PINNED RULE - STRICT SCOPE & NO TOUCH POLICY:**
   - DO NOT MODIFY, DO NOT OVERWRITE, DO NOT DELETE any function, any UI layout, or existing screens/features unless explicitly instructed by the user.
   - NEVER roll back or revert to any back/older version.
   - When the user selects or instructs to work on a particular item/feature, focus EXCLUSIVELY on that specific target. All other components, files, and layouts must remain 100% untouched and preserved.

6. **100% REAL-TIME FIRESTORE DATA BINDING (ZERO DUMMY DATA):**
   - Always check and verify that all data fields, stats, counters (visitors, followers, following, gifts, badges, levels) are strictly connected to real-time Cloud Firestore.
   - Proactively detect any missing or disconnected field and bind it directly to Firestore so that everything operates in live real-time without fake or hardcoded values.

7. **AUTO-INSTALL ON CONNECTED DEVICE UPON COMPLETION:**
   - Once a task or feature implementation is complete and verified, always automatically install the latest build on the active connected device (via adb) so the user can immediately test and experience the changes on their phone.

8. **ALWAYS PROVIDE AAB / APK FILE LOCATION PATH AT THE END:**
   - At the completion of every build or deployment task, always clearly provide the exact absolute file path and clickable link to the generated `.aab` / `.apk` at the end of the message.




