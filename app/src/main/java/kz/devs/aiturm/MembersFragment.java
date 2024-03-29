package kz.devs.aiturm;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

import kz.devs.aiturm.model.User;
import kz.devs.aiturm.presentaiton.SessionManager;

public class MembersFragment extends Fragment {
    //views
    private View v;
    private RecyclerView membersRecyclerView;
    private TextView ownerName ;
    private ImageView adminImageView , ghostImageView;
    private RelativeLayout noMembersRelativeLayout , rootLayout;
    private CustomLoadingProgressBar customLoadingProgressBar;
    //firebase
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;
    //data structures
    private ArrayList<User> membersList = new ArrayList<>();
    private UserAdapter userAdapter;
    //model
    private AiturmApartment apartment;

    private DatabaseReference rootReference;

    private FirebaseFirestore firebaseFirestore;
    private User admin;

    private Callback callback;

    private SessionManager manager;

    public MembersFragment(Callback callback){
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_shroomie_members, container, false);
        requestQueue = Volley.newRequestQueue(getActivity());
        mAuth = FirebaseAuth.getInstance();
        rootReference = FirebaseDatabase.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        manager = new SessionManager(requireContext());
        return v;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button leaveRoomButton = v.findViewById(R.id.leave_room_btn);

        adminImageView =v.findViewById(R.id.owner_image);
        ownerName=v.findViewById(R.id.admin_name);
        // set visibility to gone if there are members in add members
        ImageButton msgOwnerImageButton = v.findViewById(R.id.msg_admin);
        membersRecyclerView = v.findViewById(R.id.members_recyclerView);
        ghostImageView = v.findViewById(R.id.ghost_view);
        noMembersRelativeLayout = v.findViewById(R.id.no_members_relative_layout);
        rootLayout = v.findViewById(R.id.relative_layout_member);
        Toolbar toolbar = getActivity().findViewById(R.id.my_shroomies_toolbar);

        toolbar.setTitle("Members");

        toolbar.setTitleTextColor(getActivity().getColor(R.color.jetBlack));
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setElevation(5);
        toolbar.findViewById(R.id.myshroomies_toolbar_logo).setVisibility(View.GONE);
        toolbar.findViewById(R.id.my_shroomies_add_card_btn).setVisibility(View.GONE);
        toolbar.setNavigationOnClickListener(view1 -> {
            toolbar.setTitle(null);
            toolbar.setNavigationIcon(null);
            getActivity().onBackPressed();
        });


        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        membersRecyclerView.setHasFixedSize(true);
        membersRecyclerView.setLayoutManager(linearLayoutManager);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            apartment=bundle.getParcelable("APARTMENT_DETAILS");
            getMemberDetail(apartment);
            if(mAuth.getCurrentUser().getUid().equals(apartment.getAdminID())){
                leaveRoomButton.setVisibility(View.GONE);
                msgOwnerImageButton.setVisibility(View.INVISIBLE);
            }
            //this check is if the admin removed a member and that user is in member page so it refreshes
        }


        leaveRoomButton.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Leave group");
            builder.setPositiveButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.setNegativeButton("leave", (dialog, which) -> {
                dialog.dismiss();
                leaveRoomButton.setClickable(false);
                leaveApartment();
            });
            builder.setMessage(getString(R.string.you_will_no_longer_be_a_roommate));
            builder.setIcon(R.drawable.ic_shroomies_yelllow_black_borders);
            builder.setCancelable(true);
            builder.create().show();

        });

        msgOwnerImageButton.setOnClickListener(view12 -> {
            Intent intent = new Intent(getContext(), ChattingActivity.class);
            intent.putExtra("USERID", mAuth.getCurrentUser().getUid());
            if (admin != null) {
                intent.putExtra("USER", admin);
            }
            startActivity(intent);
        });

       Animation animUpDown = AnimationUtils.loadAnimation(getActivity(),
                R.anim.up_down);
        animUpDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ghostImageView.startAnimation(animation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        // start the animation
        ghostImageView.startAnimation(animUpDown);
    }

    private void leaveApartment() {
        customLoadingProgressBar = new CustomLoadingProgressBar(getActivity(), "Leaving...", R.raw.lf30_editor_igyp9bvy);
        customLoadingProgressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        customLoadingProgressBar.setCancelable(false);
        customLoadingProgressBar.show();

        if (apartment == null) {
            customLoadingProgressBar.dismiss();
        } else {
            var members = apartment.getApartmentMembers();
            members.remove(mAuth.getCurrentUser().getUid());
            var request = new HashMap<String, Object>();
            request.put("apartmentMembers", members);
            firebaseFirestore.collection(Config.APARTMENT_LIST).document(apartment.getApartmentID()).update(request).addOnSuccessListener(task -> {
                rootReference.child(Config.users).child(mAuth.getCurrentUser().getUid()).child(Config.apartmentID).removeValue().addOnSuccessListener(task1 -> {
                    var user = manager.getData();
                    user.setApartmentID(null);
                    manager.saveData(user);
                    callback.leaveApartment();
                    getActivity().finish();
                }).addOnFailureListener(e -> {
                    customLoadingProgressBar.dismiss();
                    Toast.makeText(requireContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                customLoadingProgressBar.dismiss();
                Toast.makeText(requireContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            });
        }

    }


    private void getMemberDetail(AiturmApartment aiturmApartment) {
        userAdapter = new UserAdapter(membersList, getContext(), apartment, getView());
        membersRecyclerView.setAdapter(userAdapter);

        var membersIds = aiturmApartment.getApartmentMembers();
        rootReference.child(Config.users).get().addOnSuccessListener(dataSnapshot -> {
            var newList = new ArrayList<User>();
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                if (membersIds.contains(child.getKey())) {
                    var user = child.getValue(User.class);
                    user.setUserID(child.getKey());
                    newList.add(user);
                } else if (apartment.getAdminID().equals(child.getKey())) {
                    var admin = child.getValue(User.class);
                    admin.setUserID(apartment.getAdminID());
                    setAdminDetails(admin);
                }

            }

            if (!newList.isEmpty()){
                membersList = newList;
                userAdapter.userList = membersList;
                userAdapter.notifyDataSetChanged();
            }else{
                noMembersRelativeLayout.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            String title = "Unexpected error";
            String message = "We have encountered an unexpected error, try to check your internet connection and log in again.";
            displayErrorAlert(title, message , null);
        });
    }

    private void setAdminDetails(User user) {
        if (user != null) {
            admin = user;
            if (mAuth.getCurrentUser().getUid().equals(user.getUserID())) {
                ownerName.setText("You");
            } else {
                ownerName.setText(user.getUsername());
            }
            if (user.getImage() != null) {
                if (!user.getImage().isEmpty()) {
                    GlideApp.with(getContext())
                            .load(user.getImage())
                            .fitCenter()
                            .circleCrop()
                            .error(R.drawable.ic_user_profile_svgrepo_com)
                            .transition(DrawableTransitionOptions.withCrossFade()) //Here a fading animation
                            .into(adminImageView);
                }
            }
        }
    }

    void displayErrorAlert(String title ,  String errorMessage  , VolleyError error){
        if (customLoadingProgressBar != null) customLoadingProgressBar.dismiss();
        String message = null; // error message, show it in toast or dialog, whatever you want
        if(error!=null) {
            if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                message = "Cannot connect to Internet";
            } else if (error instanceof ServerError) {
                message = "Server error, Please try again later";
            } else if (error instanceof ParseError) {
                message = "Parsing error! Please try again later";
            }
        }else{
            message = errorMessage;
        }
        new android.app.AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_alert)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton("return", (dialog, which) -> {
                    MembersFragment.this.getActivity().onBackPressed();
                    dialog.dismiss();
                })
                .create()
                .show();
    }

}

interface Callback{
    void leaveApartment();
}
