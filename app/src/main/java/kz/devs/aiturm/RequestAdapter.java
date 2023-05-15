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
import android.widget.RelativeLayout;
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
import com.example.shroomies.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kz.devs.aiturm.model.User;
import kz.devs.aiturm.presentaiton.SessionManager;
import kz.devs.aiturm.presentaiton.profile.UserProfileActivity;


public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private final Context context;
    View v;
    FirebaseAuth mAuth;
    private final ArrayList<User> usersList;
    Boolean receiverUsers;
    RequestQueue requestQueue;
    RelativeLayout rootLayout;

    DatabaseReference rootReference;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    public RequestAdapter(Context context, RelativeLayout rootLayout, ArrayList<User> usersList, Boolean receiverUsers, DatabaseReference rootReference) {
        this.context = context;
        this.usersList = usersList;
        this.receiverUsers = receiverUsers;
        this.rootLayout = rootLayout;
        this.rootReference = rootReference;
//        this.firebaseFirestore = firebaseFirestore
    }


    @NonNull
    @Override
    public RequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        v = layoutInflater.inflate(R.layout.request_card,parent,false);
        mAuth= FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(context);
        return  new RequestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.RequestViewHolder holder, final int position) {
       holder.senderName.setText(usersList.get(position).getUsername());
       String imageUrl= usersList.get(position).getImage();

       if(imageUrl!=null){
           if(!imageUrl.isEmpty()) {
               GlideApp.with(context)
                       .load(usersList.get(position).getImage())
                       .fitCenter()
                       .circleCrop()
                       .into(holder.senderImage);
           }
       }

       if(receiverUsers){
           holder.reject.setVisibility(View.GONE);
           holder.accept.setVisibility(View.GONE);
           holder.requestTv.setText("has been invited by you");
           holder.cancel.setVisibility(View.VISIBLE);

       }else{
           holder.reject.setVisibility(View.VISIBLE);
           holder.accept.setVisibility(View.VISIBLE);
           holder.requestTv.setText("has invited you");
           holder.cancel.setVisibility(View.GONE);
       }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout rootLayout;
        ImageButton accept,reject;
        Button cancel;
        ImageView senderImage;
        TextView senderName, requestTv;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            rootLayout = itemView.findViewById(R.id.root_layout);
            accept=itemView.findViewById(R.id.accept_button);
            reject=itemView.findViewById(R.id.decline_btn);
            senderImage=itemView.findViewById(R.id.request_user_photo);
            senderName=itemView.findViewById(R.id.user_name);
            requestTv =itemView.findViewById(R.id.requested_tv);
            cancel=itemView.findViewById(R.id.cancel_request);

            rootLayout.setOnClickListener(v -> {
                context.startActivity(UserProfileActivity.Companion.newInstance(context, usersList.get(getAdapterPosition()).getUserID()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            });

            accept.setOnClickListener(v -> {
//                    String title = "Join room";
//                    String message = "Joining this room would remove you from your current room. If you are the only member in your current room, your data will be deleted";
//                    showAcceptDialog(usersList.get(getAdapterPosition()).getUserID(), title , message);
                acceptRequest(usersList.get(getAdapterPosition()).getUserID(), getAdapterPosition());
            });
            reject.setOnClickListener(v -> rejectRequest(usersList.get(getAdapterPosition()).getUserID(), getAdapterPosition()));
            cancel.setOnClickListener(v -> cancelRequest(usersList.get(getAdapterPosition()).getUserID() , getAdapterPosition()));
        }

        private void rejectRequest(final String senderID , int position) {
            var currentUser = new SessionManager(context).getData();
            var receivedRequests = currentUser.getReceivedRequests();
            if (receivedRequests.containsKey(senderID)){
                receivedRequests.remove(senderID);
                var request = new HashMap<String, Object>();
                request.put("receivedRequests", receivedRequests);
                rootReference.child(Config.users).child(currentUser.getUserID()).updateChildren(request).addOnSuccessListener(dataSnapshot -> {
                    var sendRequests = usersList.get(position).getSendRequests();
                    sendRequests.remove(currentUser.getUserID());
                    var newRequest = new HashMap<String, Object>();
                    newRequest.put("sendRequests", sendRequests);
                    rootReference.child(Config.users).child(senderID).updateChildren(newRequest).addOnFailureListener(e -> {
                        Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                    }).addOnSuccessListener(dataSnapshot1 -> {
                        usersList.remove(position);
                        notifyItemRemoved(position);
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                });
            }else{
                Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        }

        private void cancelRequest(final String recipientId , int position) {
            var currentUser = new SessionManager(context).getData();
            var sendRequests = currentUser.getSendRequests();
            if (sendRequests.contains(recipientId)) {
                sendRequests.remove(recipientId);
                var request = new HashMap<String, Object>();
                request.put("sendRequests", sendRequests);
                rootReference.child(Config.users).child(currentUser.getUserID()).updateChildren(request).addOnSuccessListener(dataSnapshot -> {
                    var receivedRequests = usersList.get(position).getReceivedRequests();
                    receivedRequests.remove(currentUser.getUserID());
                    var newRequest = new HashMap<String, Object>();
                    newRequest.put("receivedRequests", receivedRequests);
                    rootReference.child(Config.users).child(recipientId).updateChildren(newRequest).addOnFailureListener(e -> {
                        Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                    }).addOnSuccessListener(dataSnapshot1 -> {
                        usersList.remove(position);
                        notifyItemRemoved(position);
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                });
            }else{
                Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        }

         private void acceptRequest(final String senderID, int position){
            CollectionReference apartmentReference = firebaseFirestore.collection(Config.APARTMENT_LIST);
             var currentUser = new SessionManager(context).getData();
             var receivedRequests = currentUser.getReceivedRequests();
            if (receivedRequests.containsKey(senderID) && receivedRequests.get(senderID) != null){
                var apartmentId = receivedRequests.get(senderID);
                rejectRequest(senderID, position);

                apartmentReference.document(apartmentId).get().addOnSuccessListener(documentSnapshot -> {
                    var apartment = documentSnapshot.toObject(AiturmApartment.class);
                    var members = apartment.getApartmentMembers();
                    members.put(currentUser.getUserID(), currentUser.getUserID());
                    apartmentReference.document(apartmentId).update(Config.apartmentMembers, members).addOnSuccessListener(task -> {
                        var request = new HashMap<String, Object>();
                        request.put("apartmentID", apartmentId);
                        rootReference.child(Config.users).child(currentUser.getUserID()).updateChildren(request).addOnSuccessListener(task1 ->{
                            Toast.makeText(context, context.getString(R.string.you_are_member), Toast.LENGTH_SHORT).show();
                        });
                    });
                });

//                 FirebaseUser firebaseUser = mAuth.getCurrentUser();
//                 if (firebaseUser!=null) {
//                     firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
//                         if(task.isSuccessful()){
//                             String token = task.getResult().getToken();
//                             String apartmentID = (String) task.getResult().getClaims().get(Config.apartmentID);
//                             String role  = (String) task.getResult().getClaims().get(Config.role);
//
//                             JSONObject jsonObject = new JSONObject();
//                             JSONObject data = new JSONObject();
//                             try {
//                                 jsonObject.put(Config.receiverID  , firebaseUser.getUid());
//                                 jsonObject.put(Config.receiverApartmentID  ,apartmentID );
//                                 jsonObject.put(Config.senderID , senderID);
//                                 jsonObject.put(Config.role , role);
//                                 data.put(Config.data , jsonObject);
//                             } catch (JSONException e) {
//                                 e.printStackTrace();
//                             }
//                             JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_ACCEPT_REQUEST, data, response -> {
//                                 try {
//                                     boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
//                                     String message = response.getJSONObject(Config.result).getString(Config.message);
//                                     if(success){
//                                         //todo remove ffrom adapter
//                                         usersList.remove(getAdapterPosition());
//                                         notifyItemRemoved(getAdapterPosition());
//                                         Snackbar.make(rootLayout,message, BaseTransientBottomBar.LENGTH_LONG).show();
//                                     }else{
//                                         String title = "Maximum members";
//                                         new AlertDialog.Builder(context)
//                                                 .setTitle(title)
//                                                 .setMessage(message)
//                                                 .setIcon(R.drawable.ic_alert)
//                                                 .setPositiveButton("ok", (dialog, which) -> dialog.dismiss()).create().show();
//                                     }
//
//                                 } catch (JSONException e) {
//                                     e.printStackTrace();
//                                 }
//
//                             }, error -> displayErrorAlert(error , null))
//                             {
//                                 @Override
//                                 public Map<String, String> getHeaders()  {
//                                     Map<String, String> params = new HashMap<>();
//                                     params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
//                                     params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
//                                     return params;
//                                 }
//                             };
//                             requestQueue.add(jsonObjectRequest);
//                         }else{
//                             displayErrorAlert(null,"Something went wrong!");
//                         }
//                     });
//                 }
             }else{
                Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }

        }
        private void showAcceptDialog(String userID , String title , String message) {
//            new AlertDialog.Builder(context)
//                    .setIcon(R.drawable.ic_alert)
//                    .setTitle(title)
//                    .setMessage(message)
//                    .setCancelable(false)
//                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
//                    .setPositiveButton("Accept", (dialog, which) -> acceptRequest(userID))
//                    .create()
//                    .show();

        }

    }



    void displayErrorAlert(@Nullable VolleyError error , String errorMessage){
        String message = null;
        if(error!=null){
            if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                message = "Cannot connect to Internet";
            } else if (error instanceof ServerError) {
                message = "Server error. Please try again later";
            } else if (error instanceof ParseError) {
                message = "Parsing error! Please try again later";
            }else{
                message = "Unexpected error";
            }
        }else{
            message=errorMessage;
        }


        Snackbar.make(rootLayout,message, BaseTransientBottomBar.LENGTH_LONG).show();

    }

}
