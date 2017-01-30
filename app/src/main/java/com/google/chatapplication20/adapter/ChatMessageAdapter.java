package com.google.chatapplication20.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.chatapplication20.R;
import com.google.chatapplication20.activity.ChatActivity;
import com.google.chatapplication20.model.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static android.R.attr.bitmap;
import static java.lang.System.out;

/**
 * Created by TimotiusEk on 1/18/2017.
 */

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {
    boolean showUnreadMessageBelow = false;
    String chatPartner;
    boolean isLongClick;

    public int getShowUnreadBelowPosition() {
        return showUnreadBelowPosition;
    }


    int showUnreadBelowPosition;

    public ChatMessageAdapter(Context context, int resource, ArrayList<ChatMessage> items, String chatPartner) {
        super(context, resource, items);
        this.chatPartner = chatPartner;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.message, null);
        }

        ChatMessage p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.message_text);
            TextView tt2 = (TextView) v.findViewById(R.id.message_time);
            TextView tt3 = (TextView) v.findViewById(R.id.message_user);
            TextView tt4 = (TextView) v.findViewById(R.id.unread_message_label);
            ImageView iv1 = (ImageView) v.findViewById(R.id.message_img);

            tt1.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    isLongClick = true;
                    return false;
                }
            });

            tt1.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (event.getAction() == MotionEvent.ACTION_UP && isLongClick) {
                        isLongClick = false;
                        return true;
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        isLongClick = false;
                    }
                    return v.onTouchEvent(event);
                }
            });

            if(!p.isPicture()) {
                tt1.setText(p.getMessageText());
                iv1.setVisibility(View.GONE);
            }
            else{
                try {
                    Bitmap imageBitmap = decodeFromFirebaseBase64(p.getMessageText());

                    iv1.setImageBitmap(imageBitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            if (tt2 != null) {
                long messageTime = new Date().getTime();

                String messageDate = (String) DateFormat.format("dd-MM-yyyy",
                        p.getMessageTime());

                String currentDate = (String) DateFormat.format("dd-MM-yyyy",
                        messageTime);



                int todayDate = Integer.parseInt(currentDate.substring(0, 2));
                int todayMonth = Integer.parseInt(currentDate.substring(3, 5));
                int todayYear = Integer.parseInt(currentDate.substring(6));

                int msgDate = Integer.parseInt(messageDate.substring(0, 2));
                int msgMonth = Integer.parseInt(messageDate.substring(3, 5));
                int msgYear = Integer.parseInt(messageDate.substring(6));


                if (messageDate.equalsIgnoreCase(currentDate)) {
                    tt2.setText(DateFormat.format("HH:mm",
                            p.getMessageTime()));
                } else {
                    tt2.setText(DateFormat.format("dd-MM-yyyy (HH:mm)",
                            p.getMessageTime()));
                }

            }


            if (p.getMessageSender().equalsIgnoreCase(FirebaseAuth.getInstance()
                    .getCurrentUser()
                    .getEmail())) {
                tt3.setText("You");
            } else {
                tt3.setText(p.getMessageSender());
            }

            if (!p.isMessageRead() && !showUnreadMessageBelow && p.getMessageSender().equals(chatPartner)) {
                showUnreadMessageBelow = true;
                tt4.setVisibility(View.VISIBLE);
                ChatActivity.position = position;
                ChatActivity.hasBelowUnreadMessage = true;

            }

        }

        return v;
    }



    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        ByteArrayInputStream is = new ByteArrayInputStream(decodedByteArray);
        Drawable d = Drawable.createFromStream(is, "bloodsample");
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
}
