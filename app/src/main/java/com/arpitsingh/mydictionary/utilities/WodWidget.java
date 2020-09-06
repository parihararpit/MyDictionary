package com.arpitsingh.mydictionary.utilities;
/**
 * Created by ARPIT SINGH
 * 17/10/18
 */

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

import com.arpitsingh.mydictionary.R;
import com.arpitsingh.mydictionary.activities.DrawerListener;
import com.arpitsingh.mydictionary.activities.SignInActivity;
import com.arpitsingh.mydictionary.data.DictContract;
import com.arpitsingh.mydictionary.model.WordDefinition;
import com.arpitsingh.mydictionary.networking.SyncService;
import com.google.firebase.auth.FirebaseAuth;

import static com.arpitsingh.mydictionary.networking.SyncService.ACTION_SHOW_WOD;
import static com.arpitsingh.mydictionary.utilities.DictionaryDateUtils.DEFAULT_DATE;


public class WodWidget extends AppWidgetProvider {

    public static void updateWodWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wod_widget);
        //if not logged in just show msg to log in, on click send to sign in activity
        boolean signedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        if (!signedIn) {
            Intent signInIntent = new Intent(context, SignInActivity.class);
            PendingIntent signInPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    signInIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            views.setTextViewText(R.id.widget_word_definition, context.getString(R.string.sign_in_msg));
            views.setViewVisibility(R.id.widget_head_word, View.GONE);
            views.setOnClickPendingIntent(R.id.wod_widget_rl, signInPendingIntent);
        } else {
            boolean hasWodLocally = true;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String lastSyncDate = preferences.getString(context.getString(R.string.WOD_DATE_KEY), DEFAULT_DATE);
            if (lastSyncDate.equals(DEFAULT_DATE)) {
                hasWodLocally = false;
            }
            String wordId = preferences.getString(context.getString(R.string.WOD_WORD_ID_KEY), "");

            if (!hasWodLocally) {
                //show retry and on click start the sync service
                Intent startSyncIntent = new Intent(context, SyncService.class);
                startSyncIntent.setAction(SyncService.ACTION_SYNC_WOD);
                PendingIntent startSyncPendingIntent = PendingIntent.getService(
                        context,
                        0,
                        startSyncIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                views.setTextViewText(R.id.widget_head_word, context.getString(R.string.retry));
                views.setViewVisibility(R.id.widget_word_definition, View.GONE);
                views.setOnClickPendingIntent(R.id.wod_widget_rl, startSyncPendingIntent);
            } else {
                //query and show words
                Cursor data = context.getContentResolver().query(DictContract.DictEntry.DEFINITION_CONTENT_URI,
                        DictionaryDBUtils.WORD_COMPLETE_PROJECTION,
                        DictContract.DictEntry.WORD_ID + " =?",
                        new String[]{wordId},
                        null);
                WordDefinition word = DictionaryDBUtils.getWordFromCursor(data);
                Intent showWodIntent = new Intent(context, DrawerListener.class);
                showWodIntent.setAction(ACTION_SHOW_WOD);
                PendingIntent showWodPendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        showWodIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                views.setViewVisibility(R.id.widget_head_word, View.VISIBLE);
                views.setViewVisibility(R.id.widget_word_definition, View.VISIBLE);
                views.setTextViewText(R.id.widget_head_word, word.word);
                views.setTextViewText(R.id.widget_word_definition, word.single_definition);
                views.setOnClickPendingIntent(R.id.wod_widget_rl, showWodPendingIntent);
            }
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateWodWidget(context, appWidgetManager, appWidgetId);
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

