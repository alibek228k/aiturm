package kz.devs.aiturm;

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
import android.widget.Toast;

import com.example.shroomies.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

public class DeleteUserDialogDragment extends DialogFragment {
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private User user;
    private View v;
    private TextInputLayout userPassword;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_delete_user, container, false);
        mAuth=FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        user=new User();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button cancelButton = v.findViewById(R.id.delete_account_cancel);
        Button doneButton = v.findViewById(R.id.delete_account_done);
        TextInputLayout userEmail = v.findViewById(R.id.delete_account_email);
        userPassword=v.findViewById(R.id.delete_account_password);
        Bundle bundle=getArguments();
        if (bundle!=null) {
            user=bundle.getParcelable("USER");
            if (user!=null) {
                userEmail.getEditText().setText(user.getEmail());
                doneButton.setOnClickListener(view1 -> reAuthenticateUser(user.getEmail(), userPassword.getEditText().getText().toString().trim()));
                cancelButton.setOnClickListener(view12 -> dismiss());
            }

        } else {
            dismiss();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setGravity(Gravity.END);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
            showKeyboard();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        closeKeyboard();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }
    public void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
    public void closeKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
    private void reAuthenticateUser(String email, String password) {
        FirebaseUser firebaseUser=mAuth.getCurrentUser();
        if (firebaseUser!=null) {
            AuthCredential credential= EmailAuthProvider.getCredential(email,password);
            firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
               if (task.isSuccessful()) {
                   deleteAccount(firebaseUser);
               }
            }).addOnFailureListener(e -> Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show());
        }
    }
    private void deleteAccount(FirebaseUser firebaseUser) {
        firebaseUser.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userPassword.setError(null);
                Toast.makeText(getContext(),"Successfully deleted",Toast.LENGTH_LONG).show();
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
                Task<Void> revokeAccess = mGoogleSignInClient.revokeAccess();
                try {
                    revokeAccess.addOnCompleteListener(task1 -> {
                        FirebaseUser user = mAuth.getCurrentUser();
                        rootRef.child(Config.users).child(user.getUid()).removeValue();
                        mAuth.signOut();
                        mGoogleSignInClient.signOut();
                        Toast.makeText(getContext(), "Successfully signed out", Toast.LENGTH_LONG).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to signed out", Toast.LENGTH_LONG).show();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
                this.getTargetFragment().getActivity().finish();
            }
        }).addOnFailureListener(e -> userPassword.setError(e.getMessage()));
    }
}