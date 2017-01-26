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
import android.widget.EditText;
import android.widget.ImageView;
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

        scrollMyListViewToBottom();

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
        } else if (item.getItemId() == R.id.menu_upload_photo) {

            uploadPhoto();
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
