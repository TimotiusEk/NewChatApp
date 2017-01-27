package com.google.chatapplication20.activity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.chatapplication20.R;
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

public class ShowFriendActivity extends AppCompatActivity {

    private ListView listView;
    private Button intentButton;
    private TextView showNoFriend;
    private FloatingActionButton findFriendFab;
    private Button createGroupChatBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_friend);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setTitle("Friend List");

        listView = (ListView) findViewById(R.id.show_friend_list_view);
        intentButton = (Button) findViewById(R.id.intent_to_find_friend_btn);
        showNoFriend = (TextView) findViewById(R.id.show_no_friend);
        findFriendFab = (FloatingActionButton) findViewById(R.id.fab_add_friend);
        createGroupChatBtn = (Button) findViewById(R.id.create_group_chat_btn);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setItemsCanFocus(false);


        populateView();
    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.show_friend_list_view) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_friend_activity, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete_friend:
                removeValue((String) listView.getItemAtPosition(info.position));
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                // add stuff here
                return true;

            case R.id.create_group_chat:

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

                        if(friends != null) {
                            ArrayAdapter adapter = new ArrayAdapter<String>(ShowFriendActivity.this, android.R.layout.simple_list_item_multiple_choice, friends);
                            listView.setAdapter(adapter);
                            createGroupChatBtn.setVisibility(View.VISIBLE);
                            findFriendFab.setVisibility(View.GONE);
                            listView.setItemChecked(info.position,true);
                            listView.setOnItemClickListener(null);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                createGroupChatBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SparseBooleanArray checked = listView.getCheckedItemPositions();
                        final ArrayList<String> chatMember = new ArrayList<String>();

                        for (int i = 0; i < listView.getAdapter().getCount(); i++) {
                            if (checked.get(i)) {
                                chatMember.add((String) listView.getAdapter().getItem(i));
                                // Do something
                            }
                        }

                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShowFriendActivity.this);
                        LayoutInflater inflater=ShowFriendActivity.this.getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_dialog_group_chat, null);
                        alertDialog.setView(layout);
                        alertDialog.setTitle("Group Name : ");
                        final AlertDialog ad = alertDialog.show();


                        final Button cancelBtn = (Button) layout.findViewById(R.id.cancel_group_chat_dialog_btn);
                        final Button createBtn = (Button) layout.findViewById(R.id.create_group_chat_dialog_btn);
                        final EditText groupNameInput = (EditText) layout.findViewById(R.id.input_group_chat_name);

                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ad.dismiss();
                            }
                        });

                        createBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("GroupChat");

                                String groupChatId = mDatabase.push().getKey();

                                mDatabase.child(groupChatId).setValue(new GroupChat(groupNameInput.getText().toString(), chatMember));
                                ad.dismiss();
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            }
                        });

                    }
                });

                return true;


            default:
                return super.onContextItemSelected(item);
        }
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
                    findFriendFab.setVisibility(View.GONE);
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
                    findFriendFab.setVisibility(View.VISIBLE);
                    intentButton.setVisibility(View.GONE);
                    showNoFriend.setVisibility(View.GONE);
                    ArrayAdapter adapter = new ArrayAdapter<String>(ShowFriendActivity.this, android.R.layout.simple_list_item_1, friends);

                    listView.setAdapter(adapter);
                    registerForContextMenu(listView);

                    findFriendFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ShowFriendActivity.this, FindFriendActivity.class);
                            startActivity(intent);
                        }
                    });

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
                applesQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });
    }

    public void removeValue(final String email){
        Log.d("email to remove", email);
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LastLoginUser");
        final Query removeQuery =  ref.orderByChild("userEmail").equalTo(FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail());

        removeQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<String> friendsEmail = new ArrayList<String>();
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    LastLoginUser user = data.getValue(LastLoginUser.class);
                    if(user.getFriends() != null) {
                        for (int a = 0; a < user.getFriends().size(); a++) {
                            friendsEmail.add(user.getFriends().get(a));
                        }
                        for(int a = 0 ; a< friendsEmail.size() ; a++){
                            if (friendsEmail.get(a).equalsIgnoreCase(email)) {
                                Log.d("Email Removed", friendsEmail.get(a));
                                friendsEmail.remove(a);
                            }
                        }
                        data.getRef().child("friends").setValue(friendsEmail);
                    }
                }
                removeQuery.removeEventListener(this);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if(createGroupChatBtn.getVisibility() == View.GONE) {
            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(i);
        }
        else{
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }


    }
}
