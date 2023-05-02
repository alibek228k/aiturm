package kz.devs.aiturm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.airbnb.lottie.LottieAnimationView;
import com.example.shroomies.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import kz.devs.aiturm.model.SignInMethod;
import kz.devs.aiturm.model.User;
import kz.devs.aiturm.presentaiton.SessionManager;

public class PasswordSignUpActivity extends AppCompatActivity {
    //views
    private boolean isRegistrationFinished = false;
    private TextInputLayout passwordEditText;
    private TextInputLayout repPasswordEditText;
    private CheckBox termsCond;
    private MaterialButton register;
    private LottieAnimationView loadingAnimation;
    private Toolbar toolbar;
    //firebase
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_sign_up);
        mAuth = FirebaseAuth.getInstance();

        passwordEditText = findViewById(R.id.password_sign_up);
        repPasswordEditText = findViewById(R.id.confirm_password_sign_up);
        register = findViewById(R.id.register_button);
        termsCond = findViewById(R.id.terms_conditions_privacy_check_box);
        loadingAnimation = findViewById(R.id.register_animation_view);
        toolbar = findViewById(R.id.sign_up_password_tool_bar);

        setupToolbar();


        register.setOnClickListener(view -> {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                User user = (User) bundle.getSerializable("USER");
                SignInMethod method = (SignInMethod) bundle.getSerializable("SIGN_UP_METHOD");
                user.setSignInMethod(method);
                if (user.getEmail() != null && user.getUsername() != null) {
                    String enteredPassword = Objects.requireNonNull(passwordEditText.getEditText()).getText().toString().trim();
                    String repeatedPassword = Objects.requireNonNull(repPasswordEditText.getEditText()).getText().toString().trim();
                    if (eligibaleToGetStarted(enteredPassword, repeatedPassword)) {
                        passwordEditText.setError(null);
                        repPasswordEditText.setError(null);
                        if (method == SignInMethod.DEFAULT) {
                            registerUser(user, enteredPassword);
                        } else if (method == SignInMethod.GOOGLE || method == SignInMethod.MICROSOFT) {
                            registerWithGoogleOrMicrosoft(user, enteredPassword);
                        }
                    } else {
                        if (!termsCond.isChecked()) {
                            new CustomToast(PasswordSignUpActivity.this, "Please read and accept the our policy terms and condition", R.drawable.ic_error_icon).showCustomToast();
                        }
                        if (!enteredPassword.equals(repeatedPassword)) {
                            repPasswordEditText.setError("Password do not match");
                        } else {
                            repPasswordEditText.setError(null);
                        }
                        if (enteredPassword.length() < 8) {
                            passwordEditText.setError("Password must contain at least 8 characters");
                        } else {
                            passwordEditText.setError(null);
                        }
                    }
                }
            }
        });

    }

    private boolean eligibaleToGetStarted(String enteredPass, String repPass) {
        return termsCond.isChecked() && enteredPass.equals(repPass) && enteredPass.length() >= 8;
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> {
            onBackPressed();
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
    }

    private void registerUser(User user, String password) {
        register.setClickable(false);
        loadingAnimation.setVisibility(View.VISIBLE);
        loadingAnimation.playAnimation();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth.createUserWithEmailAndPassword(user.getEmail(), password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(user.getName())
                            .build();
                    firebaseUser.updateProfile(profileChangeRequest);

                    rootRef.child(Config.users).child(firebaseUser.getUid()).setValue(user).addOnCompleteListener(task1 -> {
                        if (task.isSuccessful()) {
                            loadingAnimation.setVisibility(View.GONE);
                            loadingAnimation.pauseAnimation();
                            new CustomToast(PasswordSignUpActivity.this, "Registered Successfully", R.drawable.ic_accept_check).showCustomToast();
                            Intent intent = new Intent(PasswordSignUpActivity.this, LoginActivity.class);
                            isRegistrationFinished = true;
                            startActivity(intent);
                        }
                    }).addOnFailureListener(e -> {
                        loadingAnimation.setVisibility(View.GONE);
                        loadingAnimation.pauseAnimation();
                        new CustomToast(PasswordSignUpActivity.this , "We encountered an unexpected error" ,R.drawable.ic_error_icon).showCustomToast();
                    });
                }
            }

        }).addOnFailureListener(e -> {
            loadingAnimation.setVisibility(View.GONE);
            loadingAnimation.pauseAnimation();
            new CustomToast(PasswordSignUpActivity.this, e.getMessage(), R.drawable.ic_error_icon).showCustomToast();
        });
    }

    private void registerWithGoogleOrMicrosoft(User user, String password) {
        register.setClickable(false);
        loadingAnimation.setVisibility(View.VISIBLE);
        loadingAnimation.playAnimation();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        firebaseUser.updatePassword(password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                .setDisplayName(user.getName())
                                .build();
                        firebaseUser.updateProfile(profileChangeRequest);

                        rootRef.child(Config.users).child(firebaseUser.getUid()).setValue(user).addOnCompleteListener(task1 -> {
                            if (task.isSuccessful()) {
                                loadingAnimation.setVisibility(View.GONE);
                                loadingAnimation.pauseAnimation();
                                new CustomToast(PasswordSignUpActivity.this, "Registered Successfully", R.drawable.ic_accept_check).showCustomToast();
                                isRegistrationFinished = true;

                                rootRef.child(Config.users).child(firebaseUser.getUid()).get().addOnCompleteListener(newTask -> {
                                    User currentUser = newTask.getResult().getValue(User.class);
                                    currentUser.setUserID(firebaseUser.getUid());
                                    kz.devs.aiturm.presentaiton.SessionManager manager = new SessionManager(PasswordSignUpActivity.this);
                                    manager.removeUserData();
                                    Boolean result = manager.saveData(currentUser);
                                });
                                startActivity(MainActivity.newInstance(PasswordSignUpActivity.this));
                            }
                        }).addOnFailureListener(e -> {
                            loadingAnimation.setVisibility(View.GONE);
                            loadingAnimation.pauseAnimation();
                            new CustomToast(PasswordSignUpActivity.this, "We encountered an unexpected error", R.drawable.ic_error_icon).showCustomToast();
                        });
                    }
                }).addOnFailureListener(e -> {
                    e.printStackTrace();
                    registrationAborted();
                });


    }

    private void sendEmailVerification() {
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Registered Successfully. kindly Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PasswordSignUpActivity.this, LoginActivity.class);
                    startActivity(intent);
                    Toast.makeText(PasswordSignUpActivity.this, user.getDisplayName() + ", you are a Shroomie now", Toast.LENGTH_SHORT).show();
                    //get public and private keys for Virgil e3 kit
                } else {
                    Log.e("Register", "Send Email verification failed!", task.getException());
                    new CustomToast(PasswordSignUpActivity.this, "Failed to send verification email", R.drawable.ic_error_icon).showCustomToast();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        isRegistrationFinished = true;
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        if (!isRegistrationFinished) {
            registrationAborted();
        }
        super.onPause();
    }

    private void registrationAborted() {
        Toast.makeText(PasswordSignUpActivity.this, "Failed to register", Toast.LENGTH_SHORT).show();
        mAuth.signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(PasswordSignUpActivity.this, gso);
        mGoogleSignInClient.signOut();
        startActivity(LoginActivity.getInstance(PasswordSignUpActivity.this));
    }
}