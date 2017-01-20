package com.google.chatapplication20;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private DatabaseReference mDatabaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            );
        } else {
            updateRecentUser();

        }



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


    public void updateRecentUser(){


        /*
         * Todo : bikin method buat masukin data ke login ke db
         */
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LastLoginUser");
        final Query applesQuery = ref.orderByChild("userEmail").equalTo(FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail());

        user = new LastLoginUser(
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getEmail());

        mDatabase = FirebaseDatabase.getInstance().getReference("LastLoginUser");

        String userId = mDatabase.push().getKey();

        mDatabase.child(userId).setValue(user);


        applesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    ArrayList<String> friends = null;
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        LastLoginUser user = appleSnapshot.getValue(LastLoginUser.class);
                        friends = user.getFriends();
                    }
                    if(friends == null){
                        user = new LastLoginUser(
                                FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getEmail());
//                        Log.d("null", "true");
                    }
                    else{
                        user = new LastLoginUser(
                                FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getEmail(), friends);
//                        Log.d("null", "false");
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





        getRecentMessage();


//        checkThenAddFriend("asdfgh");
        /**
         * ToDo :Testing Buat dapetin, update, dan delete ArrayList dari firebase
         */


    }

//    public void checkThenAddFriend(final String email){
//        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LastLoginUser");
//
//        final Query applesQuery = ref.orderByChild("userEmail").equalTo(FirebaseAuth.getInstance()
//                .getCurrentUser()
//                .getEmail());
//
//        applesQuery.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                ArrayList<String> friends = null;
//                long date = 0;
//                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
//                    LastLoginUser user = appleSnapshot.getValue(LastLoginUser.class);
//                    friends = user.getFriends();
//                }
//                if(friends == null){
//                    initializeFirstFriend(email);
//                    Log.d("friendNull", String.valueOf(true));
//
//                }
//                else{
//                    addFriend(email);
//                    Log.d("friendNull", String.valueOf(false));
//                }
//
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

//    public void initializeFirstFriend(final String email) {
//
//        final ArrayList<String> friends = new ArrayList<>();
//        friends.add(email);
//
//        user = new LastLoginUser(
//                FirebaseAuth.getInstance()
//                        .getCurrentUser()
//                        .getEmail(), friends);
//
//        /*
//         * Todo : bikin method buat masukin data ke login ke db
//         */
//
//        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LastLoginUser");
//        final Query applesQuery = ref.orderByChild("userEmail").equalTo(FirebaseAuth.getInstance()
//                .getCurrentUser()
//                .getEmail());
//
//        final String[] userId = {null};
//
//
//        applesQuery.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
//                    userId[0] = appleSnapshot.getKey();
//                }
//                mDatabase = FirebaseDatabase.getInstance().getReference("LastLoginUser");
//
//
//
//                mDatabase.child(userId[0]).child("friends").setValue(friends);
//                ref.removeEventListener(this);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//
//        });
//
//
//
//
//
//    }
//    public void addFriend(final String email){
//        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LastLoginUser");
//        final Query applesQuery = ref.orderByChild("userEmail").equalTo(FirebaseAuth.getInstance()
//                .getCurrentUser()
//                .getEmail());
//
//        applesQuery.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                ArrayList<String> friendsArrayList;
//
//
//                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
//
//                    LastLoginUser user = appleSnapshot.getValue(LastLoginUser.class);
//                    boolean alreadyFriend = false;
//
//                    friendsArrayList = user.getFriends();
//
//                    if(friendsArrayList != null) {
//                        for (String friendName : friendsArrayList) {
//                            if (friendName.equalsIgnoreCase(email)) {
//
//                                alreadyFriend = true;
//                            }
//                        }
//                    }
//
//                    if(!alreadyFriend && friendsArrayList != null) {
//                        Log.d("masuk", "true");
//                        friendsArrayList.add(email);
//
//                        appleSnapshot.getRef().child("friends").setValue(friendsArrayList);
//                    }
//
//
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//
//        });
//    }

    public void getRecentMessage() {
        mDatabaseUser = FirebaseDatabase.getInstance().getReference("LastLoginUser");
        Log.d("mDatabaseUser", String.valueOf(mDatabaseUser.getRef()));
        mDatabaseUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<LastLoginUser> loginUserArrayList = new ArrayList<LastLoginUser>();
                final ArrayList<ChatMessage> chatMessageArrayList = new ArrayList<ChatMessage>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    LastLoginUser user = data.getValue(LastLoginUser.class);
                    loginUserArrayList.add(user);
                }
                mDatabase = FirebaseDatabase.getInstance().getReference("ChatMessage");
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                            ChatMessage chatMessage = messageSnapshot.getValue(ChatMessage.class);
                            chatMessageArrayList.add(chatMessage);
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
                                mIntent.putExtras(mBundle);
                                if (user.getUserEmail() != null) {
                                    startActivity(mIntent);
                                }


                            }
                        });
                        Collections.sort(loginUserArrayList, LastLoginUser.RecentChatComparator);
                        final LastLoginUserAdapter customAdapter = new LastLoginUserAdapter(HomeActivity.this, R.layout.last_login, loginUserArrayList);
                        listOfMessages.setAdapter(customAdapter);
                        customAdapter.notifyDataSetChanged();


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
        }
        else if(item.getItemId() == R.id.menu_show_friends){
            Intent intent = new Intent(this, ShowFriendActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.menu_find_friends){
            Intent intent = new Intent(this, FindFriendActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

            updateRecentUser();

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
