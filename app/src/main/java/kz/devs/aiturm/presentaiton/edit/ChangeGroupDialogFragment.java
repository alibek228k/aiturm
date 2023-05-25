package kz.devs.aiturm.presentaiton.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.example.shroomies.R;
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

    private Spinner groupSpinner, specialitySpinner;

    private User user;
    private View v;
    private GroupChangeCallback changedGroupChangeCallback;

    private String selectedSpeciality, selectedGroup, error;

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
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
        void onGroupChanged(String group, String specialization);
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
        specialitySpinner = v.findViewById(R.id.speciality_spinner);
        groupSpinner = v.findViewById(R.id.group_spinner);
        showKeyboard();
        setupSpecialitySpinner();
        Button doneButton = v.findViewById(R.id.change_group_done_button);
        ImageButton backButton = v.findViewById(R.id.change_group_back_button);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            user = (User) bundle.getSerializable("USER");
            setupUserData(user);

        } else {
            closeKeyboard();
            dismiss();
        }
        doneButton.setOnClickListener(v -> {
            String txtGroup = selectedGroup;
            if (txtGroup.equals(user.getGroup())){
                error = "No changes have been made";
            } else if (txtGroup.isBlank()) {
                error = "Group can not be empty";
            } else {
                error = null;
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    updateGroup(txtGroup);
                }
            }

        });
        backButton.setOnClickListener(v -> {
            dismiss();
        });


    }

    private void setupUserData(User user) {
        if (!(user.getSpecialization() == null || user.getSpecialization().equals(""))) {
            switch (user.getSpecialization()) {
                case "Computer Science":
                    specialitySpinner.setSelection(0);
                    if (!(user.getGroup() == null || user.getGroup().equals(""))) {
                        switch (user.getGroup()) {
                            case "IT-2000":
                                groupSpinner.setSelection(0);
                                break;
                            case "IT-2001":
                                groupSpinner.setSelection(1);
                                break;
                            case "IT-2002":
                                groupSpinner.setSelection(2);
                                break;
                            case "IT-2003":
                                groupSpinner.setSelection(3);
                                break;
                            case "IT-2004":
                                groupSpinner.setSelection(4);
                                break;
                        }
                    }
                    break;
                case "Software Engineering":
                    specialitySpinner.setSelection(1);
                    if (!(user.getGroup() == null || user.getGroup().equals(""))) {
                        switch (user.getGroup()) {
                            case "SE-2000":
                                groupSpinner.setSelection(0);
                                break;
                            case "SE-2001":
                                groupSpinner.setSelection(1);
                                break;
                            case "SE-2002":
                                groupSpinner.setSelection(2);
                                break;
                            case "SE-2003":
                                groupSpinner.setSelection(3);
                                break;
                            case "SE-2004":
                                groupSpinner.setSelection(4);
                                break;
                        }
                    }
                    break;
                case "Big Data Analysis":
                    specialitySpinner.setSelection(2);
                    if (!(user.getGroup() == null || user.getGroup().equals(""))) {
                        switch (user.getGroup()) {
                            case "BD-2000":
                                groupSpinner.setSelection(0);
                                break;
                            case "BD-2001":
                                groupSpinner.setSelection(1);
                                break;
                            case "BD-2002":
                                groupSpinner.setSelection(2);
                                break;
                            case "BD-2003":
                                groupSpinner.setSelection(3);
                                break;
                            case "BD-2004":
                                groupSpinner.setSelection(4);
                                break;
                        }
                    }
                    break;
                case "Media Technologies":
                    specialitySpinner.setSelection(3);
                    if (!(user.getGroup() == null || user.getGroup().equals(""))) {
                        switch (user.getGroup()) {
                            case "MT-2000":
                                groupSpinner.setSelection(0);
                                break;
                            case "MT-2001":
                                groupSpinner.setSelection(1);
                                break;
                            case "MT-2002":
                                groupSpinner.setSelection(2);
                                break;
                            case "MT-2003":
                                groupSpinner.setSelection(3);
                                break;
                            case "MT-2004":
                                groupSpinner.setSelection(4);
                                break;
                        }
                    }
                    break;
                case "Cyber Security":
                    specialitySpinner.setSelection(4);
                    if (!(user.getGroup() == null || user.getGroup().equals(""))) {
                        switch (user.getGroup()) {
                            case "CS-2000":
                                groupSpinner.setSelection(0);
                                break;
                            case "CS-2001":
                                groupSpinner.setSelection(1);
                                break;
                            case "CS-2002":
                                groupSpinner.setSelection(2);
                                break;
                            case "CS-2003":
                                groupSpinner.setSelection(3);
                                break;
                            case "CS-2004":
                                groupSpinner.setSelection(4);
                                break;
                        }
                    }
                    break;
                case "Telecommunication Systems":
                    specialitySpinner.setSelection(5);
                    if (!(user.getGroup() == null || user.getGroup().equals(""))) {
                        switch (user.getGroup()) {
                            case "TS-2000":
                                groupSpinner.setSelection(0);
                                break;
                            case "TS-2001":
                                groupSpinner.setSelection(1);
                                break;
                            case "TS-2002":
                                groupSpinner.setSelection(2);
                                break;
                            case "TS-2003":
                                groupSpinner.setSelection(3);
                                break;
                            case "TS-2004":
                                groupSpinner.setSelection(4);
                                break;
                        }
                    }
                    break;
                case "Smart Technologies":
                    specialitySpinner.setSelection(6);
                    if (!(user.getGroup() == null || user.getGroup().equals(""))) {
                        switch (user.getGroup()) {
                            case "ST-2000":
                                groupSpinner.setSelection(0);
                                break;
                            case "ST-2001":
                                groupSpinner.setSelection(1);
                                break;
                            case "ST-2002":
                                groupSpinner.setSelection(2);
                                break;
                            case "ST-2003":
                                groupSpinner.setSelection(3);
                                break;
                            case "ST-2004":
                                groupSpinner.setSelection(4);
                                break;
                        }
                    }
                    break;
            }
        }
    }

    private void setupSpecialitySpinner() {
        var adapter = ArrayAdapter.createFromResource(requireContext(), R.array.spinner_speciality, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        specialitySpinner.setAdapter(adapter);

        specialitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSpeciality = (String) parent.getItemAtPosition(position);
                switch (selectedSpeciality) {
                    case "Computer Science":
                        setupCSGroupsSpinner();
                        break;
                    case "Software Engineering":
                        setupSEGroupsSpinner();
                        break;
                    case "Big Data Analysis":
                        setupBDGroupsSpinner();
                        break;
                    case "Media Technologies":
                        setupMTGroupsSpinner();
                        break;
                    case "Cyber Security":
                        setupITGroupsSpinner();
                        break;
                    case "Telecommunication Systems":
                        setupTSGroupsSpinner();
                        break;
                    case "Smart Technologies":
                        setupSTGroupsSpinner();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupCSGroupsSpinner() {
        var adapter = ArrayAdapter.createFromResource(requireContext(), R.array.spinner_cs, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupBDGroupsSpinner() {
        var adapter = ArrayAdapter.createFromResource(requireContext(), R.array.spinner_bd, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupSEGroupsSpinner() {
        var adapter = ArrayAdapter.createFromResource(requireContext(), R.array.spinner_se, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupMTGroupsSpinner() {
        var adapter = ArrayAdapter.createFromResource(requireContext(), R.array.spinner_mt, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupITGroupsSpinner() {
        var adapter = ArrayAdapter.createFromResource(requireContext(), R.array.spinner_it, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupTSGroupsSpinner() {
        var adapter = ArrayAdapter.createFromResource(requireContext(), R.array.spinner_ts, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupSTGroupsSpinner() {
        var adapter = ArrayAdapter.createFromResource(requireContext(), R.array.spinner_st, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void updateGroup(String txtGroup) {
        HashMap<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("group", txtGroup);
        updateDetails.put("specialization", selectedSpeciality);

        rootRef.child(Config.users).child(user.getUserID()).updateChildren(updateDetails).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                changedGroupChangeCallback.onGroupChanged(txtGroup, selectedSpeciality);
                Toast.makeText(getContext(), "Updated your group", Toast.LENGTH_SHORT).show();
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