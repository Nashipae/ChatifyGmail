package com.example.chatifygmail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.chatifygmail.database.AppDatabase;
import com.example.chatifygmail.database.Sender;

public class AddSenderViewModel extends ViewModel {

    // COMPLETED (6) Add a task member variable for the TaskEntry object wrapped in a LiveData
    private LiveData<Sender> sender;

    // COMPLETED (8) Create a constructor where you call loadTaskById of the taskDao to initialize the tasks variable
    // Note: The constructor should receive the database and the taskId
    public AddSenderViewModel(AppDatabase database, int senderId) {
        sender = database.senderDao().loadSenderById(senderId);
    }

    // COMPLETED (7) Create a getter for the task variable
    public LiveData<Sender> getSender() {
        return sender;
    }
}