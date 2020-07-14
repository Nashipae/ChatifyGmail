package com.example.chatifygmail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.example.chatifygmail.database.Sender;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.mail.AuthenticationFailedException;

public class ShowMailsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ShowMailsActivity.class.getSimpleName() ;
    private RecyclerView mRecyclerView;
    private MailAdapter mAdapter;
    //private Toolbar showMailsToolbar;
    private EditText contentsEditText;
    private ImageButton sendButton;
    private TextView errorView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_mails);
        //showMailsToolbar = findViewById(R.id.show_mails_toolbar);
        //setSupportActionBar(showMailsToolbar);
        Sender sender = getIntent().getExtras().getParcelable("Email Details");
        //Log.i(TAG,sender.getEmails().get(0).getSubject());
        //TODO: Later retreive actual mails and show similar to an actual mail when clicked and before that show as a chat
        //TODO: For now store in Database so that works when offline too
        errorView = findViewById(R.id.show_mails_error_view);
        errorView.setVisibility(View.GONE);
        getSupportActionBar().setTitle(sender.getEmailAddress());

        mRecyclerView = findViewById(R.id.mails_recycler_view);
        contentsEditText = findViewById(R.id.mail_content_edit_text);
        sendButton = findViewById(R.id.button_mail_send);
        sendButton.setOnClickListener(this);
        if(sender.getEmails()==null){
            errorView.setVisibility(View.VISIBLE);
            errorView.setText("You have no unread emails from this sender");
        }
        else if(sender.getEmails().size()==0){
            errorView.setVisibility(View.VISIBLE);
            errorView.setText("You have no unread emails from this sender");
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new MailAdapter(this);
        mAdapter.setSender(sender);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onClick(View view) {
        String contents = contentsEditText.getText().toString();
        if(!contents.equals("")){
            //DONE: Change user and pwd
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
            String username = sharedPreferences.getString("Username","");
            String password = sharedPreferences.getString("Password","");
            new SendMailsTask().execute(username, password, getSupportActionBar().getTitle().toString(), "Sent from Chatify", contents);
        }
        else
            Toast.makeText(this,"Contents are empty",Toast.LENGTH_LONG).show();
    }

    private class SendMailsTask extends AsyncTask<String, Void, Integer> {

        protected void onPostExecute(Integer result) {
            Log.i("AsynTask","Finished");
            if(result == 0){
                Toast.makeText(ShowMailsActivity.this,"Mail sent",Toast.LENGTH_LONG).show();
                contentsEditText.setText("");
                hideKeyboard(ShowMailsActivity.this);

            }
            if(result==-1){
                Toast.makeText(ShowMailsActivity.this, "Allow Less Secure Apps in your Gmail to send the mail", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Integer doInBackground(String... strings) {
            int error=0;
            error = CheckMail.sendMail(strings[0],strings[1],strings[2],strings[3],strings[4]);
            return error;
        }

    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}