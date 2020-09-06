package com.arpitsingh.mydictionary.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arpitsingh.mydictionary.DictionaryApplication;

import java.util.Locale;

public class DictProvider extends ContentProvider {
    private static final int HISTORY = 100;
    private static final int HISTORY_ID = 101;
    private static final int FAVOURITE = 200;
    private static final int FAVOURITE_ID = 201;
    private static final int FAVOURITE_WORD_ID = 202;
    private static final int SEARCH = 300;
    public static final int DEFINITION = 400;
    private static final int DEFINITION_WORD_ID = 401;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(DictContract.DICT_AUTHORITY, DictContract.DictEntry.HISTORY_TABLE_NAME, HISTORY);
        sUriMatcher.addURI(DictContract.DICT_AUTHORITY, DictContract.DictEntry.HISTORY_TABLE_NAME + "/#", HISTORY_ID);
        sUriMatcher.addURI(DictContract.DICT_AUTHORITY, DictContract.DictEntry.FAVOURITE_TABLE_NAME, FAVOURITE);
        sUriMatcher.addURI(DictContract.DICT_AUTHORITY, DictContract.DictEntry.FAVOURITE_TABLE_NAME + "/#", FAVOURITE_ID);
        sUriMatcher.addURI(DictContract.DICT_AUTHORITY, DictContract.DictEntry.FAVOURITE_TABLE_NAME + "/" + DictContract.DictEntry.WORD_ID + "/.+", FAVOURITE_WORD_ID);
        sUriMatcher.addURI(DictContract.DICT_AUTHORITY, DictContract.DictEntry.SEARCH_TABLE_NAME, SEARCH);
        sUriMatcher.addURI(DictContract.DICT_AUTHORITY, DictContract.DictEntry.DEFINITION_TABLE_NAME, DEFINITION);
        sUriMatcher.addURI(DictContract.DICT_AUTHORITY, DictContract.DictEntry.DEFINITION_TABLE_NAME + "/" + DictContract.DictEntry.WORD_ID + "/.+", DEFINITION_WORD_ID);
    }

    private final String LOG_TAG = this.getClass().toString();
    DictDbHelper mDictDb;

    @Override
    public boolean onCreate() {
        mDictDb = new DictDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = sUriMatcher.match(uri);
        Cursor cursor = null;
        SQLiteDatabase db = mDictDb.getReadableDatabase();
        switch (match) {

            case HISTORY:
                cursor = db.query(DictContract.DictEntry.HISTORY_TABLE_NAME,
                        projection,
                        null, null, null, null, sortOrder);
                break;
            case HISTORY_ID:
                Long id = ContentUris.parseId(uri);
                selection = DictContract.DictEntry._ID + " ?=";
                selectionArgs = new String[]{String.valueOf(id)};
                cursor = db.query(DictContract.DictEntry.HISTORY_TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case FAVOURITE:
                cursor = db.query(DictContract.DictEntry.FAVOURITE_TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FAVOURITE_ID:
                selection = DictContract.DictEntry._ID + " ?=";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(DictContract.DictEntry.FAVOURITE_TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FAVOURITE_WORD_ID:
                selection = DictContract.DictEntry.WORD_ID;
                selectionArgs = new String[]{uri.getLastPathSegment()};
                cursor = db.query(DictContract.DictEntry.FAVOURITE_TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case SEARCH:
                cursor = db.query(DictContract.DictEntry.SEARCH_TABLE_NAME, projection, null,
                        null, null, null, null);
                break;
            case DEFINITION:
                cursor = db.query(DictContract.DictEntry.DEFINITION_TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case DEFINITION_WORD_ID:
                selection = DictContract.DictEntry.WORD_ID;
                selectionArgs = new String[]{uri.getLastPathSegment()};
                cursor = db.query(DictContract.DictEntry.DEFINITION_TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                Log.e(LOG_TAG, "Failed to query for : " + uri);
        }
        if (cursor != null)
            cursor.setNotificationUri(DictionaryApplication.getInstance().getApplicationContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case HISTORY:
                return DictContract.DictEntry.HISTORY_TABLE_MIME;
            case HISTORY_ID:
                return DictContract.DictEntry.HISTORY_ROW_MIME;
            default:
                throw new IllegalArgumentException("Invalid URI : " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int match = sUriMatcher.match(uri);
        SQLiteDatabase db = mDictDb.getWritableDatabase();
        //new contentValue to be used for insertion
        ContentValues values = new ContentValues();
        //implementing sanity checks
        String word = null;
        if (contentValues != null)
            word = contentValues.getAsString(DictContract.DictEntry.WORD);
        if (word != null && !word.isEmpty()) {
            word = word.trim();
            values.put(DictContract.DictEntry.WORD, word.toLowerCase(Locale.getDefault()));
        } else return null;
        //now use values fr insertions.
        Long id = -1L;
        switch (match) {
            case HISTORY:
                id = db.insert(DictContract.DictEntry.HISTORY_TABLE_NAME, null, values);//not contentValues because we need sanity checks.
                break;
            case FAVOURITE:
                String word_id = contentValues.getAsString(DictContract.DictEntry.WORD_ID);
                if (word_id != null && !word_id.isEmpty()) {
                    id = db.insert(DictContract.DictEntry.FAVOURITE_TABLE_NAME, null, contentValues);
                } else return null;
                break;
            case DEFINITION:
                id = db.insert(DictContract.DictEntry.DEFINITION_TABLE_NAME, null, contentValues);
                break;
            case SEARCH:
                //Log.d("Insertinon", "Successful");
                String wid = contentValues.getAsString(DictContract.DictEntry.WORD_ID);
                if (wid != null && !wid.isEmpty())
                    id = db.insert(DictContract.DictEntry.SEARCH_TABLE_NAME, null, contentValues);
                break;
            default:
                throw new IllegalArgumentException("Failed to insert for : " + uri);
        }
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert for " + uri);
            return null;
        }
        Context context = DictionaryApplication.getInstance().getApplicationContext();
        context.getContentResolver().notifyChange(ContentUris.withAppendedId(uri, id), null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] contentValues) {
        int rows = 0;
        try {
            if (sUriMatcher.match(uri) == SEARCH) {
                SQLiteDatabase db = mDictDb.getWritableDatabase();
                for (ContentValues values : contentValues) {
                    db.insert(DictContract.DictEntry.SEARCH_TABLE_NAME, null, values);
                }
            }
        }catch (Exception e){
            Log.e(LOG_TAG,"Failed to insert for uri: "+uri.toString());
            e.printStackTrace();
        }

        return rows;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        int rows = 0;
        SQLiteDatabase db = mDictDb.getWritableDatabase();
        switch (match) {
            case HISTORY_ID:
                Long id = ContentUris.parseId(uri);
                selection = DictContract.DictEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(id)};
                rows = db.delete(DictContract.DictEntry.HISTORY_TABLE_NAME, selection, selectionArgs);
                break;
            case HISTORY:
                rows = db.delete(DictContract.DictEntry.HISTORY_TABLE_NAME, selection, selectionArgs);
                break;
            case FAVOURITE:
                rows = db.delete(DictContract.DictEntry.FAVOURITE_TABLE_NAME, selection, selectionArgs);
                break;
            case FAVOURITE_ID:
                Long mid = ContentUris.parseId(uri);
                selection = DictContract.DictEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(mid)};
                rows = db.delete(DictContract.DictEntry.FAVOURITE_TABLE_NAME, selection, selectionArgs);
                break;
            case FAVOURITE_WORD_ID:
                selection = DictContract.DictEntry.WORD_ID;
                selectionArgs = new String[]{uri.getLastPathSegment()};
                rows = db.delete(DictContract.DictEntry.FAVOURITE_TABLE_NAME, selection, selectionArgs);
                break;
            case DEFINITION:
                rows = db.delete(DictContract.DictEntry.DEFINITION_TABLE_NAME, selection, selectionArgs);
                break;
            case DEFINITION_WORD_ID:
                selection = DictContract.DictEntry.WORD_ID;
                selectionArgs = new String[]{uri.getLastPathSegment()};
                rows = db.delete(DictContract.DictEntry.DEFINITION_TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unable to delete for : " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        int rows=0;
        SQLiteDatabase db = mDictDb.getWritableDatabase();
        String word = null;
        if (contentValues != null)
            word = contentValues.getAsString(DictContract.DictEntry.WORD);
        if (word == null) {
            throw new IllegalArgumentException("Word requires a valid key string");
        }
        switch (match) {
            case HISTORY_ID:
                Long id = ContentUris.parseId(uri);
                selection = DictContract.DictEntry._ID + "?=";
                selectionArgs = new String[]{String.valueOf(id)};
                rows = db.update(DictContract.DictEntry.HISTORY_TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case FAVOURITE_WORD_ID:
                if (contentValues.getAsString(DictContract.DictEntry.WORD_ID) == null)
                    throw new IllegalArgumentException("Word should must have a valid id");
                selection = DictContract.DictEntry.WORD_ID;
                selectionArgs = new String[]{uri.getLastPathSegment()};
                db.update(DictContract.DictEntry.FAVOURITE_TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case DEFINITION_WORD_ID:
                if (contentValues.getAsString(DictContract.DictEntry.WORD_ID) == null)
                    throw new IllegalArgumentException("Word should must have a valid word id");
                selection = DictContract.DictEntry.WORD_ID;
                selectionArgs = new String[]{uri.getLastPathSegment()};
                db.update(DictContract.DictEntry.DEFINITION_TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Failed to update for : " + uri);
        }
        Context context = DictionaryApplication.getInstance().getApplicationContext();
        context.getContentResolver().notifyChange(uri, null);
        return rows;
    }
}