package com.example.chatifygmail;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.chatifygmail.data.Email;
import com.example.chatifygmail.database.AppDatabase;
import com.example.chatifygmail.database.Sender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.Context.ACTIVITY_SERVICE;

public class UnreadCountWorker extends Worker {

    private static final String TAG = "UnreadCounterWorker";
    //private AppDatabase mDb;
    //private SenderAdapter mAdapter;
    private List<Sender> senders;
    private Context context;
    private int lastMessageNumber;
    private String unreadSenders="";
    private int unreadSenderCount=0;

    public String getUnreadSenders() {
        return unreadSenders;
    }

    public void setUnreadSenders(String unreadSenders) {
        this.unreadSenders = unreadSenders;
    }

    public List<Sender> getSenders() {
        return senders;
    }

    public void setSenders(List<Sender> senders) {
        this.senders = senders;
    }

    public UnreadCountWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG,"In Worker");
        //mAdapter = new SenderAdapter(context, (SenderAdapter.ItemClickListener) this);
        //mDb = AppDatabase.getInstance(getApplicationContext());
        //setupViewModel();
        //senders = mAdapter.getSenders();
        //LiveData<List<Sender>> liveSenders = AppDatabase.getInstance(getApplicationContext()).senderDao().loadAllSenders();
        //setSenders(liveSenders.getValue());
        /*liveSenders.observe((LifecycleOwner) context, new Observer<List<Sender>>() {
            @Override
            public void onChanged(@Nullable List<Sender> senders){
                Log.i(TAG,"Got Senders by Live Data and their number is: "+senders.size());
            }
        });*/
        setSenders(AppDatabase.getInstance(getApplicationContext()).senderDao().loadAllSendersSync());

        Log.i(TAG,"Got Senders and their number is: "+senders.size());
        //TODO: Update the counts
        updateUnreadCount();
        return Result.success();
    }

    /*private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of((FragmentActivity) context, ViewModelProvider.AndroidViewModelFactory.getInstance((Application) context.getApplicationContext())).get(MainViewModel.class);
        viewModel.getSenders ().observe ((LifecycleOwner) context, (List<Sender>senders) -> {
            mAdapter.notifyDataSetChanged ();
            Log.i(TAG,"Dataset Changed");
            Log.i(TAG,"Senders size: "+senders.size()+"");
            if(senders.size()==0){
                Log.i(TAG,"No Emails added yet!!");
            }
            mAdapter.setSenders (senders);
            Log.i(TAG,"Sender 1: "+senders.get(0).getEmailAddress());
        });
    }*/

    private void updateUnreadCount() {
        unreadSenders = "";
        unreadSenderCount=0;

        for (Sender sender:getSenders()) {
            int oldCount=0;
            if(sender.getEmails()!=null)
                if(sender.getEmails().size()!=0)
                    oldCount = sender.getEmails().size();
            Log.i(TAG,"Sender's Email: "+sender.getEmailAddress());
            Log.i(TAG,"Sender's Unread: "+sender.getUnread()+"");
            if(sender.getEmails()!=null)
                if(sender.getEmails().size()!=0)
                    lastMessageNumber = sender.getEmails().get(sender.getEmails().size()-1).getMessageNumber();
            //TODO: Change user and pwd
            EncryptedSharedPreferences sharedPreferences = LoginActivity.getSharedPreferences();
            String username = sharedPreferences.getString("Username","");
            String password = sharedPreferences.getString("Password","");
            ArrayList<Email> emails = CheckMail.checkUnreadEmailBySender("imap.gmail.com", "imaps", username, password, sender.getEmailAddress());
            sender.setUnread(emails.size());
            sender.setEmails(emails);
            Log.i(TAG,"Sender's Unread After Update: "+sender.getEmailAddress());
            AppDatabase.getInstance(getApplicationContext()).senderDao().updateSender(sender);
            Log.i(TAG,"Updated");
            //Log.i(TAG,"Received Email Subject: "+emails.get(0).getSubject());
            int currentLastMessageNumber = 0;
            //currentLastMessageNumber = emails.get(emails.size()-1).getMessageNumber();
            Log.i(TAG,"Current: "+currentLastMessageNumber);
            Log.i(TAG,"Last: "+lastMessageNumber);
            if(currentLastMessageNumber>lastMessageNumber||oldCount<emails.size()){
                unreadSenders+=sender.getEmailAddress()+System.lineSeparator();
                unreadSenderCount++;
            }
        }
        if(!unreadSenders.equals("")){
            sendNotification("You have unread mails", unreadSenders);
        }
    }

    public void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancelAll();

        //If on Oreo then notification required a notification channel.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(getApplicationContext(),MainActivity.class );
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        if(!checkApp()) {
            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "default")
                    .setContentTitle(title)
                    .setContentText("Received new mails from " + unreadSenderCount + " senders")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(message))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            notificationManager.notify(1, notification.build());
        }
    }
    public boolean checkApp(){
        ActivityManager am = (ActivityManager) getApplicationContext()
                .getSystemService(ACTIVITY_SERVICE);

        // get the info from the currently running task
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

        ComponentName componentInfo = taskInfo.get(0).topActivity;
        if (componentInfo.getPackageName().equalsIgnoreCase("com.example.chatifygmail")) {
            return true;
        } else {
            return false;
        }
    }

}
