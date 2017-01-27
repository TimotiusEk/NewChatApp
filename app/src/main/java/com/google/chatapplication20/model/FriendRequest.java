package com.google.chatapplication20.model;

import java.util.Date;

/**
 * Created by TimotiusEk on 1/25/2017.
 */

public class FriendRequest {

    private String friendRequestSender;
    private String friendRequestReceiver;
    private boolean isAccepted;
    private boolean isAnswered;
    private long requestTime;

    FriendRequest(){}
    public FriendRequest(String friendRequestReceiver, String friendRequestSender) {
        this.friendRequestReceiver = friendRequestReceiver;
        this.friendRequestSender = friendRequestSender;
        isAccepted = false;
        isAnswered = false;
        requestTime = new Date().getTime();
    }

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public boolean isAnswered() {
        return isAnswered;
    }

    public void setAnswered(boolean answered) {
        isAnswered = answered;
    }

    public String getFriendRequestSender() {
        return friendRequestSender;
    }

    public void setFriendRequestSender(String friendRequestSender) {
        this.friendRequestSender = friendRequestSender;
    }

    public String getFriendRequestReceiver() {
        return friendRequestReceiver;
    }

    public void setFriendRequestReceiver(String friendRequestReceiver) {
        this.friendRequestReceiver = friendRequestReceiver;
    }


}
