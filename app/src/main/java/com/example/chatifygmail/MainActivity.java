package com.example.chatifygmail;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.chatifygmail.data.Email;
import com.example.chatifygmail.database.AppDatabase;
import com.example.chatifygmail.database.Sender;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements SenderAdapter.ItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    // Member variables for the adapter and RecyclerView
    private RecyclerView mRecyclerView;
    private SenderAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView errorView;
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        errorView = findViewById(R.id.main_error_view);
        errorView.setVisibility(View.GONE);
        mRecyclerView = findViewById(R.id.recyclerViewTasks);


        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new SenderAdapter(this, (SenderAdapter.ItemClickListener) this);
        mRecyclerView.setAdapter(mAdapter);

        swipeRefreshLayout = findViewById(R.id.swipe_container);

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(TAG, "Starting request");
                        Constraints constraints = new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build();
                        OneTimeWorkRequest updateRequest =
                                new OneTimeWorkRequest.Builder(UnreadCountWorker.class)
                                        .setConstraints(constraints)
                                        .build();
                        //new CheckMailsTask().execute();*/
                        WorkManager.getInstance().enqueueUniqueWork("RefreshMails", ExistingWorkPolicy.REPLACE, updateRequest);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

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

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        @SuppressLint("RestrictedApi") PeriodicWorkRequest updateRequest =
                new PeriodicWorkRequest.Builder(UnreadCountWorker.class, 30, TimeUnit.MINUTES)
                        // Constraints
                        .setConstraints(constraints)
                        //.setBackoffCriteria(BackoffPolicy.EXPONENTIAL, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                        .build();
        Log.i(TAG, "Starting request");
        /*OneTimeWorkRequest updateRequest =
                new OneTimeWorkRequest.Builder(UnreadCountWorker.class)
                        .build();
        //new CheckMailsTask().execute();*/
        WorkManager.getInstance().enqueueUniquePeriodicWork("CheckMails", ExistingPeriodicWorkPolicy.REPLACE, updateRequest);

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(updateRequest.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(@Nullable WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Log.i(TAG, "Work finished!");
                        }
                        if (workInfo != null) {
                            Log.i(TAG, "SimpleWorkRequest: " + workInfo.getState().name() + "\n");
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
        viewModel.getSenders().observe(this, (List<Sender> senders) -> {
            mAdapter.notifyDataSetChanged();
            errorView.setVisibility(View.GONE);
            Log.i(TAG, "Dataset Changed");
            Log.i(TAG, "Senders size: " + senders.size() + "");
            if (senders.size() == 0) {
                errorView.setVisibility(View.VISIBLE);
                errorView.setText(R.string.no_sender_message);
                Log.i(TAG, "No Senders added yet!!");
            }
            mAdapter.setSenders(senders);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        EncryptedSharedPreferences sharedPreferences = null;
        /*String masterKeyAlias = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyGenParameterSpec keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC;

            try {
                masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            masterKeyAlias = BuildConfig.MASTER_KEY;
        }

        try {
            //TODO: Later update the create function and use MasterKey when upgrading to minSDK 23(M)
            sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    LoginActivity.PREFS_NAME,
                    masterKeyAlias,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M) {
            String masterKeyAlias = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                KeyGenParameterSpec keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC;

                try {
                    masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                masterKeyAlias = BuildConfig.MASTER_KEY;
            }

            try {
                sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                        LoginActivity.PREFS_NAME,
                        masterKeyAlias,
                        this,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                        this,
                        LoginActivity.PREFS_NAME,
                        new MasterKey.Builder(this).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        assert sharedPreferences != null;
        //SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
        //sharedPrefsEditor.clear().commit();
        sharedPreferences.edit().remove("Username").remove("Password").remove("hasLoggedIn").commit();
        //ArrayList<Email> emails = new ArrayList<>();
        //mDb.senderDao().resetTable(emails);
        new LogoutTask().execute();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        //ArrayList<Email> emails = new ArrayList<>();
        //AppDatabase.getInstance(getApplicationContext()).senderDao().resetTable(emails);

        finish();
    }

    @Override
    public void onItemClickListener(Sender sender) {
        // Launch AddTaskActivity adding the itemId as an extra in the intent
        Log.i(TAG, "Item Clicked");
        Intent intent = new Intent(MainActivity.this, ShowMailsActivity.class);
        intent.putExtra("Email Details", sender);
        startActivity(intent);
    }

    /*private class CheckMailsTask extends AsyncTask<Void, Void, Void> {

        protected void onPostExecute(Long result) {
            Log.i("MainActivity", "Finished");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            CheckMail.main(null);
            return null;
        }
    }*/
    private class LogoutTask extends AsyncTask<Void, Void, Void> {

        protected void onPostExecute(Long result) {
            Log.i(TAG, "Logged out");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ArrayList<Email> emails = new ArrayList<>();
            mDb.senderDao().resetTable(emails);
            return null;
        }
    }
}
