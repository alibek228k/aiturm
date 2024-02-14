package kz.devs.aiturm.login.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kz.devs.aiturm.Config
import kz.devs.aiturm.model.SignInMethod
import kz.devs.aiturm.model.User
import kz.devs.aiturm.presentaiton.SessionManager

private const val RC_SIGN_IN = 7

class LoginViewModel(
    private val firebaseAuth: FirebaseAuth,
    private val databaseReference: DatabaseReference,
    private val sessionManager: SessionManager,
) : ViewModel(), ValueEventListener {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiState = MutableSharedFlow<Event>()
    val uiState: SharedFlow<Event> = _uiState.asSharedFlow()

    private var method = SignInMethod.DEFAULT

    fun dispatch(action: Action) = viewModelScope.launch {
        when (action) {
            is Action.OnLoginDefaultClicked -> {}
            is Action.OnSuccessfullyAuthenticated -> successfullyAuthenticated(action.userId)
        }
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        if (snapshot.exists()) {
            viewModelScope.launch { _isLoading.emit(true) }
            val username = snapshot.getValue(String::class.java)
            if (username == null) {
                viewModelScope.launch { _uiState.emit(Event.NavigateToFillOutActivity(method)) }
            } else {
                val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
                databaseReference.child(Config.users).child(firebaseUser?.uid.orEmpty()).get()
                    .addOnCompleteListener { task: Task<DataSnapshot> ->
                        val user =
                            task.result.getValue(
                                User::class.java
                            )
                        user!!.userID = firebaseUser?.uid
                        user.signInMethod = method
                        if (user.signInMethod != method) {
                            val signInMethodMap =
                                HashMap<String, Any>()
                            signInMethodMap["signInMethod"] = method
                            databaseReference.child(Config.users)
                                .child(firebaseUser?.uid.orEmpty())
                                .updateChildren(signInMethodMap).addOnCompleteListener(
                                    OnCompleteListener<Void?> { task1: Task<Void?> ->
                                        if (task1.isSuccessful) {
                                            user.signInMethod = method
                                            sessionManager.removeUserData()
                                            sessionManager.saveData(user)
                                        }
                                    }).addOnFailureListener(OnFailureListener { e: Exception? ->
                                    sessionManager.removeUserData()
                                    sessionManager.saveData(user)
                                })
                        } else {
                            user.signInMethod = method
                            sessionManager.removeUserData()
                            sessionManager.saveData(user)
                        }
                        sessionManager.removeUserData()
                        sessionManager.saveData(user)
                        viewModelScope.launch {
                            _uiState.emit(Event.NavigateToMainPage)
                        }
                    }
                Log.d("MICROSOFT SIGN IN", "not new user")
            }
        } else {
            viewModelScope.launch { _uiState.emit(Event.NavigateToFillOutActivity(method)) }
        }
        viewModelScope.launch { _isLoading.emit(false) }
    }

    override fun onCancelled(error: DatabaseError) {
        viewModelScope.launch { _isLoading.emit(false) }
    }

    private fun successfullyAuthenticated(userId: String) {
        val userRef = FirebaseDatabase
            .getInstance()
            .getReference("users")
            .child(userId)
            .child("username")
        userRef.addListenerForSingleValueEvent(this)
    }

    sealed class Action {
        object OnLoginDefaultClicked : Action()
        class OnSuccessfullyAuthenticated(val userId: String) : Action()
    }

    sealed class Event {
        object OnErrorOccurred : Event()
        object NavigateToMainPage : Event()
        class NavigateToFillOutActivity(val signInMethod: SignInMethod) : Event()
    }
}