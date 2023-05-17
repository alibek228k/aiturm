package kz.devs.aiturm;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import kz.devs.aiturm.model.SignInMethod;
import kz.devs.aiturm.model.User;
import kz.devs.aiturm.presentaiton.SessionManager;
import kz.devs.aiturm.presentaiton.loading.LoadingManager;

public class DeleteUserDialogDragment extends DialogFragment {
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage storage;
    private User user;
    private View v;
    private TextInputLayout userPassword;

    private Dialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_delete_user, container, false);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        loadingDialog = LoadingManager.getLoadingDialog(requireContext(), false);
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
            user = (User) bundle.getSerializable("USER");
            if (user!=null) {
                userEmail.getEditText().setText(user.getEmail());
                doneButton.setOnClickListener(view1 -> {
                    loadingDialog.show();
                    if (userPassword.getEditText().getText() != null && !userPassword.getEditText().getText().toString().trim().isBlank()){
                        reAuthenticateUser(user.getEmail(), userPassword.getEditText().getText().toString().trim());
                    }else{
                        Toast.makeText(getContext(), "Password field can not be empty!", Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });
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
        if (firebaseUser != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);
            firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    deleteAllPosts(user.getUserID());
                    deleteAllMessages(user.getUserID());
                    rootRef.child(Config.users).child(user.getUserID()).removeValue();
                    rootRef.child("tokens").child(user.getUserID()).removeValue();
                    deleteAccount(firebaseUser);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                loadingDialog.dismiss();
            });
        } else {
            loadingDialog.dismiss();
        }
    }
    private void deleteAccount(FirebaseUser firebaseUser) {
        Context context = requireContext();
        SessionManager manager = new SessionManager(context);
        firebaseUser.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userPassword.setError(null);
                Toast.makeText(getContext(), "Successfully deleted", Toast.LENGTH_LONG).show();
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
                if (user.getSignInMethod() == SignInMethod.GOOGLE) {
                    Task<Void> revokeAccess = mGoogleSignInClient.revokeAccess();
                    try {
                        revokeAccess.addOnCompleteListener(task1 -> {
                            manager.removeUserData();
                            mAuth.signOut();
                            mGoogleSignInClient.signOut();
                            Toast.makeText(context, "Successfully signed out", Toast.LENGTH_LONG).show();
                            loadingDialog.dismiss();
                            this.getTargetFragment().getActivity().finish();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to signed out", Toast.LENGTH_LONG).show();
                            loadingDialog.dismiss();
                            this.getTargetFragment().getActivity().finish();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                        this.getTargetFragment().getActivity().finish();
                    }
                } else {
                    manager.removeUserData();
                    mAuth.signOut();
                    Toast.makeText(getContext(), "Successfully signed out", Toast.LENGTH_LONG).show();
                    loadingDialog.dismiss();
                    this.getTargetFragment().getActivity().finish();
                }
            }
        }).addOnFailureListener(e -> {
            userPassword.setError(e.getMessage());
            loadingDialog.dismiss();
        });
    }

    private void deleteAllMessages(String userId){
        rootRef.child(Config.messages).child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                List<String> ids = new ArrayList<>();
                task.getResult().getChildren().forEach(dataSnapshot -> {
                    ids.add(dataSnapshot.getKey());
                });
                ids.forEach(id -> {
                    rootRef.child(Config.messages).child(id).child(userId).removeValue();
                });
                rootRef.child(Config.messages).child(userId).removeValue();
            }
        });
    }

    private void deleteAllPosts(String userId){
        firebaseFirestore.collection(Config.APARTMENT_POST).whereEqualTo(Config.userID, userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            queryDocumentSnapshots.getDocuments().forEach(documentSnapshot -> {
                documentSnapshot.getReference().delete();
            });
        });

        firebaseFirestore.collection(Config.PERSONAL_POST).whereEqualTo(Config.userID, userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            queryDocumentSnapshots.getDocuments().forEach(documentSnapshot -> {
                documentSnapshot.getReference().delete();
            });
        });
    }
}