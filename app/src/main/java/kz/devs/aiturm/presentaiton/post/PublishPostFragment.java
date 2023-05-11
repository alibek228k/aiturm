package kz.devs.aiturm.presentaiton.post;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.shroomies.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import kz.devs.aiturm.Config;
import kz.devs.aiturm.GlideApp;
import kz.devs.aiturm.PublishPostPreferencesFragment;
import kz.devs.aiturm.presentaiton.SessionManager;
import kz.devs.aiturm.presentaiton.post.type.PostTypeDialogFragment;


public class PublishPostFragment extends Fragment implements PostTypeDialogFragment.PostTypeCallback {

    private MaterialButton nextButton;
    private View v;
    private TextView searchForNameTextView, selectTypeofUnitTextView;
    private ImageView userImageView;
    private ChipGroup typeOfUnitChipGroup;
    private Chip postTypeChip;
    private EditText descriptionEditText;
    private RelativeLayout apartmentPostLayout, mainLayout;
    private TextInputLayout addressInputLayout;

    private String postType = Config.APARTMENT_POST;
    ;

    @Override
    public void onPostTypeChanged(String postType) {
        this.postType = postType;
        if (postType.equals(Config.APARTMENT_POST)) {
            setupApartmentPostState();
        } else {
            setupPersonalPostState();
        }
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_publish_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addressInputLayout = v.findViewById(R.id.address_input_text);
        typeOfUnitChipGroup = v.findViewById(R.id.type_of_unit_chip_group);
        searchForNameTextView = v.findViewById(R.id.search_for_name_text_view);
        apartmentPostLayout = v.findViewById(R.id.apartment_post_layout);
        selectTypeofUnitTextView = v.findViewById(R.id.select_type_text_view);
        nextButton = v.findViewById(R.id.publish_post_next_button);
        mainLayout = v.findViewById(R.id.relative_layout);
        descriptionEditText = v.findViewById(R.id.post_description);
        userImageView = v.findViewById(R.id.user_image_publish_post);
        postTypeChip = v.findViewById(R.id.post_type_chip);

        setupUserImage();
        setupNextButton();
        setupDescriptionEditText();
        setupAddressTextField();
        setupApartmentTypeChipGroup();

        postTypeChip.setOnClickListener(v -> {
            PostTypeDialogFragment postTypeDialogFragment = new PostTypeDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Config.POST_TYPE, postType);
            postTypeDialogFragment.setArguments(bundle);
            postTypeDialogFragment.setTargetFragment(this, 0);
            postTypeDialogFragment.show(getParentFragmentManager(), null);
        });

    }

    void setupApartmentPostState() {
        apartmentPostLayout.setVisibility(View.VISIBLE);
        searchForNameTextView.setVisibility(View.VISIBLE);
        typeOfUnitChipGroup.setSingleSelection(true);
        searchForNameTextView.setVisibility(View.GONE);
        addressInputLayout.setVisibility(View.VISIBLE);
        postTypeChip.setText("Room post");
        selectTypeofUnitTextView.setText("Select the type of your residential unit");
    }

    void setupPersonalPostState() {
        typeOfUnitChipGroup.setSingleSelection(false);
        searchForNameTextView.setVisibility(View.GONE);
        addressInputLayout.setVisibility(View.GONE);
        selectTypeofUnitTextView.setText("Select your preferred unit types");
        postTypeChip.setText("Roommate post");

    }

    private void setupNextButton() {
        nextButton.setOnClickListener(v -> {

            if (postType.equals(Config.APARTMENT_POST)) {
                if (checkDataForApartmentPost()) {
                    getFragment(PublishPostPreferencesFragment.getInstance(
                            getBuildingType(),
                            addressInputLayout.getEditText().getText().toString(),
                            descriptionEditText.getText().toString().trim(),
                            postType
                    ));
                }
            } else if (checkDataForPersonalPost()) {
                getFragment(PublishPostPreferencesFragment.getInstance(
                        getBuildingType(),
                        null,
                        descriptionEditText.getText().toString().trim(),
                        postType
                ));
            }

        });
    }

    private void setupDescriptionEditText() {
        descriptionEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(descriptionEditText, InputMethodManager.SHOW_IMPLICIT);

        }
    }

    private void setupAddressTextField() {
        if (postType.equals(Config.APARTMENT_POST)) {
            addressInputLayout.setVisibility(View.VISIBLE);
        } else {
            addressInputLayout.setVisibility(View.GONE);
        }
    }

    private void setupApartmentTypeChipGroup() {
        typeOfUnitChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            searchForNameTextView.setVisibility(View.VISIBLE);
            //if it not in single selection mode
            // then we know its in personal post mode
            if (!typeOfUnitChipGroup.isSingleSelection()) {
                searchForNameTextView.setText(R.string.search_for_the_nearest_place);
            } else {
                if (checkedId == R.id.house_type_chip) {
                    searchForNameTextView.setText(R.string.search_for_the_nearest_place);
                } else {
                    searchForNameTextView.setText(R.string.search_for_building_name);
                }
            }


        });
    }

    @SuppressLint("NonConstantResourceId")
    private String getBuildingType() {
        switch (typeOfUnitChipGroup.getCheckedChipId()) {
            case R.id.apartment_type_chip:
                return Config.TYPE_APARTMENT;
            case R.id.flat_type_chip:
                return Config.TYPE_FLAT;
            case R.id.condo_type_chip:
                return Config.TYPE_CONDO;
            case R.id.house_type_chip:
                return Config.TYPE_HOUSE;
            default:
                return null;
        }
    }

    @SuppressLint("NonConstantResourceId")
    private ArrayList<String> getBuildingTypes() {
        ArrayList<String> buildingTypes = new ArrayList<>();

        for (int chipId :
                typeOfUnitChipGroup.getCheckedChipIds()) {
            switch (chipId) {
                case R.id.apartment_type_chip:
                    buildingTypes.add(Config.TYPE_APARTMENT);
                    break;
                case R.id.flat_type_chip:
                    buildingTypes.add(Config.TYPE_FLAT);
                    break;
                case R.id.condo_type_chip:
                    buildingTypes.add(Config.TYPE_CONDO);
                    break;
                case R.id.house_type_chip:
                    buildingTypes.add(Config.TYPE_HOUSE);
                    break;
            }
        }
        return buildingTypes;
    }

    private boolean checkDataForApartmentPost() {
        boolean status = true;
        ArrayList<String> errors = new ArrayList<>();

        if (typeOfUnitChipGroup.getCheckedChipId() == View.NO_ID) {
            selectTypeofUnitTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.canceRed));
            errors.add("Please select the type of your unit");
            status = false;
        } else {
            selectTypeofUnitTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.jetBlack));
        }
        if ((addressInputLayout.getEditText().getText().toString() == null || addressInputLayout.getEditText().getText().toString().isBlank()) && typeOfUnitChipGroup.getCheckedChipId() != R.id.house_type_chip) {
            errors.add("Please add a locality");
            addressInputLayout.setError("Please add a locality");
            status = false;
        } else {
            addressInputLayout.setError(null);
        }
        if (descriptionEditText.getText().toString().trim().isEmpty()) {
            errors.add("Please enter a description");
            descriptionEditText.setHintTextColor(getActivity().getColor(R.color.canceRed));
            status = false;
        } else {
            descriptionEditText.setHintTextColor(getActivity().getColor(R.color.lightGrey));
        }

        if (!errors.isEmpty()) {
            Snackbar.make(mainLayout, errors.get(0), Snackbar.LENGTH_LONG).show();
        }

        return status;
    }

    private boolean checkDataForPersonalPost() {
        boolean status = true;
        ArrayList<String> errors = new ArrayList<>();
        if (descriptionEditText.getText().toString().trim().isEmpty()) {
            errors.add("Please enter a description");
            descriptionEditText.setHintTextColor(getActivity().getColor(R.color.canceRed));
            status = false;
        } else {
            descriptionEditText.setHintTextColor(getActivity().getColor(R.color.lightGrey));
        }
        if (addressInputLayout.getEditText().getText().toString() == null || addressInputLayout.getEditText().getText().toString().isBlank()) {
            errors.add("Please add a locality");
            addressInputLayout.setError("Please add a locality");
            status = false;
        } else {
            addressInputLayout.setError(null);
        }
        if (!errors.isEmpty()) {
            Snackbar.make(mainLayout, errors.get(0), Snackbar.LENGTH_LONG).show();
        }

        return status;
    }


    private void getFragment(Fragment fragment) {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.publish_post_container, fragment);
        ft.commit();
    }

    private void setupUserImage() {
        var user = new SessionManager(requireContext()).getData();
        String url = user.getImage();
        if (url != null) {
            GlideApp.with(requireActivity())
                    .load(url)
                    .centerCrop()
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(userImageView);
        }
    }


}






