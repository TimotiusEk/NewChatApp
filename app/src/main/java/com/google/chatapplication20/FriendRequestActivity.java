package com.google.chatapplication20;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendRequestActivity extends AppCompatActivity {
    private ListView friendRequestLv;
    private FriendRequestAdapter friendRequestAdapter;
    private DatabaseReference mDatabase;
    private Query friendRequestQuery;
    private ArrayList<FriendRequest> friendRequestArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);
        friendRequestLv = (ListView) findViewById(R.id.friend_request_lv);
        friendRequestArrayList = new ArrayList<>();
        populateView();
    }

    public void populateView() {
        mDatabase = FirebaseDatabase.getInstance().getReference("FriendRequest");
        friendRequestQuery = mDatabase.orderByChild("friendRequestReceiver").equalTo(FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail());

        friendRequestQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    FriendRequest friendRequest = data.getValue(FriendRequest.class);
                    if (!friendRequest.isAnswered()) {
                        friendRequestArrayList.add(friendRequest);
                    }
                }

                Log.d("friendRequestArrayListSize", String.valueOf(friendRequestArrayList.size()));

                for(int a = 0 ; a < friendRequestArrayList.size(); a++){
                    Log.d("valueOfFriendRequest", String.valueOf(friendRequestArrayList.get(a).isAnswered()));
                }
                friendRequestAdapter = new FriendRequestAdapter(FriendRequestActivity.this, R.layout.friend_request, friendRequestArrayList);
                friendRequestLv.setAdapter(friendRequestAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {

        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(i);

    }
}
