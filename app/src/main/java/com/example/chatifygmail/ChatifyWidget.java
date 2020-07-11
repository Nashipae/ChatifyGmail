package com.example.chatifygmail;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.chatifygmail.database.AppDatabase;
import com.example.chatifygmail.database.Sender;

import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class ChatifyWidget extends AppWidgetProvider {
    private static Context mContext;
    private static CharSequence widgetText;
    private static RemoteViews views;
    private static AppWidgetManager appWidgetManager;
    private static ComponentName chatifyWidget;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        mContext = context;
        ChatifyWidget.appWidgetManager = appWidgetManager;
        //widgetText = context.getString(R.string.appwidget_text);

        // Construct the RemoteViews object
        views = new RemoteViews(context.getPackageName(), R.layout.chatify_widget);
        chatifyWidget = new ComponentName(context, ChatifyWidget.class);
        views.setTextViewText(R.id.appwidget_text, "Hello");

        new GetSendersTask().execute();

        Class destinationClass=MainActivity.class;
        Intent intentToStart=new Intent(context,destinationClass);
        //intentToStart.putExtra("Recipe",Recipes.getRecipes()[recipePosition]);
        //intentToStart.putExtra("RecipePosition",recipePosition);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentToStart, 0);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
    private static class GetSendersTask extends AsyncTask<Void, Void, List<Sender>> {

        protected void onPostExecute(List<Sender> result) {
            Log.i("Widget","Finished");
            String widgetString = "";
            for(Sender sender: result){
                widgetString+=sender.getEmailAddress()+" - "+sender.getUnread()+System.lineSeparator();
            }
            widgetText = widgetString;
            if(widgetText.equals("")){
                widgetText = "You have no registered senders yet";
            }
            Log.i("Widget", "Text received: "+widgetText);
            //views.setTextViewText(R.id.appwidget_text, widgetText);
            if (appWidgetManager != null) {
                String finalString = "sync @";
                views.setTextViewText(R.id.appwidget_text, widgetText);
                appWidgetManager.updateAppWidget(chatifyWidget, views);
            }
        }

        @Override
        protected List<Sender> doInBackground(Void... voids) {
            return AppDatabase.getInstance(ChatifyWidget.mContext).senderDao().loadAllSendersSync();
        }
    }
}

