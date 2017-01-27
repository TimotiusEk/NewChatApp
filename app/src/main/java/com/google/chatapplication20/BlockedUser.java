package com.google.chatapplication20;

import java.util.ArrayList;

/**
 * Created by TimotiusEk on 1/26/2017.
 */

public class BlockedUser {
    private String userWhoBlock;
    private ArrayList<String> blockedUser;

    public BlockedUser(){}

    public BlockedUser(String userWhoBlock, ArrayList<String> blockedUser) {
        this.userWhoBlock = userWhoBlock;
        this.blockedUser = blockedUser;
    }

    public String getUserWhoBlock() {
        return userWhoBlock;
    }

    public void setUserWhoBlock(String userWhoBlock) {
        this.userWhoBlock = userWhoBlock;
    }

    public ArrayList<String> getBlockedUser() {
        return blockedUser;
    }

    public void setBlockedUser(ArrayList<String> blockedUser) {
        this.blockedUser = blockedUser;
    }
}
