package com.example.chatifygmail;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.chatifygmail.database.AppDatabase;

// COMPLETED (1) Make this class extend ViewModel ViewModelProvider.NewInstanceFactory
public class AddSenderViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    // COMPLETED (2) Add two member variables. One for the database and one for the taskId
    private final AppDatabase mDb;
    private final String mEmailAddress;

    // COMPLETED (3) Initialize the member variables in the constructor with the parameters received
    public AddSenderViewModelFactory(AppDatabase database, String emailAddress) {
        mDb = database;
        mEmailAddress = emailAddress;
    }

    // COMPLETED (4) Uncomment the following method
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new AddSenderViewModel(mDb, mEmailAddress);
    }
}
