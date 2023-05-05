package kz.devs.aiturm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.shroomies.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import kz.devs.aiturm.model.User;

public class ChangeGroupDialog extends DialogFragment {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private User user;
    private TextInputLayout newGroup;
    private View v;
    private GroupChangeCallback changedGroupChangeCallback;

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
            showKeyboard();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.EditProfileOptionsAnimation);
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            changedGroupChangeCallback =(GroupChangeCallback) getTargetFragment();
        }catch (Exception e) {
            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.dialog_fragment_change_group, container, false);
        mAuth=FirebaseAuth.getInstance();
        rootRef=FirebaseDatabase.getInstance().getReference();
        return v;
    }
    public interface GroupChangeCallback {
        void onGroupChanged(String emailTxt);
    }
    public void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
    public void closeKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newGroup = v.findViewById(R.id.change_group_input_text);
        newGroup.requestFocus();
        Button doneButton = v.findViewById(R.id.change_group_done_button);
        ImageButton backButton = v.findViewById(R.id.change_group_back_button);
        Bundle bundle=this.getArguments();
        if (bundle!=null) {
            user = (User) bundle.getSerializable("USER");
            newGroup.getEditText().setText(user.getEmail());
            newGroup.getEditText().requestFocus();
            newGroup.getEditText().setSelection(newGroup.getEditText().getText().length());
        } else {
//            todo error handling when bundle is null
            dismiss();
        }
        newGroup.setEndIconOnClickListener(view1 -> newGroup.getEditText().setText(""));


        doneButton.setOnClickListener(v -> {
            FirebaseUser firebaseUser=mAuth.getCurrentUser();
            if (firebaseUser!=null) {
                String txtGroup = newGroup.getEditText().getText().toString().trim();
                if (user.getEmail().equals(txtGroup)) {
                    newGroup.setError("No changes have been made");
                } else {
                    if(validGroup(txtGroup)) {
                        newGroup.setError(null);
                        updateGroup(txtGroup);
                    }else{
                        newGroup.setError("Please enter valid group number");
                    }
                }
            } else{
                //            todo error handling when user is null or signed out
            }
        });

        backButton.setOnClickListener(v -> {
            if (!user.getGroup().equals(newGroup.getEditText().getText().toString())) {
                newGroup.setError("unSaved Changes");
            } else {
                closeKeyboard();
                dismiss();
            }
        });

    }
    private void updateGroup(String txtGroup) {
        HashMap<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("group", txtGroup);
        rootRef.child(Config.users).child(user.getUserID()).updateChildren(updateDetails).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mAuth.getCurrentUser().updateEmail(txtGroup);
                changedGroupChangeCallback.onGroupChanged(txtGroup);
                closeKeyboard();
                Toast.makeText(getActivity(), "Updated group number successfully", Toast.LENGTH_SHORT).show();
                dismiss();
//                    FirebaseUser firebaseUser=mAuth.getCurrentUser();
//                    sendEmailVerification(firebaseUser);
            }
        }).addOnFailureListener(e -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private boolean validGroup(String enteredGroup) {
        if (enteredGroup.length() < 0 || enteredGroup.length() > 50) {
            return false;
        }

        if (!enteredGroup.matches("[a-zA-Z0-9_]+")) {
            return false;
        }

        return true;
    }
//    private void sendEmailVerification(FirebaseUser firebaseUser) {
////        firebaseUser.updateEmail(newEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
////            @Override
////            public void onComplete(@NonNull Task<Void> task) {
////                if(task.isSuccessful()){
////                   // Toast.makeText(getContext(), "Error! Try again", Toast.LENGTH_SHORT).show();
//        firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                Toast.makeText(getActivity(), "Updated Successfully. Verification email sent to " + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getActivity(), LoginActivity.class);
//                startActivity(intent);
//
//            } else {
//                Toast.makeText(getActivity(), "Failed to send verification email", Toast.LENGTH_SHORT).show();
//            }
//        });
////                }
////                else {
////
////                }
////            }
////        });
//    }

    @Override
    public void onStop() {
        super.onStop();
        closeKeyboard();
    }

    @Override
    public void onCancel(@NonNull @NotNull DialogInterface dialog) {
        super.onCancel(dialog);
        closeKeyboard();
    }
}