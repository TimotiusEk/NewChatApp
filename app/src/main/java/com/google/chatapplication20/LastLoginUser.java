package com.google.chatapplication20;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by TimotiusEk on 1/16/2017.
 */

public class LastLoginUser implements Comparable<LastLoginUser> {
    private String userEmail;
    private long userLastLoginTime;
    private String lastMessage;
    private long lastMessageTime;
    private int unreadMessage;
    private ArrayList<String> friends;

    public ArrayList<String> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<String> friends) {
        this.friends = friends;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }


    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public long getUserLastLoginTime() {
        return userLastLoginTime;
    }

    public void setUserLastLoginTime(long userLastLoginTime) {
        this.userLastLoginTime = userLastLoginTime;
    }

    public LastLoginUser(String userEmail) {
        this.userEmail = userEmail;
        this.userLastLoginTime = new Date().getTime();
        friends = new ArrayList<>();

    }

    public LastLoginUser(String userEmail, ArrayList<String> friends) {
        this.userEmail = userEmail;
        this.friends = friends;
        this.userLastLoginTime = new Date().getTime();
    }

    LastLoginUser() {

    }

    public static Comparator<LastLoginUser> RecentChatComparator
            = new Comparator<LastLoginUser>() {

        public int compare(LastLoginUser user1, LastLoginUser user2) {

            Long chatDate1 = user1.getLastMessageTime();
            Long chatDate2 = user2.getLastMessageTime();

            //ascending order
//            return chatDate1.compareTo(chatDate2);

            //descending order
            return chatDate2.compareTo(chatDate1);
        }

    };

    @Override
    public int compareTo(LastLoginUser o) {
        return 0;
    }

    public int getUnreadMessage() {
        return unreadMessage;
    }

    public void setUnreadMessage(int unreadMessage) {
        this.unreadMessage = unreadMessage;
    }
}
