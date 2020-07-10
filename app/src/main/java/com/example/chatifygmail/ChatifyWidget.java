package com.example.chatifygmail;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.chatifygmail.database.AppDatabase;
import com.example.chatifygmail.database.Sender;

import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class ChatifyWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        List<Sender> senders = AppDatabase.getInstance(context).senderDao().loadAllSendersSync();
        String widgetString = "";
        for(Sender sender: senders){
            widgetString+=sender.getEmailAddress()+" - "+sender.getUnread()+System.lineSeparator();
        }
        widgetText = widgetString;
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.chatify_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

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
}

