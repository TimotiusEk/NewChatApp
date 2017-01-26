package com.google.chatapplication20;

import android.graphics.Bitmap;

import java.io.File;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by TimotiusEk on 1/16/2017.
 */

public class ChatMessage implements Comparable<ChatMessage>{
    private String messageText;
    private String messageReceiver;
    private long messageTime;
    private String messageSender;

    private File chatPicture;

    private boolean messageRead;

    private boolean isPicture;

    public boolean isPicture() {
        return isPicture;
    }

    public void setPicture(boolean picture) {
        isPicture = picture;
    }

    public File getChatPicture() {
        return chatPicture;
    }

    public void setChatPicture(File chatPicture) {
        this.chatPicture = chatPicture;
    }

    public void setMessageRead(boolean messageRead) {
        this.messageRead = messageRead;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageReceiver() {
        return messageReceiver;
    }

    public void setMessageReceiver(String messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    public String getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(String messageSender) {
        this.messageSender = messageSender;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }


    public boolean isMessageRead() {
        return messageRead;
    }

    public ChatMessage(String messageText, String messageReceiver, String messageSender, boolean isPicture) {
        this.messageText = messageText;
        this.messageReceiver = messageReceiver;
        this.messageSender = messageSender;
        messageTime = new Date().getTime();
        this.isPicture = isPicture;
    }


    ChatMessage(){

    }


    public static Comparator<ChatMessage> ChatDateComparator
            = new Comparator<ChatMessage>() {

        public int compare(ChatMessage chat1, ChatMessage chat2) {

            Long chatDate1 = chat1.getMessageTime();
            Long chatDate2 = chat2.getMessageTime();

            //ascending order
//            return chatDate1.compareTo(chatDate2);

            //descending order
            return chatDate2.compareTo(chatDate1);
        }

    };

    @Override
    public int compareTo(ChatMessage o) {
        return 0;
    }

}
