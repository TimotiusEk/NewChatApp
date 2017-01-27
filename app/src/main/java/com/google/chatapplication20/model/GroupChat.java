package com.google.chatapplication20.model;

import java.util.ArrayList;

/**
 * Created by TimotiusEk on 1/27/2017.
 */

public class GroupChat {
    private String groupChatName;
    private ArrayList<ChatMessage> groupChatMessages;
    private ArrayList<String> groupChatMember;

    public GroupChat(){}

    public GroupChat(String groupChatName, ArrayList<String> groupChatMember) {
        this.groupChatName = groupChatName;
        this.groupChatMember = groupChatMember;
        groupChatMessages = new ArrayList<>();
    }

    public String getGroupChatName() {
        return groupChatName;
    }

    public void setGroupChatName(String groupChatName) {
        this.groupChatName = groupChatName;
    }

    public ArrayList<ChatMessage> getGroupChatMessages() {
        return groupChatMessages;
    }

    public void setGroupChatMessages(ArrayList<ChatMessage> groupChatMessages) {
        this.groupChatMessages = groupChatMessages;
    }

    public ArrayList<String> getGroupChatMember() {
        return groupChatMember;
    }

    public void setGroupChatMember(ArrayList<String> groupChatMember) {
        this.groupChatMember = groupChatMember;
    }



}
