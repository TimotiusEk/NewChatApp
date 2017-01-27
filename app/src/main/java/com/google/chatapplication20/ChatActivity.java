package com.google.chatapplication20;

import android.*;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.attr.bitmap;
import static android.R.attr.text;
import static com.facebook.FacebookSdk.getApplicationContext;

public class ChatActivity extends AppCompatActivity {

    private String chatPartner;
    private ChatMessageAdapter chatMessageAdapter;
    private DatabaseReference mDatabase;
    public static int position = 0;
    public static boolean hasBelowUnreadMessage = false;
    private static int READ_EXTERNAL_STORAGE_PERMISSIONS_REQUEST = 111;
    private static int RESULT_LOAD_IMG = 1;
    private RelativeLayout textMsgLayout;
    private RelativeLayout pictureMsgLayout;
    private ListView listOfMessages;
    private FloatingActionButton sendImgFab;
    private ImageView showPictureMsg;
    private Bitmap bm;
    private ProgressBar downloadImageProgressBar;
    private boolean isPicUploaded = false;
    private LinearLayout notFriendLayout;
    private Button addFriendBtn;
    private Button blockFriendBtn;
    private RelativeLayout blockedUserLayout;
    private FloatingActionButton fab;
    private EditText input;
    private Button unblockBtn;
    private TextView showBlockedInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        textMsgLayout = (RelativeLayout) findViewById(R.id.text_message_layout);
        pictureMsgLayout = (RelativeLayout) findViewById(R.id.picture_message_layout);
        listOfMessages = (ListView) findViewById(R.id.list_of_messages);
        sendImgFab = (FloatingActionButton) findViewById(R.id.fab_picture_msg);
        showPictureMsg = (ImageView) findViewById(R.id.picture_message);
        downloadImageProgressBar = (ProgressBar) findViewById(R.id.download_image_progress_bar);
        notFriendLayout = (LinearLayout) findViewById(R.id.not_friend_button_layout);
        input = (EditText) findViewById(R.id.input);
        blockedUserLayout = (RelativeLayout) findViewById(R.id.blocked_user_layout);
        unblockBtn = (Button) findViewById(R.id.unblock_btn);
        showBlockedInfo = (TextView) findViewById(R.id.show_blocked_info);

        scrollMyListViewToBottom();

        hasBelowUnreadMessage = false;


        chatPartner = getIntent().getExtras().getString("email");

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setTitle(chatPartner);


        showAddFriendLayout(chatPartner);
        isItBlocked(chatPartner);

        displayChatMessages();

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                ChatMessage message = new ChatMessage(input.getText().toString(),
                        chatPartner
                        , FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getEmail(), false);

                String msg = input.getText().toString().trim();

                mDatabase = FirebaseDatabase.getInstance().getReference("ChatMessage");


                String messageId = mDatabase.push().getKey();
                if (!msg.matches("")) {
                    mDatabase.child(messageId).setValue(message);


                    // Clear the input
                    input.setText("");
                }
            }
        });


    }

    public void isItBlocked(final String email){
        mDatabase = FirebaseDatabase.getInstance().getReference("BlockedUser");
        Query isItBlockedQuery = mDatabase.orderByChild("userWhoBlock").equalTo(email);

        isItBlockedQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() != 0){
                    boolean isItBlocked = false;
                    for(DataSnapshot data : dataSnapshot.getChildren()) {
                        BlockedUser blockedUser = data.getValue(BlockedUser.class);
                        for(String emailToCompare : blockedUser.getBlockedUser()) {
                            if(emailToCompare.equalsIgnoreCase(FirebaseAuth.getInstance()
                                    .getCurrentUser()
                                    .getEmail())){
                                isItBlocked = true;
                            }
                        }

                    }

                    if(isItBlocked){
                        blockedUserLayout.setVisibility(View.VISIBLE);
                        notFriendLayout.setVisibility(View.GONE);
                        input.setEnabled(false);
                        fab.setEnabled(false);
                        listOfMessages.setEnabled(false);
                        showBlockedInfo.setText("You have been blocked by this user");
                    }
                }

                if(unblockBtn.getVisibility() == View.GONE){

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showAddFriendLayout(final String email) {
        /**
         * Ngecek FriendStatus
         */
        final boolean[] friendStatus = {false};
        mDatabase = FirebaseDatabase.getInstance().getReference("LastLoginUser");
        Query friendListQuery = mDatabase.orderByChild("userEmail").equalTo(FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail());

        friendListQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    LastLoginUser user = appleSnapshot.getValue(LastLoginUser.class);

                    if(user.getFriends() != null) {
                        for (String emailToCompare : user.getFriends()) {
                            if (emailToCompare.equalsIgnoreCase(email)) {
                                friendStatus[0] = true;
                            }
                        }
                    }
                }

                if (!friendStatus[0]) {
                    notFriendLayout.setVisibility(View.VISIBLE);
                    addFriendBtn = (Button) findViewById(R.id.add_friend_btn);
                    blockFriendBtn = (Button) findViewById(R.id.block_user_btn);

                    addFriendBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            checkThenAddFriend(email);
                        }
                    });

                    blockFriendBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            blockUser(email);
                        }
                    });
                }
                else{
                    notFriendLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /**
         * Ngecek BlockedStatus
         */

        final boolean[] blockedStatus = {false};
        mDatabase = FirebaseDatabase.getInstance().getReference("BlockedUser");
        final Query blockedListQuery = mDatabase.orderByChild("userWhoBlock").equalTo(FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail());

        blockedListQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    BlockedUser blockedUser = data.getValue(BlockedUser.class);

                    if(blockedUser != null && blockedUser.getBlockedUser() != null){
                        for(String emailToCompare : blockedUser.getBlockedUser()){
                            Log.d("emailToCompare", emailToCompare);
                            if (emailToCompare.equalsIgnoreCase(email)) {
                                blockedStatus[0] = true;
                            }
                        }
                    }
                }

                if (!blockedStatus[0]) {
                    addFriendBtn = (Button) findViewById(R.id.add_friend_btn);
                    blockFriendBtn = (Button) findViewById(R.id.block_user_btn);

                    addFriendBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            checkThenAddFriend(email);
                        }
                    });

                    blockFriendBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            blockUser(email);
                        }
                    });
                }
                else{
                    notFriendLayout.setVisibility(View.GONE);
                    input.setEnabled(false);
                    fab.setEnabled(false);
                    listOfMessages.setEnabled(false);

                    blockedUserLayout.setVisibility(View.VISIBLE);
                    unblockBtn.setVisibility(View.VISIBLE);
                    unblockBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            /**
                             * Todo : buat method unblock
                             */

                            blockedListQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    ArrayList<String> blockedUserAl = null;
                                    String blockedUserId = "";
                                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                                        BlockedUser blockedUser = data.getValue(BlockedUser.class);
                                        blockedUserAl = blockedUser.getBlockedUser();
                                        blockedUserId = data.getKey();

                                        if(blockedUserAl != null) {
                                            for (int a = 0; a < blockedUserAl.size(); a++) {
                                                if(chatPartner.equalsIgnoreCase(blockedUserAl.get(a))){
                                                    blockedUserAl.remove(a);
                                                }
                                            }

                                            if(blockedUserAl.size() != 0) {
                                                data.getRef().child("blockedUser").setValue(blockedUserAl);
                                            }
                                            else{
                                               data.getRef().removeValue();
                                            }

                                            Intent intent = getIntent();
                                            finish();
                                            startActivity(intent);

                                        }
                                    }


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void blockUser(final String email){
        mDatabase = FirebaseDatabase.getInstance().getReference("BlockedUser");
        final Query blockedUserQuery = mDatabase.orderByChild("userWhoBlock").equalTo(FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail());
        final ArrayList<String> blockedUserAl = new ArrayList<>();
        final String blockedUserId = mDatabase.push().getKey();

        Log.d("blockedId",blockedUserQuery.getRef().getKey());


        blockedUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 0){
                    Log.d("masuk", "true");
                    blockedUserAl.add(email);

                    mDatabase.child(blockedUserId).setValue(new BlockedUser(FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getEmail(), blockedUserAl));
                }
                else{
                    for(DataSnapshot data : dataSnapshot.getChildren()){
                        BlockedUser blockedUser = data.getValue(BlockedUser.class);
                        final ArrayList<String> blockedUserAl = blockedUser.getBlockedUser();
                        if(blockedUserAl != null) {
                            blockedUserAl.add(email);
                            mDatabase.child(data.getKey()).child("blockedUser").setValue(blockedUserAl);
                        }

                    }
                }

                notFriendLayout.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkThenAddFriend(final String email) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LastLoginUser");

        final Query applesQuery = ref.orderByChild("userEmail").equalTo(FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail());

        applesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> friends = null;
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    LastLoginUser user = appleSnapshot.getValue(LastLoginUser.class);
                    friends = user.getFriends();
                }
                if (friends == null) {
                    initializeFirstFriend(email);
                } else {
                    addFriend(email);
                }

                applesQuery.removeEventListener(this);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initializeFirstFriend(final String email) {

        final ArrayList<String> friends = new ArrayList<>();
        friends.add(email);

        final ArrayList<String> toCheckFriends = new ArrayList<>();


        LastLoginUser user = new LastLoginUser(
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getEmail(), friends);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LastLoginUser");
        final Query applesQuery = ref.orderByChild("userEmail").equalTo(FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail());

        final String[] userId = {null};

        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    userId[0] = appleSnapshot.getKey();
                }
                mDatabase = FirebaseDatabase.getInstance().getReference("LastLoginUser");

                mDatabase.child(userId[0]).child("friends").setValue(friends);

                updateFriendRequest(email, ref);
                updateFriendRequestStatus(email);

                applesQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

    public void updateFriendRequest(final String email, DatabaseReference ref) {
        final Query checkFriendsQuery = ref.orderByChild("userEmail").equalTo(email);

        final boolean[] alreadyFriend_ = new boolean[1];


        checkFriendsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    LastLoginUser user = data.getValue(LastLoginUser.class);
                    if (user.getFriends() != null) {
                        for (String loginUser : user.getFriends()) {
                            if (loginUser.equalsIgnoreCase(FirebaseAuth.getInstance()
                                    .getCurrentUser()
                                    .getEmail())) {
                                alreadyFriend_[0] = true;
                            }
                        }
                    }
                }
                if (!alreadyFriend_[0]) {


                    final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("FriendRequest");

                    Query findFriendRequest = ref.orderByChild("friendRequestReceiver").equalTo(email);

                    findFriendRequest.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            FriendRequest friendRequest = new FriendRequest(email, FirebaseAuth.getInstance()
                                    .getCurrentUser()
                                    .getEmail());

                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                FriendRequest friendRequestData = data.getValue(FriendRequest.class);

                                if (data.getChildrenCount() > 0) {
                                    if (friendRequestData.getFriendRequestSender().equalsIgnoreCase(FirebaseAuth.getInstance()
                                            .getCurrentUser()
                                            .getEmail()) && friendRequestData.getRequestTime() != friendRequest.getRequestTime()) {
                                        data.getRef().removeValue();
                                    }
                                }
                            }
                            String objId = ref.push().getKey();

                            ref.child(objId).setValue(friendRequest);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void addFriend(final String email) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LastLoginUser");
        final Query applesQuery = ref.orderByChild("userEmail").equalTo(FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail());

        applesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> friendsArrayList;


                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {

                    LastLoginUser user = appleSnapshot.getValue(LastLoginUser.class);

                    friendsArrayList = user.getFriends();

                    if (friendsArrayList != null) {
                        friendsArrayList.add(email);

                        appleSnapshot.getRef().child("friends").setValue(friendsArrayList);

                        updateFriendRequest(email, ref);
                        updateFriendRequestStatus(email);

                    }

                }
                applesQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    public void updateFriendRequestStatus(final String email) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("FriendRequest");

        final Query updateRequestQuery = ref.orderByChild("friendRequestSender").equalTo(email);

        updateRequestQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    FriendRequest friendRequest = data.getValue(FriendRequest.class);

                    if (friendRequest.getFriendRequestSender().equalsIgnoreCase(email) && friendRequest.getFriendRequestReceiver().equalsIgnoreCase(FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getEmail())) {
                        data.getRef().child("accepted").setValue(true);
                        data.getRef().child("answered").setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void scrollMyListViewToBottom() {

        listOfMessages.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                Log.d("adapterChatMsg", String.valueOf(chatMessageAdapter.getCount()));
                listOfMessages.setSelection(chatMessageAdapter.getCount() - 1);
            }
        });
    }


    private void updateToRead() {
        mDatabase = FirebaseDatabase.getInstance().getReference("ChatMessage");
        final Query updateQuery = mDatabase.orderByChild("messageReceiver").equalTo(FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail());
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {


                    final ChatMessage chatMessage = messageSnapshot.getValue(ChatMessage.class);

                    if (chatMessage.getMessageReceiver() != null && chatPartner != null && chatMessage.getMessageSender() != null) {
                        if ((chatMessage.getMessageReceiver().equalsIgnoreCase(chatPartner) && chatMessage.getMessageSender().equalsIgnoreCase(FirebaseAuth.getInstance()
                                .getCurrentUser()
                                .getEmail())) || (chatMessage.getMessageReceiver().equalsIgnoreCase(FirebaseAuth.getInstance()
                                .getCurrentUser()
                                .getEmail()) && chatMessage.getMessageSender().equalsIgnoreCase(chatPartner))) {

                            updateQuery.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                                        if (chatPartner.equalsIgnoreCase((String) data.child("messageSender").getValue())) {
                                            data.getRef().child("messageRead").setValue(true);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void displayChatMessages() {
        final EditText chatEditText = (EditText) findViewById(R.id.input);


        mDatabase = FirebaseDatabase.getInstance().getReference("ChatMessage");

        if (!chatPartner.equalsIgnoreCase(FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail())) {
            chatEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateToRead();
                }
            });
        } else {
            updateToRead();
        }


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<ChatMessage> chatMessageArrayList = new ArrayList<ChatMessage>();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {

                    ChatMessage chatMessage = messageSnapshot.getValue(ChatMessage.class);

                    if (chatMessage.getMessageReceiver() != null && chatPartner != null && chatMessage.getMessageSender() != null) {
                        if ((chatMessage.getMessageReceiver().equalsIgnoreCase(chatPartner) && chatMessage.getMessageSender().equalsIgnoreCase(FirebaseAuth.getInstance()
                                .getCurrentUser()
                                .getEmail())) || (chatMessage.getMessageReceiver().equalsIgnoreCase(FirebaseAuth.getInstance()
                                .getCurrentUser()
                                .getEmail()) && chatMessage.getMessageSender().equalsIgnoreCase(chatPartner))) {

                            chatMessageArrayList.add(chatMessage);
                        }
                    }
                }


                chatMessageAdapter = new ChatMessageAdapter(ChatActivity.this, R.layout.message, chatMessageArrayList, chatPartner);


                listOfMessages.setAdapter(chatMessageAdapter);
                registerForContextMenu(listOfMessages);

                listOfMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        ChatMessage chatMessage = (ChatMessage) adapterView.getItemAtPosition(i);
                        RelativeLayout pictureMsgLayout = (RelativeLayout) findViewById(R.id.picture_message_layout);

                        ImageView pictureMsg = (ImageView) findViewById(R.id.picture_message);
                        if (chatMessage.isPicture()) {
                            pictureMsgLayout.setVisibility(View.VISIBLE);
                            textMsgLayout.setVisibility(View.GONE);
                            try {
                                Bitmap imageBitmap = decodeFromFirebaseBase64(chatMessage.getMessageText());
                                pictureMsg.setImageBitmap(imageBitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sendImgFab.setVisibility(View.GONE);
                            invalidateOptionsMenu();
                        }
                    }
                });


                if (hasBelowUnreadMessage) {
                    listOfMessages.setSelectionFromTop(position, 0);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ChatMessage chatMessage = (ChatMessage) listOfMessages.getItemAtPosition(info.position);
        if (v.getId() == R.id.list_of_messages && !chatMessage.isPicture()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.copy_text:
                ChatMessage chatMessage = (ChatMessage) listOfMessages.getItemAtPosition(info.position);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", chatMessage.getMessageText());
                clipboard.setPrimaryClip(clip);
                // add stuff here
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ChatActivity.this,
                                    "You have been signed out.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            // Close Application
                            finishAffinity();
                        }
                    });
        } else if (item.getItemId() == R.id.menu_show_friends) {
            Intent intent = new Intent(this, ShowFriendActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.menu_find_friends) {
            Intent intent = new Intent(this, FindFriendActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.menu_upload_photo) {
            if(blockedUserLayout.getVisibility() == View.VISIBLE){
                Toast.makeText(this, "Unblock to Send Picture", Toast.LENGTH_SHORT).show();
            }
            else {
                uploadPhoto();
            }
        } else if (item.getItemId() == R.id.menu_save_photo) {
            isStoragePermissionGranted();

            new SaveImageTask().execute(null, null, null);
        }
        return true;
    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public void downloadPhoto() {

        showPictureMsg.setDrawingCacheEnabled(true);
        bm = showPictureMsg.getDrawingCache();
        File root = new File(Environment.getExternalStorageDirectory()
                + File.separator + "ChatApplication" + File.separator);
        OutputStream fOut = null;
        Uri outputFileUri;
        root.mkdirs();
        Long pictureTime = new Date().getTime();

        File sdImageMainDirectory = new File(root, String.valueOf(pictureTime) + ".jpg");
        try {
            outputFileUri = Uri.fromFile(sdImageMainDirectory);
            fOut = new FileOutputStream(sdImageMainDirectory);

            Log.d("savePhoto", "Success");
        } catch (Exception e) {
            Toast.makeText(ChatActivity.this, "Error occured. Please try again later.",
                    Toast.LENGTH_SHORT).show();
        }
        try {
            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            addImageToGallery(sdImageMainDirectory.getAbsolutePath(), ChatActivity.this);

        } catch (Exception e) {
        }
    }

    public class SaveImageTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            downloadPhoto();


//            File root = Environment.getExternalStorageDirectory();
//            File cachePath = new File(root.getAbsolutePath() + "/DCIM/Camera/image.jpg");
//            try {
//                cachePath.createNewFile();
//                FileOutputStream ostream = new FileOutputStream(cachePath);
//                bm.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
//                ostream.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }


            // Save your image here.
            return null;
        }

        @Override
        protected void onPreExecute() {
            downloadImageProgressBar.setVisibility(View.VISIBLE);
            // Show your progress bar here.
        }

        @Override
        protected void onPostExecute(Void v) {
            downloadImageProgressBar.setVisibility(View.GONE);
            Toast.makeText(ChatActivity.this, "Picture Saved", Toast.LENGTH_SHORT).show();
            // Hide the progress bar here.
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final String imgDecodableString;

        try {

            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data) {

                textMsgLayout.setVisibility(View.GONE);
                pictureMsgLayout.setVisibility(View.VISIBLE);

                ImageView pictureView = (ImageView) findViewById(R.id.picture_message);

                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                // Move to first row
                if (cursor != null) {
                    cursor.moveToFirst();
                }

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                // Set the Image in ImageView after decoding the String
                final Bitmap photo = BitmapFactory.decodeFile(imgDecodableString);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] byteArray = baos.toByteArray();
                final String imageEncoded = Base64.encodeToString(byteArray, Base64.DEFAULT);


                pictureView.setImageBitmap(Bitmap.createScaledBitmap(photo, photo.getWidth(), photo.getHeight(), false));

                sendImgFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ChatMessage pictureMsg = new ChatMessage(imageEncoded, chatPartner
                                , FirebaseAuth.getInstance()
                                .getCurrentUser()
                                .getEmail(), true);


                        Log.d("masuk", "true");
                        sendImgFab.setClickable(false);
                        textMsgLayout.setVisibility(View.VISIBLE);
                        pictureMsgLayout.setVisibility(View.GONE);

                        mDatabase = FirebaseDatabase.getInstance().getReference("ChatMessage");


                        String messageId = mDatabase.push().getKey();
                        mDatabase.child(messageId).setValue(pictureMsg).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ChatActivity.this, "Picture has been sent", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatActivity.this, "Failed to sent picture", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                isPicUploaded = false;
                                sendImgFab.setClickable(true);
                            }
                        });
                    }
                });


            }


        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            Log.d("PickGallery", e.getMessage());
        }

        displayChatMessages();
    }

    private void checkForReadStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int locPermit = this.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);

            //now check if permission is granted
            if (locPermit == PackageManager.PERMISSION_GRANTED) {
                Log.d("BarengStorage", "Read Storage enabled. You can access external storage!");
            } else {
                Log.d("BarengStorage", "Read Storage need permission.");
                //permission is not granted.
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.d("Permission", "Permission is being asked(?)");
                    //Snackbar.make(this, "Location permission is needed in order to use the location finder.", 1000).show();
                }

                //request for fine location
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSIONS_REQUEST);
            }
        } else {

            //should not be invoking anything for now. This works for Marshmallow and below
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {

                return true;
            } else {


                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    public void uploadPhoto() {
        //check first if they have read external storage permission
        checkForReadStoragePermission();

        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    public void onBackPressed() {
        if (pictureMsgLayout.getVisibility() == View.GONE) {
            updateToRead();
            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(i);
        } else {
            pictureMsgLayout.setVisibility(View.GONE);
            textMsgLayout.setVisibility(View.VISIBLE);
        }
        sendImgFab.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (pictureMsgLayout.getVisibility() == View.GONE) {
            getMenuInflater().inflate(R.menu.menu_chat_activity, menu);
        } else {
            getMenuInflater().inflate(R.menu.show_photo_menu, menu);
        }

        return true;

    }


}
