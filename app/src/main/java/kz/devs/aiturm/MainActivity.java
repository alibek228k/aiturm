package kz.devs.aiturm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.shroomies.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import kz.devs.aiturm.model.User;
import kz.devs.aiturm.presentaiton.SessionManager;
import kz.devs.aiturm.presentaiton.post.PublishPostActivity;

public class MainActivity extends AppCompatActivity implements UserCallback {
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ImageButton myAiturm, inboxButton;
    private TextView usernameDrawer;
    private Button logoutButton, requestsButton, favoriteButton;
    private ImageView profilePic, drawerButton;
    private BottomNavigationView bottomNavigationview;
    private FrameLayout requestButtonFrame;

    private FragmentTransaction ft;
    private FragmentManager fm;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private User user;

    private SessionManager manager;

    private FirebaseUser fUser;

    public static Intent newInstance(Context context) {
        return new Intent(context, MainActivity.class);
    }


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        fUser = mAuth.getCurrentUser();
        logoutButton = findViewById(R.id.logout);
        requestsButton = findViewById(R.id.my_requests_menu);
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        drawerButton = findViewById(R.id.drawerButton);
        myAiturm = findViewById(R.id.logo_toolbar);
        inboxButton = findViewById(R.id.inbox_button);
        usernameDrawer = findViewById(R.id.drawer_nav_profile_name);
        profilePic = findViewById(R.id.drawer_nav_profile_pic);
        bottomNavigationview = findViewById(R.id.bottomNavigationView);
        requestButtonFrame = findViewById(R.id.drawer_nav_request_button_frame_layout);
        favoriteButton = findViewById(R.id.my_favorite_menu);

        manager = new SessionManager(this);

        setupUserDetails();
        setupBottomNavigation();
        setupDrawerButton();
        setupDrawerLayoutButtons();
        setupActionBar();
        setupFirebaseMessaging();

        getFragment(new FindRoomFragment());

    }

    private void getFragment(Fragment fragment) {
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.addToBackStack(null);
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }

    private void setupBottomNavigation() {
        bottomNavigationview.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.find_roomie_menu) {
                getFragment(new FindRoomFragment());
            }
            if (item.getItemId() == R.id.user_profile_menu) {
                getFragment(new UserProfileFragment(this));
            }
            if (item.getItemId() == R.id.publish_post_menu) {
                startActivity(new Intent(MainActivity.this, PublishPostActivity.class));
            }
            return true;
        });
    }

    private void setupDrawerButton() {
        drawerButton.setOnClickListener(view -> {
            drawerLayout.open();
        });
    }

    private void setupDrawerLayoutButtons() {
        requestsButton.setOnClickListener(view -> {
            startActivity(new Intent(this, RequestActivity.class));
            drawerLayout.close();
        });

        favoriteButton.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), FavouritesActivity.class));
            drawerLayout.close();
        });

        logoutButton.setOnClickListener(view -> {
            if (fUser != null) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
                mAuth.signOut();
                mGoogleSignInClient.signOut();
                manager.removeUserData();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(null);
        myAiturm.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MyAiturmActivity.class)));
        inboxButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MessageInbox.class)));
    }

    private void setupFirebaseMessaging() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            String userID = user.getUid();
            DatabaseReference ref = database.getReference("tokens");
            ref.child(userID).setValue(task.getResult());
        });
    }

    static void setBadgeToNumberOfNotifications(final DatabaseReference rootRef, final FirebaseAuth mAuth) {
        // get the number of unseen
        // private messages
        final List<Messages> unSeenMessageList = new ArrayList<>();
        rootRef.child("Messages").child(mAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot
                            : snapshot.getChildren()) {
                        for (DataSnapshot dataSnapshot1 :
                                dataSnapshot.getChildren()) {
                            Messages message = dataSnapshot1.getValue(Messages.class);
                            if (!message.getIsSeen()) {
                                unSeenMessageList.add(message);
                            }

                        }
                    }


                }
                getUnseenGroupMessages(rootRef, mAuth, unSeenMessageList.size());
                unSeenMessageList.clear();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private static void getUnseenGroupMessages(final DatabaseReference rootRef, final FirebaseAuth mAuth, final int unseenPrivetMsgs) {
        //get the number of unseen group messages
        final ArrayList<String> unseenGroupMessages = new ArrayList<>();
        rootRef.child("GroupChatList").child(mAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (final DataSnapshot ds :
                            snapshot.getChildren()) {
                        rootRef.child("GroupChats").child(ds.getKey()).child("Messages").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    unseenGroupMessages.clear();
                                    for (DataSnapshot dataSnapshot
                                            : snapshot.getChildren()) {
                                        for (DataSnapshot snapshot1
                                                : dataSnapshot.child("seenBy").getChildren()) {
                                            if (snapshot1.getKey().equals(mAuth.getInstance().getCurrentUser().getUid()) && snapshot1.getValue().equals("false")) {
                                                unseenGroupMessages.add(snapshot1.getValue().toString());
                                            }
                                        }
                                    }

//                                    btm_view.getOrCreateBadge(R.id.message_inbox_menu).setNumber(unseenGroupMessages.size()+unseenPrivetMsgs);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setupUserDetails() {
        if (mAuth.getCurrentUser() != null) {
            rootRef.child(Config.users).child(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user = task.getResult().getValue(User.class);
                    if (user != null) {
                        user.setUserID(mAuth.getCurrentUser().getUid());
                        manager.saveData(user);
                        if (user.getUsername() == null) {
                            finish();
                            startActivity(new Intent(getApplicationContext(), AddUsername.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));

                        } else {
                            usernameDrawer.setText("@" + user.getUsername());
                            int requestNo = user.getRequestCount();
                            requestsButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @SuppressLint("UnsafeExperimentalUsageError")
                                @Override
                                @OptIn(markerClass = ExperimentalBadgeUtils.class)
                                public void onGlobalLayout() {
                                    BadgeDrawable badgeDrawable = BadgeDrawable.create(MainActivity.this);
                                  badgeDrawable.setBackgroundColor(getColor(R.color.lightGrey));
                                  badgeDrawable.setBadgeTextColor(Color.WHITE);
                                  BadgeUtils.attachBadgeDrawable(badgeDrawable, requestsButton, requestButtonFrame);
                                  badgeDrawable.setHorizontalOffset(100);
                                    badgeDrawable.setVerticalOffset(70);
                                    badgeDrawable.setHotspotBounds(100, 100, 100, 100);
                                    badgeDrawable.setBadgeGravity(BadgeDrawable.BOTTOM_END);
                                    requestButtonFrame.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    if (requestNo == 0) {
                                        badgeDrawable.setVisible(false);
                                    } else {
                                        badgeDrawable.setVisible(true);
                                        badgeDrawable.setNumber(requestNo);
                                    }
                                }
                            });
                            if (user.getImage() != null) {
                                if (!user.getImage().isEmpty()) {
                                    GlideApp.with(getApplicationContext())
                                            .load(user.getImage())
                                            .fitCenter()
                                            .circleCrop()
                                            .transition(DrawableTransitionOptions.withCrossFade())
                                            .into(profilePic);
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onUserDataChanged() {
        user = manager.getData();
        if (user.getImage() != null && !user.getImage().isEmpty()) {
            GlideApp.with(getApplicationContext())
                    .load(user.getImage())
                    .fitCenter()
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(profilePic);
        } else {
            GlideApp.with(this)
                    .load(R.drawable.ic_user_profile_svgrepo_com)
                    .transform(new CircleCrop())
                    .into(profilePic);
        }

        usernameDrawer.setText(user.getUsername());
    }
}