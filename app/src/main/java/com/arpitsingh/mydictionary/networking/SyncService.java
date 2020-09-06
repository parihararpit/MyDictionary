package com.arpitsingh.mydictionary.networking;

/**
 * Created by ARPIT SINGH
 * 11/10/19
 */

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.arpitsingh.mydictionary.R;
import com.arpitsingh.mydictionary.data.DictContract;
import com.arpitsingh.mydictionary.model.WordDefinition;
import com.arpitsingh.mydictionary.utilities.DictionaryDBUtils;
import com.arpitsingh.mydictionary.utilities.DictionaryDateUtils;
import com.arpitsingh.mydictionary.utilities.DictionaryJsonUtils;
import com.arpitsingh.mydictionary.utilities.WodWidget;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncService extends IntentService {

    public final String LOG_TAG = SyncService.class.getSimpleName();
    public static final String ACTION_SYNC_WOD =
            "com.example.android.dictionary.action.sync_wod";

    public static final String ACTION_SYNC_WOD_FINISHED =
            "com.example.android.dictionary.action.sync_wod_complete";

    public static final String ACTION_LOAD_WOD =
            "com.example.android.dictionary.action.load_wod";


    public static final String ACTION_SHOW_WOD =
            "com.example.android.dictionary.action.show_wod";


    public SyncService() {
        super("SyncService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SYNC_WOD.equals(action)) {
                handleSyncWod();
            } else if (ACTION_LOAD_WOD.equals(action)) {
                handleWidgetUpdate();
            }
        }
    }

    public static void startActionSyncWod(Context context) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_SYNC_WOD);
        context.startService(intent);
    }

    private void handleSyncWod() {
        //download data from cloud and update database
        Log.d(LOG_TAG, "Sync Service started.");
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("word_of_day");
        String jsonFileName = DictionaryDateUtils.getTodayInStringFormat().replace("/", "");
        Log.d("Filename", jsonFileName);
        try {
            storageReference.child(jsonFileName + ".json").getDownloadUrl()
                    .addOnCompleteListener((@NonNull Task<Uri> task) -> {
                                if (task.getResult() != null) {
                                    Uri uri = task.getResult();
                                    Log.d(LOG_TAG, "Download url: " + uri.toString());
                                    //start asyc task to start download
                                    NetworkUtils.queryWordOfDay(uri, new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, @NonNull Response<String> response) {
                                            WordDefinition word = DictionaryJsonUtils.getSingleWord(response.body());
                                            DictionaryDBUtils.insertWordIntoDatabase(
                                                    SyncService.this,
                                                    DictContract.DictEntry.DEFINITION_CONTENT_URI,
                                                    word);

                                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SyncService.this);
                                            SharedPreferences.Editor editor = preferences.edit();

                                            editor.putString(getString(R.string.WOD_DATE_KEY), DictionaryDateUtils.getTodayInStringFormat());
                                            editor.putString(getString(R.string.WOD_WORD_ID_KEY), word.id);

                                            editor.apply();
                                            handleWidgetUpdate();
                                            LocalBroadcastManager.getInstance(SyncService.this).sendBroadcast(new Intent(ACTION_SYNC_WOD_FINISHED));
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, @NonNull Throwable t) {
                                            Log.d(LOG_TAG, "Failed to download wod from url: " + uri.toString());
                                            t.printStackTrace();
                                        }
                                    });
                                }
                            }
                    );
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to get word of day json data from firebase.");
            e.printStackTrace();
            handleWidgetUpdate();
            LocalBroadcastManager.getInstance(SyncService.this).sendBroadcast(new Intent(ACTION_SYNC_WOD_FINISHED));
        }
    }

    private void handleWidgetUpdate() {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
        int[] widgetIds = widgetManager.getAppWidgetIds(new ComponentName(this, WodWidget.class));
        if (widgetIds == null || widgetIds.length <= 0)
            return;
        for (int widgetId : widgetIds)
            WodWidget.updateWodWidget(this, widgetManager, widgetId);
    }

}
