package kz.devs.aiturm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import kz.devs.aiturm.model.User;

public class ChangeNameDialogFragment extends DialogFragment {
    private View v;
    private NameChangedCallback changedNameChangedCallback;
    private FirebaseAuth mAuth;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @SuppressLint("RtlHardcoded")
    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setGravity(Gravity.LEFT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
            showKeyboard();
        }
    }
    public interface NameChangedCallback {
        void onNameChanged(String changedName);
    }
    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            changedNameChangedCallback =(NameChangedCallback) getTargetFragment();

        } catch (Exception e) {
            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        closeKeyboard();
    }
    public void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
    public void closeKeyboard(){
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.EditProfileOptionsAnimation);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         v= inflater.inflate(R.layout.fragment_change_name_dialog, container, false);
         mAuth=FirebaseAuth.getInstance();
         user=new User();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextInputLayout nameEditTxt=v.findViewById(R.id.enter_new_name);
        ImageButton backButton=v.findViewById(R.id.change_name_back_button);
        MaterialButton doneButton=v.findViewById(R.id.change_name_done_button);
        nameEditTxt.setEndIconOnClickListener(v -> nameEditTxt.getEditText().setText(""));
        Bundle bundle=this.getArguments();
        if(bundle!=null) {
            user = (User) bundle.getSerializable("USER");
            if(user!=null) {
                if(user.getName() != null && !user.getName().isEmpty()){
                    nameEditTxt.getEditText().setText(user.getName());
                }else{
                    nameEditTxt.getEditText().setHint("My name");
                }
                nameEditTxt.getEditText().requestFocus();
                nameEditTxt.getEditText().setSelection(nameEditTxt.getEditText().getText().length());
                backButton.setOnClickListener(view1 -> {
                    String enteredName=nameEditTxt.getEditText().getText().toString().trim();
                    if(!user.getName().equals(enteredName)){
                        nameEditTxt.setError("Unsaved Changes");
                    }else{
                        closeKeyboard();
                        nameEditTxt.setError(null);
                        dismiss();
                    }
                });
                doneButton.setOnClickListener(view12 -> {
                    nameEditTxt.setError(null);
                    String enteredName=nameEditTxt.getEditText().getText().toString().trim();
                    if(enteredName.equals(user.getName())) {
                        nameEditTxt.setError("No changes have been made");
                    } else {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            updateName(enteredName, firebaseUser.getUid());
                        } else {
                            closeKeyboard();
                            dismiss();
                        }
                    }
                });
            }
        }
    }
    private void updateName(String enteredName,String userUid){
         DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
         rootRef.child(Config.users).child(userUid).child("name").setValue(enteredName).addOnCompleteListener(task -> {
             if(task.isSuccessful()){
                 changedNameChangedCallback.onNameChanged(enteredName);
                 closeKeyboard();
                 dismiss();
             }
         }).addOnFailureListener(e -> Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show());
    }
    @Override
    public void onCancel(@NonNull @NotNull DialogInterface dialog) {
        super.onCancel(dialog);
        closeKeyboard();
    }
}