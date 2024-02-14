package kz.devs.aiturm.presentaiton.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
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


public class ChangePhoneDialogFragment extends DialogFragment {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private TextInputLayout phoneNumberInputLayout;
    private String newPhoneNumber = "";
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
        phoneNumberInputLayout = v.findViewById(R.id.change_phone_input_text);
        Button doneButton = v.findViewById(R.id.change_phone_done_button);
        ImageButton backButton = v.findViewById(R.id.change_phone_back_button);
        Bundle bundle=this.getArguments();
        if (bundle!=null) {
            user = (User) bundle.getSerializable("USER");
            newPhoneNumber = user.getPhoneNumber();
        } else {
            closeKeyboard();
            dismiss();

        }

        setupPhoneNumberFormatting();
        phoneNumberInputLayout.requestFocus();
        showKeyboard();

        phoneNumberInputLayout.setEndIconOnClickListener(view1 -> phoneNumberInputLayout.getEditText().setText(""));
        doneButton.setOnClickListener(v -> {
            String txtPhone = newPhoneNumber;
            if (txtPhone.equals(user.getPhoneNumber())){
                phoneNumberInputLayout.setError("No changes have been made");
            } else if (txtPhone.isBlank()) {
                phoneNumberInputLayout.setError("phone number can not be empty");
            } else {
                phoneNumberInputLayout.setError(null);
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    updatePhone(txtPhone);
                }
            }

        });
        backButton.setOnClickListener(v -> {
            if ((user.getPhoneNumber() != null && !user.getPhoneNumber().equals(newPhoneNumber))) {
                Toast.makeText(getContext(), "unSaved Changes", Toast.LENGTH_LONG).show();
            } else {
                closeKeyboard();
                dismiss();
            }
        });


    }

    private void setupPhoneNumberFormatting() {
        var phoneNumber = user.getPhoneNumber();
        if (phoneNumber == null || phoneNumber.isBlank()) {
            phoneNumberInputLayout.getEditText().setText("+7");
        } else {
            String ans = "+7(" + phoneNumber.substring(1, 4) + ") " + phoneNumber.substring(
                    4, 7
            ) + "-" + phoneNumber.substring(7, 9) + "-" + phoneNumber.substring(9);
            phoneNumberInputLayout.getEditText().setText(ans);
        }
        phoneNumberInputLayout.getEditText().addTextChangedListener(new PhoneNumberFormattingTextWatcher() {

            boolean backspacingFlag = false;
            int cursorComplement = 0;
            boolean editedFlag = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                cursorComplement = s.length() - phoneNumberInputLayout.getEditText().getSelectionStart();
                //we check if the user ir inputing or erasing a character
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


    private void updatePhone(String txtPhone) {
        if (phoneNumberInputLayout.getError() != null) return;
        if (phoneNumberInputLayout.getEditText().getText() == null || phoneNumberInputLayout.getEditText().getText().toString().trim().isBlank()) return;
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