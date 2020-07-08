package com.example.chatifygmail;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new CheckMailsTask().execute();
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
