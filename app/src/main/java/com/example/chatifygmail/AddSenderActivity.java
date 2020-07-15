package com.example.chatifygmail;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.chatifygmail.data.Email;
import com.example.chatifygmail.database.AppDatabase;
import com.example.chatifygmail.database.Sender;

import java.util.ArrayList;

public class AddSenderActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL_ID = "extraEmailId";
    public static final String INSTANCE_EMAIL_ID = "instanceEmailId";
    private static final String TAG = AddSenderActivity.class.getSimpleName();
    private static final String DEFAULT_EMAIL_ID = "";

    EditText emailEditText;
    Button addSenderButton;


    private AppDatabase mDb;

    private String mEmailId=DEFAULT_EMAIL_ID;
    private int unread=0;
    private ArrayList<Email> emails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sender);
        mDb = AppDatabase.getInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_EMAIL_ID)) {
            mEmailId = savedInstanceState.getString(INSTANCE_EMAIL_ID);
        }
        initViews();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_EMAIL_ID)) {
            addSenderButton.setText(R.string.update);
            if (mEmailId == DEFAULT_EMAIL_ID) {
                // populate the UI
                mEmailId = intent.getStringExtra(EXTRA_EMAIL_ID);

                // COMPLETED (9) Remove the logging and the call to loadTaskById, this is done in the ViewModel now
                // COMPLETED (10) Declare a AddTaskViewModelFactory using mDb and mTaskId
                AddSenderViewModelFactory factory = new AddSenderViewModelFactory(mDb, mEmailId);
                // COMPLETED (11) Declare a AddTaskViewModel variable and initialize it by calling ViewModelProviders.of
                // for that use the factory created above AddTaskViewModel
                final AddSenderViewModel viewModel
                        = ViewModelProviders.of(this, factory).get(AddSenderViewModel.class);

                // COMPLETED (12) Observe the LiveData object in the ViewModel. Use it also when removing the observer
                viewModel.getSender().observe(this, new Observer<Sender>() {
                    @Override
                    public void onChanged(@Nullable Sender sender) {
                        viewModel.getSender().removeObserver(this);
                        populateUI(sender);
                    }
                });
            }
        }
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditTextView);

        addSenderButton = findViewById(R.id.buttonAddSender);
        addSenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveButtonClicked();
            }
        });
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(INSTANCE_EMAIL_ID, mEmailId);
        super.onSaveInstanceState(outState);
    }

    private void populateUI(Sender sender) {
        if (sender == null) {
            return;
        }

        emailEditText.setText(sender.getEmailAddress());
        unread = sender.getUnread();
        emails = sender.getEmails();
    }
    public void onSaveButtonClicked() {
        final String emailAddress = emailEditText.getText().toString();

        final Sender sender = new Sender(emailAddress, unread, emails);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mEmailId == DEFAULT_EMAIL_ID) {
                    // insert new task
                    mDb.senderDao().insertSender(sender);
                    Log.i(TAG,"Inserted");
                } else if(!mEmailId.equals(emailAddress)){
                    //update task
                    //Delete and insert as the Primary key is changed
                    mDb.senderDao().deleteSenderbyEmail(mEmailId);
                    sender.setEmailAddress(emailAddress);
                    //sender.setUnread(unread);
                    //sender.setEmails(emails);
                    mDb.senderDao().insertSender(sender);
                    Log.i(TAG,"Updated");
                }
                else if(mEmailId.equals(emailAddress)){
                    Log.i(TAG, "Email Address not modified");
                    finish();
                }
                /*OneTimeWorkRequest updateRequest =
                        new OneTimeWorkRequest.Builder(UnreadCountWorker.class)
                                .build();
                //new CheckMailsTask().execute();
                WorkManager.getInstance().enqueue(updateRequest);*/
                Constraints constraints = new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build();
                OneTimeWorkRequest updateRequest =
                        new OneTimeWorkRequest.Builder(UnreadCountWorker.class)
                                //.setConstraints(constraints)
                                .build();
                //new CheckMailsTask().execute();*/
                WorkManager.getInstance().enqueueUniqueWork("RefreshMails", ExistingWorkPolicy.REPLACE, updateRequest);
                finish();
            }
        });
    }
}