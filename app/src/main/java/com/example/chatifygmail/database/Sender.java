package com.example.chatifygmail.database;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.chatifygmail.data.Email;

import java.util.ArrayList;

@Entity
public class Sender implements Parcelable {

    @PrimaryKey
    @NonNull
    private String emailAddress;
    private int unread;
    private ArrayList<Email> emails;

    public ArrayList<Email> getEmails() {
        return emails;
    }

    public void setEmails(ArrayList<Email> emails) {
        this.emails = emails;
    }

    public Sender(String emailAddress, int unread, ArrayList<Email> emails) {
        this.emailAddress = emailAddress;
        this.unread = unread;
        this.emails = emails;
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

    protected Sender(Parcel in) {
        emailAddress = in.readString();
        unread = in.readInt();
        emails = in.readArrayList(Email.class.getClassLoader());
    }

    public static final Creator<Sender> CREATOR = new Creator<Sender>() {
        @Override
        public Sender createFromParcel(Parcel in) {
            return new Sender(in);
        }

        @Override
        public Sender[] newArray(int size) {
            return new Sender[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(emailAddress);
        parcel.writeInt(unread);
        parcel.writeList(emails);
    }
}
