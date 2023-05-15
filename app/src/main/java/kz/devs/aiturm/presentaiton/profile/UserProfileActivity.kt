package kz.devs.aiturm.presentaiton.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.shroomies.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kz.devs.aiturm.ChattingActivity
import kz.devs.aiturm.Config
import kz.devs.aiturm.model.User

class UserProfileActivity : AppCompatActivity() {

    companion object {
        private const val USER_ID = "USER_ID"
        fun newInstance(
            context: Context,
            userId: String
        ): Intent = Intent(context, UserProfileActivity::class.java).putExtra(USER_ID, userId)
    }

    private var user: User? = null
    private var rootReference: DatabaseReference? = null
    private var firebaseAuth: FirebaseAuth? = null

    private val uiScope = CoroutineScope(Dispatchers.IO)

    private var toolbar: MaterialToolbar? = null
    private var rootLayout: ConstraintLayout? = null
    private var progressBar: ProgressBar? = null
    private var userImage: ShapeableImageView? = null
    private var usernameTextView: MaterialTextView? = null
    private var nameTextView: MaterialTextView? = null
    private var bioTextView: MaterialTextView? = null
    private var emailTextView: MaterialTextView? = null
    private var genderTextView: MaterialTextView? = null
    private var groupTextView: MaterialTextView? = null
    private var phoneNumberTextView: MaterialTextView? = null
    private var specialityTextView: MaterialTextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        rootReference = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()

        toolbar = findViewById(R.id.toolbar)
        rootLayout = findViewById(R.id.root_layout)
        progressBar = findViewById(R.id.progress_bar)
        userImage = findViewById(R.id.user_image)
        usernameTextView = findViewById(R.id.username_edit_text)
        bioTextView = findViewById(R.id.bio_edit_text)
        nameTextView = findViewById(R.id.name_edit_text)
        emailTextView = findViewById(R.id.email_edit_text)
        genderTextView = findViewById(R.id.gender_edit_text)
        groupTextView = findViewById(R.id.group_edit_text)
        phoneNumberTextView = findViewById(R.id.phone_number_edit_text)
        specialityTextView = findViewById(R.id.specialization_edit_text)

        setupToolbar()

        val userId = intent.extras?.getString(USER_ID)

        if (userId == null) {
            showErrorDialog()
        } else {
            uiScope.launch {
                loadUserDetails(userId)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_details_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.send_message_button -> {
                if (user == null) {
                    Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
                } else {
                    if (firebaseAuth?.currentUser?.uid == user?.userID) {
                        Toast.makeText(
                            this,
                            getString(R.string.error_cannot_message_with_yourself),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val intent = Intent(this, ChattingActivity::class.java)
                        intent.apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra("USER", user)
                            putExtra("USERID", user?.userID)
                        }
                        startActivity(intent)
                    }
                }

                true
            }
            R.id.user_posts_button -> {
                val fragment = PostsDialogFragment()
                val arguments = Bundle()
                arguments.putString("USERID", user?.userID)
                fragment.arguments = arguments
                fragment.show(supportFragmentManager, null)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private suspend fun loadUserDetails(userId: String) {
        showProgress()
        rootReference?.child(Config.users)?.child(userId)?.get()?.addOnSuccessListener {
            if (it.exists()) {
                user = it.getValue(User::class.java)
                user?.userID = userId
                disableProgress()
                setupUserDetails()
            } else {
                disableProgress()
                showErrorDialog()
            }
        }
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_alert)
            .setTitle(getString(R.string.error))
            .setMessage(getString(R.string.network_error))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
    }

    private fun showProgress() {
        progressBar?.visibility = View.VISIBLE
        rootLayout?.visibility = View.GONE
    }

    private fun disableProgress() {
        progressBar?.visibility = View.GONE
        rootLayout?.visibility = View.VISIBLE
    }

    private fun setupUserDetails() {
        if (userImage == null) return
        Glide.with(this)
            .load(user?.image)
            .fitCenter()
            .placeholder(R.drawable.ic_user_profile_svgrepo_com)
            .transform(CircleCrop())
            .into(userImage!!)
        usernameTextView?.text = user?.username
        bioTextView?.text =
            if (user?.bio.isNullOrBlank()) getString(R.string.undefined) else user?.bio
        nameTextView?.text = user?.name
        emailTextView?.text = user?.email
        genderTextView?.text = when (user?.gender) {
            User.Gender.MALE -> getString(R.string.gender_select_male)
            User.Gender.FEMALE -> getString(R.string.gender_select_female)
            else -> getString(R.string.undefined)
        }
        groupTextView?.text = user?.group
        val ans = "+7(" + user?.phoneNumber?.substring(1, 4) + ") " + user?.phoneNumber?.substring(
            4, 7
        ) + "-" + user?.phoneNumber?.substring(7, 9) + "-" + user?.phoneNumber?.substring(9)
        phoneNumberTextView?.text = ans
        specialityTextView?.text = user?.specialization
    }
}