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


public class ChangePhoneDialogFragment extends DialogFragment {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private TextInputLayout newPhone;
    private User user;
    private View v;
    private PhoneChangeCallback changedPhoneChangeCallback;

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
        v=inflater.inflate(R.layout.dialog_fragment_change_phone, container, false);
        mAuth=FirebaseAuth.getInstance();
        rootRef=FirebaseDatabase.getInstance().getReference();
        return v;
    }

    public interface PhoneChangeCallback {
        void onPhoneChanged(String phoneTxt);
}

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            changedPhoneChangeCallback = (PhoneChangeCallback) getTargetFragment();

        } catch (Exception e) {
            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newPhone = v.findViewById(R.id.change_phone_input_text);
        newPhone.requestFocus();
        showKeyboard();
        Button doneButton = v.findViewById(R.id.change_phone_done_button);
        ImageButton backButton = v.findViewById(R.id.change_phone_back_button);
        Bundle bundle=this.getArguments();
        if (bundle!=null) {
            user = (User) bundle.getSerializable("USER");
            if (!(user.getPhoneNumber() == null || user.getPhoneNumber().equals(""))) {
                newPhone.getEditText().setText(user.getPhoneNumber());
                newPhone.getEditText().setSelection(newPhone.getEditText().getText().length());
            } else {
                newPhone.setHint("phoneNumber");
            }
        } else {
            closeKeyboard();
            dismiss();

        }
        newPhone.setEndIconOnClickListener(view1 -> newPhone.getEditText().setText(""));
        doneButton.setOnClickListener(v -> {
            String txtPhone = newPhone.getEditText().getText().toString().trim();
            if (txtPhone.equals(user.getPhoneNumber())){
                newPhone.setError("No changes have been made");
            } else if (txtPhone.isBlank()) {
                newPhone.setError("phone number can not be empty");
            } else {
                newPhone.setError(null);
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    updatePhone(txtPhone);
                }
            }

        });
        backButton.setOnClickListener(v -> {
            if ((user.getPhoneNumber() != null && !user.getPhoneNumber().equals(newPhone.getEditText().getText().toString()))) {
                Toast.makeText(getContext(), "unSaved Changes", Toast.LENGTH_LONG).show();
            } else {
                closeKeyboard();
                dismiss();
            }
        });


    }
    private void updatePhone(String txtPhone) {
        if (!txtPhone.startsWith("+7") && !txtPhone.startsWith("8")) {
            newPhone.setError("Invalid phone number format");
            return;
        }
        HashMap<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("phoneNumber", txtPhone);

        rootRef.child(Config.users).child(user.getUserID()).updateChildren(updateDetails).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                changedPhoneChangeCallback.onPhoneChanged(txtPhone);
                Toast.makeText(getContext(), "Updated your phone number" +
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