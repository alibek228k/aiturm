package kz.devs.aiturm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import kz.devs.aiturm.model.SignInMethod;
import kz.devs.aiturm.model.User;
import kz.devs.aiturm.presentaiton.SessionManager;
import kz.devs.aiturm.presentaiton.edit.ChangeEmailDialog;
import kz.devs.aiturm.presentaiton.edit.ChangeGenderDialogFragment;
import kz.devs.aiturm.presentaiton.edit.ChangeGroupDialogFragment;
import kz.devs.aiturm.presentaiton.edit.ChangePhoneDialogFragment;

public class EditProfileDialogFragment extends DialogFragment implements ChangePhoneDialogFragment.PhoneChangeCallback, ChangeGenderDialogFragment.GenderChangeCallback, ChangeBioDialogFragment.BioChangeCallback, ChangeGroupDialogFragment.GroupChangeCallback, ChangeEmailDialog.EmailChangeCallback, ChangeUsernameDialog.UsernameChangeCallback, ChangeNameDialogFragment.NameChangedCallback {

    private EditProfileCallback callback;

    public EditProfileDialogFragment(EditProfileCallback callback) {
        this.callback = callback;
    }

    private View v;
    private ImageView profileImage;
    private TextView nameTxtView, emailTxtView, bioTxt, genderTextView, groupTextView, phoneNumberTextView, specialityTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference storageRef;
    private StorageTask uploadTask;
    private CustomLoadingProgressBar customLoadingProgressBar;
    private User user;
    private MaterialButton changeUserNameButton;

    private static final int IMAGE_REQUEST = 1;
    private SessionManager manager;
    public static final int DIALOG_FRAGMENT_REQUEST_CODE = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        manager = new SessionManager(getActivity());
        user = manager.getData();
        storageRef = FirebaseStorage.getInstance().getReference();
        return v;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        customLoadingProgressBar= new CustomLoadingProgressBar(getActivity(), "Updating" , R.raw.loading_animation);
        customLoadingProgressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        profileImage = v.findViewById(R.id.edit_profile_image_view);
        ImageButton editImage = v.findViewById(R.id.change_profile_picture);
        nameTxtView = v.findViewById(R.id.edit_username);
        emailTxtView = v.findViewById(R.id.edit_email);
        genderTextView = v.findViewById(R.id.edit_gender);
        groupTextView = v.findViewById(R.id.edit_group);
        phoneNumberTextView = v.findViewById(R.id.edit_phone_number);
        specialityTextView = v.findViewById(R.id.edit_specialization);
        changeUserNameButton = v.findViewById(R.id.change_username_button);
        Button changePassword = v.findViewById(R.id.change_password);
        bioTxt = v.findViewById(R.id.edit_bio);
        LinearLayout changeBio = v.findViewById(R.id.bio_linear);
        LinearLayout changeEmail = v.findViewById(R.id.email_linear);
        LinearLayout changeGender = v.findViewById(R.id.gender_linear);
        LinearLayout changeName = v.findViewById(R.id.name_linear);
        LinearLayout changeGroup = v.findViewById(R.id.group_linear);
        LinearLayout changePhone = v.findViewById(R.id.phone_number_linear);
        LinearLayout changeSpeciality = v.findViewById(R.id.specialization_linear);
        Button deleteAccount = v.findViewById(R.id.delete_account_button);
        Button cancle = v.findViewById(R.id.edit_profile_cancel);
        Button done = v.findViewById(R.id.edit_profile_done);
        FirebaseUser firebaseUser=mAuth.getCurrentUser();
        if (firebaseUser!=null) {
            getUserDetails();
            editImage.setOnClickListener(v -> openImage());
            changeBio.setOnClickListener(v -> {
                ChangeBioDialogFragment changeBioDialogFragment = new ChangeBioDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("USER", user);
                changeBioDialogFragment.setArguments(bundle);
                changeBioDialogFragment.setTargetFragment(EditProfileDialogFragment.this, DIALOG_FRAGMENT_REQUEST_CODE);
                changeBioDialogFragment.show(getParentFragmentManager(), null);
            });
            changeName.setOnClickListener(view1 -> {
                ChangeNameDialogFragment changeNameDialogFragment = new ChangeNameDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("USER", user);
                changeNameDialogFragment.setArguments(bundle);
                changeNameDialogFragment.setTargetFragment(EditProfileDialogFragment.this, DIALOG_FRAGMENT_REQUEST_CODE);
                changeNameDialogFragment.show(getParentFragmentManager(), null);
            });

            changeGender.setOnClickListener(v -> {
                ChangeGenderDialogFragment changeGenderDialog = new ChangeGenderDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("USER", user);
                changeGenderDialog.setArguments(bundle);
                changeGenderDialog.setTargetFragment(EditProfileDialogFragment.this,DIALOG_FRAGMENT_REQUEST_CODE);
                changeGenderDialog.show(getParentFragmentManager(), null);
            });
            changeUserNameButton.setOnClickListener(v -> {
                ChangeUsernameDialog changeUsernameDialog = new ChangeUsernameDialog();
                Bundle bundle = new Bundle();
                bundle.putSerializable("USER", user);
                changeUsernameDialog.setArguments(bundle);
                changeUsernameDialog.setTargetFragment(EditProfileDialogFragment.this, DIALOG_FRAGMENT_REQUEST_CODE);
                changeUsernameDialog.show(getParentFragmentManager(), null);
            });

            changeEmail.setOnClickListener(v -> {
                SignInMethod method = user.getSignInMethod();
                if (method == null) {
                    Toast.makeText(getContext(), "Technical problems, try changing your email a little later.", Toast.LENGTH_SHORT).show();
                } else {
                    if (method == SignInMethod.GOOGLE) {
                        Toast.makeText(getContext(), "You can't change your account because you're using a Google account.", Toast.LENGTH_SHORT).show();
                    } else if (method == SignInMethod.MICROSOFT) {
                        Toast.makeText(getContext(), "You can't change your account because you're using a Microsoft account.", Toast.LENGTH_SHORT).show();
                    } else {
                        ChangeEmailDialog changeEmailDialog = new ChangeEmailDialog();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("USER", user);
                        changeEmailDialog.setArguments(bundle);
                        changeEmailDialog.setTargetFragment(EditProfileDialogFragment.this, DIALOG_FRAGMENT_REQUEST_CODE);
                        changeEmailDialog.show(getParentFragmentManager(), null);
                    }
                }
            });

            changeGroup.setOnClickListener(v -> {
                ChangeGroupDialogFragment changeGroupDialogFragment = new ChangeGroupDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("USER", user);
                changeGroupDialogFragment.setArguments(bundle);
                changeGroupDialogFragment.setTargetFragment(EditProfileDialogFragment.this, DIALOG_FRAGMENT_REQUEST_CODE);
                changeGroupDialogFragment.show(getParentFragmentManager(), null);
            });

            changePhone.setOnClickListener(v -> {
                ChangePhoneDialogFragment changePhoneDialogFragment = new ChangePhoneDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("USER", user);
                changePhoneDialogFragment.setArguments(bundle);
                changePhoneDialogFragment.setTargetFragment(EditProfileDialogFragment.this, DIALOG_FRAGMENT_REQUEST_CODE);
                changePhoneDialogFragment.show(getParentFragmentManager(), null);
            });


            changePassword.setOnClickListener(v ->
                    new AlertDialog.Builder(getActivity())
                            .setMessage("A message to reset your password will be sent to your email.")
                            .setTitle("Change password")
                            .setPositiveButton("OK", (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                                resetPass();
                            })
                            .setNegativeButton("Cancel", (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .create()
                            .show()
            );
            deleteAccount.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.sure_delete_account));
                builder.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    DeleteUserDialogDragment deleteUserDialogDragment = new DeleteUserDialogDragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("USER", user);
                    deleteUserDialogDragment.setArguments(bundle);
                    deleteUserDialogDragment.setTargetFragment(EditProfileDialogFragment.this, DIALOG_FRAGMENT_REQUEST_CODE);
                    deleteUserDialogDragment.show(getParentFragmentManager(), null);
                });
                builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
                builder.show();
            });
        } else {
            dismiss();
        }


        done.setOnClickListener(v -> dismiss());
        cancle.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data !=null) {
            Uri imageUri = data.getData();
            if (uploadTask !=null && uploadTask.isInProgress()){
                customLoadingProgressBar.show();
                customLoadingProgressBar.setLoadingText("Uploading...");
            } else {
                uploadImage(imageUri);
            }
        }
    }
    private void openImage() {
        final CharSequence[] pictureOptions = {"Choose From Gallery", "Remove Profile Picture"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Change Profile Image");
        builder.setItems(pictureOptions, (dialog, item) -> {
            if (pictureOptions[item].equals("Choose From Gallery")) {
                Intent pickPicture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPicture, IMAGE_REQUEST);
            } else if (pictureOptions[item].equals("Remove Profile Picture")) {
                removeProfileImage();
            }
        });
        builder.show();
    }
    private void removeProfileImage(){
        rootRef.child(Config.users).child(user.getUserID()).child("iamge").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()) {
                    GlideApp.with(getContext())
                            .load(R.drawable.ic_user_profile_svgrepo_com)
                            .transform(new CircleCrop())
                            .into(profileImage);
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(user.getImage());
                    if (storageReference != null) {
                        storageReference.delete().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(getContext(), "Image deleted successfully.", Toast.LENGTH_SHORT).show();
                                user.setImage(null);
                                manager.removeUserData();
                                manager.saveData(user);
                                callback.onProfileDataChanged(user);
                            }
                        }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show());

                    }
                    customLoadingProgressBar.dismiss();
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void uploadImage(Uri imageUri) {

        if (user.getImage() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(user.getImage());
            if (storageReference != null) {
                storageReference.delete().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        user.setImage(null);
                        manager.removeUserData();
                        manager.saveData(user);
                    }
                }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show());

            }
        }

        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT >= 29) {
            ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), imageUri);
            try {
                bitmap = ImageDecoder.decodeBitmap(source);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        customLoadingProgressBar.show();
        if (imageUri !=null){
            final StorageReference fileRef = storageRef
                    .child("profile pictures")
                    .child(user.getUserID())
                    .child(imageUri.getLastPathSegment());
            uploadTask = fileRef.putBytes(byteArray);
            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                if(!task.isSuccessful()){
                    throw Objects.requireNonNull(task.getException());
                }
                return fileRef.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    String mUrl = downloadUri.toString();
                    HashMap <String, Object> imageDetails = new HashMap<>();
                    imageDetails.put("image", mUrl);
                    rootRef.child(Config.users).child(user.getUserID()).updateChildren(imageDetails).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            user.setImage(mUrl);
                            manager.removeUserData();
                            manager.saveData(user);
                            GlideApp.with(getContext())
                                    .load(mUrl)
                                    .transform(new CircleCrop())
                                    .placeholder(R.drawable.ic_user_profile_svgrepo_com)
                                    .into(profileImage);
                            customLoadingProgressBar.dismiss();
                            callback.onProfileDataChanged(user);
                        }
                    });
                }
                else{
                    Toast.makeText(getActivity(), "Upload failed",Toast.LENGTH_SHORT).show();
                }
                customLoadingProgressBar.dismiss();
            }).addOnFailureListener(e -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
//            TODO ERROR HANDLING
            Toast.makeText(getActivity(), "No image selected", Toast.LENGTH_SHORT).show();
        }

    }

    private void getUserDetails() {
        if (user != null) {
            if (user.getBio() != null && !user.getBio().equals("")) {
                bioTxt.setText(user.getBio());
            }
            nameTxtView.setText(user.getName());
            changeUserNameButton.setText("@" + user.getUsername());
            emailTxtView.setText(user.getEmail());
            var gender = user.getGender();
            if (gender == User.Gender.MALE){
                genderTextView.setText(getString(R.string.gender_select_male));
            }else if (gender == User.Gender.FEMALE){
                genderTextView.setText(getString(R.string.gender_select_female));
            }
            groupTextView.setText(user.getGroup());
            setupPhoneNumberFormatting(user.getPhoneNumber());
            specialityTextView.setText(user.getSpecialization());
            if (user.getImage() != null) {
                GlideApp.with(getActivity().getApplicationContext())
                        .load(user.getImage())
                        .fitCenter()
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.ic_user_profile_svgrepo_com)
                        .into(profileImage);
            }
        }

    }

    private void setupPhoneNumberFormatting(String phone){
        String ans = "+7(" + phone.substring(1, 4) + ") " + phone.substring(
                4, 7
        ) + "-" + phone.substring(7, 9) + "-" + phone.substring(9);
        phoneNumberTextView.setText(ans);
    }

    private void resetPass() {
        mAuth.sendPasswordResetEmail(user.getEmail()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "An email has been sent to reset your password", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
//                TODO ERROR HANDLING
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onBioChanged(String bioTxt) {
        user.setBio(bioTxt);
        callback.onProfileDataChanged(user);
        manager.removeUserData();
        manager.saveData(user);
        this.bioTxt.setText(bioTxt);
    }


    public void onGroupChanged(String group, String specialization) {
        user.setGroup(group);
        user.setSpecialization(specialization);
        callback.onProfileDataChanged(user);
        manager.removeUserData();
        manager.saveData(user);
        this.groupTextView.setText(group);
        this.specialityTextView.setText(specialization);
    }
    @Override
    public void onPhoneChanged(String phoneTxt) {
        user.setPhoneNumber(phoneTxt);
        callback.onProfileDataChanged(user);
        manager.removeUserData();
        manager.saveData(user);
        setupPhoneNumberFormatting(phoneTxt);
    }

    @Override
    public void onGenderChanged(User.Gender gender) {
        user.setGender(gender);
        callback.onProfileDataChanged(user);
        manager.removeUserData();
        manager.saveData(user);
        var genderTxt = "";
        if (gender == User.Gender.MALE) {
            genderTxt = "Male";
        } else if (gender == User.Gender.FEMALE) {
            genderTxt = "Female";
        }
        this.genderTextView.setText(genderTxt);
    }



    public void onEmailChanged(String emailTxt) {
        user.setEmail(emailTxt);
        callback.onProfileDataChanged(user);
        manager.removeUserData();
        manager.saveData(user);
        emailTxtView.setText(emailTxt);
    }

    @Override
    public void onUsernameChanged(String nameTxt) {
        user.setUsername(nameTxt);
        callback.onProfileDataChanged(user);
        manager.removeUserData();
        manager.saveData(user);
        changeUserNameButton.setText(nameTxt);
    }

    @Override
    public void onNameChanged(String changedName) {
        user.setName(changedName);
        callback.onProfileDataChanged(user);
        manager.removeUserData();
        manager.saveData(user);
        nameTxtView.setText(changedName);
    }
}

interface EditProfileCallback {
    void onProfileDataChanged(User user);
}

