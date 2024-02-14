package kz.devs.aiturm;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import kz.devs.aiturm.model.User;
import kz.devs.aiturm.utils.PermissionManager;
import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollStateListener;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class ChattingActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100, STORAGE_REQUEST_CODE = 200,
            IMAGE_PICK_CAMERA_CODE = 300, IMAGE_PICK_GALLERY_CODE = 400;
    //views
    private ImageButton sendMessage, addImage;
    private EditText messageBody;
    private ImageView receiverProfileImage , selectedImageView;
    private TextView receiverUsername , imageTextView;
    private RecyclerView chattingRecycler;

    private IOverScrollDecor chattingRecyclerViewDecor;
    private IOverScrollStateListener onOverPullListener;
    private LinearLayoutManager linearLayoutManager;
    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    //data
    private List<Messages> messagesArrayList = new ArrayList<>();
    private MessagesAdapter messagesAdapter;
    //variables
    private String imageUrl, senderID, saveCurrentDate, saveCurrentTime, receiverID;
    private int messageStartPosition = 0, messageEndPosition = 0;
    private final int MESSAGE_PAGINATION_AMOUNT = 30;
    private String firstMessageID;
    private boolean firstMessageFromChildListener = true;
    private boolean loading = true, scrollFromTop;
    private Uri chosenImage;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    chosenImage = uri;
                    Glide.with(getApplication())
                            .load(chosenImage)
                            .transform(new CenterCrop(),new RoundedCorners(20) )
                            .error(R.drawable.ic_no_file_added)
                            .into(selectedImageView);
                    selectedImageView.setVisibility(View.VISIBLE);
                    imageTextView.setVisibility(View.VISIBLE);
                    addImage.setVisibility(View.GONE);
                    messageBody.setVisibility(View.GONE);
                    //Save to firebase
                } else {
                }
            });

    private final ActivityResultContracts.RequestMultiplePermissions multiplePermissionsContract = new ActivityResultContracts.RequestMultiplePermissions();

    private final ActivityResultLauncher<String[]> multiplePermissionLauncher = registerForActivityResult(multiplePermissionsContract, mapResult -> {
        var permissions = new ArrayList<>(mapResult.keySet());
        boolean areAllPermissionsGranted = true;
        var grantedList = new ArrayList<>(mapResult.values());

        if (grantedList.contains(false)){
            for (int i = 0; i < permissions.size(); i++) {
                if (!shouldShowRequestPermissionRationale(permissions.get(i))) {
                    areAllPermissionsGranted = false;
                }
            }
        }else{
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        }

        if (!areAllPermissionsGranted) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            Toast.makeText(this, getString(R.string.grant_permissions_for_photos), Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }
    });

//    @Override
//    protected void onStop() {
//        rootRef.removeEventListener(seenLisenter);
//        super.onStop();
//    }
//
//    @Override
//    public void onBackPressed() {
//        rootRef.removeEventListener(seenLisenter);
//        super.onBackPressed();
//
//    }
//
//
//
//    @Override
//    protected void onPause() {
//        rootRef.removeEventListener(seenLisenter);
//        super.onPause();
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        rootRef.removeEventListener(seenLisenter);
//        super.onDestroy();
//    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);
        initializeViews();
        mAuth = FirebaseAuth.getInstance();
        senderID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        Bundle extras = getIntent().getExtras();
        if (!(extras == null)) {
            User receiver = (User) extras.getSerializable("USER");
            String recieverIDFromNoti = extras.getString("USERID");
            if (recieverIDFromNoti != null) {
                receiverID = recieverIDFromNoti;
                retrieveMessages();
            }
            if (receiver != null) {
                receiverID = receiver.getUserID();
                setUserDetails(receiver);
                retrieveMessages();
            }

        } else {
            displayErrorAlert();
        }

        onOverPullListener = (decor, oldState, newState) -> {
            if(oldState== 1){
                scrollFromTop=true;
            }
            if (newState == 0 && scrollFromTop) {
                scrollFromTop=false;
                getMoreMessages();
            }
        };
        sendMessage.setOnClickListener(v -> sendMessageToUser());
        addImage.setOnClickListener(v -> showImagePickDialog());
    }

    private void getMoreMessages() {
        ArrayList<Messages> paginatedMessages  = new ArrayList<>();
        rootRef.child(Config.messages)
                .child(senderID)
                .child(receiverID)
                .orderByKey()
                .limitToLast(MESSAGE_PAGINATION_AMOUNT)
                .endBefore(firstMessageID)
                .get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                int count=0;
                for (DataSnapshot dataSnapshot
                        : task.getResult().getChildren()) {
                    if(count==0){
                        firstMessageID = dataSnapshot.getKey();
                    }
                    Messages message = dataSnapshot.getValue(Messages.class);
                    if (message.getType().equals("text")) {
                        message.setText(message.getText());
                    }
                    paginatedMessages.add(message);
                    count++;
                }
                messagesArrayList.addAll(0,paginatedMessages);
                messagesAdapter.notifyItemRangeInserted(0 , count);
                chattingRecycler.smoothScrollToPosition(chattingRecycler.getAdapter().getItemCount());

            }
        });

    }

    private void initializeViews() {
        Toolbar chattingToolbar = findViewById(R.id.chat_toolbar);
        sendMessage = findViewById(R.id.send_message_button);
        selectedImageView = findViewById(R.id.selected_image_view);
        imageTextView  = findViewById(R.id.image_text_view);
        addImage = findViewById(R.id.choose_file_button);
        messageBody = findViewById(R.id.messeg_body_edit_text);
        chattingRecycler = findViewById(R.id.recycler_view_group_chatting);
        receiverUsername = chattingToolbar.findViewById(R.id.receiver_username);
        receiverProfileImage = chattingToolbar.findViewById(R.id.receiver_image_profile);

        setSupportActionBar(chattingToolbar);
        chattingToolbar.setNavigationIcon(R.drawable.ic_back_button);
        chattingToolbar.setNavigationOnClickListener(view1 -> {
            onBackPressed();
        });

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        chattingRecycler.setLayoutManager(linearLayoutManager);
        chattingRecycler.setHasFixedSize(true);

        chattingRecyclerViewDecor = OverScrollDecoratorHelper.setUpOverScroll(chattingRecycler, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
    }

    private void sendMessageToUser() {
        String encryptedMessage;
        final String messageText = messageBody.getText().toString();
        if (chosenImage != null) {
            sendImageMessage(chosenImage);
            return;
        }
        if (TextUtils.isEmpty(messageText.trim())) {
            //todo remove the send button if the edit
            // text is empty and only show when text is entered
            Toast.makeText(getApplicationContext(), "please enter a message", Toast.LENGTH_LONG).show();
        } else {
            messageBody.setText("");

            // referencing to database which user is send message to whom
            String messageSenderRef = Config.messages + "/" + senderID + "/" + receiverID;
            String messageReceiverRef = Config.messages + "/" + receiverID + "/" + senderID;
            //create the database ref
            final String messagePushId = rootRef.child(Config.messages).child(senderID).child(receiverID).push().getKey();
            //now making a unique id for each single message so that they wont be replaced and we save everything
//            String messagePushId=userMessageKey.getKey();
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calendarDate.getTime());
            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = currentTime.format(calendarTime.getTime());
            //create maps for  the sender and the reciever
            // the only differance is that the sender's
            //isSeen value will bre true

            Map recieverMessageTextBody = new HashMap();
            recieverMessageTextBody.put("message", messageText);
            recieverMessageTextBody.put("time", saveCurrentTime);
            recieverMessageTextBody.put("date", saveCurrentDate);
            recieverMessageTextBody.put("type", "text");
            recieverMessageTextBody.put("from", senderID);
            recieverMessageTextBody.put("isSeen", false);
            Map senderMessageTextBody = new HashMap();
            senderMessageTextBody.put("message", messageText);
            senderMessageTextBody.put("time", saveCurrentTime);
            senderMessageTextBody.put("date", saveCurrentDate);
            senderMessageTextBody.put("type", "text");
            senderMessageTextBody.put("from", senderID);
            senderMessageTextBody.put("isSeen", true);

            Map messageBodyDetails = new HashMap();

            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, senderMessageTextBody);

            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, recieverMessageTextBody);
            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    chattingRecycler.smoothScrollToPosition(messagesAdapter.getItemCount());
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(getApplicationContext(), "Error " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void retrieveMessages() {
        messagesArrayList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(messagesArrayList, this);
        chattingRecycler.setAdapter(messagesAdapter);
        //listener will retrieve only the last messages
        //
        rootRef.child(Config.messages)
                .child(senderID)
                .child(receiverID)
                .orderByKey()
                .limitToLast(MESSAGE_PAGINATION_AMOUNT)
                .get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d("retrieveMessages" , "message retrieved");
                        messagesArrayList.clear();
                        int count= 0;
                        String lastMessageID = null;
                        for (DataSnapshot dataSnapshot
                                : task.getResult().getChildren()) {
                            lastMessageID = dataSnapshot.getKey();
                            //get the the for the first message in the snapshot
                            //which is the last message in the queue
                            if(count==0){
                                firstMessageID = dataSnapshot.getKey();
                            }
                            Messages message = dataSnapshot.getValue(Messages.class);
                            message.setMessageId(dataSnapshot.getKey());
                            if (message.getType().equals("text")) {
                                message.setText(message.getText());
                            }
                            messagesArrayList.add(message);
                            count++;
                        }
                        messagesAdapter.notifyDataSetChanged();

                        chattingRecycler.smoothScrollToPosition(chattingRecycler.getAdapter().getItemCount());

                        chattingRecyclerViewDecor.setOverScrollStateListener(onOverPullListener);
                        //todo handle error if last message id is null
                        listenForNewMessages(lastMessageID);

                    }
                }).addOnFailureListener(e -> Log.d("retrieveMessages" , e.getMessage()));
    }

    private void listenForNewMessages(String lastMessageID){
        Query query = rootRef.child(Config.messages)
                .child(senderID)
                .child(receiverID)
                .orderByKey()
                .limitToLast(1);
        if(lastMessageID!=null){
            query = query.startAfter(lastMessageID);
        }
        query.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if(snapshot.exists()){
                            Log.d("snapshot child event listener" , snapshot.toString());
                            Messages message = snapshot.getValue(Messages.class);
                            message.setMessageId(snapshot.getKey());
                            if (message.getType().equals("text")) {
                                message.setText(message.getText());
                            }else{
                                //todo add implementation for  images
                            }
                            if (!isMessageRepeating(message)){
                                messagesArrayList.add(message);
                                messagesAdapter.notifyItemInserted(chattingRecycler.getAdapter().getItemCount());
                                chattingRecycler.smoothScrollToPosition(chattingRecycler.getAdapter().getItemCount());
                            }
                        }

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void showImagePickDialog() {
        String[] options = {getString(R.string.gallery)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.get_image_from));
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (!PermissionManager.INSTANCE.checkStoragePermissions(this)) {
                    multiplePermissionLauncher.launch(PermissionManager.INSTANCE.getStoragePermissions());
                } else {
                    pickMedia.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                }
            }
        });
        builder.create().show();
    }

    private boolean isMessageRepeating(Messages newMessage){
        AtomicBoolean bool = new AtomicBoolean(false);
        messagesArrayList.forEach(message -> {
            if (message.messageId.equals(newMessage.messageId)) {
                bool.set(true);
            }
        });
        return bool.get();
    }

    private void sendImageMessage(Uri image) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.sending_image));
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        //load the image as a bitmap
        Bitmap bitmap = null;

        if (Build.VERSION.SDK_INT >= 29) {
            ImageDecoder.Source source = ImageDecoder.createSource(getApplicationContext().getContentResolver(), image);
            try {
                bitmap = ImageDecoder.decodeBitmap(source);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();

        StorageReference filePathName = storageReference.child(Config.privateChatImages).child(image.getLastPathSegment()
                + System.currentTimeMillis());


        filePathName.putBytes(byteArray).addOnCompleteListener(task -> {
            chosenImage=null;
            if (task.isSuccessful()) {
                progressDialog.dismiss();
                imageUrl = task.getResult().getMetadata().getReference().getPath();
                selectedImageView.setVisibility(View.GONE);
                imageTextView.setVisibility(View.GONE);
                addImage.setVisibility(View.VISIBLE);
                messageBody.setVisibility(View.VISIBLE);
                addToRealTimeDataBase(imageUrl);
            }
        }).addOnFailureListener(e -> {
            selectedImageView.setVisibility(View.GONE);
            imageTextView.setVisibility(View.GONE);
            addImage.setVisibility(View.VISIBLE);
            messageBody.setVisibility(View.VISIBLE);
            progressDialog.dismiss();
            e.printStackTrace();
        });

    }

    private void addToRealTimeDataBase(String url) {
        String messageSenderRef = Config.messages + "/" + senderID + "/" + receiverID;
        String messageReceiverRef = Config.messages + "/" + receiverID + "/" + senderID;
        //create the database ref
        final String messagePushId = rootRef.child(Config.messages).child(senderID).child(receiverID).push().getKey();
        //now making a unique id for each single message so that they wont be replaced and we save everything
//            String messagePushId=userMessageKey.getKey();
        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calendarDate.getTime());
        Calendar calendarTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
        saveCurrentTime = currentTime.format(calendarTime.getTime());
        Map messageTextBody = new HashMap();
        messageTextBody.put("messageId", messagePushId);
        messageTextBody.put("message", url);
        messageTextBody.put("time", saveCurrentTime);
        messageTextBody.put("date", saveCurrentDate);
        messageTextBody.put("type", "image");
        messageTextBody.put("from", senderID);
        messageTextBody.put("isSeen", false);
        Map messageBodyDetails = new HashMap();
        Log.i("this is message ", messageBodyDetails.toString());
        messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
        messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);
        rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (!task.isSuccessful()) {
                    String message = task.getException().getMessage();
                    Toast.makeText(getApplicationContext(), "Error " + message, Toast.LENGTH_SHORT).show();
                }
                messageBody.setText("");
            }
        });

    }

    private void setUserDetails(final User receiver) {
        if (receiver.getImage() != null && !receiver.getImage().isEmpty()) {
            GlideApp.with(getApplicationContext())
                    .load(receiver.getImage())
                    .fitCenter()
                    .circleCrop()
                    .into(receiverProfileImage);
            receiverProfileImage.setPadding(0, 0, 0, 0);
        }
        receiverUsername.setText(receiver.getUsername());
        messageSeen(receiver.getUserID());
    }

    private void messageSeen(final String receiverID) {
        rootRef.child(Config.messages)
                .child(senderID)
                .child(receiverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot sp : snapshot.getChildren()) {
                                Messages messages = sp.getValue(Messages.class);
                                if (messages != null) {
                                    if (messages.getFrom().equals(senderID) || messages.getFrom().equals(receiverID)) {
                                        HashMap<String, Object> hash = new HashMap<>();
                                        hash.put("isSeen", true);
                                        sp.getRef().updateChildren(hash);
                                    }
                                }
                            }
                            MainActivity.setBadgeToNumberOfNotifications(rootRef, mAuth);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void displayErrorAlert() {
        new android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.attention))
                .setMessage(getString(R.string.network_error))
                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .setOnDismissListener(dialogInterface -> {
                    onBackPressed();
                }).create()
                .show();
    }
}
