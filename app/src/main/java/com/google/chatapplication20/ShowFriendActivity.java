package com.google.chatapplication20;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class ShowFriendActivity extends AppCompatActivity {

    private ListView listView;
    private Button intentButton;
    private TextView showNoFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_friend);

        listView = (ListView) findViewById(R.id.show_friend_list_view);
        intentButton = (Button) findViewById(R.id.intent_to_find_friend_btn);
        showNoFriend = (TextView) findViewById(R.id.show_no_friend);

        populateView();
    }

    private void populateView() {
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

                if(friends == null){
                    listView.setVisibility(View.GONE);
                    intentButton.setVisibility(View.VISIBLE);
                    showNoFriend.setVisibility(View.VISIBLE);

                    intentButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ShowFriendActivity.this, FindFriendActivity.class);
                            startActivity(intent);
                        }
                    });

                }
                else {
                    listView.setVisibility(View.VISIBLE);
                    intentButton.setVisibility(View.GONE);
                    showNoFriend.setVisibility(View.GONE);
                    ArrayAdapter adapter = new ArrayAdapter<String>(ShowFriendActivity.this, android.R.layout.simple_list_item_1, friends);
                    listView.setAdapter(adapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Intent mIntent = new Intent(ShowFriendActivity.this, ChatActivity.class);
                            Bundle mBundle = new Bundle();
                            mBundle.putString("email", (String) adapterView.getItemAtPosition(i));
                            mIntent.putExtras(mBundle);
                            startActivity(mIntent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });
    }
}
