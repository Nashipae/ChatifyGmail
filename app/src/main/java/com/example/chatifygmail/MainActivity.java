package com.example.chatifygmail;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.chatifygmail.database.AppDatabase;
import com.example.chatifygmail.database.Sender;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements SenderAdapter.ItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    // Member variables for the adapter and RecyclerView
    private RecyclerView mRecyclerView;
    private SenderAdapter mAdapter;

    private AppDatabase mDb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.recyclerViewTasks);


        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new SenderAdapter(this, (SenderAdapter.ItemClickListener) this);
        mRecyclerView.setAdapter(mAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<Sender> senders = mAdapter.getSenders();
                        mDb.senderDao().deleteSender(senders.get(position));
                    }
                });
            }
        }).attachToRecyclerView(mRecyclerView);

        FloatingActionButton fabButton = findViewById(R.id.fab);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new intent to start an AddTaskActivity
                Intent addSenderIntent = new Intent(MainActivity.this, AddSenderActivity.class);
                startActivity(addSenderIntent);
            }
        });
        mDb = AppDatabase.getInstance(getApplicationContext());
        setupViewModel();

        @SuppressLint("RestrictedApi") PeriodicWorkRequest updateRequest =
                new PeriodicWorkRequest.Builder(UnreadCountWorker.class, 30, TimeUnit.MINUTES)
                        // Constraints
                        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                        .build();
        Log.i(TAG,"Starting request");
        /*OneTimeWorkRequest updateRequest =
                new OneTimeWorkRequest.Builder(UnreadCountWorker.class)
                        .build();
        //new CheckMailsTask().execute();*/
        WorkManager.getInstance().enqueue(updateRequest);

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(updateRequest.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(@Nullable WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Log.i(TAG,"Work finished!");
                        }
                        if (workInfo != null) {
                            Log.i(TAG,"SimpleWorkRequest: " + workInfo.getState().name() + "\n");
                        }
                    }
                });
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(MainViewModel.class);
        /*viewModel.getSenders().observe(this, new Observer<List<Sender>>() {
            @Override
            public void onChanged(@Nullable List<Sender> senders) {
                Log.d(TAG, "Updating list of tasks from LiveData in ViewModel");
                mAdapter.notifyDataSetChanged();
                Log.i(TAG,"Notified dataset changed");
                mAdapter.setSenders(senders);
            }
        });*/
        viewModel.getSenders ().observe (this, (List<Sender>senders) -> {
            mAdapter.notifyDataSetChanged ();
            Log.i(TAG,"Dataset Changed");
            Log.i(TAG,"Senders size: "+senders.size()+"");
            if(senders.size()==0){
                Log.i(TAG,"No Emails added yet!!");
            }
            mAdapter.setSenders (senders);
        });
    }

    @Override
    public void onItemClickListener(String emailAddress) {
        // Launch AddTaskActivity adding the itemId as an extra in the intent
        Intent intent = new Intent(MainActivity.this, AddSenderActivity.class);
        intent.putExtra(AddSenderActivity.EXTRA_EMAIL_ID, emailAddress);
        startActivity(intent);
    }

    private class CheckMailsTask extends AsyncTask<Void, Void, Void> {

        protected void onPostExecute(Long result) {
            Log.i("MainActivity","Finished");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            CheckMail.main(null);
            return null;
        }
    }
}
