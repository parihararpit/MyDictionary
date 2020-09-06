package com.arpitsingh.mydictionary.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class DictContract {
    public static final String DICT_AUTHORITY = "com.example.amitnsky.dictionary";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + DICT_AUTHORITY);
    public static final String PATH_HISTORY = "dictionary_history_table";
    public static final String PATH_FAVOURITE = "dictionary_favourite_table";
    public static final String PATH_SEARCH = "dictionary_search_table";
    public static final String PATH_DEFINITION = "dictionary_definition_table";

    private DictContract() {
    }

    public static class DictEntry implements BaseColumns {
        //uri to tables
        public static final Uri HISTORY_CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_HISTORY);
        public static final Uri FAVOURITE_CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FAVOURITE);
        public static final Uri SEARCH_CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SEARCH);
        public static final Uri DEFINITION_CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_DEFINITION);


        //fields often we are going to use
        public static final String HISTORY_TABLE_NAME = "dictionary_history_table";
        public static final String FAVOURITE_TABLE_NAME = "dictionary_favourite_table";
        public static final String SEARCH_TABLE_NAME = "dictionary_search_table";
        public static final String DEFINITION_TABLE_NAME = "dictionary_definition_table";

        public static final String _ID = BaseColumns._ID;
        public static final String WORD_ID = "word_id";
        public static final String WORD = "word";
        public static final String PRONNUNCIATION_URL = "word_pro_url";
        public static final String PRONNUNCIATION_TEXT = "word_pro_txt";
        public static final String LANGUAGE = "word_lang";
        public static final String LEXICAL_ENTRIES = "word_lex_entries";
        public static final String SINGLE_DEFINITION = "word_one_line_definition";

        //mime types for data
        public static final String HISTORY_ROW_MIME = "vnd.android.cursor.item/vnd.com.example.amitnsky.provider.dictionary." + HISTORY_TABLE_NAME;
        public static final String HISTORY_TABLE_MIME = "vnd.android.cursor.dir/vnd.com.example.amitnsky.provider.dictionary." + HISTORY_TABLE_NAME;
    }
}
