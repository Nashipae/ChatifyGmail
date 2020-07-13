package com.example.chatifygmail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class LoginActivity extends AppCompatActivity {
    private String username;
    private String password;
    private ProgressBar loadingProgressBar;
    private ImageView posterImageView;
    private static EncryptedSharedPreferences sharedPreferences;

    public static EncryptedSharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static void setSharedPreferences(EncryptedSharedPreferences sharedPreferences) {
        LoginActivity.sharedPreferences = sharedPreferences;
    }

    public static final String PREFS_NAME = "ChatifyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        //Get "hasLoggedIn" value. If the value doesn't exist yet false is returned
        posterImageView = findViewById(R.id.posterImageView);
        int width  = Resources.getSystem().getDisplayMetrics().widthPixels;
        //int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        int targetHeight = (int) ((2/(float)3)*width);
        Picasso.get() .load(R.drawable.chatify_poster).resize(width,targetHeight).centerCrop().into(posterImageView);
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
                    LoginActivity.this,
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
                        LoginActivity.this,
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

        boolean hasLoggedIn = sharedPreferences.getBoolean("hasLoggedIn", false);

        Log.i("LoggedIn", hasLoggedIn + "");
        if (hasLoggedIn) {
            Toast.makeText(this, "Logged in as " + sharedPreferences.getString("Username", ""), Toast.LENGTH_LONG).show();
            goToMainActivity();
            finish();
        }
        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        loadingProgressBar = findViewById(R.id.loading);
        loginButton.setEnabled(true);
        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            username = usernameEditText.getText().toString();
            password = passwordEditText.getText().toString();
            login(username, password);
        });
    }

    private void login(String username, String password) {
        //int flag = CheckMail.validateMail(username, password);
        new ValidateMailTask().execute(username, password);
        /*if(flag == 0) {
            //User has successfully logged in, save this information
            // We need an Editor object to make preference changes.
            SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0); // 0 - for private mode
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("Username", username);
            editor.putString("Password", password);
            //Set "hasLoggedIn" to true
            editor.putBoolean("hasLoggedIn", true);

            // Commit the edits!
            editor.commit();
            Toast.makeText(this,"Username: "+username+" Validated",Toast.LENGTH_LONG).show();
            //goToMainActivity();
            //finish();
        }
        if(flag == 1){
            Toast.makeText(this,"Please check your email/password",Toast.LENGTH_LONG).show();
        }
        if(flag == 2){
            Toast.makeText(this,"There seems to be an error. Try Again!!",Toast.LENGTH_LONG).show();
        }*/
    }

    public void goToMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    private class ValidateMailTask extends AsyncTask<String, Void, Integer> {

        protected void onPostExecute(Integer result) {
            Log.i("AsynTask", "Finished");
            if (result == 0) {
                //User has successfully logged in, save this information
                // We need an Editor object to make preference changes.
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
                            LoginActivity.this,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    );
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
                sharedPrefsEditor.putString("Username", username);
                sharedPrefsEditor.putString("Password", password);
                //Set "hasLoggedIn" to true
                sharedPrefsEditor.putBoolean("hasLoggedIn", true);

                // Commit the edits!
                sharedPrefsEditor.apply();
                Toast.makeText(LoginActivity.this, "Logged in as " + username, Toast.LENGTH_LONG).show();
                loadingProgressBar.setVisibility(View.GONE);
                goToMainActivity();
                finish();
            }
            if (result == 1) {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Please check your email/password", Toast.LENGTH_LONG).show();

            }
            if (result == 2) {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "There seems to be an error. Try Again!!", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Integer doInBackground(String... strings) {
            int error = 0;
            error = CheckMail.validateMail(strings[0], strings[1]);
            return error;
        }

    }
}