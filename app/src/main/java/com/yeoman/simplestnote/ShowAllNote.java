package com.yeoman.simplestnote;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class ShowAllNote extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.show_all_note);

        SimplestNoteDbHelper mDbHelper = new SimplestNoteDbHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(FeedEntry.SelectALL, null);
        String content;
        // Instruct the widget manager to update the widget
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            content = cursor.getString(1);
            views.setTextViewText(R.id.textView1, content);
            cursor.moveToNext();
            if (!cursor.isAfterLast()) {
                content = cursor.getString(1);
                views.setTextViewText(R.id.textView2, content);
                cursor.moveToNext();
                if (!cursor.isAfterLast()) {
                    content = cursor.getString(1);
                    views.setTextViewText(R.id.textView3, content);
                }
            }
        }
        cursor.close();
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.show_all_note);
            Intent configIntent = new Intent(context, MainActivity.class);

            PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);

            remoteViews.setOnClickPendingIntent(R.id.widget_layout, configPendingIntent);
            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
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

