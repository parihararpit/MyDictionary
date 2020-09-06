package com.arpitsingh.mydictionary.adapters;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.Html;
import android.text.Spannable;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.arpitsingh.mydictionary.R;
import com.arpitsingh.mydictionary.data.DictContract;

/**
 * Created by ARPIT SINGH
 * 14/10/18
 */

public class FavouriteCursorAdapter extends CursorAdapter {
    private Uri mContentUri;

    public FavouriteCursorAdapter(Context context, Cursor c, int flags, Uri uri) {
        super(context, c, flags);
        mContentUri = uri;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.favourite_word, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView head_word = view.findViewById(R.id.fav_head_word);
        TextView definition = view.findViewById(R.id.fav_word_definition);
        view.setTag(cursor.getString(cursor.getColumnIndex(DictContract.DictEntry.WORD_ID)));

        if (cursor.getColumnIndex(DictContract.DictEntry.WORD) != -1) {
            String word = cursor.getString(cursor.getColumnIndex(DictContract.DictEntry.WORD));
            word = "<i>" + "<b>" + word + "</b>" + "</i>";
            head_word.setText(Html.fromHtml(word), TextView.BufferType.SPANNABLE);
            Spannable s = (Spannable) head_word.getText();
            s.setSpan(new RelativeSizeSpan(1.2f), 0, head_word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (cursor.getColumnIndex(DictContract.DictEntry.PRONNUNCIATION_TEXT) != -1) {
            String pro = cursor.getString(cursor.getColumnIndex(DictContract.DictEntry.PRONNUNCIATION_TEXT));
            if (pro != null && !pro.isEmpty()) {
                head_word.append("    (" + pro + ")");
            }
        }

        if (cursor.getColumnIndex(DictContract.DictEntry.SINGLE_DEFINITION) != -1) {
            definition.setText(cursor.getString(cursor.getColumnIndex(DictContract.DictEntry.SINGLE_DEFINITION)));
        } else definition.setHeight(0);

    }


}
