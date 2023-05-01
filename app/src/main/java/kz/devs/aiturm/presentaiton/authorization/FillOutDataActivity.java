package kz.devs.aiturm.presentaiton.authorization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shroomies.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import kz.devs.aiturm.Config;
import kz.devs.aiturm.CustomToast;
import kz.devs.aiturm.LoginActivity;
import kz.devs.aiturm.PasswordSignUpActivity;
import kz.devs.aiturm.model.SignInMethod;
import kz.devs.aiturm.model.User;

public class FillOutDataActivity extends AppCompatActivity {

    private static final String SIGN_IN_METHOD_KEY = "SIGN_IN_METHOD_KEY";

    public static Intent getInstance(Context context, SignInMethod method) {
        return new Intent(context, FillOutDataActivity.class).putExtra(SIGN_IN_METHOD_KEY, method);
    }

    private SignInMethod method;
    private boolean isRegistrationFinished = false;

    //UI view
    private MaterialToolbar toolbar;
    private ImageButton nextButton;
    private TextInputLayout usernameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout firstNameInputLayout;
    private TextInputLayout lastNameInputLayout;
    private TextInputLayout patronymicNameInputLayout;
    private RadioGroup genderRadioGroup;

    private RadioButton maleRadioButton;
    private RadioButton femaleRadioButton;
    private TextInputLayout phoneNumberInputLayout;
    private TextInputLayout specialityInputLayout;
    private TextInputLayout groupInputLayout;


    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private GoogleSignInOptions gso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_out_data);

        toolbar = findViewById(R.id.sign_up_tool_bar);
        nextButton = findViewById(R.id.next_button_sign_up);
        usernameInputLayout = findViewById(R.id.username_sign_up);
        emailInputLayout = findViewById(R.id.email_sign_up);
        firstNameInputLayout = findViewById(R.id.first_name_sign_up);
        lastNameInputLayout = findViewById(R.id.last_name_sign_up);
        patronymicNameInputLayout = findViewById(R.id.patronymic_name_sign_up);
        genderRadioGroup = findViewById(R.id.gender_sign_up);
        maleRadioButton = genderRadioGroup.findViewById(R.id.male_radio_button);
        femaleRadioButton = genderRadioGroup.findViewById(R.id.female_radio_button);
        phoneNumberInputLayout = findViewById(R.id.phone_sign_up);
        specialityInputLayout = findViewById(R.id.speciality_sign_up);
        groupInputLayout = findViewById(R.id.group_sign_up);

        initFirebaseArguments();
        setupSignInMethod();
        setupToolbar();
        setupNextButton();
    }

    private void setupSignInMethod() {
        Bundle bundle = getIntent().getExtras();
        method = (SignInMethod) bundle.getSerializable(SIGN_IN_METHOD_KEY);
        switch (method) {
            case GOOGLE:
            case MICROSOFT:
                emailInputLayout.setVisibility(View.GONE);
                firstNameInputLayout.setVisibility(View.GONE);
                lastNameInputLayout.setVisibility(View.GONE);
                patronymicNameInputLayout.setVisibility(View.GONE);

                gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                break;
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> {
            onBackPressed();
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
    }

    private void initFirebaseArguments() {
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(FillOutDataActivity.this, gso);
    }

    private void setupNextButton() {
        nextButton.setOnClickListener(view -> {

            String username = usernameInputLayout.getEditText().getText().toString().trim();
            String phoneNumber = phoneNumberInputLayout.getEditText().getText().toString().trim();
            String speciality = specialityInputLayout.getEditText().getText().toString();
            String group = groupInputLayout.getEditText().getText().toString().trim();
            String firstName = firstNameInputLayout.getEditText().getText().toString().trim();
            String lastName = lastNameInputLayout.getEditText().getText().toString().trim();
            String email = emailInputLayout.getEditText().getText().toString().trim().toLowerCase();

            if (username.isBlank() || username.length() < 4) {
                usernameInputLayout.setError("Username should be at least 4 characters");
            } else {
                usernameInputLayout.setError(null);
            }
            if (phoneNumber.isBlank() || !phoneNumber.matches("^77\\d{9}$")) {
                phoneNumberInputLayout.setError("Phone number format should be 77555555555");
            } else {
                phoneNumberInputLayout.setError(null);
            }
            if (speciality.isBlank()) {
                specialityInputLayout.setError("This field can not ve empty!");
            } else {
                specialityInputLayout.setError(null);
            }
            if (group.isBlank()) {
                groupInputLayout.setError("This field can not ve empty!");
            } else {
                groupInputLayout.setError(null);
            }

            if (maleRadioButton.isChecked() || femaleRadioButton.isChecked()) {
                maleRadioButton.setError(null);
                femaleRadioButton.setError(null);
            } else {
                maleRadioButton.setError("This field can not ve empty!");
                femaleRadioButton.setError("This field can not ve empty!");
            }


            switch (method) {
                case GOOGLE:
                case MICROSOFT:
                    if (usernameInputLayout.getError() == null
                            && phoneNumberInputLayout.getError() == null &&
                            specialityInputLayout.getError() == null &&
                            groupInputLayout.getError() == null &&
                            maleRadioButton.getError() == null &&
                            femaleRadioButton.getError() == null) {
                        signUpWithGoogleOrMicrosoft(username);
                    }
                    break;
                default:
                    if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailInputLayout.setError("Wrong format");
                    } else {
                        emailInputLayout.setError(null);
                    }
                    if (firstName.isBlank()) {
                        firstNameInputLayout.setError("This field can not ve empty!");
                    } else {
                        firstNameInputLayout.setError(null);
                    }
                    if (lastName.isBlank()) {
                        lastNameInputLayout.setError("This field can not ve empty!");
                    } else {
                        lastNameInputLayout.setError(null);
                    }

                    if (usernameInputLayout.getError() == null &&
                            phoneNumberInputLayout.getError() == null &&
                            specialityInputLayout.getError() == null &&
                            groupInputLayout.getError() == null &&
                            emailInputLayout.getError() == null &&
                            lastNameInputLayout.getError() == null &&
                            firstNameInputLayout.getError() == null &&
                            maleRadioButton.getError() == null &&
                            femaleRadioButton.getError() == null
                    ) {
                        defaultSignUp(email, username);
                    }
                    break;
            }

        });
    }

    private void defaultSignUp(String email, String username) {
        checkDuplicateUserName(username).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().getValue() != null) {
                    usernameInputLayout.setError(username + " is taken");
                } else {
                    emailInputLayout.setError(null);
                    usernameInputLayout.setError(null);
                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setGroup(groupInputLayout.getEditText().getText().toString());
                    user.setName(firstNameInputLayout.getEditText().getText().toString() + " " + lastNameInputLayout.getEditText().getText().toString() + " " + patronymicNameInputLayout.getEditText().getText().toString());
                    if (maleRadioButton.isChecked()) {
                        user.setGender(User.Gender.MALE);
                    } else if (femaleRadioButton.isChecked()) {
                        user.setGender(User.Gender.FEMALE);
                    }
                    user.setPhoneNumber(phoneNumberInputLayout.getEditText().getText().toString());
                    user.setSpecialization(specialityInputLayout.getEditText().getText().toString());
                    user.setGroup(groupInputLayout.getEditText().getText().toString());
                    Intent intent = new Intent(FillOutDataActivity.this, PasswordSignUpActivity.class);
                    intent.putExtra("USER", user);
                    intent.putExtra("SIGN_UP_METHOD", method);
                    isRegistrationFinished = true;
                    startActivity(intent);
                }
            } else {
                new CustomToast(FillOutDataActivity.this, "We encountered a problem", R.drawable.ic_error_icon).showCustomToast();
                registrationAborted();
            }
        });
    }

    private void signUpWithGoogleOrMicrosoft(String username) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String email = currentUser.getEmail();
        if (email != null) {
            checkDuplicateUserName(username).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().getValue() != null) {
                        usernameInputLayout.setError(username + " is taken");
                    } else {
                        usernameInputLayout.setError(null);
                        User user = new User();
                        user.setUsername(username);
                        user.setEmail(email);
                        user.setGroup(groupInputLayout.getEditText().getText().toString());
                        user.setName(currentUser.getDisplayName());
                        if (maleRadioButton.isChecked()) {
                            user.setGender(User.Gender.MALE);
                        } else if (femaleRadioButton.isChecked()) {
                            user.setGender(User.Gender.FEMALE);
                        }
                        user.setPhoneNumber(phoneNumberInputLayout.getEditText().getText().toString());
                        user.setSpecialization(specialityInputLayout.getEditText().getText().toString());
                        user.setGroup(groupInputLayout.getEditText().getText().toString());

                        Intent intent = new Intent(FillOutDataActivity.this, PasswordSignUpActivity.class);
                        intent.putExtra("USER", user);
                        intent.putExtra("SIGN_UP_METHOD", method);
                        isRegistrationFinished = true;
                        startActivity(intent);
                    }
                }
            });
        }
    }

    private Task<DataSnapshot> checkDuplicateUserName(String userName) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference(Config.usersnames);
        return rootRef.orderByKey().equalTo(userName).get().addOnCompleteListener(task -> {
        });
    }

    @Override
    public void onBackPressed() {
        if (!isRegistrationFinished) {
            onBackButtonClicked();
            isRegistrationFinished = true;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        isRegistrationFinished = false;
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (!isRegistrationFinished) {
            registrationAborted();
        }
        super.onPause();
    }

    private void registrationAborted() {
        if (method == SignInMethod.GOOGLE || method == SignInMethod.MICROSOFT) {
            mAuth.signOut();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(FillOutDataActivity.this, gso);
            mGoogleSignInClient.signOut();
        }
    }

    private void onBackButtonClicked() {
        if (method == SignInMethod.GOOGLE || method == SignInMethod.MICROSOFT) {
            Toast.makeText(FillOutDataActivity.this, "Failed to register", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(FillOutDataActivity.this, gso);
            mGoogleSignInClient.signOut();
        }
    }
}