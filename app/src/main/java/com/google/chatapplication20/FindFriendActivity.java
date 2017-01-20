package com.google.chatapplication20;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        findFriendBtn = (Button) findViewById(R.id.find_friend_btn);

        inputFriendEmail = (EditText) findViewById(R.id.input_find_friend_email);

        showFriendEmail = (TextView) findViewById(R.id.show_friend_email);

        findFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findFriend();
            }
        });


    }

    public void findFriend() {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LastLoginUser");

        final Query findFriendQuery = ref.orderByChild("userEmail").equalTo(String.valueOf(inputFriendEmail.getText()));

        findFriendQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Drawable drw = ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_delete);
                boolean found = false;
                String email = null;
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    LastLoginUser user = appleSnapshot.getValue(LastLoginUser.class);
                    if(user.getUserEmail() != null){
                        found = true;
                        email = user.getUserEmail();
                    }
                }

                if(found){
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
                }
                else{
                    inputFriendEmail.setError("email tidak ditemukan", drw);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Drawable drw = ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_delete);
        drw.setBounds(0, 0, drw.getIntrinsicWidth(), drw.getIntrinsicHeight());
    }

    public void checkThenAddFriend(final String email, final boolean justCheck){
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LastLoginUser");

        final Query applesQuery = ref.orderByChild("userEmail").equalTo(FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail());

        applesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> friends = null;
                long date = 0;
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    LastLoginUser user = appleSnapshot.getValue(LastLoginUser.class);
                    friends = user.getFriends();
                }
                if(friends == null){
                    initializeFirstFriend(email);
                    Log.d("friendNull", String.valueOf(true));

                }
                else{
                    addFriend(email, justCheck);
                    Log.d("friendNull", String.valueOf(false));
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addFriend(final String email, final boolean justCheck){
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

                    if(friendsArrayList != null) {
                        for (String friendName : friendsArrayList) {
                            if (friendName.equalsIgnoreCase(email)) {
                                alreadyFriend = true;
                            }
                        }
                    }

                    if(alreadyFriend && justCheck){
                        showFriendEmail.append("\n (Already Friend)");
                    }
                    else{
                        if(friendsArrayList != null){
                            friendsArrayList.add(email);

                            appleSnapshot.getRef().child("friends").setValue(friendsArrayList);
                        }
                    }

//                    if(!alreadyFriend && friendsArrayList != null) {
//                        Log.d("masuk", "true");
//                        friendsArrayList.add(email);
//
//                        appleSnapshot.getRef().child("friends").setValue(friendsArrayList);
//                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    public void initializeFirstFriend(final String email) {

        final ArrayList<String> friends = new ArrayList<>();
        friends.add(email);

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


        applesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    userId[0] = appleSnapshot.getKey();
                }
                mDatabase = FirebaseDatabase.getInstance().getReference("LastLoginUser");



                mDatabase.child(userId[0]).child("friends").setValue(friends);
                ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });





    }


}
