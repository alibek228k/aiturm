package kz.devs.aiturm.presentaiton.authorization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
    private Spinner specialitySpinner, groupSpinner;

    private String selectedSpeciality;
    private String specialityError;

    private String selectedGroup;
    private String groupError;


    private RadioButton maleRadioButton;
    private RadioButton femaleRadioButton;
    private TextInputLayout phoneNumberInputLayout;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private GoogleSignInOptions gso;
    private String newPhoneNumber = "";

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
        specialitySpinner = findViewById(R.id.speciality_spinner);
        groupSpinner = findViewById(R.id.group_spinner);

        initFirebaseArguments();
        setupSignInMethod();
        setupToolbar();
        setupPhoneNumberFormatting();
        setupNextButton();
        setupSpecialitySpinner();
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

    private void setupSpecialitySpinner() {
        var adapter = ArrayAdapter.createFromResource(this, R.array.spinner_speciality, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        specialitySpinner.setAdapter(adapter);

        specialitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSpeciality = (String) parent.getItemAtPosition(position);
                switch (selectedSpeciality) {
                    case "Computer Science":
                        setupITGroupsSpinner();
                        break;
                    case "Software Engineering":
                        setupSEGroupsSpinner();
                        break;
                    case "Big Data Analysis":
                        setupBDGroupsSpinner();
                        break;
                    case "Media Technologies":
                        setupMTGroupsSpinner();
                        break;
                    case "Cyber Security":
                        setupCSGroupsSpinner();
                        break;
                    case "Telecommunication Systems":
                        setupTSGroupsSpinner();
                        break;
                    case "Smart Technologies":
                        setupSTGroupsSpinner();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupCSGroupsSpinner() {
        var adapter = ArrayAdapter.createFromResource(this, R.array.spinner_cs, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupBDGroupsSpinner() {
        var adapter = ArrayAdapter.createFromResource(this, R.array.spinner_bd, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupSEGroupsSpinner() {
        var adapter = ArrayAdapter.createFromResource(this, R.array.spinner_se, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupMTGroupsSpinner() {
        var adapter = ArrayAdapter.createFromResource(this, R.array.spinner_mt, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupITGroupsSpinner() {
        var adapter = ArrayAdapter.createFromResource(this, R.array.spinner_it, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupTSGroupsSpinner() {
        var adapter = ArrayAdapter.createFromResource(this, R.array.spinner_ts, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupSTGroupsSpinner() {
        var adapter = ArrayAdapter.createFromResource(this, R.array.spinner_st, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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

    private void setupPhoneNumberFormatting() {
        phoneNumberInputLayout.getEditText().addTextChangedListener(new PhoneNumberFormattingTextWatcher() {
            boolean backspacingFlag = false;
            int cursorComplement = 0;
            boolean editedFlag = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                cursorComplement = s.length() - phoneNumberInputLayout.getEditText().getSelectionStart();
                backspacingFlag = count > after;
            }

            @Override
            public synchronized void afterTextChanged(Editable s) {
                String string = s.toString();
                String phone = string.replaceAll("[^\\d]", "");

                if (!editedFlag) {

                    if (phone.length() >= 9 && !backspacingFlag) {
                        editedFlag = true;
                        String ans = "+7(" + phone.substring(1, 4) + ") " + phone.substring(
                                4, 7
                        ) + "-" + phone.substring(7, 9) + "-" + phone.substring(9);
                        phoneNumberInputLayout.getEditText().setText(ans);
                        phoneNumberInputLayout.getEditText().setSelection(phoneNumberInputLayout.getEditText().getText().length() - cursorComplement);

                    } else if (phone.length() >= 4 && !backspacingFlag) {
                        editedFlag = true;
                        String ans = "+7(" + phone.substring(1, 4) + ") " + phone.substring(4);
                        phoneNumberInputLayout.getEditText().setText(ans);
                        phoneNumberInputLayout.getEditText().setSelection(phoneNumberInputLayout.getEditText().getText().length() - cursorComplement);
                    }
                } else {
                    editedFlag = false;
                }

                newPhoneNumber = phone;
            }
        });

    }

    private void setupNextButton() {
        nextButton.setOnClickListener(view -> {

            String username = usernameInputLayout.getEditText().getText().toString().trim();
            String speciality = selectedSpeciality;
            String group = selectedGroup;
            String firstName = firstNameInputLayout.getEditText().getText().toString().trim();
            String lastName = lastNameInputLayout.getEditText().getText().toString().trim();
            String email = emailInputLayout.getEditText().getText().toString().trim().toLowerCase();

            if (username.isBlank() || username.length() < 4) {
                usernameInputLayout.setError("Username should be at least 4 characters");
            } else {
                usernameInputLayout.setError(null);
            }
            if (newPhoneNumber.isBlank()) {
                phoneNumberInputLayout.setError("Phone number can not be blanc");
            } else {
                phoneNumberInputLayout.setError(null);
            }
            if (speciality == null || speciality.isBlank()) {
                Toast.makeText(this, R.string.field_can_not_be_empty, Toast.LENGTH_SHORT).show();
                specialityError = getString(R.string.field_can_not_be_empty);
                System.out.println("It is here speciality");
            } else {
                specialityError = null;
            }
            if (group == null || group.isBlank()) {
                Toast.makeText(this, R.string.field_can_not_be_empty, Toast.LENGTH_SHORT).show();
                groupError = getString(R.string.field_can_not_be_empty);
                System.out.println("It is here group $");
            } else {
                groupError = null;
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
                            specialityError == null &&
                            groupError == null &&
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
                            specialityError == null &&
                            groupError == null &&
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
                    user.setGroup(selectedGroup);
                    user.setName(firstNameInputLayout.getEditText().getText().toString() + " " + lastNameInputLayout.getEditText().getText().toString() + " " + patronymicNameInputLayout.getEditText().getText().toString());
                    if (maleRadioButton.isChecked()) {
                        user.setGender(User.Gender.MALE);
                    } else if (femaleRadioButton.isChecked()) {
                        user.setGender(User.Gender.FEMALE);
                    }
                    user.setPhoneNumber(newPhoneNumber);
                    user.setSpecialization(selectedSpeciality);
                    user.setGroup(selectedGroup);
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
                        user.setGroup(selectedGroup);
                        user.setName(currentUser.getDisplayName());
                        if (maleRadioButton.isChecked()) {
                            user.setGender(User.Gender.MALE);
                        } else if (femaleRadioButton.isChecked()) {
                            user.setGender(User.Gender.FEMALE);
                        }
                        user.setPhoneNumber(newPhoneNumber);
                        user.setSpecialization(selectedSpeciality);
                        user.setGroup(selectedGroup);

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