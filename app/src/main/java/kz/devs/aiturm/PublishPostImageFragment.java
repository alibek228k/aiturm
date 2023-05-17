package kz.devs.aiturm;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.example.shroomies.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.make.dots.dotsindicator.DotsIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kz.devs.aiturm.presentaiton.SessionManager;
import kz.devs.aiturm.presentaiton.post.PublishPostActivity;

public class PublishPostImageFragment extends Fragment {
    private static final int PICK_IMAGE_MULTIPLE = 1;
    public static final int NUMBER_OF_IMAGES_ALLOWED = 5;
    private ViewPager viewPager;
    private DotsIndicator dotsIndicator;
    private ImageButton deleteImageButton;
    private LottieAnimationView lottieAnimationView;
    private MaterialButton publishPostButton, addMoreImagesButton;
    private CardView addImageCardView;
    private AppCompatActivity appCompatActivity;
    private RelativeLayout rootLayout;

    private CustomLoadingProgressBar progressBar;

    private ArrayList<Uri> imageUris;
    private ViewPagerAdapter viewPagerAdapter;

    private int currentViewPagerPosition;
    private String description, buildingAddress, buildingType, preferences;
    private int budget, numberOfRoomMates;

    private FirebaseFirestore fireStoreDatabase;
    private DatabaseReference realTimeDatabaseReference;

    public static PublishPostImageFragment getInstance(
            String preferences,
            String buildingType,
            String buildingAddress,
            String description,
            int budget,
            int numberOfRoommates
    ) {
        var bundle = new Bundle();
        bundle.putString(Config.PREFERENCE, preferences);
        bundle.putString(Config.BUILDING_TYPE, buildingType);
        bundle.putString(Config.BUILDING_ADDRESS, buildingAddress);
        bundle.putString(Config.DESCRIPTION, description);
        bundle.putInt(Config.BUDGET, budget);
        bundle.putInt(Config.NUMBER_OF_ROOMMATES, numberOfRoommates);
        var fragment = new PublishPostImageFragment();
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
        fireStoreDatabase = FirebaseFirestore.getInstance();
        realTimeDatabaseReference = FirebaseDatabase.getInstance().getReference();
        progressBar = new CustomLoadingProgressBar(requireContext(), getString(R.string.publishing_post), R.raw.loading_animation);
        progressBar.setCancelable(false);
        return inflater.inflate(R.layout.fragment_publish_post_image, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lottieAnimationView = view.findViewById(R.id.add_image_animation);
        viewPager = view.findViewById(R.id.view_pager);
        dotsIndicator = view.findViewById(R.id.dots_indicator);
        deleteImageButton = view.findViewById(R.id.delete_image_post);
        addImageCardView = view.findViewById(R.id.add_image_card_view);
        publishPostButton = view.findViewById(R.id.publish_post_button);
        addMoreImagesButton = view.findViewById(R.id.add_more_images_button);
        rootLayout = view.findViewById(R.id.root_layout);

        Bundle bundle = requireArguments();
        preferences = bundle.getString(Config.PREFERENCE);
        description = bundle.getString(Config.DESCRIPTION);
        budget = bundle.getInt(Config.BUDGET);
        numberOfRoomMates = bundle.getInt(Config.NUMBER_OF_ROOMMATES);
        buildingAddress = bundle.getString(Config.BUILDING_ADDRESS);
        buildingType = bundle.getString(Config.BUILDING_TYPE);

        imageUris = new ArrayList<>();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentViewPagerPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        deleteImageButton.setOnClickListener(v -> deleteFromView());
        addImageCardView.setOnClickListener(v -> pickFromGallery());
        addMoreImagesButton.setOnClickListener(v -> pickFromGallery());
        publishPostButton.setOnClickListener(v -> {
            if (!imageUris.isEmpty()) {
                postImagesAddToDatabase(imageUris);
            } else {
                new CustomToast((PublishPostActivity) getActivity(), getString(R.string.add_image_of_your_place)).showCustomToast();
            }
        });
    }



    private void pickFromGallery() {
        Dexter.withContext(getActivity()).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                            // navigate user to app settings
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        } else {
                            Intent image = new Intent();
                            image.setAction(Intent.ACTION_GET_CONTENT);
                            image.setType("image/*");
                            image.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            startActivityForResult(image, PICK_IMAGE_MULTIPLE);
                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }

                }).onSameThread().check();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean duplicateFound = false;
        Uri selectedImageUri;
        int spaceAvailable = NUMBER_OF_IMAGES_ALLOWED - imageUris.size();

        getActivity();
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_MULTIPLE) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount(); //evaluate the count before the for loop
                Snackbar.make(rootLayout, "space is  " + spaceAvailable, Snackbar.LENGTH_SHORT).show();

                for (int i = 0; i < count; i++) {
                    if (i > spaceAvailable) {
                        break;
                    }
                    selectedImageUri = data.getClipData().getItemAt(i).getUri();
                    if (!imageUris.contains(selectedImageUri)) {
                        addToViewPager(selectedImageUri);
                    }
                }
            }
            else if (data.getData() != null) {
                selectedImageUri = data.getData();
                for (Uri uri : imageUris) {
                    if (data.getData().equals(uri)) {
                        duplicateFound = true;
                        break;
                    }
                }
                if (!duplicateFound) {
                    addToViewPager(selectedImageUri);
                }
            }
        }

    }

    void addToViewPager(Uri newImageUri) {
        viewPager.setVisibility(View.VISIBLE);
        dotsIndicator.setVisibility(View.VISIBLE);
        deleteImageButton.setVisibility(View.VISIBLE);
        addImageCardView.setVisibility(View.GONE);
        lottieAnimationView.pauseAnimation();
        if(imageUris.size()==NUMBER_OF_IMAGES_ALLOWED-1){
            addMoreImagesButton.setVisibility(View.GONE);
        }else{
            addMoreImagesButton.setVisibility(View.VISIBLE);
        }
        imageUris.add(newImageUri);
        viewPagerAdapter = new ViewPagerAdapter(getActivity(), imageUris);
        viewPager.setAdapter(viewPagerAdapter);
        dotsIndicator.setViewPager(viewPager);
        viewPager.getAdapter().registerDataSetObserver(dotsIndicator.getDataSetObserver());
    }

    void deleteFromView() {
        if (imageUris.size() <= 1) {
            imageUris.remove(0);
        } else {
            imageUris.remove(currentViewPagerPosition);
        }
        if (imageUris.size() == 0) {
            viewPager.setVisibility(View.GONE);
            dotsIndicator.setVisibility(View.GONE);
            deleteImageButton.setVisibility(View.GONE);
            addMoreImagesButton.setVisibility(View.GONE);
            addImageCardView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        } else{
            if(imageUris.size()==NUMBER_OF_IMAGES_ALLOWED){
                addMoreImagesButton.setVisibility(View.GONE);
            }else{
                addMoreImagesButton.setVisibility(View.VISIBLE);
            }

            lottieAnimationView.pauseAnimation();
            //initalize adapter with the list of uri
            viewPagerAdapter = new ViewPagerAdapter(getActivity(), imageUris);
            // set the view pager to the adapter
            viewPager.setAdapter(viewPagerAdapter);
            // add the indicator to the view pager and set to update on chage od dataset
            dotsIndicator.setViewPager(viewPager);
            viewPager.getAdapter().registerDataSetObserver(dotsIndicator.getDataSetObserver());

            viewPager.setCurrentItem(currentViewPagerPosition);
        }



    }

    void postImagesAddToDatabase(final List<Uri> imageUri) {
        publishPostButton.setVisibility(View.INVISIBLE);
        progressBar.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        final List<String> imageUrls = new ArrayList<>();
        int counter = 0;

        String uniqueId = getUniqueName();

        for (Uri uri :
                imageUri) {
            StorageReference filePath = storageReference.child(Config.APARTMENT_POST_IMAGE).child(uniqueId);
            filePath.child(counter + ".jpg").putFile(uri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    imageUrls.add(task.getResult().getMetadata().getReference().getPath());
                    if (imageUrls.size() == imageUri.size()) {
                        publishApartmentPost(imageUrls, uniqueId);
                    }
                } else {
                    showCustomToast(getString(R.string.network_error_uploading_image));
                }
            });
            counter++;

        }


    }


    private String getUniqueName() {
        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        String saveCurrentDate = currentDate.format(calendarDate.getTime());

        Calendar calendarTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss.SSS");
        String saveCurrentTime = currentTime.format(calendarTime.getTime());


        return new SessionManager(requireContext()).getData().getUserID() + saveCurrentDate + saveCurrentTime;
    }

    void publishApartmentPost(List<String> imageUris, String imageFolderPath) {
        var manager = new SessionManager(requireContext());
        var currentUser = manager.getData();

        if (currentUser.getApartmentID() == null) {
            Map<String, Object> apartment = new HashMap<>();
            List<String> emptyList = Collections.emptyList();
            apartment.put(Config.adminID, currentUser.getUserID());
            apartment.put(Config.apartmentMembers, emptyList);
            apartment.put(Config.taskCards, new HashMap<String, TasksCard>());
            apartment.put(Config.expensesCards, new HashMap<String, ExpensesCard>());

            fireStoreDatabase.collection(Config.APARTMENT_LIST).add(apartment).addOnSuccessListener(documentReference -> {


                Map<String, Object> apartmentId = new HashMap<>();

                apartmentId.put(Config.apartmentID, documentReference.getId());

                realTimeDatabaseReference.child(Config.users).child(currentUser.getUserID()).updateChildren(apartmentId);
                currentUser.setApartmentID(documentReference.getId());
                manager.saveData(currentUser);


                Map<String, Object> apartmentPost = new HashMap<>();
                apartmentPost.put(Config.apartmentID, documentReference.getId());
                apartmentPost.put(Config.IMAGE_FOLDER_PATH, imageFolderPath);
                apartmentPost.put(Config.DESCRIPTION, description);
                apartmentPost.put(Config.userID, currentUser.getUserID());
                apartmentPost.put(Config.PRICE, budget);
                apartmentPost.put(Config.NUMBER_OF_ROOMMATES, numberOfRoomMates);
                apartmentPost.put(Config.PREFERENCE, preferences);
                apartmentPost.put(Config.IMAGE_URL, imageUris);
                apartmentPost.put(Config.TIME_STAMP, System.currentTimeMillis());
                apartmentPost.put(Config.BUILDING_ADDRESS, buildingAddress);
                apartmentPost.put(Config.BUILDING_TYPE, buildingType);


                fireStoreDatabase.collection(Config.APARTMENT_POST).add(apartmentPost).addOnSuccessListener(reference -> {
                    progressBar.dismiss();
                    showCustomToast(requireActivity().getString(R.string.post_success));
                    requireActivity().onBackPressed();
                }).addOnFailureListener(e -> {
                    e.printStackTrace();
                    progressBar.dismiss();
                    showCustomToast(requireActivity().getString(R.string.post_failed));
                    requireActivity().onBackPressed();
                });
            }).addOnFailureListener(e -> {
                progressBar.dismiss();
                showCustomToast(requireActivity().getString(R.string.post_failed));
            });
        } else {
            var oldApartmentId = currentUser.getApartmentID();

            fireStoreDatabase.collection(Config.APARTMENT_LIST).document(oldApartmentId).get().addOnSuccessListener(documentSnapshot ->{
                var oldApartment = documentSnapshot.toObject(AiturmApartment.class);
                var members = oldApartment.getApartmentMembers();
                var tasks = oldApartment.getTaskCard();
                var expenses = oldApartment.getExpensesCard();
                if (members == null || members.isEmpty()){
                    members = Collections.emptyList();
                }
                if (tasks == null || members.isEmpty()){
                    tasks = new HashMap();
                }
                if (expenses == null || members.isEmpty()){
                    expenses = new HashMap();
                }

                Map<String, Object> apartment = new HashMap<>();
                apartment.put(Config.adminID, currentUser.getUserID());
                apartment.put(Config.apartmentMembers, members);
                apartment.put(Config.taskCards, tasks);
                apartment.put(Config.expensesCards, expenses);

                var previousMembersIds = members;

                fireStoreDatabase.collection(Config.APARTMENT_LIST).add(apartment).addOnSuccessListener(documentReference -> {


                    Map<String, Object> apartmentId = new HashMap<>();

                    apartmentId.put(Config.apartmentID, documentReference.getId());

                    realTimeDatabaseReference.child(Config.users).child(currentUser.getUserID()).updateChildren(apartmentId);

                    for (int i = 0; i < previousMembersIds.size(); i++) {
                        var request = new HashMap<String, Object>();
                        var index = i;
                        request.put(Config.apartmentID, documentReference.getId());
                        realTimeDatabaseReference.child(Config.users).child(previousMembersIds.get(i)).updateChildren(request).addOnSuccessListener(v -> {

                            if (index == previousMembersIds.size()-1){
                                currentUser.setApartmentID(documentReference.getId());
                                manager.saveData(currentUser);


                                Map<String, Object> apartmentPost = new HashMap<>();
                                apartmentPost.put(Config.apartmentID, documentReference.getId());
                                apartmentPost.put(Config.IMAGE_FOLDER_PATH, imageFolderPath);
                                apartmentPost.put(Config.DESCRIPTION, description);
                                apartmentPost.put(Config.userID, currentUser.getUserID());
                                apartmentPost.put(Config.PRICE, budget);
                                apartmentPost.put(Config.NUMBER_OF_ROOMMATES, numberOfRoomMates);
                                apartmentPost.put(Config.PREFERENCE, preferences);
                                apartmentPost.put(Config.IMAGE_URL, imageUris);
                                apartmentPost.put(Config.TIME_STAMP, System.currentTimeMillis());
                                apartmentPost.put(Config.BUILDING_ADDRESS, buildingAddress);
                                apartmentPost.put(Config.BUILDING_TYPE, buildingType);


                                fireStoreDatabase.collection(Config.APARTMENT_POST).add(apartmentPost).addOnSuccessListener(reference -> {
                                    fireStoreDatabase.collection(Config.APARTMENT_LIST).document(oldApartmentId).delete().addOnSuccessListener(v1 -> {
                                        progressBar.dismiss();
                                        showCustomToast(requireActivity().getString(R.string.post_success));
                                        requireActivity().onBackPressed();
                                    }).addOnFailureListener(e -> {
                                        e.printStackTrace();
                                        progressBar.dismiss();
                                        showCustomToast(requireActivity().getString(R.string.post_failed));
                                        requireActivity().onBackPressed();
                                    });
                                }).addOnFailureListener(e -> {
                                    e.printStackTrace();
                                    progressBar.dismiss();
                                    showCustomToast(requireActivity().getString(R.string.post_failed));
                                    requireActivity().onBackPressed();
                                });
                            }
                        });
                    }

                }).addOnFailureListener(e -> {
                    progressBar.dismiss();
                    showCustomToast(requireActivity().getString(R.string.post_failed));
                });

            }).addOnFailureListener(e -> {
                requireActivity().onBackPressed();
            });

        }
    }


    public void showCustomToast(String msg) {
        appCompatActivity.runOnUiThread(() -> {
            //inflate the custom toast
            LayoutInflater inflater = appCompatActivity.getLayoutInflater();
            // Inflate the Layout
            View layout = inflater.inflate(R.layout.custom_toast, appCompatActivity.findViewById(R.id.toast_layout));

            TextView text = layout.findViewById(R.id.toast_text);

            // Set the Text to show in TextView
            text.setText(msg);

            Toast toast = new Toast(appCompatActivity.getApplication());

            //Setting up toast position, similar to Snackbar
            toast.setGravity(Gravity.BOTTOM | Gravity.START | Gravity.FILL_HORIZONTAL, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        });

    }
}