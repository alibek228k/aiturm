package kz.devs.aiturm.login.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kz.devs.aiturm.Config
import kz.devs.aiturm.CustomLoadingProgressBar
import kz.devs.aiturm.CustomToast
import kz.devs.aiturm.LoginPasswordActivity
import kz.devs.aiturm.MainActivity
import kz.devs.aiturm.R
import kz.devs.aiturm.model.SignInMethod
import kz.devs.aiturm.model.User
import kz.devs.aiturm.presentaiton.SessionManager
import kz.devs.aiturm.presentaiton.authorization.FillOutDataActivity
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity(), ValueEventListener {

    companion object {
        fun getInstance(context: Context?): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }

    private val loginButton: ImageButton by lazy { findViewById(R.id.login_button) }
    private val emailEditText: TextInputLayout by lazy { findViewById(R.id.email_login) }
    private val signupButton: MaterialButton by lazy { findViewById(R.id.sign_up_button) }
    private val googleSignInButton: ImageButton by lazy { findViewById(R.id.google_sign_up) }
    private val microsoftSignInButton: ImageButton by lazy { findViewById(R.id.microsoft_sign_up) }
    private val customLoadingProgressBar: CustomLoadingProgressBar by lazy {
        CustomLoadingProgressBar(
            this,
            getString(R.string.loading_progress_message),
            R.raw.loading_animation
        )
    }

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var rootRef: DatabaseReference
    private val RC_SIGN_IN = 7
    private val pattern = Pattern.compile(Config.USERNAME_PATTERN)
    private var method = SignInMethod.DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initFirebaseArguments()
        setupSignUpButtons()
        setupLoginButton()
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        if (snapshot.exists()) {
            val username = snapshot.getValue(String::class.java)
            if (username == null) {
                Log.d("MICROSOFT SIGN IN", "new user")
                startActivity(FillOutDataActivity.getInstance(applicationContext, method))
            } else {
                val firebaseUser: FirebaseUser? = mAuth.currentUser
                rootRef.child(Config.users).child(firebaseUser?.uid.orEmpty()).get()
                    .addOnCompleteListener { task: Task<DataSnapshot> ->
                        val user =
                            task.result.getValue(
                                User::class.java
                            )
                        user!!.userID = firebaseUser?.uid
                        user.signInMethod = method
                        val manager =
                            SessionManager(this@LoginActivity)
                        if (user.signInMethod != method) {
                            val signInMethodMap =
                                HashMap<String, Any>()
                            signInMethodMap["signInMethod"] = method
                            rootRef.child(Config.users).child(firebaseUser?.uid.orEmpty())
                                .updateChildren(signInMethodMap).addOnCompleteListener(
                                    OnCompleteListener<Void?> { task1: Task<Void?> ->
                                        if (task1.isSuccessful) {
                                            user.signInMethod = method
                                            manager.removeUserData()
                                            manager.saveData(user)
                                        }
                                    }).addOnFailureListener(OnFailureListener { e: Exception? ->
                                    manager.removeUserData()
                                    manager.saveData(user)
                                })
                        } else {
                            user.signInMethod = method
                            manager.removeUserData()
                            manager.saveData(user)
                        }
                        manager.removeUserData()
                        manager.saveData(user)
                        startActivity(MainActivity.newInstance(this@LoginActivity))
                        finish()
                    }
                Log.d("MICROSOFT SIGN IN", "not new user")
            }
        } else {
            startActivity(FillOutDataActivity.getInstance(applicationContext, method))
        }
        customLoadingProgressBar.dismiss()
    }

    override fun onCancelled(error: DatabaseError) {
        Log.e(
            "getUserUsernameFromDatabase",
            "getUserUsernameFromDatabase: onCancelled",
            error.toException()
        )
        customLoadingProgressBar.dismiss()
    }

    private fun setupSignUpButtons() {
        signupButton.setOnClickListener(View.OnClickListener { v: View? ->
            startActivity(
                FillOutDataActivity.getInstance(applicationContext, SignInMethod.DEFAULT)
            )
        })
        googleSignInButton.setOnClickListener { v: View? ->
            customLoadingProgressBar.show()
            customLoadingProgressBar.setCancelable(false)
            method = SignInMethod.GOOGLE
            signInWithGoogle()
        }
        microsoftSignInButton.setOnClickListener { v: View? ->
            customLoadingProgressBar.show()
            customLoadingProgressBar.setCancelable(false)
            method = SignInMethod.MICROSOFT
            signInWithMicrosoft()
        }
    }

    private fun setupLoginButton() {
        loginButton.setOnClickListener { view: View? ->
            val enteredEmail =
                emailEditText.editText!!.text.toString().trim { it <= ' ' }
            if (!validEmail(enteredEmail)) {
                emailEditText.error = "Please enter a valid email"
            } else {
                emailEditText.error = null
                val intent = Intent(this, LoginPasswordActivity::class.java)
                intent.putExtra("EMAIL", enteredEmail)
                startActivity(intent)
            }
        }
    }

    private fun validEmail(enteredEmail: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches()
    }

    private fun initFirebaseArguments() {
        mAuth = FirebaseAuth.getInstance()
        rootRef = FirebaseDatabase.getInstance().reference
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this@LoginActivity, gso)
    }

    private fun firebaseAuthWithGoogle(tokenID: String) {
        val credential = GoogleAuthProvider.getCredential(tokenID, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener(
            this
        ) { task: Task<AuthResult?> ->
            if (task.isSuccessful) {
                val user = mAuth.currentUser
                if (user != null) {
                    val userRef = FirebaseDatabase
                        .getInstance()
                        .getReference("users")
                        .child(user.uid)
                        .child("username")
                    userRef.addListenerForSingleValueEvent(this)
                } else {
                    CustomToast(
                        this@LoginActivity,
                        "We encountered a problem with your account",
                        R.drawable.ic_error_icon
                    ).showCustomToast()
                    customLoadingProgressBar.dismiss()
                }
            } else {
                CustomToast(
                    this@LoginActivity,
                    "We encountered an unexpected problem",
                    R.drawable.ic_error_icon
                ).showCustomToast()
                customLoadingProgressBar.dismiss()
            }
        }
    }

    private fun signInWithMicrosoft() {
        val provider = OAuthProvider.newBuilder("microsoft.com")
        provider.addCustomParameter("prompt", "select_account")
        provider.addCustomParameter("login_hint", "")
        provider.addCustomParameter("tenant", "158f15f3-83e0-4906-824c-69bdc50d9d61")
        val scopes: List<String> = listOf("mail.read", "calendars.read")
        provider.setScopes(scopes)
        mAuth.startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener { authResult: AuthResult? ->
                customLoadingProgressBar.dismiss()
                val user = mAuth.currentUser
                if (user != null) {
                    val userRef = FirebaseDatabase
                        .getInstance()
                        .getReference("users")
                        .child(user.uid)
                        .child("username")
                    userRef.addListenerForSingleValueEvent(this)
                } else {
                    CustomToast(
                        this@LoginActivity,
                        "We encountered a problem with your account",
                        R.drawable.ic_error_icon
                    ).showCustomToast()
                    customLoadingProgressBar.dismiss()
                }
            }
            .addOnFailureListener { e: java.lang.Exception ->
                e.printStackTrace()
                CustomToast(
                    this@LoginActivity,
                    "Error occurred during the authorization",
                    R.drawable.ic_error_icon
                ).showCustomToast()
                customLoadingProgressBar.dismiss()
            }
    }


    private fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
}