package kz.devs.aiturm.presentaiton.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
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

import kz.devs.aiturm.Config;
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

    private User.Gender currentGender;

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
        void onGenderChanged(User.Gender gender);
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
                user = (User) bundle.getSerializable("USER");
                currentGender = user.getGender();
                if (user.getGender() == User.Gender.FEMALE){
                    femaleRadioButton.setChecked(true);
                    maleRadioButton.setChecked(false);
                }else if (user.getGender() == User.Gender.MALE){
                    maleRadioButton.setChecked(true);
                    femaleRadioButton.setChecked(false);
                }

                genderRadioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
                    if (i == 0){
                        currentGender = User.Gender.MALE;
                    }else if (i == 1){
                        currentGender = User.Gender.FEMALE;
                    }
                });
            }

            doneButton.setOnClickListener(v -> {
                if (currentGender != user.getGender()) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Change gender")
                            .setMessage("Are you sure to change gender?")
                            .setNegativeButton("No", (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .setPositiveButton("Yes", ((dialogInterface, i) -> {
                                updateGender(currentGender);
                                dialogInterface.dismiss();
                            }))
                            .create()
                            .show();
                } else {
                    dismiss();
                }
            });

            backButton.setOnClickListener(v -> {
                dismiss();
            });

            return view;
        }


    private void updateGender(User.Gender gender) {
        HashMap<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("gender", gender);

        rootRef.child(Config.users).child(user.getUserID()).updateChildren(updateDetails).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                changedGenderChangeCallback.onGenderChanged(currentGender);
                Toast.makeText(getContext(), "Your gender successfully changed", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }).addOnFailureListener(e -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    @Override
    public void onStop() {
        super.onStop();
    }
}