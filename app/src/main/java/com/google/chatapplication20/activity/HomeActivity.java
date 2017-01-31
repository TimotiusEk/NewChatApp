package com.google.chatapplication20.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.chatapplication20.adapter.LastLoginUserAdapter;
import com.google.chatapplication20.R;
import com.google.chatapplication20.model.ChatMessage;
import com.google.chatapplication20.model.FriendRequest;
import com.google.chatapplication20.model.GroupChat;
import com.google.chatapplication20.model.LastLoginUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;

public class HomeActivity extends AppCompatActivity {
    private static final int SIGN_IN_REQUEST_CODE = 10;
    private LastLoginUser user;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseGroup;
    private TextView showNoChat;
    private int countFriendRequest;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Intent intent = getIntent();
        String goToFriendRequest = intent.getStringExtra("goToFriendRequest");



        if(goToFriendRequest != null){
            Log.d("goToFriendRequest", goToFriendRequest);
            Intent i = new Intent(this, FriendRequestActivity.class);
            startActivity(i);
        }


        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setTitle("Chat List");

        showNoChat = (TextView) findViewById(R.id.show_no_chat);
        FloatingActionButton fabNewChat = (FloatingActionButton) findViewById(R.id.fab_new_chat);


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            );
        } else {
            progressDialog = new ProgressDialog(HomeActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Loading ..");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            updateRecentUser();
            invalidateOptionsMenu();

        }
        fabNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ShowFriendActivity.class);
                startActivity(intent);
            }
        });



        /**
         * TODO : Ini query yg lebih ter-organized. Implementasi ke semua.
         */


//        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("ChatMessage");
//
//        mDatabase.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
//                    ChatMessage chatMessage = messageSnapshot.getValue(ChatMessage.class);
//
//                    Log.d("chatMessageText", chatMessage.getMessageText());
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        String userId = mDatabase.push().getKey();
//
//        ChatMessage chatMessage = new ChatMessage("asd", "asd", "asd");
//
//        mDatabase.child(userId).setValue(chatMessage);

        // http://www.androidhive.info/2016/10/android-working-with-firebase-realtime-database/


    }


    public void updateRecentUser() {

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LastLoginUser");
        final Query applesQuery = ref.orderByChild("userEmail").equalTo(FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail());


        /**
         * Listener for Single Value Event bisa dipake jg buat ngecek datanya ada atau ga.
         */
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() == 0) {
                    // run some code
                    user = new LastLoginUser(
                            FirebaseAuth.getInstance()
                                    .getCurrentUser()
                                    .getEmail());


                    mDatabase = FirebaseDatabase.getInstance().getReference("LastLoginUser");

                    String userId = mDatabase.push().getKey();

                    mDatabase.child(userId).setValue(user);

                } else {
                    applesQuery.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                                ArrayList<String> friends = null;
                                for (DataSnapshot data : dataSnapshot.getChildren()) {
                                    LastLoginUser user = data.getValue(LastLoginUser.class);
                                    friends = user.getFriends();
                                }
                                if (friends == null) {
                                    user = new LastLoginUser(
                                            FirebaseAuth.getInstance()
                                                    .getCurrentUser()
                                                    .getEmail());

                                } else {
                                    user = new LastLoginUser(
                                            FirebaseAuth.getInstance()
                                                    .getCurrentUser()
                                                    .getEmail(), friends);

                                }
                                mDatabase = FirebaseDatabase.getInstance().getReference("LastLoginUser");

                                String userId = mDatabase.push().getKey();

                                mDatabase.child(userId).setValue(user);

                            }
                            ref.removeEventListener(this);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }

                    });


                    applesQuery.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                                if (user.getUserLastLoginTime() > (long) appleSnapshot.child("userLastLoginTime").getValue()) {
                                    appleSnapshot.getRef().removeValue();
                                }
                            }
//                ref.removeEventListener(this);
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

        getRecentMessage();

    }


    /**
     * ToDo :Cari cara Buat delete ArrayList dari firebase (delete friend)
     */


    public void getRecentMessage() {
        DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference("LastLoginUser");
        Log.d("mDatabaseUser", String.valueOf(mDatabaseUser.getRef()));
        mDatabaseUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<LastLoginUser> loginUserArrayList = new ArrayList<LastLoginUser>();
                final ArrayList<ChatMessage> chatMessageArrayList = new ArrayList<ChatMessage>();
                ArrayList<String> friendArrayList = new ArrayList<>();
                final ArrayList<LastLoginUser> loginUsersFriend = new ArrayList<LastLoginUser>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    LastLoginUser user = data.getValue(LastLoginUser.class);
                    if (user.getUserEmail().equalsIgnoreCase(FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getEmail())) {
                        friendArrayList = user.getFriends();
                    }
                    loginUserArrayList.add(user);
                }

                if(friendArrayList != null) {
                    for (String email : friendArrayList) {
                        for (LastLoginUser user : loginUserArrayList) {
                            if (user.getUserEmail().equalsIgnoreCase(email)) {
                                loginUsersFriend.add(user);
                            }
                        }
                    }
                }

                Log.d("loginUserFriend", String.valueOf(loginUsersFriend.size()));
                mDatabase = FirebaseDatabase.getInstance().getReference("ChatMessage");
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                            ChatMessage chatMessage = messageSnapshot.getValue(ChatMessage.class);
                            if(chatMessage.getMessageTime() != 0) {
                                chatMessageArrayList.add(chatMessage);
                            }
                        }


                        for (int a = 0; a < chatMessageArrayList.size(); a++) {
                            for (int b = 0; b < loginUserArrayList.size(); b++) {
                                if ((chatMessageArrayList.get(a).getMessageSender().equalsIgnoreCase(loginUserArrayList.get(b).getUserEmail()) && chatMessageArrayList.get(a).getMessageReceiver().equalsIgnoreCase(FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getEmail())) || (chatMessageArrayList.get(a).getMessageSender().equalsIgnoreCase(FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getEmail()) && chatMessageArrayList.get(a).getMessageReceiver().equalsIgnoreCase(loginUserArrayList.get(b).getUserEmail()))) { //Mau nangkep data message yg receiver dan sender nya diri kita sendiri

                                    if (!chatMessageArrayList.get(a).isMessageRead() && chatMessageArrayList.get(a).getMessageSender().equalsIgnoreCase(loginUserArrayList.get(b).getUserEmail())) {
                                        loginUserArrayList.get(b).setUnreadMessage(loginUserArrayList.get(b).getUnreadMessage() + 1);

                                    }


                                    if (loginUserArrayList.get(b).getLastMessageTime() == 0 || loginUserArrayList.get(b).getLastMessageTime() < chatMessageArrayList.get(a).getMessageTime()) {
                                        loginUserArrayList.get(b).setLastMessage(chatMessageArrayList.get(a).getMessageText());
                                        loginUserArrayList.get(b).setLastMessageTime(chatMessageArrayList.get(a).getMessageTime());
                                        loginUserArrayList.get(b).setLastMessagePicture(chatMessageArrayList.get(a).isPicture());
                                    }



                                }

                            }


                        }



                        final ListView listOfMessages = (ListView) findViewById(R.id.list_of_users_and_messages);


                        listOfMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                LastLoginUser user = (LastLoginUser) listOfMessages.getItemAtPosition(i);

                                Intent mIntent = new Intent(HomeActivity.this, ChatActivity.class);
                                Bundle mBundle = new Bundle();
                                mBundle.putString("email", user.getUserEmail());
                                if(user.getUserEmail().length()>=6) {
                                    if(user.getUserEmail().substring(user.getUserEmail().length() - 6, user.getUserEmail().length() - 1).equalsIgnoreCase("Group")) {
                                        mBundle.putBoolean("isGroup", true);
                                    }
                                    else{
                                        mBundle.putBoolean("isGroup", false);
                                    }
                                }
                                mIntent.putExtras(mBundle);
                                startActivity(mIntent);
                            }
                        });


                        for(int a = 0 ; a < loginUserArrayList.size() ; a++){
                            if(loginUserArrayList.get(a).getLastMessageTime() == 0){
                                loginUserArrayList.remove(a);
                            }
                        }

                        for(int a = 0 ; a < loginUserArrayList.size() ; a++){
                            if(loginUserArrayList.get(a).getLastMessageTime() == 0){
                                loginUserArrayList.remove(a);
                            }
                        }

                      mDatabaseGroup = FirebaseDatabase.getInstance().getReference("GroupChat");
                        mDatabaseGroup.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot data : dataSnapshot.getChildren()){
                                    GroupChat groupChat = data.getValue(GroupChat.class);
                                    for(String member : groupChat.getGroupChatMember()){
                                        if(member.equals(FirebaseAuth.getInstance()
                                                .getCurrentUser()
                                                .getEmail())){
                                            loginUserArrayList.add(new LastLoginUser(groupChat.getGroupChatName() + " (Group)"));
                                            if(groupChat.getGroupChatMessages() != null) {
                                                for (ChatMessage ca : groupChat.getGroupChatMessages()) {
                                                    loginUserArrayList.get(loginUserArrayList.size() - 1).setLastMessage(ca.getMessageText());
                                                    loginUserArrayList.get(loginUserArrayList.size() - 1).setLastMessageTime(ca.getMessageTime());
                                                    loginUserArrayList.get(loginUserArrayList.size() - 1).setLastMessagePicture(ca.isPicture());
                                                }
                                            }
                                        }
                                    }
                                }


                                if (loginUserArrayList.size() != 0) {
                                    listOfMessages.setVisibility(View.VISIBLE);
                                    showNoChat.setVisibility(View.GONE);
                                    Collections.sort(loginUserArrayList, LastLoginUser.RecentChatComparator);
                                    final LastLoginUserAdapter customAdapter = new LastLoginUserAdapter(HomeActivity.this, R.layout.last_login, loginUserArrayList, FirebaseAuth.getInstance()
                                            .getCurrentUser()
                                            .getEmail());
                                    listOfMessages.setAdapter(customAdapter);
                                    customAdapter.notifyDataSetChanged();
                                }
                                else{
                                    listOfMessages.setVisibility(View.GONE);
                                    showNoChat.setVisibility(View.VISIBLE);
                                }
                                if(progressDialog != null) {
                                    progressDialog.dismiss();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });




                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }


                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(HomeActivity.this,
                                    "You have been signed out.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            // Close activity
                            finish();
                        }
                    });
        } else if (item.getItemId() == R.id.menu_show_friends) {
            Intent intent = new Intent(this, ShowFriendActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.menu_find_friends) {
            Intent intent = new Intent(this, FindFriendActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.menu_friend_request) {
            Intent intent = new Intent(this, FriendRequestActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        final MenuItem menuItem = menu.getItem(2);
        mDatabase = FirebaseDatabase.getInstance().getReference("FriendRequest");
        if(FirebaseAuth.getInstance()
                .getCurrentUser()
                 != null) {
            Query friendRequestQuery = mDatabase.orderByChild("friendRequestReceiver").equalTo(FirebaseAuth.getInstance()
                    .getCurrentUser()
                    .getEmail());


            friendRequestQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String defaultValue = "Friend Request";
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        FriendRequest friendRequest = data.getValue(FriendRequest.class);
                        if (!friendRequest.isAnswered()) {
                            countFriendRequest++;
                        }
                    }
                    if (countFriendRequest != 0) {
                        menuItem.setTitle(defaultValue + " (" + countFriendRequest + ") ");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                updateRecentUser();
                invalidateOptionsMenu();

                Toast.makeText(this,
                        "Successfully signed in. Welcome!",
                        Toast.LENGTH_LONG)
                        .show();


                /**
                 * Todo : bikin method buat masukin data ke login ke db
                 */


            } else {
                Toast.makeText(this,
                        "We couldn't sign you in. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show();

                // Close the app
                finish();
            }
        }

    }

}
