package com.example.chatifygmail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatifygmail.database.Sender;

import javax.mail.AuthenticationFailedException;

public class ShowMailsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ShowMailsActivity.class.getSimpleName() ;
    private RecyclerView mRecyclerView;
    private MailAdapter mAdapter;
    private TextView emailSenderView;
    private EditText contentsEditText;
    private ImageButton sendButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_mails);
        Sender sender = getIntent().getExtras().getParcelable("Email Details");
        Log.i(TAG,sender.getEmails().get(0).getSubject());
        //TODO: Later retreive actual mails and show similar to an actual mail when clicked and before that show as a chat
        //TODO: For now store in Database so that works when offline too
        emailSenderView = findViewById(R.id.sender_header);
        emailSenderView.setText(sender.getEmailAddress());
        mRecyclerView = findViewById(R.id.mails_recycler_view);
        contentsEditText = findViewById(R.id.mail_content_edit_text);
        sendButton = findViewById(R.id.button_mail_send);
        sendButton.setOnClickListener(this);

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
            //TODO: Change user and pwd
            EncryptedSharedPreferences sharedPreferences = LoginActivity.getSharedPreferences();
            String username = sharedPreferences.getString("Username","");
            String password = sharedPreferences.getString("Password","");
            new SendMailsTask().execute(username, password, emailSenderView.getText().toString(), "Sent from Chatify", contents);
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
}