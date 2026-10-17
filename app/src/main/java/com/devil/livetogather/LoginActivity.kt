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
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

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
        // If user has already signed in, do not display login screen on relaunch / kill from recents
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val prefs = getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE)
            val profileName = prefs.getString("profile_name", "") ?: ""
            if (profileName.isNotBlank()) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return
            } else {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("users").document(currentUser.uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc != null && doc.exists()) {
                            var userObj = parseUserProfileFromDoc(doc)
                            if (userObj.userId.isBlank()) {
                                val genId = (100000..999999).random().toString()
                                userObj = userObj.copy(userId = genId)
                                db.collection("users").document(currentUser.uid).update("userId", genId)
                            }
                            if (userObj.name.isNotBlank()) {
                                saveProfileToPrefs(prefs, userObj)
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                                return@addOnSuccessListener
                            }
                        }
                        val intent = Intent(this, ProfileSetupActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        val intent = Intent(this, ProfileSetupActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                return
            }
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
                    navigateToProfileSetup()
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    Toast.makeText(this, "Sign Up Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        } else {
            // Sign In Existing Account
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Welcome back! 🎉", Toast.LENGTH_SHORT).show()
                    navigateToProfileSetup()
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

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                Toast.makeText(this, "Google sign-in successful! 🎉", Toast.LENGTH_SHORT).show()
                navigateToProfileSetup()
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
                navigateToProfileSetup()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Firebase Facebook Auth failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Flow Requirement:
     * Directly navigate the user to ProfileSetupActivity so they can complete profile setup/creation.
     * Do NOT navigate to MainActivity. Finish LoginActivity.
     */
    private fun navigateToProfileSetup() {
        setLoading(false)
        val uid = auth.currentUser?.uid
        if (!uid.isNullOrBlank()) {
            val prefs = getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("user_uid", uid).apply()

            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        var userObj = parseUserProfileFromDoc(doc)
                        if (userObj.userId.isBlank()) {
                            val genId = (100000..999999).random().toString()
                            userObj = userObj.copy(userId = genId)
                            db.collection("users").document(uid).update("userId", genId)
                        }
                        if (userObj.name.isNotBlank()) {
                            saveProfileToPrefs(prefs, userObj)
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                            return@addOnSuccessListener
                        }
                    }
                    val intent = Intent(this, ProfileSetupActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    val intent = Intent(this, ProfileSetupActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
        } else {
            val intent = Intent(this, ProfileSetupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnSignIn.isEnabled = !loading
        btnGoogleSignIn.isEnabled = !loading
        btnFacebookSignIn.isEnabled = !loading
    }
}
