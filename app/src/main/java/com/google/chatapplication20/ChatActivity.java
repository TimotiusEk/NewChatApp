package com.google.chatapplication20;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
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

import static com.facebook.FacebookSdk.getApplicationContext;

public class ChatActivity extends AppCompatActivity {

    private String chatPartner;
    private ChatMessageAdapter chatMessageAdapter;
    private DatabaseReference mDatabase;
    public static int position = 0;
    public static boolean hasBelowUnreadMessage = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        hasBelowUnreadMessage = false;


        chatPartner = getIntent().getExtras().getString("email");

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setTitle(chatPartner);

        displayChatMessages();

        FloatingActionButton fab =
                (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText) findViewById(R.id.input);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                ChatMessage message = new ChatMessage(input.getText().toString(),
                        chatPartner
                        , FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getEmail());

                mDatabase = FirebaseDatabase.getInstance().getReference("ChatMessage");

                String messageId = mDatabase.push().getKey();

                mDatabase.child(messageId).setValue(message);


                // Clear the input
                input.setText("");

                scrollMyListViewToBottom();


            }
        });


    }

    private void scrollMyListViewToBottom() {
        final ListView listOfMessages = (ListView) findViewById(R.id.list_of_messages);
        listOfMessages.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listOfMessages.setSelection(chatMessageAdapter.getCount() - 1);
            }
        });
    }

    private void updateToRead(){
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

        final ListView listOfMessages = (ListView) findViewById(R.id.list_of_messages);



        mDatabase = FirebaseDatabase.getInstance().getReference("ChatMessage");

        if(!chatPartner.equalsIgnoreCase(FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail())) {
            chatEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateToRead();
                }
            });
        }

        else{
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

                scrollMyListViewToBottom();

                if(hasBelowUnreadMessage) {
                    listOfMessages.setSelectionFromTop(position, 0);
                }


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
                            Toast.makeText(ChatActivity.this,
                                    "You have been signed out.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            // Close activity

                            ChatActivity.this.finishAffinity();
                        }
                    });

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        updateToRead();
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(i);
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
        displayChatMessages();
    }
}
