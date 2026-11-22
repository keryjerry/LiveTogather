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
                    PolicyMetaItem(label = "Official Website", value = "ayadcreation.online")
                    PolicyMetaItem(label = "Lead Developer", value = "MD Dilshad Warsi")
                    PolicyMetaItem(label = "Official Inquiries", value = "servicesecure822@gmail.com")
                    PolicyMetaItem(label = "Effective Date", value = "November 12, 2026")
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
                    text = "To deliver dynamic proximity matchmaking, live audio/video broadcast rooms, secure direct messaging, and interactive in-room gaming, LiveTogather collects the following categories of data:",
                    color = PolicyTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                PolicySubclauseBox(
                    title = "1. Profile & Account Information",
                    description = "When setting up your profile, we collect your display name, age, gender identity, dating intent and preferences (\"Interested In\"), occupation, education, city, location name, bio quote, lifestyle tags, zodiac sign, languages spoken, and communication habits."
                )

                PolicySubclauseBox(
                    title = "2. Photos, Avatars & Moments Media",
                    description = "We securely store profile pictures, luxury avatar frames, and user-submitted multimedia posts published to the Moments feed, including captions, likes count, and upload timestamps. Media files are compressed and securely hosted on Google Cloud Storage."
                )

                PolicySubclauseBox(
                    title = "3. 1-on-1 Direct Chats & Messaging",
                    description = "When matched members communicate via direct chat, we store message texts, transmission timestamps, delivery statuses, sender and recipient UIDs, and read receipts. Chat content remains private between the conversation participants."
                )

                PolicySubclauseBox(
                    title = "4. Real-Time Room Activity & Presence",
                    description = "When hosting or joining an Intent Room (Audio or Video Hangout), we track your active presence marker, role (host, co-host, speaker, or listener), mute state, and synchronized background music queue. Audio and video streams are live and ephemeral."
                )

                PolicySubclauseBox(
                    title = "5. Geolocation Data (GPS)",
                    description = "With your explicit runtime consent, we collect precise latitude and longitude coordinates and locality names to compute proximity distances to nearby members and filter local rooms. Location is never tracked in the background when the application is closed."
                )

                PolicySubclauseBox(
                    title = "6. Device Identifiers & Hardware Telemetry",
                    description = "To ensure persistent authentication, prevent fraud, enforce device-level bans against abusive actors, and recover sessions across app restarts, we collect hardware device identifiers (Android ID), device brand/model, OS version, and anonymous crash telemetry."
                )

                PolicySubclauseBox(
                    title = "7. Push Notification Tokens",
                    description = "We generate and store anonymous Firebase Cloud Messaging (FCM) registration tokens to deliver real-time notifications for incoming direct messages, room invitations, gift receipts, and security alerts."
                )
            }

            // SECTION 2: REAL-TIME AUDIO & VIDEO STREAMING (AGORA RTC)
            PolicySectionCard(
                sectionNumber = "02",
                sectionTitle = "Real-Time Audio & Video Streaming (Agora RTC)",
                icon = Icons.Default.Videocam
            ) {
                Text(
                    text = "LiveTogather features ultra-low latency real-time audio and video communications powered by the industry-standard Agora RTC SDK:",
                    color = PolicyTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                PolicySubclauseBox(
                    title = "1. Live Ephemeral Transmission",
                    description = "All live voice conversations, video broadcasts, and microphone links inside Intent Rooms are transmitted dynamically through Agora RTC infrastructure in real-time. Media streams are ephemeral and delivered solely for active participants in the room."
                )

                PolicySubclauseBox(
                    title = "2. No Unauthorized Server Recording",
                    description = "AYAD Creations does NOT continuously record, archive, or store your live audio or video streams on our servers. When you leave a broadcast room or end a call, the live media session terminates immediately."
                )

                PolicySubclauseBox(
                    title = "3. Cryptographic Token Protection",
                    description = "RTC room access is protected by dynamic, time-limited cryptographic tokens (RtcTokenBuilder2) generated securely per user session to prevent unauthorized eavesdropping or room hijacking."
                )
            }

            // SECTION 3: PROFILE VERIFICATION & IDENTITY AUTHENTICITY (CAMERAX & AUDIO)
            PolicySectionCard(
                sectionNumber = "03",
                sectionTitle = "Profile Verification & Anti-Catfishing Authenticity",
                icon = Icons.Default.CameraAlt
            ) {
                Text(
                    text = "To protect our community from impersonation, catfishing, bots, and fraudulent profiles, LiveTogather provides advanced user verification mechanisms:",
                    color = PolicyTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                PolicySubclauseBox(
                    title = "1. Real-Time Front-Camera Selfie Verification",
                    description = "Users may voluntarily undergo photo verification via an interactive circular front-camera capture (powered by CameraX). The real-time selfie is verified against existing profile photos to award the official \"Photo-Verified ★\" badge."
                )

                PolicySubclauseBox(
                    title = "2. Voice Sample Badge Verification",
                    description = "Users can record a short authentic voice greeting using the microphone to obtain a verified voice badge. Voice samples are accessible on your profile for prospective matches to hear your real voice."
                )

                PolicySubclauseBox(
                    title = "3. Verification Data Security",
                    description = "Verification media is stored securely on encrypted Google Cloud Storage and is never shared, leased, or sold to third-party facial recognition brokers or commercial AI trainers."
                )
            }

            // SECTION 4: HOW WE USE INFORMATION
            PolicySectionCard(
                sectionNumber = "04",
                sectionTitle = "How We Use Information",
                icon = Icons.Default.SettingsSuggest
            ) {
                Text(
                    text = "All collected data is utilized exclusively for core functional requirements and community safety protocols within the LiveTogather platform:",
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
                    title = "2. Real-Time Room Synchronization",
                    description = "We synchronize live listener lists, active host statuses, participant queues, and collaborative background music streaming across all participants inside an intent room."
                )

                PolicySubclauseBox(
                    title = "3. Tier Privileges & Premium Features",
                    description = "We verify your active membership tier (VIP Level ★, Platinum, or Standard) to grant premium perks such as first-message privileges (\"My Move\"), photo-verified chat preferences, and custom room hosting."
                )

                PolicySubclauseBox(
                    title = "4. Community Safety & Behavioral Scoring",
                    description = "We compute user safety scores through our HonorSystem to detect spam, harassment, or abusive conduct, enforce automated keyword masking, and apply device-level bans against bad actors who violate Community Guidelines."
                )
            }

            // SECTION 5: DATA STORAGE, FIREBASE & SECURITY
            PolicySectionCard(
                sectionNumber = "05",
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

            // SECTION 6: GOOGLE PLAY IN-APP BILLING & FINANCIAL SECURITY
            PolicySectionCard(
                sectionNumber = "06",
                sectionTitle = "Google Play In-App Billing & Payment Exclusivity",
                icon = Icons.Default.Payments
            ) {
                Text(
                    text = "LiveTogather enforces 100% compliance with Google Play Developer Program Policies for all in-app purchases and digital goods:",
                    color = PolicyTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                PolicySubclauseBox(
                    title = "1. Exclusive Google Play Billing Integration",
                    description = "All in-app purchases within LiveTogather (including Diamond Packs, VIP Memberships, and Luxury Avatar Frames) are processed exclusively through Google Play In-App Billing (com.android.billingclient). The application does NOT contain, use, or display any direct UPI QR codes, personal UPI IDs, or custom off-store payment gateways."
                )

                PolicySubclauseBox(
                    title = "2. Absolute Financial Data Privacy",
                    description = "AYAD Creations does NOT receive, process, or store your credit/debit card numbers, UPI PINs, bank accounts, or CVV codes for purchases. All billing transactions are encrypted and secured directly on Google Play's PCI-DSS compliant infrastructure."
                )

                PolicySubclauseBox(
                    title = "3. Virtual Goods & In-App Economy",
                    description = "Diamonds and coins purchased inside LiveTogather are strictly digital goods intended for interactive room gifting, profile customization, and in-game participation. All purchases are final and governed by Google Play Refund Policies."
                )
            }

            // SECTION 7: CREATOR EARNINGS & PAYOUT TERMS
            PolicySectionCard(
                sectionNumber = "07",
                sectionTitle = "Creator Earnings & Payout Terms",
                icon = Icons.Default.CardGiftcard
            ) {
                Text(
                    text = "LiveTogather supports an authentic creator economy where live audio and video broadcasters can earn real-world rewards from audience gifts:",
                    color = PolicyTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                PolicySubclauseBox(
                    title = "1. Virtual Gifts & Earning Wallet Accumulation",
                    description = "When room listeners appreciate your broadcast, they send animated virtual gifts powered by diamonds. These gifts convert into accumulated host commissions inside your dedicated Earning Wallet according to established platform commission tiers."
                )

                PolicySubclauseBox(
                    title = "2. Fixed Monthly Payout Cycle (7th - 10th of Every Month)",
                    description = "LiveTogather operates on a regular, predictable Monthly Payout Cycle. All eligible creator earnings accrued during the previous calendar month are audited and transferred directly to creators' verified Indian Bank Accounts or UPI IDs between the 7th and 10th of every month."
                )

                PolicySubclauseBox(
                    title = "3. 100% Voluntary (Optional) Participation",
                    description = "Creator monetization is completely voluntary. Users who stream, join audio/video rooms, play casual games, or chat for personal fun and friendship are NEVER required to submit any banking, PAN, or financial details. Financial credentials are only requested from creators who actively choose to withdraw their virtual earnings as real money."
                )

                PolicySubclauseBox(
                    title = "4. Government & KYC Compliance (PAN Card Requirement)",
                    description = "To comply with Indian statutory financial regulations, tax laws, and anti-money laundering (AML) guidelines, creators requesting cash withdrawals must provide a valid Indian PAN Card alongside a matching verified Bank Account (with IFSC) or UPI ID. This securely verifies the creator's identity for lawful disbursements."
                )

                PolicySubclauseBox(
                    title = "5. Transparent Tax Compliance & Friendly Processing",
                    description = "All payouts are processed transparently in accordance with applicable tax regulations (such as TDS guidelines). Submitting a valid PAN ensures your transfers are processed seamlessly at standard statutory rates without unnecessary withholding deductions. The process is straightforward, reassuring, and designed to protect your hard-earned rewards."
                )
            }

            // SECTION 8: IN-ROOM MULTIPLAYER GAMES (CARROM & MINI-GAMES)
            PolicySectionCard(
                sectionNumber = "08",
                sectionTitle = "In-Room Multiplayer Games & Entertainment Mechanics",
                icon = Icons.Default.SportsEsports
            ) {
                Text(
                    text = "LiveTogather integrates interactive multiplayer casual board games (such as Carrom) directly inside audio and video hangout rooms:",
                    color = PolicyTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                PolicySubclauseBox(
                    title = "1. Real-Time Board State Synchronization",
                    description = "Multiplayer game physics, striker coordinates, puck positions, turn timers, and player join events are synchronized across participants via real-time cloud room documents."
                )

                PolicySubclauseBox(
                    title = "2. Virtual Currency Wagering (Pure Entertainment)",
                    description = "In-game stakes and wagers using coins or diamonds are purely virtual entertainment mechanics intended to foster social competition. These virtual tokens hold no external fiat currency value and cannot be withdrawn as real-world gambling winnings."
                )

                PolicySubclauseBox(
                    title = "3. Fair Play & Anti-Cheat Telemetry",
                    description = "We log game turn durations and match outcomes to detect automated bots, unnatural click intervals, or game tampering to ensure a fair playing field for all users."
                )
            }

            // SECTION 9: THIRD-PARTY AUTHENTICATION & SERVICE PROVIDERS
            PolicySectionCard(
                sectionNumber = "09",
                sectionTitle = "Third-Party Authentication & Service Providers",
                icon = Icons.Default.AccountCircle
            ) {
                Text(
                    text = "LiveTogather enables frictionless login via trusted identity providers and integrates specialized infrastructure SDKs:",
                    color = PolicyTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                PolicySubclauseBox(
                    title = "1. Google Sign-In Data",
                    description = "When signing in with Google, we access your basic public profile (name, verified email address, profile picture URL, and Google Account ID). We never access your Google Drive, Gmail messages, or search history."
                )

                PolicySubclauseBox(
                    title = "2. Facebook (Meta) Login Data",
                    description = "When signing in with Facebook, we retrieve your public profile name, public avatar, and Facebook User ID solely to authenticate your account. We never post to your timeline or access your friend lists."
                )

                PolicySubclauseBox(
                    title = "3. Essential SDK Partners",
                    description = "We partner exclusively with reputable industry service providers: Google Firebase (database, auth, storage), Agora RTC (audio/video streaming), and Google Play Services (billing & security). All partners operate under strict enterprise data processing agreements."
                )
            }

            // SECTION 10: DEVICE PERMISSIONS & HARDWARE ACCESS
            PolicySectionCard(
                sectionNumber = "10",
                sectionTitle = "Device Permissions & Hardware Access",
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
                    title = "1. Camera (CAMERA)",
                    description = "Used for real-time front-camera selfie verification (CameraX) and capturing photos for your profile and Moments feed. The camera is never accessed in the background."
                )

                PolicySubclauseBox(
                    title = "2. Microphone (RECORD_AUDIO & MODIFY_AUDIO_SETTINGS)",
                    description = "Required to broadcast your voice when speaking in audio/video hangouts, recording voice verification greetings, and engaging with room participants. The microphone is never recorded in the background."
                )

                PolicySubclauseBox(
                    title = "3. Bluetooth (BLUETOOTH_CONNECT)",
                    description = "Used to seamlessly route high-fidelity hangout audio to your connected wireless Bluetooth headphones, headsets, or car audio systems."
                )

                PolicySubclauseBox(
                    title = "4. Location (ACCESS_FINE_LOCATION & ACCESS_COARSE_LOCATION)",
                    description = "Used to determine your approximate or precise city and coordinates so that nearby profiles and local Intent Rooms can be discovered. Never tracked when the application is completely closed."
                )

                PolicySubclauseBox(
                    title = "5. Media & Storage (READ_MEDIA_IMAGES / READ_EXTERNAL_STORAGE)",
                    description = "Required when you intentionally pick a picture from your device gallery to set your profile avatar or publish a photo to the Moments feed."
                )

                PolicySubclauseBox(
                    title = "6. Notifications (POST_NOTIFICATIONS)",
                    description = "Used to alert you in real-time when you receive a new match, an incoming 1-on-1 chat message, a virtual gift, or when followed creators initiate an audio hangout broadcast."
                )
            }

            // SECTION 11: USER-GENERATED CONTENT (UGC) & COMMUNITY SAFETY
            PolicySectionCard(
                sectionNumber = "11",
                sectionTitle = "User-Generated Content (UGC) & Community Safety",
                icon = Icons.Default.Shield
            ) {
                Text(
                    text = "LiveTogather maintains zero tolerance for objectionable content, harassment, hate speech, explicit sexual material, or fraudulent behavior:",
                    color = PolicyTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                PolicySubclauseBox(
                    title = "1. Automated Profanity & Abuse Filter",
                    description = "Our integrated AbuseFilterEngine actively scans text messages in 1-on-1 chats and live room feeds, automatically detecting and masking abusive keywords, offensive language, and harassment in real time."
                )

                PolicySubclauseBox(
                    title = "2. In-App Immediate User Reporting & Blocking",
                    description = "Users can report any offensive participant, inappropriate photo, or abusive message directly within the app with a single tap. Blocking a user instantly prevents further messaging and room interactions."
                )

                PolicySubclauseBox(
                    title = "3. 24-Hour Moderation Action & Permanent Bans",
                    description = "All user reports are investigated by our safety team within 24 hours. Violators face immediate content removal, account suspension, and permanent hardware device-level bans."
                )
            }

            // SECTION 12: AGE RESTRICTION (STRICTLY 18+ ONLY)
            PolicySectionCard(
                sectionNumber = "12",
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

            // SECTION 13: USER RIGHTS & COMPLETE ACCOUNT DELETION
            PolicySectionCard(
                sectionNumber = "13",
                sectionTitle = "User Rights & Complete Account / Data Deletion",
                icon = Icons.Default.DeleteSweep
            ) {
                Text(
                    text = "You maintain full autonomy and legal rights over your personal data at all times, including the rights to access, rectify, export, or completely delete your personal information:",
                    color = PolicyTextLight,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                PolicySubclauseBox(
                    title = "1. In-App Immediate Self-Service Deletion",
                    description = "You can delete your account instantly at any time from within the application. Simply open the Profile tab, navigate to the Safety & Account section at the bottom, and tap \"Delete Account\". This permanently purges your profile, matches, moments, and chat records from live servers."
                )

                PolicySubclauseBox(
                    title = "2. Online Data Deletion Desk & Dedicated Email",
                    description = "For external data purges, server audit logs, or manual data erasure verification without opening the app, you can submit a deletion request via our official portal or email:"
                )

                Surface(
                    color = PolicyCardSubtle,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PolicyGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = PolicyGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "https://ayadcreation.online/data-deletion",
                                color = PolicyGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = PolicyGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "servicesecure822@gmail.com",
                                color = PolicyGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Subject: \"LiveTogather Data Deletion Request\"\nInclude your registered display name or UID. Deletion requests are processed and verified within 48 to 72 business hours.",
                            color = PolicyTextLight,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // SECTION 14: DEVELOPER, ORGANIZATION & STATUTORY GRIEVANCE REDRESSAL
            PolicySectionCard(
                sectionNumber = "14",
                sectionTitle = "Organization & Grievance Redressal Officer",
                icon = Icons.AutoMirrored.Filled.ContactSupport
            ) {
                Text(
                    text = "In compliance with the Information Technology (Intermediary Guidelines and Digital Media Ethics Code) Rules, 2021 and Digital Personal Data Protection Act, 2023, AYAD Creations has designated a formal Grievance Redressal Officer:",
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
                        PolicyContactRow(icon = Icons.Default.Business, label = "Publishing Organization", value = "AYAD Creations")
                        PolicyContactRow(icon = Icons.Default.Language, label = "Official Website", value = "https://ayadcreation.online")
                        PolicyContactRow(icon = Icons.Default.Person, label = "Lead Developer & Grievance Officer", value = "MD Dilshad Warsi")
                        PolicyContactRow(icon = Icons.Default.Email, label = "Official Grievance Email", value = "servicesecure822@gmail.com")
                        PolicyContactRow(icon = Icons.Default.Event, label = "Policy Effective Date", value = "November 12, 2026")
                        PolicyContactRow(icon = Icons.Default.Gavel, label = "Legal Status", value = "Binding Digital Privacy Agreement")
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
