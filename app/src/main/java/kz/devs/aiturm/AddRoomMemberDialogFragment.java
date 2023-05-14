package kz.devs.aiturm;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.shroomies.R;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kz.devs.aiturm.model.User;

public class AddRoomMemberDialogFragment extends DialogFragment {
    private SearchView memberSearchView;
    private RecyclerView addMembersRecycler;
    private ImageButton closeButton;
    private RelativeLayout rootLayout;
    private LottieAnimationView progressView;
    //data
    private UserAdapter userRecyclerAdapter;
    private ArrayList<User> suggestedUser;
    private AiturmApartment apartment;

    private DatabaseReference database;


    //TODO add users from the  inbox


    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
        }
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        database = FirebaseDatabase.getInstance().getReference().child(Config.users);
        return inflater.inflate(R.layout.dialog_fragment_add_members, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        memberSearchView = view.findViewById(R.id.search_member);
        addMembersRecycler = view.findViewById(R.id.add_member_recyclerview);
        closeButton = view.findViewById(R.id.close_button_add_shroomie);
        TextView infoTextView = view.findViewById(R.id.information_text_view);
        rootLayout = view.findViewById(R.id.add_member_root_layout);
        progressView = view.findViewById(R.id.search_user_progress_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        addMembersRecycler.setHasFixedSize(true);
        addMembersRecycler.setLayoutManager(linearLayoutManager);

        closeButton.setOnClickListener(v -> dismiss());

        if (getArguments()!=null){
            Bundle bundle = getArguments();
            apartment=bundle.getParcelable("APARTMENT_DETAILS");

        }
//        getMessageInboxListIntoAdapter();

        memberSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(suggestedUser!=null){
                    suggestedUser.clear();
                }
                memberSearchView.clearFocus();
                searchUsers(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

    }
    void searchUsers(String query){
        progressView.setVisibility(View.VISIBLE);
        if(!query.trim().isEmpty()){
            database.orderByChild(Config.username).startAt(query).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        var arrayList = new ArrayList<User>();
                        snapshot.getChildren().forEach(children -> {
                            var member = children.getValue(User.class);
                            member.setUserID(children.getKey());
                            if (member.getApartmentID() == null) arrayList.add(member);
                        });
                        suggestedUser = arrayList;

                        if(suggestedUser.size()!=0){
                            userRecyclerAdapter= new UserAdapter(suggestedUser,getContext(),true,apartment);
                            userRecyclerAdapter.setHasStableIds(true);
                            addMembersRecycler.setAdapter(userRecyclerAdapter);
                        }else{
                            Snackbar.make(rootLayout,getString(R.string.no_such_user_found) , Snackbar.LENGTH_SHORT).show();
                        }
                    }
                    progressView.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(requireContext(), "Search is canceled", Toast.LENGTH_SHORT).show();
                    progressView.setVisibility(View.GONE);
                }
            });
//            firebaseUser.getIdToken(true)
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()){
//                            String token = task.getResult().getToken();
//                            suggestedUser = new ArrayList<>();
//                            JSONObject jsonObject = new JSONObject();
//                            JSONObject data =  new JSONObject();
//
//                            try {
//                                jsonObject.put(Config.name, query.trim());
//                                jsonObject.put(Config.currentUser, mAuth.getCurrentUser().getUid());
//                                data.put(Config.data , jsonObject);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_SEARCH_USERS, data, response -> {
//                                try {
//                                    JSONObject result = response.getJSONObject(Config.result);
//                                    boolean success = result.getBoolean(Config.success);
//                                if(success){
//                                    JSONArray userArray = result.getJSONArray(Config.message);
//                                    //the result is a json object containing
//                                    //json array of json arrays
//                                    //the nested json array contains 2 indices
//                                    // first index is the user object
//                                    // and the second  index is a boolean value
//                                    //indicating whether the current user has already
//                                    // sent a request to the searched user
//                                    final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
//                                    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//
//                                    for(int i= 0 ;i< userArray.length();i++){
//                                        try {
//                                            JSONArray jsonArray =((JSONArray)(userArray.get(i)));
//                                            //cast the first index as a json object and the second as a boolean value
//                                            JSONObject userJsonObject = (JSONObject) jsonArray.get(0);
//                                            boolean requestSent = (boolean)jsonArray.get(1);
//                                            User user =mapper.readValue(userJsonObject.toString() , User.class);
//                                            user.setRequestSent(requestSent);
//                                            if(apartment.getApartmentMembers()!=null){
//                                                if(!apartment.getApartmentMembers().containsKey(user.getUserID())){
//                                                    suggestedUser.add(user);
//                                                }
//                                            }else{
//                                                suggestedUser.add(user);
//                                            }
//
//                                        } catch (JSONException | JsonProcessingException e) {
//                                            e.printStackTrace();
//                                        }        progressView.setVisibility(View.GONE);
//
//                                    }
//                                    if(suggestedUser.size()!=0){
//                                        userRecyclerAdapter= new UserAdapter(suggestedUser,getContext(),true,apartment);
//                                        userRecyclerAdapter.setHasStableIds(true);
//                                        addMembersRecycler.setAdapter(userRecyclerAdapter);
//                                    }else{
//                                        Snackbar.make(rootLayout,"No such user found" , Snackbar.LENGTH_SHORT).show();
//
//                                    }
//
//                                }else{
//                                    String message = result.getString(Config.message);
//                                    Snackbar.make(rootLayout,message , Snackbar.LENGTH_SHORT).show();
//                                }
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                    progressView.setVisibility(View.GONE);
//
//                                }
//                                progressView.setVisibility(View.GONE);
//
//                            }, error -> {
//                                String message = null; // error message, show it in toast or dialog, whatever you want
//                                if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof TimeoutError) {
//                                    message = "Cannot connect to Internet";
//                                } else if (error instanceof ServerError) {
//                                    message = "The server could not be found. Please try again later";
//                                }  else if (error instanceof ParseError) {
//                                    message = "Parsing error! Please try again later";
//                                }
//                                displayErrorAlert("Error" ,message);
//                            })
//                            {
//                                @Override
//                                public Map<String, String> getHeaders() {
//                                    Map<String, String> params = new HashMap<String, String>();
//                                    params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
//                                    params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
//                                    return params;
//                                }
//                            };
//                            requestQueue.add(jsonObjectRequest);
//                        }else{
//                            String title = "Authentication error";
//                            String message = "We encountered a problem while authenticating your account";
//                            displayErrorAlert(title, message);
//                        }
//                    });

        }
    }
    void displayErrorAlert(String title ,  String message ){
        progressView.setVisibility(View.GONE);
        new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_alert)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton("Return", (dialog, which) -> {
                    AddRoomMemberDialogFragment.this.getActivity().onBackPressed();
                    dialog.dismiss();
                })
                .create()
                .show();
    }


}
