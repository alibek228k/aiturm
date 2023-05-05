package kz.devs.aiturm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.example.shroomies.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

import kz.devs.aiturm.model.User;


public class ChangeGenderDialogFragment extends DialogFragment {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private TextInputLayout newGender;
    private User user;
    private View v;
    private RadioGroup genderRadioGroup;
    private RadioButton maleRadioButton;
    private RadioButton femaleRadioButton;

    private GenderChangeListener genderChangeListener;
    private GenderChangeCallback changedGenderChangeCallback;

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setGravity(Gravity.VERTICAL_GRAVITY_MASK);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Objects.requireNonNull(getDialog()).getWindow().setWindowAnimations(R.style.EditProfileOptionsAnimation);
    }

    public interface GenderChangeCallback {
        void onGenderChanged(String bioTxt);
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            changedGenderChangeCallback = (GenderChangeCallback) getTargetFragment();

        } catch (Exception e) {
            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

        public interface GenderChangeListener {
            void onGenderChanged(String gender);
        }

        public void setGenderChangeListener(GenderChangeListener genderChangeListener) {
            this.genderChangeListener = genderChangeListener;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.dialog_fragment_change_gender, container, false);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            genderRadioGroup = view.findViewById(R.id.gender_radio_group);
            maleRadioButton = view.findViewById(R.id.male_radio_button);
            femaleRadioButton = view.findViewById(R.id.female_radio_button);
            Button doneButton = view.findViewById(R.id.change_email_done_button);
            ImageButton backButton = view.findViewById(R.id.change_gender_back_button);

            Bundle bundle = getArguments();
            if (bundle != null) {
                String gender = bundle.getString("GENDER");
                if (gender != null && gender.equals("female")) {
                    femaleRadioButton.setChecked(true);
                } else {
                    maleRadioButton.setChecked(true);
                }
            }

            doneButton.setOnClickListener(v -> {
                String gender;
                if (maleRadioButton.isChecked()) {
                    gender = "male";
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        updateGender(bundle.toString());
                    }
                } else {
                    gender = "female";
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        updateGender(bundle.toString());
                    }
                }
                genderChangeListener.onGenderChanged(gender);

                updateGender(gender);
                dismiss();
            });

            backButton.setOnClickListener(v -> {
                dismiss();
            });

            return view;
        }


    private void updateGender(String txtGender) {
        HashMap<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("gender", txtGender);

        rootRef.child(Config.users).child(user.getUserID()).updateChildren(updateDetails).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
//                changedGroupChangeCallback.
//                d(txtGroup);
                changedGenderChangeCallback.onGenderChanged(txtGender);
                Toast.makeText(getContext(), "Updated your group" +
                        "", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }).addOnFailureListener(e -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    @Override
    public void onStop() {
        super.onStop();
    }
}