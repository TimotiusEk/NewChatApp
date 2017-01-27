package com.google.chatapplication20.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.chatapplication20.R;
import com.google.chatapplication20.model.LastLoginUser;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by TimotiusEk on 1/17/2017.
 */

public class LastLoginUserAdapter extends ArrayAdapter<LastLoginUser> {
    private String currentUserEmail;

    public LastLoginUserAdapter(Context context, int resource, ArrayList<LastLoginUser> items, String currentUserEmail) {
        super(context, resource, items);
        this.currentUserEmail = currentUserEmail;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.last_login, null);
        }

        LastLoginUser p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.user_email);

            TextView tt3 = (TextView) v.findViewById(R.id.user_last_message);
            TextView tt4 = (TextView) v.findViewById(R.id.user_last_message_time);
            TextView tt5 = (TextView) v.findViewById(R.id.user_message_number);


            if (tt1 != null) {
                tt1.setText(p.getUserEmail());
            }


            if (p.isLastMessagePicture()) {
                tt3.setText("Picture File");

            } else if(p.getLastMessage() != null){
                tt3.setText(p.getLastMessage().trim());
            }


        long messageTime = new Date().getTime();

        String messageDate = (String) DateFormat.format("dd-MM-yyyy",
                p.getLastMessageTime());

        String currentDate = (String) DateFormat.format("dd-MM-yyyy",
                messageTime);

        int todayDate = Integer.parseInt(currentDate.substring(0, 2));
        int todayMonth = Integer.parseInt(currentDate.substring(3, 5));
        int todayYear = Integer.parseInt(currentDate.substring(6));

        int msgDate = Integer.parseInt(messageDate.substring(0, 2));
        int msgMonth = Integer.parseInt(messageDate.substring(3, 5));
        int msgYear = Integer.parseInt(messageDate.substring(6));

        if (messageDate.equalsIgnoreCase(currentDate)) {
            tt4.setText(DateFormat.format("HH:mm",
                    p.getLastMessageTime()));
        } else if (todayMonth == msgMonth && todayYear == msgYear && todayDate - 1 == msgDate) {
            tt4.setText("Yesterday");
        } else if(p.getLastMessageTime() != 0) {
            tt4.setText(DateFormat.format("dd-MM-yyyy (HH:mm)",
                    p.getLastMessageTime()));
        }


        if (p.getUnreadMessage() != 0) {
            tt5.setVisibility(View.VISIBLE);


            tt5.setText(String.valueOf(p.getUnreadMessage()));


        } else {
            tt5.setVisibility(View.INVISIBLE);
        }

    }


    return v;
}
}
