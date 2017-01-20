package com.google.chatapplication20;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TimotiusEk on 1/17/2017.
 */

public class LastLoginUserAdapter extends ArrayAdapter<LastLoginUser> {

    public LastLoginUserAdapter(Context context, int resource, ArrayList<LastLoginUser> items) {
        super(context, resource, items);
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


            if(p.getLastMessageTime() != 0) {
                tt3.setText(p.getLastMessage());
                tt4.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", p.getLastMessageTime()));
            }

            if(p.getUnreadMessage() != 0){
                tt5.setVisibility(View.VISIBLE);
                tt5.setText(String.valueOf(p.getUnreadMessage()));
            }
            else{
                tt5.setVisibility(View.INVISIBLE);
            }

        }


        return v;
    }
}
