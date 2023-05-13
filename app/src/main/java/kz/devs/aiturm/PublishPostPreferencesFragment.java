package kz.devs.aiturm;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.shroomies.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kz.devs.aiturm.presentaiton.SessionManager;


public class PublishPostPreferencesFragment extends Fragment {
    private TextInputEditText budgetEditText, roomMatesEditText;
    private MaterialButton nextButton;
    private String description, postType, buildingAddress, buildingType;
    ArrayList<String> buildingTypes;
    private CheckBox maleCB, femaleCB, smokingCB, petCB, alcoholCB;
    private CollectionReference dataBase;
    private AppCompatActivity appCompatActivity;

    public static Fragment getInstance(
            String buildingType,
            ArrayList<String> buildingTypes,
            String address,
            String postDescription,
            String postType
    ){
        var bundle = new Bundle();
        bundle.putString(Config.BUILDING_TYPE, buildingType);
        bundle.putStringArrayList(Config.BUILDING_TYPES, buildingTypes);
        bundle.putString(Config.BUILDING_ADDRESS, address);
        bundle.putString(Config.DESCRIPTION, postDescription);
        bundle.putString(Config.POST_TYPE, postType);

        var fragment = new PublishPostPreferencesFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appCompatActivity = (AppCompatActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dataBase = FirebaseFirestore.getInstance().collection(Config.PERSONAL_POST);
        return inflater.inflate(R.layout.fragment_publish_post_preferances, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        roomMatesEditText = view.findViewById(R.id.roommate_number_edit_text);
        LinearLayout numberOfRoommatesLayout = view.findViewById(R.id.number_of_roommates_linear_layout);
        budgetEditText = view.findViewById(R.id.budget_edit_text);
        nextButton = view.findViewById(R.id.publish_post_next_button);
        maleCB = view.findViewById(R.id.male_checkbox);
        femaleCB = view.findViewById(R.id.female_checkbox);
        smokingCB = view.findViewById(R.id.smoking_checkbox);
        petCB = view.findViewById(R.id.pet_friendly_checkbox);
        alcoholCB = view.findViewById(R.id.alcohol_checkbox);

        Bundle bundle = this.getArguments();
        postType = bundle.getString(Config.POST_TYPE);
        if (postType == null) displayErrorAlert();

        if (postType.equals(Config.APARTMENT_POST)) {
            buildingAddress = bundle.getString(Config.BUILDING_ADDRESS);
            buildingType = bundle.getString(Config.BUILDING_TYPE);
        } else {
            buildingTypes = bundle.getStringArrayList(Config.BUILDING_TYPES);
        }
        description = bundle.getString(Config.DESCRIPTION);

        if (postType.equals(Config.PERSONAL_POST)) {
            numberOfRoommatesLayout.setVisibility(View.GONE);
            nextButton.setText(getString(R.string.publish_post));
        }

        nextButton.setOnClickListener(v -> {
            int numberOfRoommates = 0;
            int budget = 0;
            StringBuilder preferences = new StringBuilder();
            if (maleCB.isChecked()) {
                preferences.append("1");
            } else {
                preferences.append("0");
            }
            if (femaleCB.isChecked()) {
                preferences.append("1");
            } else {
                preferences.append("0");
            }
            if (smokingCB.isChecked()) {
                preferences.append("1");
            } else {
                preferences.append("0");
            }
            if (petCB.isChecked()) {
                preferences.append("1");
            } else {
                preferences.append("0");
            }
            if (alcoholCB.isChecked()) {
                preferences.append("1");
            } else {
                preferences.append("0");
            }

            if (roomMatesEditText.getText() == null || roomMatesEditText.getText().toString().isEmpty()) {
                if (postType.equals(Config.APARTMENT_POST)) {
                    showCustomToast(getString(R.string.error_room_members_can_not_be_empty));
                    return;
                }
            } else {
                numberOfRoommates = Integer.parseInt(roomMatesEditText.getText().toString());
            }

            if (budgetEditText.getText() == null || budgetEditText.getText().toString().isEmpty()) {
                showCustomToast(getString(R.string.error_budget_can_not_be_empty));
                return;
            } else {
                budget = Integer.parseInt(budgetEditText.getText().toString());
            }
            if (postType.equals(Config.PERSONAL_POST)) {
                publishPersonalPost(Integer.parseInt(budgetEditText.getText().toString()), preferences.toString());
            } else {
                setupNextButtonForApartmentPost(preferences.toString(), budget, numberOfRoommates);
            }
        });
    }

    private void setupNextButtonForApartmentPost(String preferences, int budget, int numberOfRoommates) {
        getFragment(PublishPostImageFragment.getInstance(preferences, buildingType, buildingAddress, description, budget, numberOfRoommates));
    }

    private void publishPersonalPost(int price, String preferences) {
        var user = new SessionManager(requireContext()).getData();

        ArrayList<String> keyWords = new ArrayList(Arrays.asList(description.trim().split("[?U\\W]")));
        for (int i = 0; i < keyWords.size(); i++) {
            if (keyWords.get(i) == null || "".equals(keyWords.get(i))) {
                keyWords.remove(i);
            }
        }
        Set<String> set = new HashSet<>(keyWords);
        List<String> filteredList = new ArrayList<>(set);
        Map<String, Object> personalPost = new HashMap<>();
        personalPost.put(Config.DESCRIPTION, description);
        personalPost.put(Config.KEY_WORDS, filteredList);
        personalPost.put(Config.userID, user.getUserID());
        personalPost.put(Config.PRICE, price);
        personalPost.put(Config.PREFERENCE, preferences);
        personalPost.put(Config.BUILDING_TYPES, buildingTypes);
        personalPost.put(Config.TIME_STAMP, System.currentTimeMillis());

        dataBase.add(personalPost).addOnSuccessListener(documentReference -> {
            showCustomToast(getString(R.string.post_success));
            requireActivity().onBackPressed();
        }).addOnFailureListener(e -> {
            e.printStackTrace();
            showCustomToast(getString(R.string.post_failed));
            requireActivity().onBackPressed();
        });
    }

    public void showCustomToast(String msg) {
        appCompatActivity.runOnUiThread(() -> {
            LayoutInflater inflater = appCompatActivity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast, appCompatActivity.findViewById(R.id.toast_layout));
            TextView text = layout.findViewById(R.id.toast_text);

            text.setText(msg);

            Toast toast = new Toast(appCompatActivity.getApplication());

            toast.setGravity(Gravity.BOTTOM | Gravity.START | Gravity.FILL_HORIZONTAL, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        });

    }

    private void getFragment(Fragment fragment) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.publish_post_container, fragment);
        ft.commit();
    }

    private void displayErrorAlert() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.attention))
                .setMessage(getString(R.string.error_occurred_with_post_creating))
                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .setOnDismissListener(dialogInterface -> {
                    requireActivity().onBackPressed();
                }).create()
                .show();
    }
}