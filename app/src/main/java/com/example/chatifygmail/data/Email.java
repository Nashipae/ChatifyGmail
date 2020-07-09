package com.example.chatifygmail.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Email implements Parcelable {
    private String subject;
    private String contents;
    private int messageNumber;

    protected Email(Parcel in) {
        subject = in.readString();
        contents = in.readString();
        messageNumber = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(subject);
        dest.writeString(contents);
        dest.writeInt(messageNumber);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Email> CREATOR = new Creator<Email>() {
        @Override
        public Email createFromParcel(Parcel in) {
            return new Email(in);
        }

        @Override
        public Email[] newArray(int size) {
            return new Email[size];
        }
    };

    public int getMessageNumber() {
        return messageNumber;
    }

    public void setMessageNumber(int messageNumber) {
        this.messageNumber = messageNumber;
    }

    /*protected Email(Parcel in) {
            subject = in.readString();
            contents = in.readString();
        }

        public static final Creator<Email> CREATOR = new Creator<Email>() {
            @Override
            public Email createFromParcel(Parcel in) {
                return new Email(in);
            }

            @Override
            public Email[] newArray(int size) {
                return new Email[size];
            }
        };*/
    public Email(){

    }

    /*protected Email(Parcel in) {
        subject = in.readString();
        contents = in.readString();
    }

    public static final Creator<Email> CREATOR = new Creator<Email>() {
        @Override
        public Email createFromParcel(Parcel in) {
            return new Email(in);
        }

        @Override
        public Email[] newArray(int size) {
            return new Email[size];
        }
    };*/

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    /*@Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(subject);
        parcel.writeString(contents);
    }*/

    /*@Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(subject);
        parcel.writeString(contents);
    }*/
}
