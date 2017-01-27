package com.google.chatapplication20.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

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

/**
 * Created by TimotiusEk on 1/25/2017.
 */

public class FriendRequestAdapter extends ArrayAdapter<FriendRequest> {
    private ArrayList<FriendRequest> items;
    private FriendRequestAdapter adapter;
    private Context context;
    private FriendRequest item;

    public FriendRequestAdapter(Context context, int resource, ArrayList<FriendRequest> items) {
        super(context, resource, items);
        this.items = items;
        this.context = context;
        this.adapter = this;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.friend_request, null);
        }
        item = items.get(position);

        final FriendRequest p = getItem(position);


        TextView emailRequest = (TextView) v.findViewById(R.id.show_email_friend_request_label);
        Button acceptBtn = (Button) v.findViewById(R.id.accept_friend_request_btn);
        Button declineBtn = (Button) v.findViewById(R.id.decline_friend_request_btn);

        emailRequest.setText(p.getFriendRequestSender());


        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("buttonClicked", "accept " + p.getFriendRequestSender());
                updateDatabase(p.getFriendRequestSender(), true);
                checkThenAddFriend(p.getFriendRequestSender());
                items.remove(item); //Actually change your list of items here
                adapter.notifyDataSetChanged();
            }
        });

        declineBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d("buttonClicked", "decline " + p.getFriendRequestSender());
                updateDatabase(p.getFriendRequestSender(), false);
                items.remove(item); //Actually change your list of items here
                adapter.notifyDataSetChanged();

            }
        });
        return v;
    }

    public void updateDatabase(final String email, final boolean isAccepted){
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
                        if(isAccepted) {
                            data.getRef().child("accepted").setValue(true);
                            data.getRef().child("answered").setValue(true);
                        }
                        else{
                            data.getRef().child("accepted").setValue(false);
                            data.getRef().child("answered").setValue(true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

//    public void declineRequest(final String email){
//        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("FriendRequest");
//
//        final Query declineRequestQuery = ref.orderByChild("friendRequestSender").equalTo(email);
//
//        declineRequestQuery.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                for(DataSnapshot data : dataSnapshot.getChildren()){
//                    FriendRequest friendRequest = data.getValue(FriendRequest.class);
//
//
//                    if(friendRequest.getFriendRequestSender().equalsIgnoreCase(email) && friendRequest.getFriendRequestReceiver().equalsIgnoreCase(FirebaseAuth.getInstance()
//                            .getCurrentUser()
//                            .getEmail())){
//                        data.getRef().child("accepted").setValue(false);
//                        data.getRef().child("answered").setValue(true);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }


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

    public void initializeFirstFriend(final String email) {

        final ArrayList<String> friends = new ArrayList<>();
        friends.add(email);

        final ArrayList<String> toCheckFriends = new ArrayList<>();


        LastLoginUser user = new LastLoginUser(
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


        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    userId[0] = appleSnapshot.getKey();
                }
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("LastLoginUser");

                mDatabase.child(userId[0]).child("friends").setValue(friends);

                applesQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });


    }

}
