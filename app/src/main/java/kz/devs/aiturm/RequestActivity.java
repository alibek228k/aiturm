package kz.devs.aiturm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

import kz.devs.aiturm.model.User;
import kz.devs.aiturm.presentaiton.SessionManager;
import kz.devs.aiturm.presentaiton.post.PublishPostActivity;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;


public class
RequestActivity extends AppCompatActivity {
    //views
   private RecyclerView requestRecyclerView, invitationRecyclerView;
   private RelativeLayout  noSentRequestsLayout , noReceivedRequestsLayout;
   private RelativeLayout rootLayout;
   private LottieAnimationView catAnimationView;
    //firebase
   private FirebaseAuth mAuth;
   private RequestQueue requestQueue;
   //data
   private RequestAdapter requestAdapter;
   private RequestAdapter invitationAdapter;
   private ArrayList<User> receivedRequestList;
   private ArrayList<User> sentRequestList;
   private TabLayout tabLayout;

   private DatabaseReference rootReference;
   private User currentUser;
   private SessionManager manager;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_activity);
        mAuth=FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestRecyclerView = findViewById(R.id.request_recyclerview);
        tabLayout=findViewById(R.id.tab_layout_req);
        invitationRecyclerView=findViewById(R.id.invitation_recyclerview);
        rootLayout = findViewById(R.id.request_fragment_root_layout);
        noSentRequestsLayout = findViewById(R.id.no_sent_request_Layout);
        catAnimationView = findViewById(R.id.empty_animation_req);
        MaterialButton goToMyAiturmButton = findViewById(R.id.go_to_shroomies_button);
        MaterialButton goToPublishPostButton = findViewById(R.id.go_to_publish_post_button);

        rootReference = FirebaseDatabase.getInstance().getReference();
        manager = new SessionManager(this);
        currentUser = manager.getData();
        Toolbar reqToolbar = findViewById(R.id.request_toolbar);
        setSupportActionBar(reqToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        noReceivedRequestsLayout = findViewById(R.id.no_received_request_Layout);
        goToMyAiturmButton.setOnClickListener(v -> startActivity(new Intent(this, MyAiturmActivity.class)));
        goToPublishPostButton.setOnClickListener(v -> startActivity(new Intent(this, PublishPostActivity.class)));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext() , RecyclerView.VERTICAL,false);
        requestRecyclerView.setHasFixedSize(true);
        requestRecyclerView.setLayoutManager(linearLayoutManager);

        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL , false);
        invitationRecyclerView.setLayoutManager(linearLayoutManager1);
        invitationRecyclerView.setHasFixedSize(true);
        receivedRequestList =new ArrayList<>();
        sentRequestList =new ArrayList<>();
        OverScrollDecoratorHelper.setUpOverScroll(invitationRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        OverScrollDecoratorHelper.setUpOverScroll(requestRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        getToken();
        Objects.requireNonNull(tabLayout.getTabAt(0)).select();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    noReceivedRequestsLayout.setVisibility(View.GONE);
                    noSentRequestsLayout.setVisibility(View.GONE);
                    requestRecyclerView.setVisibility(View.GONE);
                    invitationRecyclerView.setVisibility(View.VISIBLE);
                    catAnimationView.pauseAnimation();
                    catAnimationView.setVisibility(View.GONE);
                    if (receivedRequestList !=null){
                        if(receivedRequestList.isEmpty()){
                            invitationRecyclerView.setVisibility(View.GONE);
                            requestRecyclerView.setVisibility(View.GONE);
                            noSentRequestsLayout.setVisibility(View.GONE);
                            noReceivedRequestsLayout.setVisibility(View.VISIBLE);
                            catAnimationView.playAnimation();
                            catAnimationView.setVisibility(View.VISIBLE);
                        }
                    }
                } if (tab.getPosition()==1) {
                    noReceivedRequestsLayout.setVisibility(View.GONE);
                    noSentRequestsLayout.setVisibility(View.GONE);
                    invitationRecyclerView.setVisibility(View.GONE);
                    requestRecyclerView.setVisibility(View.VISIBLE);
                    catAnimationView.pauseAnimation();
                    catAnimationView.setVisibility(View.GONE);
                    if (sentRequestList !=null){
                        if(sentRequestList.isEmpty()){
                            requestRecyclerView.setVisibility(View.GONE);
                            invitationRecyclerView.setVisibility(View.GONE);
                            noReceivedRequestsLayout.setVisibility(View.GONE);
                            noSentRequestsLayout.setVisibility(View.VISIBLE);
                            catAnimationView.playAnimation();
                            catAnimationView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    private void getRequests(String token, String userUid) {
        receivedRequestList = new ArrayList<>();
        sentRequestList = new ArrayList<>();

        var receivedRequests = currentUser.getReceivedRequests();
        if (receivedRequests == null){
            if (tabLayout.getSelectedTabPosition()==0){
                invitationRecyclerView.setVisibility(View.GONE);
                requestRecyclerView.setVisibility(View.GONE);
                noSentRequestsLayout.setVisibility(View.GONE);
                noReceivedRequestsLayout.setVisibility(View.VISIBLE);
                catAnimationView.playAnimation();
                catAnimationView.setVisibility(View.VISIBLE);
            }
        }else{
            rootReference.child(Config.users).get().addOnSuccessListener(dataSnapshot ->{
                if (dataSnapshot.exists()){
                    dataSnapshot.getChildren().forEach(children -> {
                        receivedRequests.keySet().forEach(id -> {
                            if (id.equals(children.getKey())){
                                var user = children.getValue(User.class);
                                user.setUserID(children.getKey());
                                receivedRequestList.add(user);
                            }
                        });
                    });
                }
                invitationAdapter = new RequestAdapter(RequestActivity.this, rootLayout, receivedRequestList, false, FirebaseDatabase.getInstance().getReference());
                invitationAdapter.notifyDataSetChanged();
                invitationRecyclerView.setAdapter(invitationAdapter);
            });
        }

        var sentRequests = currentUser.getSendRequests();
        if (sentRequests == null){
            if (tabLayout.getSelectedTabPosition()==1) {
                requestRecyclerView.setVisibility(View.GONE);
                invitationRecyclerView.setVisibility(View.GONE);
                noReceivedRequestsLayout.setVisibility(View.GONE);
                noSentRequestsLayout.setVisibility(View.VISIBLE);
                catAnimationView.playAnimation();
                catAnimationView.setVisibility(View.VISIBLE);
            }
        }else {
            rootReference.child(Config.users).get().addOnSuccessListener(dataSnapshot ->{
                if (dataSnapshot.exists()){
                    dataSnapshot.getChildren().forEach(children -> {
                        sentRequests.forEach(id -> {
                            if (id.equals(children.getKey())){
                                var user = children.getValue(User.class);
                                user.setUserID(children.getKey());
                                sentRequestList.add(user);
                            }
                        });
                    });
                }
                requestAdapter = new RequestAdapter(RequestActivity.this, rootLayout, sentRequestList, true, FirebaseDatabase.getInstance().getReference());
                requestAdapter.notifyDataSetChanged();
                requestRecyclerView.setAdapter(requestAdapter);
            });
        }
    }
 void getToken(){
     FirebaseUser firebaseUser = mAuth.getCurrentUser();
     if (firebaseUser!=null) {
         firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
             if (task.isSuccessful()) {
                 String token = task.getResult().getToken();
                 getRequests(token, firebaseUser.getUid());

             }else{
                 displayErrorAlert(null,"Something went wrong!");
             }
         });
     }
 }
    void displayErrorAlert(@Nullable VolleyError error , String errorMessage){
        String message = null; // error message, show it in toast or dialog, whatever you want
        if(error!=null) {
            if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                message = "Cannot connect to Internet";
            } else if (error instanceof ServerError) {
                message = "Server error. Please try again later";
            } else if (error instanceof ParseError) {
                message = "Parsing error! Please try again later";
            }
        }else{
            message = errorMessage;
        }
    }
}