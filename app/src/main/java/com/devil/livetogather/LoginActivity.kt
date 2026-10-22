package com.devil.livetogather

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import android.content.SharedPreferences
import android.provider.Settings
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import android.text.InputType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source

/**
 * Dedicated Standalone Login Screen for LiveTogether.
 *
 * Supported Authentication Methods:
 * 1. Email & Password (Sign In and Sign Up)
 * 2. Google Sign-In (Firebase GoogleAuthProvider)
 * 3. Facebook Login (Facebook SDK + Firebase FacebookAuthProvider)
 *
 * Flow Requirement:
 * After ANY successful authentication, strictly navigates to ProfileSetupActivity.
 */
class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvForgotPassword: TextView
    private lateinit var btnSignIn: Button
    private lateinit var tvToggleMode: TextView
    private lateinit var btnGoogleSignIn: View
    private lateinit var btnFacebookSignIn: View
    private lateinit var progressBar: ProgressBar

    private var isSignUpMode = false

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        setLoading(false)
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                setLoading(true)
                firebaseAuthWithGoogle(idToken)
            } else {
                Toast.makeText(this, "Could not retrieve Google ID Token", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            val msg = when (e.statusCode) {
                10 -> "Google Sign-In Error (10: DEVELOPER_ERROR): Release SHA-1 is missing from Firebase Console!"
                12500 -> "Google Sign-In Error (12500): Google Play Services configuration issue."
                12501, 12502 -> null // User cancelled or back pressed
                7 -> "Network error. Please check your internet connection."
                else -> "Google Sign-In failed (${e.statusCode}): ${e.message}"
            }
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                android.util.Log.e("LoginActivity", "Google Sign-In ApiException: ${e.statusCode} - ${e.message}", e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Persistent Session / Auto-Login:
        // If user is already authenticated with Firebase Auth, verify Firestore from SERVER
        val currentUser = auth.currentUser
        if (currentUser != null) {
            checkExistingProfileAndProceed()
            return
        }

        setContentView(R.layout.activity_login)

        // 1. Initialize Views
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        btnSignIn = findViewById(R.id.btnSignIn)
        tvToggleMode = findViewById(R.id.tvToggleMode)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        btnFacebookSignIn = findViewById(R.id.btnFacebookSignIn)
        progressBar = findViewById(R.id.progressBar)

        // 2. Setup Google Sign-In Client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 3. Setup Facebook Login SDK
        try {
            FacebookSdk.setApplicationId(getString(R.string.facebook_app_id))
            FacebookSdk.setClientToken(getString(R.string.facebook_client_token))
            FacebookSdk.sdkInitialize(applicationContext)
            LoginManager.getInstance().setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK)
        } catch (e: Exception) {
            android.util.Log.e("LoginActivity", "FacebookSdk init error", e)
        }

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    firebaseAuthWithFacebook(result.accessToken.token)
                }

                override fun onCancel() {
                    setLoading(false)
                    Toast.makeText(this@LoginActivity, "Facebook login canceled", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    setLoading(false)
                    val errorMsg = error.message ?: "Authentication error"
                    Toast.makeText(this@LoginActivity, "Facebook error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
        )

        // 4. Setup Click Listeners
        btnSignIn.setOnClickListener { handleEmailPasswordAuth() }

        tvToggleMode.setOnClickListener { toggleAuthMode() }

        tvForgotPassword.setOnClickListener { showForgotPasswordDialog() }

        btnGoogleSignIn.setOnClickListener {
            setLoading(true)
            // Sign out of previous Google session to ensure account chooser dialog appears
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        }

        btnFacebookSignIn.setOnClickListener {
            setLoading(true)
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("email", "public_profile")
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleEmailPasswordAuth() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Email address required"
            etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty() || password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return
        }

        setLoading(true)

        if (isSignUpMode) {
            // Create New Account
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Account created successfully! 🎉", Toast.LENGTH_SHORT).show()
                    checkExistingProfileAndProceed()
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    if (e is FirebaseAuthUserCollisionException) {
                        AlertDialog.Builder(this)
                            .setTitle("Account Already Exists ℹ️")
                            .setMessage("An account with this email ($email) is already registered.\n\nPlease sign in with your credentials.")
                            .setPositiveButton("Go to Sign In") { _, _ ->
                                toggleAuthMode()
                                etEmail.setText(email)
                                etPassword.setText("")
                                etPassword.requestFocus()
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    } else {
                        Toast.makeText(this, "Sign Up Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            // Sign In Existing Account
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Welcome back! 🎉", Toast.LENGTH_SHORT).show()
                    checkExistingProfileAndProceed()
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(this, "Login Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun toggleAuthMode() {
        isSignUpMode = !isSignUpMode
        if (isSignUpMode) {
            btnSignIn.text = "Create Account"
            tvToggleMode.text = "Already have an account? Sign In"
            tvForgotPassword.visibility = View.GONE
        } else {
            btnSignIn.text = "Sign In"
            tvToggleMode.text = "Don't have an account? Sign Up"
            tvForgotPassword.visibility = View.VISIBLE
        }
    }

    private fun showForgotPasswordDialog() {
        val input = EditText(this)
        input.hint = "Enter your registered email"
        input.setPadding(40, 30, 40, 30)
        input.setTextColor(android.graphics.Color.BLACK)

        val currentEnteredEmail = etEmail.text.toString().trim()
        if (currentEnteredEmail.isNotEmpty()) {
            input.setText(currentEnteredEmail)
        }

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("We will send a password reset link to your email address.")
            .setView(input)
            .setPositiveButton("Send Link") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    setLoading(true)
                    auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            setLoading(false)
                            Toast.makeText(this, "Reset link sent to $email! 📧", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { e ->
                            setLoading(false)
                            Toast.makeText(this, "Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCreatePasswordDialogForGoogleUser(user: FirebaseUser, onComplete: () -> Unit) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 10)
        }
        val etPass = EditText(this).apply {
            hint = "Create Password (min 6 chars)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(android.graphics.Color.BLACK)
        }
        val etConfirm = EditText(this).apply {
            hint = "Confirm Password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(android.graphics.Color.BLACK)
        }
        layout.addView(etPass)
        layout.addView(etConfirm)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Create Your Password 🔐")
            .setMessage("Set a password for your account so you can log in anytime using either Google or your Email (${user.email}).")
            .setView(layout)
            .setCancelable(false)
            .setPositiveButton("Set Password", null)
            .setNegativeButton("Cancel") { _, _ ->
                auth.signOut()
                setLoading(false)
            }
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val p1 = etPass.text.toString().trim()
                val p2 = etConfirm.text.toString().trim()
                if (p1.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (p1 != p2) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                setLoading(true)
                user.updatePassword(p1)
                    .addOnSuccessListener {
                        dialog.dismiss()
                        Toast.makeText(this, "Password created successfully! 🎉", Toast.LENGTH_SHORT).show()
                        onComplete()
                    }
                    .addOnFailureListener { err ->
                        setLoading(false)
                        Toast.makeText(this, "Failed to set password: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
        }
        dialog.show()
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val isNewUser = authResult.additionalUserInfo?.isNewUser == true
                val user = authResult.user

                if (isSignUpMode && !isNewUser) {
                    val userEmail = user?.email ?: ""
                    auth.signOut()
                    setLoading(false)
                    AlertDialog.Builder(this)
                        .setTitle("Account Already Exists ℹ️")
                        .setMessage("You are already a registered user!\nYou have already signed up. Please sign in using your credentials.")
                        .setPositiveButton("Go to Sign In") { _, _ ->
                            if (isSignUpMode) toggleAuthMode()
                            if (userEmail.isNotBlank()) etEmail.setText(userEmail)
                            etPassword.setText("")
                            etPassword.requestFocus()
                        }
                        .setNegativeButton("Close", null)
                        .show()
                    return@addOnSuccessListener
                }

                if (isNewUser && user != null) {
                    setLoading(false)
                    showCreatePasswordDialogForGoogleUser(user) {
                        checkExistingProfileAndProceed()
                    }
                } else {
                    Toast.makeText(this, "Google sign-in successful! 🎉", Toast.LENGTH_SHORT).show()
                    checkExistingProfileAndProceed()
                }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Firebase Google Auth failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun firebaseAuthWithFacebook(token: String) {
        val credential = FacebookAuthProvider.getCredential(token)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                Toast.makeText(this, "Facebook sign-in successful! 🎉", Toast.LENGTH_SHORT).show()
                checkExistingProfileAndProceed()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Firebase Facebook Auth failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * 100% Cloud-First Architecture & Multi-Device Login:
     * Check Cloud Firestore directly (Source.SERVER) for user's account by UID, email, or device ID.
     * Restores existing profile (diamonds, followers, VIP) without resetting or prompting "Create Account".
     */
    private fun checkExistingProfileAndProceed() {
        setLoading(true)
        val user = auth.currentUser
        if (user == null) {
            setLoading(false)
            return
        }
        val uid = user.uid
        val email = user.email?.trim() ?: ""
        val prefs = getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("user_uid", uid).apply()
        if (email.isNotBlank()) {
            prefs.edit().putString("profile_email", email).apply()
        }

        val db = FirebaseFirestore.getInstance()
        val deviceId = try {
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: ""
        } catch (e: Exception) { "" }

        // Step 1: Check users/{uid} directly from Firestore SERVER
        db.collection("users").document(uid).get(Source.SERVER)
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    var userObj = parseUserProfileFromDoc(doc)
                    val diamonds = doc.getLong("diamonds") ?: 0L
                    if (userObj.name.isNotBlank() && (diamonds > 0L || userObj.hasClaimedWelcomeBonus || userObj.isAdmin)) {
                        if (email.isNotBlank() && userObj.email.isBlank()) {
                            userObj = userObj.copy(email = email)
                            db.collection("users").document(uid).update("email", email)
                        }
                        if (userObj.userId.isBlank() || userObj.userId.length != 8 || !userObj.userId.all { it.isDigit() }) {
                            val genId = (10000000..99999999).random().toString()
                            userObj = userObj.copy(userId = genId)
                            db.collection("users").document(uid).update("userId", genId)
                        }
                        saveProfileToPrefs(prefs, userObj)
                        setLoading(false)
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                        return@addOnSuccessListener
                    }
                }

                // Step 2: Check by email if available
                if (email.isNotBlank()) {
                    db.collection("users").whereEqualTo("email", email).limit(1).get(Source.SERVER)
                        .addOnSuccessListener { emailDocs ->
                            if (emailDocs != null && !emailDocs.isEmpty) {
                                val matchedDoc = emailDocs.documents[0]
                                val restoredProfile = parseUserProfileFromDoc(matchedDoc).copy(
                                    uid = uid,
                                    email = email,
                                    deviceId = if (deviceId.isNotBlank()) deviceId else matchedDoc.getString("deviceId") ?: ""
                                )
                                saveProfileToPrefs(prefs, restoredProfile)
                                db.collection("users").document(uid).set(restoredProfile, SetOptions.merge())
                                setLoading(false)
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                                return@addOnSuccessListener
                            }
                            checkByDeviceIdOrFallback(db, uid, email, deviceId, prefs)
                        }
                        .addOnFailureListener {
                            checkByDeviceIdOrFallback(db, uid, email, deviceId, prefs)
                        }
                } else {
                    checkByDeviceIdOrFallback(db, uid, email, deviceId, prefs)
                }
            }
            .addOnFailureListener {
                val cachedName = prefs.getString("profile_name", "") ?: ""
                if (cachedName.isNotBlank()) {
                    setLoading(false)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    proceedToProfileSetup()
                }
            }
    }

    private fun checkByDeviceIdOrFallback(
        db: FirebaseFirestore,
        uid: String,
        email: String,
        deviceId: String,
        prefs: SharedPreferences
    ) {
        if (deviceId.isNotBlank()) {
            db.collection("users").whereEqualTo("deviceId", deviceId).limit(5).get(Source.SERVER)
                .addOnSuccessListener { devDocs ->
                    if (devDocs != null && !devDocs.isEmpty) {
                        val bestDoc = devDocs.documents.maxByOrNull { (it.getLong("diamonds") ?: 0L) + (it.getLong("wealthXp") ?: 0L) }
                            ?: devDocs.documents[0]
                        val parsed = parseUserProfileFromDoc(bestDoc)
                        var restored = parsed.copy(
                            uid = uid,
                            deviceId = deviceId
                        )
                        if (email.isNotBlank()) {
                            restored = restored.copy(email = email)
                            bestDoc.reference.update("email", email)
                        }
                        saveProfileToPrefs(prefs, restored)
                        db.collection("users").document(uid).set(restored, SetOptions.merge())
                        setLoading(false)
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                        return@addOnSuccessListener
                    }
                    proceedToProfileSetup()
                }
                .addOnFailureListener {
                    proceedToProfileSetup()
                }
        } else {
            proceedToProfileSetup()
        }
    }

    private fun proceedToProfileSetup() {
        setLoading(false)
        val intent = Intent(this, ProfileSetupActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setLoading(loading: Boolean) {
        if (::progressBar.isInitialized) {
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
        if (::btnSignIn.isInitialized) {
            btnSignIn.isEnabled = !loading
        }
        if (::btnGoogleSignIn.isInitialized) {
            btnGoogleSignIn.isEnabled = !loading
        }
        if (::btnFacebookSignIn.isInitialized) {
            btnFacebookSignIn.isEnabled = !loading
        }
    }
}
