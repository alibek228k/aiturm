package kz.devs.aiturm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.shroomies.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kz.devs.aiturm.model.User;
import kz.devs.aiturm.presentaiton.SessionManager;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    ArrayList<User> userList;
    private final Context context;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private FirebaseFirestore firestore;
    private boolean fromSearchMember = false;
    private RequestQueue requestQueue;
    private final AiturmApartment apartment;
    private View parentView;


    public UserAdapter(ArrayList<User> userList, Context context, AiturmApartment apartment, View parentView) {
        this.userList = userList;
        this.context = context;
        this.apartment = apartment;
        this.parentView = parentView;
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();
    }
    public UserAdapter(ArrayList<User> userList, Context context, Boolean fromSearchMember, AiturmApartment apartment) {
        this.userList = userList;
        this.context = context;
        this.fromSearchMember=fromSearchMember;
        this.apartment=apartment;
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View v = layoutInflater.inflate(R.layout.send_request_card, parent, false);
        mAuth=FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(context);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, final int position) {
        if(userList.get(position).getImage()!=null) {
            if (!userList.get(position).getImage().isEmpty()) {
                GlideApp.with(context)
                        .load(userList.get(position).getImage())
                        .fitCenter()
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade()) //Here a fading animation
                        .into(holder.userImage);
            }
        }
        if(userList.get(position).getUsername()!=null){
            holder.userName.setText(userList.get(position).getUsername());

        }
        if (!mAuth.getCurrentUser().getUid().equals(apartment.getAdminID())) {
            holder.removeMember.setVisibility(View.INVISIBLE);
        }
        if (fromSearchMember) {
            holder.msgMember.setVisibility(View.INVISIBLE);
            holder.removeMember.setVisibility(View.INVISIBLE);
            holder.sendRequest.setVisibility(View.VISIBLE);

//            if(userList.get(position).isRequestSent()){
//                holder.sendRequest.setClickable(false);
//                holder.sendRequest.setText("Sent!");
//            }
            if (userList
                    .get(position).getUserID().equals(mAuth.getCurrentUser().getUid())) {
                holder.sendRequest.setClickable(false);
                holder.sendRequest.setVisibility(View.GONE);
            }
        }else{
            if(apartment.getAdminID().equals(mAuth.getCurrentUser().getUid())){
                holder.removeMember.setVisibility(View.VISIBLE);
            }else{
                holder.removeMember.setVisibility(View.INVISIBLE);
                if (userList.get(position).getUserID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    holder.msgMember.setVisibility(View.INVISIBLE);
                    holder.userName.setText(context.getString(R.string.you));
                }else{
                    holder.msgMember.setVisibility(View.VISIBLE);
                }
            }



        }

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userName;
        Button sendRequest;
        ImageButton msgMember, removeMember;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage=itemView.findViewById(R.id.request_member_photo);
            userName=itemView.findViewById(R.id.request_member_name);
            sendRequest=itemView.findViewById(R.id.send_request_btn);
            msgMember = itemView.findViewById(R.id.msg_member);
            removeMember = itemView.findViewById(R.id.remove_member);
            msgMember.setOnClickListener(view -> {
                Intent intent = new Intent(context, ChattingActivity.class);
                intent.putExtra("USER", userList.get(getAdapterPosition()));
                context.startActivity(intent);
            });

            removeMember.setOnClickListener(v -> new AlertDialog.Builder(context)
            .setIcon(R.drawable.ic_alert)
            .setTitle(R.string.remove_member)
            .setMessage(R.string.delete_member_message)
            .setCancelable(true)
            .setNegativeButton(R.string.yes, (dialog, which) -> {
                dialog.dismiss();
                removeMember(getAdapterPosition());
            })
            .setPositiveButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
            .create()
            .show());

            sendRequest.setOnClickListener(v -> sendRequestToUser(userList.get(getAdapterPosition()), apartment.getApartmentID()));
        }

        private void removeMember(int position) {
            User removedUser = userList.remove(position);

            databaseReference.child(Config.users).child(removedUser.getUserID()).child(Config.apartmentID).removeValue().addOnFailureListener(e -> {
                Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }).addOnSuccessListener(v -> {
                var members = apartment.getApartmentMembers();
                members.remove(removedUser.getUserID());
                var request = new HashMap<String, Object>();
                request.put("apartmentMembers", members);
                firestore.collection(Config.APARTMENT_LIST).document(apartment.getApartmentID()).update(request).addOnSuccessListener(task -> {
                    Toast.makeText(context, context.getString(R.string.user_has_been_removed, removedUser.getUsername()), Toast.LENGTH_SHORT).show();
                    notifyItemRemoved(position);
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                });
            });


//            firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
//                if(task.isSuccessful()){
//                    String token = task.getResult().getToken();
//                    JSONObject jsonObject = new JSONObject();
//                    JSONObject data = new JSONObject();
//                    try {
//                        jsonObject.put(Config.apartmentID , apartmentID);
//                        jsonObject.put(Config.userID , removedUser.getUserID());
//                        data.put(Config.data , jsonObject);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_REMOVE_MEMBER, data, response -> {
//                        try {
//                            boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
//                            if(success){
//
//                                Snackbar snack=Snackbar.make(parentView, context.getString(R.string.user_has_been_removed, removedUser.getUsername()), BaseTransientBottomBar.LENGTH_SHORT);
//                                snack.show();
//                            }else{
//                                //return the user back
//                                userList.add(removedUser);
//                                notifyDataSetChanged();
//                                Snackbar snack=Snackbar.make(parentView, context.getString(R.string.user_deleting_error), BaseTransientBottomBar.LENGTH_SHORT);
//                                snack.show();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }, error -> displayErrorAlert( error , null))
//                    {
//                        @Override
//                        public Map<String, String> getHeaders() {
//                            Map<String, String> params = new HashMap<>();
//                            params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
//                            params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
//                            return params;
//                        }
//                    };
//                    requestQueue.add(jsonObjectRequest);
//
//                }else{
//                    String message = context.getString(R.string.authentication_error);
//                    displayErrorAlert(null, message);
//                }
//            });
        }

        private void sendRequestToUser(User user, String apartmentID) {
            sendRequest.setText(context.getString(R.string.sending));
            sendRequest.setClickable(false);
            var currentUser = new SessionManager(context).getData();

            HashMap<String, Object> map = new HashMap<>();
            var receivedRequests = new HashMap<String, String>();
            receivedRequests.put(currentUser.getUserID(), apartmentID);
            map.put("receivedRequests", receivedRequests);
            databaseReference.child(Config.users).child(user.getUserID()).updateChildren(map).addOnCompleteListener(task -> {
                HashMap<String, Object> map1 = new HashMap<>();
                var sendRequests = new ArrayList<String>();
                sendRequests.add(user.getUserID());
                map1.put("sendRequests", sendRequests);

                databaseReference.child(Config.users).child(currentUser.getUserID()).updateChildren(map1).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        sendRequest.setText(context.getString(R.string.sent));
                        sendRequest.setClickable(false);
                        Toast.makeText(context, context.getString(R.string.request_sent_success), Toast.LENGTH_SHORT).show();
                        ;
                    } else {
                        Toast.makeText(context, context.getString(R.string.request_sent_failed), Toast.LENGTH_SHORT).show();
                        sendRequest.setText(context.getString(R.string.request));
                        sendRequest.setClickable(true);

                    }
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(context, context.getString(R.string.request_sent_failed), Toast.LENGTH_SHORT).show();
                sendRequest.setText(context.getString(R.string.request));
                sendRequest.setClickable(true);
            });
        }
    }
    void displayErrorAlert(@Nullable VolleyError error , String errorMessage){
        String message = null; // error message, show it in toast or dialog, whatever you want
        if(error!=null) {
            if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                message = context.getString(R.string.cannot_connect_internet);
            } else if (error instanceof ServerError) {
                message = context.getString(R.string.server_could_not_be_found);
            } else if (error instanceof ParseError) {
                message = context.getString(R.string.server_could_not_be_found);
            }
        }else{
            message = errorMessage;
        }
        Snackbar snack=Snackbar.make(parentView,message, BaseTransientBottomBar.LENGTH_SHORT);
        snack.show();

    }


}
