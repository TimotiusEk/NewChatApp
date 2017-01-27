package com.google.chatapplication20.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.chatapplication20.R;
import com.google.chatapplication20.model.FriendRequest;
import com.google.chatapplication20.model.LastLoginUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class FindFriendActivity extends AppCompatActivity {
    private Button findFriendBtn;
    private EditText inputFriendEmail;
    private TextView showFriendEmail;
    private LastLoginUser user;
    private DatabaseReference mDatabase;
    Drawable drw;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.find_and_add_friend);



//        final int abTitleId = getResources().getIdentifier("action_bar_title", "id", "android");
//
//        findViewById(abTitleId).setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(FindFriendActivity.this, "Action Bar Clicked", Toast.LENGTH_SHORT).show();//Do something
//            }
//        });



        findFriendBtn = (Button) findViewById(R.id.find_friend_btn);

        inputFriendEmail = (EditText) findViewById(R.id.input_find_friend_email);

        showFriendEmail = (TextView) findViewById(R.id.show_friend_email);

        findFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findFriend();
            }
        });

        drw = ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_delete);
        drw.setBounds(0, 0, drw.getIntrinsicWidth(), drw.getIntrinsicHeight());
    }


    public void findFriend() {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LastLoginUser");

        final Query findFriendQuery = ref.orderByChild("userEmail").equalTo(String.valueOf(inputFriendEmail.getText()));

        findFriendQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Drawable drw = ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_delete);
                boolean found = false;
                String email = null;
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    LastLoginUser user = appleSnapshot.getValue(LastLoginUser.class);
                    if (user.getUserEmail() != null) {
                        found = true;
                        email = user.getUserEmail();
                    }
                }

                if (found) {
                    inputFriendEmail.setVisibility(View.GONE);
                    showFriendEmail.setVisibility(View.VISIBLE);
                    showFriendEmail.setText(email);
                    inputFriendEmail.setError(null);
                    findFriendBtn.setText("Add Friend");
                    final String finalEmail = email;

                    checkThenAddFriend(finalEmail, true);
                    findFriendBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            checkThenAddFriend(finalEmail, false);
                        }
                    });
                } else {
                    inputFriendEmail.setError("email tidak ditemukan", drw);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void checkThenAddFriend(final String email, final boolean justCheck) {
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
                    initializeFirstFriend(email, justCheck);
                } else {

                    addFriend(email, justCheck);
                }

                applesQuery.removeEventListener(this);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateFriendRequestStatus(final String email){
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("FriendRequest");

        final Query updateRequestQuery = ref.orderByChild("friendRequestSender").equalTo(email);

        updateRequestQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    FriendRequest friendRequest = data.getValue(FriendRequest.class);

                    if(friendRequest.getFriendRequestSender().equalsIgnoreCase(email) && friendRequest.getFriendRequestReceiver().equalsIgnoreCase(FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getEmail())){
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

    public void addFriend(final String email, final boolean justCheck) {
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
                    boolean alreadyFriend = false;

                    friendsArrayList = user.getFriends();

                    if (friendsArrayList != null) {
                        for (String friendName : friendsArrayList) {
                            if (friendName.equalsIgnoreCase(email)) {
                                alreadyFriend = true;
                            }
                        }
                    }

                    if (alreadyFriend) {
                        showFriendEmail.setText(email + "\n (Already Friend)");
                        findFriendBtn.setText("Find Another Friend");
                        findFriendBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            }
                        });
                    } else {
                        if (friendsArrayList != null) {
                            friendsArrayList.add(email);
                            if (!justCheck) {
                                appleSnapshot.getRef().child("friends").setValue(friendsArrayList);
                                showFriendEmail.setText(email + "\n (Already Friend)");
                                findFriendBtn.setText("Find Another Friend");

                                updateFriendRequest(email, ref);
                                updateFriendRequestStatus(email);


                                findFriendBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = getIntent();
                                        finish();
                                        startActivity(intent);
                                    }
                                });
                            }
                        }
                    }

//                    if(!alreadyFriend && friendsArrayList != null) {
//                        Log.d("masuk", "true");
//                        friendsArrayList.add(email);
//
//                        appleSnapshot.getRef().child("friends").setValue(friendsArrayList);
//                    }


                }
                applesQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    public void updateFriendRequest(final String email, DatabaseReference ref){
        final Query checkFriendsQuery = ref.orderByChild("userEmail").equalTo(email);

        final boolean[] alreadyFriend_ = new boolean[1];


        checkFriendsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    LastLoginUser user = data.getValue(LastLoginUser.class);
                    if(user.getFriends() != null) {
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

    public void initializeFirstFriend(final String email, final boolean justCheck) {

        final ArrayList<String> friends = new ArrayList<>();
        friends.add(email);

        final ArrayList<String> toCheckFriends = new ArrayList<>();


        user = new LastLoginUser(
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getEmail(), friends);

        /*
         * Todo : bikin method buat masukin data ke login ke db
         */

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LastLoginUser");
        final Query applesQuery = ref.orderByChild("userEmail").equalTo(FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail());

        final String[] userId = {null};

        if (!justCheck) {
            applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                        userId[0] = appleSnapshot.getKey();
                    }
                    mDatabase = FirebaseDatabase.getInstance().getReference("LastLoginUser");

                    mDatabase.child(userId[0]).child("friends").setValue(friends);

                    updateFriendRequest(email,ref);
                    updateFriendRequestStatus(email);

                    showFriendEmail.setText(email + "\n (Already Friend)");
                    findFriendBtn.setText("Find Another Friend");
                    findFriendBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                    });
                    applesQuery.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
        }
    }

    @Override
    public void onBackPressed() {

        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(i);

    }


}
