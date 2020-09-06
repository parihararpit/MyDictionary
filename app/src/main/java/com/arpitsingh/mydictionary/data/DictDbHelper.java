package com.arpitsingh.mydictionary.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DictDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "dictionary.db";
    private static final int DATABASE_VERSION = 1;

    private String SQL_CREATE_HISTORY_TABLE = "CREATE TABLE "
            + DictContract.DictEntry.HISTORY_TABLE_NAME
            + " ("
            + DictContract.DictEntry._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , "
            + DictContract.DictEntry.WORD + " TEXT NOT NULL UNIQUE ON CONFLICT IGNORE "
            + " );";

    private String SQL_CREATE_FAVOURITE_TABLE = "CREATE TABLE "
            + DictContract.DictEntry.FAVOURITE_TABLE_NAME
            + " ("
            + DictContract.DictEntry._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
            + DictContract.DictEntry.WORD_ID + " TEXT NOT NULL UNIQUE ON CONFLICT IGNORE, "
            + DictContract.DictEntry.WORD + " TEXT NOT NULL, "
            + DictContract.DictEntry.LANGUAGE + " TEXT, "
            + DictContract.DictEntry.SINGLE_DEFINITION + " TEXT, "
            + DictContract.DictEntry.PRONNUNCIATION_URL + " TEXT, "
            + DictContract.DictEntry.PRONNUNCIATION_TEXT + " TEXT, "
            + DictContract.DictEntry.LEXICAL_ENTRIES + " TEXT"
            + " );";

    private String SQL_CREATE_DEFINITION_TABLE = "CREATE TABLE "
            + DictContract.DictEntry.DEFINITION_TABLE_NAME
            + " ("
            + DictContract.DictEntry._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
            + DictContract.DictEntry.WORD_ID + " TEXT NOT NULL UNIQUE ON CONFLICT IGNORE,"
            + DictContract.DictEntry.WORD + " TEXT NOT NULL, "
            + DictContract.DictEntry.LANGUAGE + " TEXT, "
            + DictContract.DictEntry.SINGLE_DEFINITION + " TEXT, "
            + DictContract.DictEntry.PRONNUNCIATION_URL + " TEXT, "
            + DictContract.DictEntry.PRONNUNCIATION_TEXT + " TEXT, "
            + DictContract.DictEntry.LEXICAL_ENTRIES + " TEXT"
            + " );";

    private String SQL_CREATE_SEARCH_TABLE = "CREATE TABLE "
            + DictContract.DictEntry.SEARCH_TABLE_NAME + " ("
            + DictContract.DictEntry._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
            + DictContract.DictEntry.WORD_ID + " TEXT NOT NULL UNIQUE ON CONFLICT IGNORE,"
            + DictContract.DictEntry.WORD + " TEXT "
            + " );";

    DictDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {


        sqLiteDatabase.execSQL(SQL_CREATE_HISTORY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FAVOURITE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SEARCH_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_DEFINITION_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DictContract.DictEntry.FAVOURITE_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DictContract.DictEntry.SEARCH_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DictContract.DictEntry.DEFINITION_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DictContract.DictEntry.HISTORY_TABLE_NAME);

        sqLiteDatabase.execSQL(SQL_CREATE_HISTORY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FAVOURITE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SEARCH_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_DEFINITION_TABLE);


    }
}
