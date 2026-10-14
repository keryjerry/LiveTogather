package com.devil.livetogather

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color constants matching LiveTogather Brand Dark theme
private val PolicyBgDark = Color(0xFF0B090A)
private val PolicyCardDark = Color(0xFF161214)
private val PolicyCardSubtle = Color(0xFF201A1D)
private val PolicyCoral = Color(0xFFFF5E62)
private val PolicyOrange = Color(0xFFFF9966)
private val PolicyGold = Color(0xFFFFD700)
private val PolicyBorder = Color(0xFF2A2226)
private val PolicyTextWhite = Color(0xFFFFFFFF)
private val PolicyTextLight = Color(0xFFE2E2E6)
private val PolicyTextMuted = Color(0xFFA09B9E)
private val PolicyGreen = Color(0xFF00E676)

/**
 * Dedicated Full-Screen Privacy Policy for LiveTogather.
 * Developed by MD Dilshad Warsi for AYAD Creations.
 *
 * Implements complete, unabridged clauses with dark theme aesthetics (#0B090A, #161214, #FF5E62),
 * scrollable layout, and top bar back navigation returning to the Profile Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    // Intercept hardware and gesture back navigation
    BackHandler {
        onBack()
    }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = PolicyBgDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Privacy Policy",
                            color = PolicyTextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "LiveTogather • AYAD Creations",
                            color = PolicyCoral,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Profile",
                            tint = PolicyTextWhite
                        )
                    }
                },
                actions = {
                    Surface(
                        color = PolicyCoral.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, PolicyCoral.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "OFFICIAL",
                            color = PolicyCoral,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PolicyCardDark,
                    titleContentColor = PolicyTextWhite,
                    navigationIconContentColor = PolicyTextWhite
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PolicyBgDark)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App & Metadata Hero Card
            Card(
                colors = CardDefaults.cardColors(containerColor = PolicyCardDark),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(PolicyCoral, PolicyOrange))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(PolicyCoral.copy(alpha = 0.18f))
                                    .border(1.dp, PolicyCoral, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = PolicyCoral,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "LiveTogather",
                                    color = PolicyTextWhite,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Legal Privacy Agreement",
                                    color = PolicyOrange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            color = PolicyGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, PolicyGreen.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = PolicyGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = PolicyBorder)

                    PolicyMetaItem(label = "Application", value = "LiveTogather")
                    PolicyMetaItem(label = "Organization", value = "AYAD Creations")
                    PolicyMetaItem(label = "Lead Developer", value = "MD Dilshad Warsi")
                    PolicyMetaItem(label = "Official Inquiries", value = "servicesecure822@gmail.com")
                    PolicyMetaItem(label = "Effective Date", value = "October 15, 2026")
                    PolicyMetaItem(label = "Platform", value = "Android (Native Compose)")
                }
            }

            // Introduction Card
            Card(
                colors = CardDefaults.cardColors(containerColor = PolicyCardDark),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, PolicyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "OUR COMMITMENT TO YOUR PRIVACY",
                        color = PolicyGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "AYAD Creations (\"we\", \"us\", or \"our\") operates the LiveTogather mobile application. We respect your digital privacy and are wholeheartedly dedicated to protecting the personal information you share with us. This Privacy Policy details the exact types of information we collect, the specific operational purposes for which it is processed, our stringent storage protocols, device permission rationales, your absolute data rights, and the simple procedure for obtaining complete and irreversible data deletion.",
                        color = PolicyTextLight,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            // SECTION 1: INFORMATION WE COLLECT
            PolicySectionCard(
                sectionNumber = "01",
                sectionTitle = "Information We Collect",
                icon = Icons.Default.FolderShared
            ) {
                Text(
                    text = "To power dynamic matchmaking, audio hangout presence, and safe user interactions, LiveTogather collects specific categories of user information:",
                    color = PolicyTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                PolicySubclauseBox(
                    title = "1. Profile & Account Information",
                    description = "When you create or update your profile, we collect your chosen display name, age, gender identity, dating intent and preferences (\"Interested In\"), professional title, education, city, location name, personal bio quote, lifestyle tags, zodiac sign, languages spoken, and communication habits."
                )

                PolicySubclauseBox(
                    title = "2. Photos & Media Content",
                    description = "We store profile pictures and user-submitted multimedia posts published to the Moments feed, including associated captions, likes count, and upload timestamps. Media is compressed and stored securely to facilitate visual identity without capturing unrequested background camera footage."
                )

                PolicySubclauseBox(
                    title = "3. 1-on-1 Direct Chats & Conversations",
                    description = "When matched members communicate via direct chat, we store message strings, transmission timestamps, delivery statuses, sender IDs, recipient IDs, and read receipts. Direct chat messages remain private between matched participants."
                )

                PolicySubclauseBox(
                    title = "4. Audio Hangout Activity & Intent Room Presence",
                    description = "When joining or hosting an Intent Room (such as Free Tonight, Coffee Catchup, Late Night Talks, or Gaming Hubs), we track your active presence marker, listener/speaker role, mute status, and synchronized audio playback state in real-time. Audio streams are ephemeral and transmitted live for communication purposes."
                )

                PolicySubclauseBox(
                    title = "5. Geolocation Data (GPS)",
                    description = "With your explicit runtime consent, we collect your precise geographical coordinates (latitude and longitude) and locality name to compute proximity distances to nearby members and filter local Intent Rooms. Location is never continuously tracked in the background when the app is closed."
                )

                PolicySubclauseBox(
                    title = "6. Device Identifiers & Technical Telemetry",
                    description = "For persistent account authentication, fraud prevention, and session recovery across app restarts, we collect your hardware device identifier (Android ID), device brand and model, operating system version, and anonymous crash telemetry."
                )
            }

            // SECTION 2: HOW WE USE INFORMATION
            PolicySectionCard(
                sectionNumber = "02",
                sectionTitle = "How We Use Information",
                icon = Icons.Default.SettingsSuggest
            ) {
                Text(
                    text = "All collected data is utilized exclusively for core functional requirements and safety protocols within the LiveTogather platform:",
                    color = PolicyTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                PolicySubclauseBox(
                    title = "1. Proximity Matchmaking & Intent Discovery",
                    description = "We compute mathematical distances between users based on coordinates, age filters, and active hangout categories to curate the Swipe Card deck and Intent Rooms catalog."
                )

                PolicySubclauseBox(
                    title = "2. Real-Time Audio Hangout Synchronization",
                    description = "We synchronize live listener lists, active host statuses, participant queues, and collaborative background music streaming across all participants inside an intent room."
                )

                PolicySubclauseBox(
                    title = "3. Tier Privileges & Premium Features",
                    description = "We verify your active membership tier (VIP Level ★, Platinum, or Standard) to grant premium perks such as first-message privileges (\"My Move\"), photo-verified chat preferences, and custom room hosting."
                )

                PolicySubclauseBox(
                    title = "4. Community Safety & Ban Enforcement",
                    description = "We use device identifiers and profile data to detect abusive behavior, prevent spam or harassment, maintain platform integrity, and enforce device-level bans against bad actors who violate Community Guidelines."
                )
            }

            // SECTION 3: DATA STORAGE, FIREBASE & SECURITY
            PolicySectionCard(
                sectionNumber = "03",
                sectionTitle = "Data Storage, Firebase Infrastructure & Security",
                icon = Icons.Default.CloudQueue
            ) {
                Text(
                    text = "We adhere to industry-leading enterprise security standards to protect all user data stored within our ecosystem:",
                    color = PolicyTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                PolicySubclauseBox(
                    title = "1. Google Firebase Cloud Infrastructure",
                    description = "All LiveTogather user records, intent rooms, moments, and chat documents are managed on Google Cloud Firestore, Firebase Authentication, and Firebase Cloud Storage under strict security rules."
                )

                PolicySubclauseBox(
                    title = "2. Absolute Zero Selling of Data",
                    description = "AYAD Creations does NOT sell, rent, lease, trade, or monetize your personal profile, chat history, or location coordinates to third-party data brokers, marketers, or external advertisers. Your data remains strictly within the LiveTogather operational ecosystem."
                )

                PolicySubclauseBox(
                    title = "3. Encryption in Transit & At Rest",
                    description = "All communications between your device and our cloud servers are encrypted using Transport Layer Security (TLS 1.3 / HTTPS). In addition, stored documents and media files are encrypted at rest on enterprise storage volumes with AES-256 bit encryption."
                )

                PolicySubclauseBox(
                    title = "4. Data Retention Limitations",
                    description = "We retain personal profile information and user generated content only for as long as your account remains active. When you delete your account or submit a deletion request, all personal documents are purged from the live database."
                )
            }

            // SECTION 4: DEVICE PERMISSIONS
            PolicySectionCard(
                sectionNumber = "04",
                sectionTitle = "Device Permissions",
                icon = Icons.Default.PermDeviceInformation
            ) {
                Text(
                    text = "LiveTogather requests specific Android device permissions strictly when necessary to deliver application features. You may review, grant, or revoke these permissions at any time via your device's System Settings:",
                    color = PolicyTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                PolicySubclauseBox(
                    title = "1. Location (ACCESS_FINE_LOCATION & ACCESS_COARSE_LOCATION)",
                    description = "Used to determine your approximate or precise city and coordinates so that nearby profiles and local Intent Rooms can be discovered. Never tracked when the application is completely closed."
                )

                PolicySubclauseBox(
                    title = "2. Media & Storage (READ_MEDIA_IMAGES / READ_EXTERNAL_STORAGE)",
                    description = "Required when you intentionally pick a picture from your device gallery to set your profile avatar or publish a photo to the Moments feed."
                )

                PolicySubclauseBox(
                    title = "3. Microphone (RECORD_AUDIO & MODIFY_AUDIO_SETTINGS)",
                    description = "Required solely to capture and transmit your voice when you are an active speaker or host in a LiveTogather Audio Hangout room. The microphone is never accessed in the background."
                )

                PolicySubclauseBox(
                    title = "4. Notifications (POST_NOTIFICATIONS)",
                    description = "Used to alert you in real-time when you receive a new match, an incoming 1-on-1 chat message, or when followed creators initiate an audio hangout broadcast."
                )
            }

            // SECTION 5: AGE RESTRICTION (STRICTLY 18+ ONLY)
            PolicySectionCard(
                sectionNumber = "05",
                sectionTitle = "Age Restriction (Strictly 18+ Only)",
                icon = Icons.Default.Warning
            ) {
                Surface(
                    color = PolicyCoral.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PolicyCoral.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔞", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STRICT ADULT REQUIREMENT",
                                color = PolicyCoral,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "LiveTogather is intended solely for adult individuals aged eighteen (18) years or older. Anyone under the age of 18 is strictly prohibited from creating an account or accessing the platform.",
                            color = PolicyTextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "AYAD Creations does not knowingly solicit or collect personal information from minors. If we discover or are notified that an account belongs to an individual under 18 years of age, we will immediately and permanently terminate the account, delete all related photos and messages from our servers, and restrict further access from that device.",
                    color = PolicyTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            // SECTION 6: USER RIGHTS & COMPLETE ACCOUNT DELETION
            PolicySectionCard(
                sectionNumber = "06",
                sectionTitle = "User Rights & Complete Account / Data Deletion",
                icon = Icons.Default.DeleteSweep
            ) {
                Text(
                    text = "You maintain full autonomy and legal rights over your personal data at all times, including the rights to access, rectify, or completely delete your personal information:",
                    color = PolicyTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                PolicySubclauseBox(
                    title = "1. In-App Immediate Self-Service Deletion",
                    description = "You can delete your account instantly at any time from within the application. Simply open the Profile tab, navigate to the Safety & Account section at the bottom, and tap \"Delete Account\". This permanently removes your profile record, matches, moments, and cached records."
                )

                PolicySubclauseBox(
                    title = "2. Complete Data Purge via Dedicated Email",
                    description = "For full archival data purges, server audit logs, or manual data erasure verification, you can contact our dedicated data deletion desk directly via email at:"
                )

                Surface(
                    color = PolicyCardSubtle,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PolicyGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = PolicyGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "servicesecure822@gmail.com",
                                color = PolicyGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Subject: \"LiveTogather Data Deletion Request\"\nInclude your registered display name or UID. Deletion requests are processed and verified within 30 business days.",
                            color = PolicyTextLight,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // SECTION 7: DEVELOPER & ORGANIZATION CONTACT DETAILS
            PolicySectionCard(
                sectionNumber = "07",
                sectionTitle = "Developer & Organization Contact Details",
                icon = Icons.AutoMirrored.Filled.ContactSupport
            ) {
                Text(
                    text = "If you have questions, feedback, or legal inquiries regarding this Privacy Policy or our data protection practices, please contact us directly:",
                    color = PolicyTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = PolicyCardSubtle,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PolicyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PolicyContactRow(icon = Icons.Default.Business, label = "Organization", value = "AYAD Creations")
                        PolicyContactRow(icon = Icons.Default.Person, label = "Lead Developer", value = "MD Dilshad Warsi")
                        PolicyContactRow(icon = Icons.Default.Email, label = "Official Contact Email", value = "servicesecure822@gmail.com")
                        PolicyContactRow(icon = Icons.Default.Event, label = "Policy Date", value = "October 15, 2026")
                        PolicyContactRow(icon = Icons.Default.Gavel, label = "Legal Status", value = "Binding Digital Privacy Policy")
                    }
                }
            }

            // Return to Profile Screen Action Button
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = PolicyCoral),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Return to Profile Screen",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PolicySectionCard(
    sectionNumber: String,
    sectionTitle: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PolicyCardDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, PolicyBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = PolicyCoral.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, PolicyCoral.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "SEC $sectionNumber",
                        color = PolicyCoral,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PolicyOrange,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = sectionTitle,
                    color = PolicyCoral,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = PolicyBorder)

            content()
        }
    }
}

@Composable
private fun PolicySubclauseBox(
    title: String,
    description: String
) {
    Surface(
        color = PolicyCardSubtle,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.8.dp, PolicyBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = PolicyGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = PolicyTextLight,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun PolicyMetaItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = PolicyTextMuted, fontSize = 12.sp)
        Text(
            text = value,
            color = PolicyTextWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PolicyContactRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PolicyCoral,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = label, color = PolicyTextMuted, fontSize = 10.sp)
            Text(text = value, color = PolicyTextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
