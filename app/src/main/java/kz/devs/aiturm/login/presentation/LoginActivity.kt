package kz.devs.aiturm.login.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kz.devs.aiturm.CustomLoadingProgressBar
import kz.devs.aiturm.CustomToast
import kz.devs.aiturm.LoginPasswordActivity
import kz.devs.aiturm.MainActivity
import kz.devs.aiturm.R
import kz.devs.aiturm.model.SignInMethod
import kz.devs.aiturm.presentaiton.authorization.FillOutDataActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginActivity : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 7

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
        ).apply {
            setCancelable(false)
        }
    }

    private val viewModel: LoginViewModel by viewModel()

    private val mGoogleSignInClient: GoogleSignInClient by inject()
    private val mAuth: FirebaseAuth by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupSignUpButtons()
        setupLoginButton()
        observeUiEvent()
        observeLoginResult()
    }

    private fun setupSignUpButtons() {
        signupButton.setOnClickListener {
            startActivity(
                FillOutDataActivity.getInstance(applicationContext, SignInMethod.DEFAULT)
            )
        }

        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        microsoftSignInButton.setOnClickListener {
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

    private fun firebaseAuthWithGoogle(tokenID: String) {
        val credential = GoogleAuthProvider.getCredential(tokenID, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener(
            this
        ) { task: Task<AuthResult?> ->
            if (task.isSuccessful) {
                val userId = task.getResult(ApiException::class.java)?.user?.uid
                if (userId != null) {
                    viewModel.dispatch(LoginViewModel.Action.OnSuccessfullyAuthenticated(userId))
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
        provider.setScopes(listOf("mail.read", "calendars.read"))
        mAuth.startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener {
                customLoadingProgressBar.dismiss()
                val userId = it.user?.uid
                if (userId != null) {
                    viewModel.dispatch(LoginViewModel.Action.OnSuccessfullyAuthenticated(userId))
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
        startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@LoginActivity, e.toString(), Toast.LENGTH_SHORT).show()
                customLoadingProgressBar.dismiss()
            }
        } else {
            Toast.makeText(
                this@LoginActivity,
                "authentication failed, try again later$requestCode", Toast.LENGTH_SHORT
            ).show()
            customLoadingProgressBar.dismiss()
        }
    }

    private fun observeUiEvent() = lifecycleScope.launch {
        viewModel.uiState.collectLatest { event ->
            when (event) {
                LoginViewModel.Event.OnErrorOccurred -> {}
                LoginViewModel.Event.NavigateToMainPage -> {
                    startActivity(MainActivity.newInstance(this@LoginActivity))
                    finish()
                }
                is LoginViewModel.Event.NavigateToFillOutActivity -> {
                    startActivity(FillOutDataActivity.getInstance(this@LoginActivity, event.signInMethod))
                }
            }
        }
    }

    private fun observeLoginResult() = lifecycleScope.launch {
        viewModel.isLoading.collectLatest { isLoading ->
            if (isLoading) {
                customLoadingProgressBar.show()
            } else {
                customLoadingProgressBar.dismiss()
            }
        }
    }

}