package com.arpitsingh.mydictionary.utilities;
/**
 * Created by ARPIT SINGH
 * 17/10/18
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.arpitsingh.mydictionary.data.DictContract;
import com.arpitsingh.mydictionary.model.WordDefinition;


public class DictionaryDBUtils {

    public static final String[] WORD_COMPLETE_PROJECTION =
            new String[]{
                    DictContract.DictEntry.WORD_ID,
                    DictContract.DictEntry.WORD,
                    DictContract.DictEntry.LANGUAGE,
                    DictContract.DictEntry.SINGLE_DEFINITION,
                    DictContract.DictEntry.PRONNUNCIATION_URL,
                    DictContract.DictEntry.PRONNUNCIATION_TEXT,
                    DictContract.DictEntry.LEXICAL_ENTRIES,
            };

    public static WordDefinition getWordFromCursor(Cursor cursor) {
        WordDefinition word = null;
        if (cursor != null && !cursor.isClosed() && cursor.moveToFirst()) {
            word = new WordDefinition();
            word.id = cursor.getString(cursor.getColumnIndex(DictContract.DictEntry.WORD_ID));

            if ((cursor.getColumnIndex(DictContract.DictEntry.WORD) != -1))
                word.word = cursor.getString(cursor.getColumnIndex(DictContract.DictEntry.WORD));

            if ((cursor.getColumnIndex(DictContract.DictEntry.LANGUAGE) != -1))
                word.language = cursor.getString(cursor.getColumnIndex(DictContract.DictEntry.LANGUAGE));

            if ((cursor.getColumnIndex(DictContract.DictEntry.PRONNUNCIATION_URL) != -1))
                word.pronunciationUrl = cursor.getString(cursor.getColumnIndex(DictContract.DictEntry.PRONNUNCIATION_URL));

            if ((cursor.getColumnIndex(DictContract.DictEntry.PRONNUNCIATION_TEXT) != -1))
                word.pronunciationText = cursor.getString(cursor.getColumnIndex(DictContract.DictEntry.PRONNUNCIATION_TEXT));

            if ((cursor.getColumnIndex(DictContract.DictEntry.LEXICAL_ENTRIES) != -1))
                word.lexicalEntries = com.arpitsingh.mydictionary.utilities.DictionaryJsonUtils.getLexList(cursor.getString(cursor.getColumnIndex(DictContract.DictEntry.LEXICAL_ENTRIES)));

            if ((cursor.getColumnIndex(DictContract.DictEntry.SINGLE_DEFINITION) != -1))
                word.single_definition = cursor.getString(cursor.getColumnIndex(DictContract.DictEntry.SINGLE_DEFINITION));


            cursor.close();
            word.isFav = true;
        }
        return word;
    }

    public static void insertWordIntoDatabase(Context context, Uri uri, WordDefinition word) {
        ContentValues cv = new ContentValues();
        cv.put(DictContract.DictEntry.WORD_ID, word.id);
        cv.put(DictContract.DictEntry.WORD, word.word);
        cv.put(DictContract.DictEntry.LANGUAGE, word.language);
        cv.put(DictContract.DictEntry.SINGLE_DEFINITION, word.single_definition);
        cv.put(DictContract.DictEntry.PRONNUNCIATION_TEXT, word.pronunciationText);
        cv.put(DictContract.DictEntry.PRONNUNCIATION_URL, word.pronunciationUrl);
        cv.put(DictContract.DictEntry.LEXICAL_ENTRIES, com.arpitsingh.mydictionary.utilities.DictionaryJsonUtils.getLexJsonString(word));
        context.getContentResolver().insert(uri, cv);
    }

}
