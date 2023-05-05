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


public class ChangeGroupDialogFragment extends DialogFragment {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private TextInputLayout newGroup;
    private User user;
    private View v;
    private GroupChangeCallback changedGroupChangeCallback;

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
        v=inflater.inflate(R.layout.dialog_fragment_change_group, container, false);
        mAuth=FirebaseAuth.getInstance();
        rootRef=FirebaseDatabase.getInstance().getReference();
        return v;
    }

    public interface GroupChangeCallback {
        void onGroupChanged(String groupTxt);
}

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            changedGroupChangeCallback = (GroupChangeCallback) getTargetFragment();

        } catch (Exception e) {
            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newGroup = v.findViewById(R.id.change_group_input_text);
        newGroup.requestFocus();
        showKeyboard();
        Button doneButton = v.findViewById(R.id.change_group_done_button);
        ImageButton backButton = v.findViewById(R.id.change_group_back_button);
        Bundle bundle=this.getArguments();
        if (bundle!=null) {
            user = (User) bundle.getSerializable("USER");
            if (!(user.getGroup() == null || user.getGroup().equals(""))) {
                newGroup.getEditText().setText(user.getGroup());
                newGroup.getEditText().setSelection(newGroup.getEditText().getText().length());
            } else {
                newGroup.setHint("my group");
            }
        } else {
            closeKeyboard();
            dismiss();

        }
        newGroup.setEndIconOnClickListener(view1 -> newGroup.getEditText().setText(""));
        doneButton.setOnClickListener(v -> {
            String txtGroup = newGroup.getEditText().getText().toString().trim();
            if (txtGroup.equals(user.getGroup())){
                newGroup.setError("No changes have been made");
            } else if (txtGroup.isBlank()) {
                newGroup.setError("Group can not be empty");
            } else {
                newGroup.setError(null);
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    updateGroup(txtGroup);
                }
            }

        });
        backButton.setOnClickListener(v -> {
            if ((user.getGroup() != null && !user.getGroup().equals(newGroup.getEditText().getText().toString()))) {
                Toast.makeText(getContext(), "unSaved Changes", Toast.LENGTH_LONG).show();
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
//                changedGroupChangeCallback.
//                d(txtGroup);
                changedGroupChangeCallback.onGroupChanged(txtGroup);
                Toast.makeText(getContext(), "Updated your group" +
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