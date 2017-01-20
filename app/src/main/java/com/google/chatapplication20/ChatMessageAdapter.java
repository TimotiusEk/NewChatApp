package com.google.chatapplication20;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by TimotiusEk on 1/18/2017.
 */

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {
    boolean showUnreadMessageBelow = false;
    String chatPartner;

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



            if (tt1 != null) {
                tt1.setText(p.getMessageText());
            }

            if (tt2 != null) {
                tt2.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        p.getMessageTime()));
            }

            if(p.getMessageSender().equalsIgnoreCase(FirebaseAuth.getInstance()
                    .getCurrentUser()
                    .getEmail())) {
                tt3.setText("You");
            }
            else{
                tt3.setText(p.getMessageSender());
            }

            if(!p.isMessageRead() && !showUnreadMessageBelow && p.getMessageSender().equals(chatPartner)){
                showUnreadMessageBelow = true;
                tt4.setVisibility(View.VISIBLE);
                ChatActivity.position = position;
                ChatActivity.hasBelowUnreadMessage = true;

            }

            tt3.setText(p.getMessageSender());

        }

        return v;
    }
}
