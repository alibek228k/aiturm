//package kz.devs.aiturm;
//
//import static com.google.android.material.internal.ViewUtils.showKeyboard;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.os.Bundle;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import android.widget.Button;
//import android.widget.ImageButton;
//import android.widget.RadioButton;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.ActionBar;
//import androidx.appcompat.widget.Toolbar;
//import androidx.fragment.app.DialogFragment;
//
//import com.example.shroomies.R;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.UserProfileChangeRequest;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import org.jetbrains.annotations.NotNull;
//
//import java.util.HashMap;
//import java.util.Objects;
//
//import kz.devs.aiturm.model.User;
//
//public class ChangeGenderDialogFragment extends DialogFragment {
//
//    private FirebaseAuth mAuth;
//    private DatabaseReference rootRef;
//
//    private RadioButton newGender;
//    private kz.devs.aiturm.model.User user;
//    private View v;
//    private ChangeGenderDialogFragment.GenderChangeCallback changedGenderChangeCallback;
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        if (getDialog() != null) {
//            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
//            getDialog().getWindow().setGravity(Gravity.VERTICAL_GRAVITY_MASK);
//            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
//        }
//    }
//
//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        Objects.requireNonNull(getDialog()).getWindow().setWindowAnimations(R.style.EditProfileOptionsAnimation);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        v = inflater.inflate(R.layout.dialog_fragment_change_gender, container, false);
//        mAuth = FirebaseAuth.getInstance();
//        rootRef = FirebaseDatabase.getInstance().getReference();
//        return v;
//    }
//
//    public interface GenderChangeCallback {
//        void onGenderChanged(String gender);
//    }
//
//    @Override
//    public void onAttach(@NonNull @NotNull Context context) {
//        super.onAttach(context);
//        try {
//            changedGenderChangeCallback = (ChangeGenderDialogFragment.GenderChangeCallback) getTargetFragment();
//        } catch (Exception e) {
//            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//    }
//
//    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        newGender = v.findViewById(R.id.change_gender_input_text);
//        newGender.requestFocus();
//        showKeyboard();
//        Button doneButton = v.findViewById(R.id.change_gender_done_button);
//        ImageButton backButton = v.findViewById(R.id.change_gender_back_button);
//        Bundle bundle = this.getArguments();
//        if (bundle != null) {
//            user = (User) bundle.getSerializable("USER");
//            if (!(user.getGender() == null || user.getGender().equals(""))) {
//                newGender.setText(user.getGender());
//            } else {
//                newGender.setText("");
//            }
//        } else {
//            closeKeyboard();
//            dismiss();
//        }
//        doneButton.setOnClickListener(v -> {
//            String txtGender = newGender.getText().toString().trim();
//            if (txtGender.equals(user.getGender())) {
//                newGender.setError("No changes have been made");
//            } else if (txtGender.isBlank()) {
//                newGender.setError("Gender can not be empty");
//            } else {
//                newGender.setError(null);
//                FirebaseUser firebaseUser = mAuth.getCurrentUser();
//                firebaseUser.updateProfile(new UserProfileChangeRequest.Builder()
//                                .setDisplayName(txtGender)
//                                .build())
//                        .addOnCompleteListener(task -> {
//                            if (task.isSuccessful()) {
//                                // Update user object
//                                user.setGender(txtGender);
//                                // Update user in database
//                                databaseReference.child("users").child(firebaseUser.getUid()).setValue(user)
//                                        .addOnCompleteListener(task1 -> {
//                                            if (task1.isSuccessful()) {
//                                                Toast.makeText(getActivity(), "Gender updated", Toast.LENGTH_SHORT).show();
//                                                dismiss();
//                                            } else {
//                                                Toast.makeText(getActivity(), "Failed to update gender", Toast.LENGTH_SHORT).show();
//                                            }
//                                        });
//                            } else {
//                                Toast.makeText(getActivity(), "Failed to update gender", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//            }
//        });
//        backButton.setOnClickListener(v1 -> {
//            closeKeyboard();
//            dismiss();
//        });
//    }
//}
