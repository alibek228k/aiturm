package kz.devs.aiturm.presentaiton.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

import kz.devs.aiturm.Config;
import kz.devs.aiturm.R;
import kz.devs.aiturm.model.User;


public class ChangeSpecialityDialogFragment extends DialogFragment {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private TextInputLayout newSpeciality;
    private User user;
    private View v;
    private SpecialityChangeCallback changedSpecialityChangeCallback;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.dialog_fragment_change_speciality, container, false);
        mAuth=FirebaseAuth.getInstance();
        rootRef=FirebaseDatabase.getInstance().getReference();
        return v;
    }

    public interface SpecialityChangeCallback {
        void onSpecialityChanged(String specialityTxt);
}

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            changedSpecialityChangeCallback = (SpecialityChangeCallback) getTargetFragment();

        } catch (Exception e) {
            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newSpeciality = v.findViewById(R.id.change_speciality_input_text);
        newSpeciality.requestFocus();
        showKeyboard();
        Button doneButton = v.findViewById(R.id.change_speciality_done_button);
        ImageButton backButton = v.findViewById(R.id.change_speciality_back_button);
        Bundle bundle=this.getArguments();
        if (bundle!=null) {
            user = (User) bundle.getSerializable("USER");
            if (!(user.getSpecialization() == null || user.getSpecialization().equals(""))) {
                newSpeciality.getEditText().setText(user.getSpecialization());
                newSpeciality.getEditText().setSelection(newSpeciality.getEditText().getText().length());
            } else {
                newSpeciality.setHint("my specialization");
            }
        } else {
            closeKeyboard();
            dismiss();

        }
        newSpeciality.setEndIconOnClickListener(view1 -> newSpeciality.getEditText().setText(""));
        doneButton.setOnClickListener(v -> {
            String txtSpeciality = newSpeciality.getEditText().getText().toString().trim();
            if (txtSpeciality.equals(user.getSpecialization())){
                newSpeciality.setError("No changes have been made");
            } else if (txtSpeciality.isBlank()) {
                newSpeciality.setError("Specialization can not be empty");
            } else {
                newSpeciality.setError(null);
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    updateSpeciality(txtSpeciality);
                }
            }

        });
        backButton.setOnClickListener(v -> {
            if ((user.getSpecialization() != null && !user.getSpecialization().equals(newSpeciality.getEditText().getText().toString()))) {
                Toast.makeText(getContext(), "unSaved Changes", Toast.LENGTH_LONG).show();
            } else {
                closeKeyboard();
                dismiss();
            }
        });


    }
    private void updateSpeciality(String txtSpeciality) {
        HashMap<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("specialization", txtSpeciality);

        rootRef.child(Config.users).child(user.getUserID()).updateChildren(updateDetails).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
//                changedGroupChangeCallback.
//                d(txtGroup);
                changedSpecialityChangeCallback.onSpecialityChanged(txtSpeciality);
                Toast.makeText(getContext(), "Updated your speciality" +
                        "", Toast.LENGTH_SHORT).show();
                closeKeyboard();
                dismiss();
            }
        }).addOnFailureListener(e -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    public void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
    public void closeKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    @Override
    public void onStop() {
        super.onStop();
        closeKeyboard();
    }
}