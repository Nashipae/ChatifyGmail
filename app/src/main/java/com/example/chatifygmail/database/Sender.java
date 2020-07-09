package com.example.chatifygmail.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Sender {

    @PrimaryKey
    @NonNull
    private String emailAddress;
    private int unread;


    public Sender(String emailAddress, int unread) {
        this.emailAddress = emailAddress;
        this.unread = unread;
    }

    /*public Sender(int id, String emailAddress, int unread) {
        this.id = id;
        this.emailAddress = emailAddress;
        this.unread = unread;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }*/

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }
}
