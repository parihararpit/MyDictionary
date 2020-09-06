package com.arpitsingh.mydictionary.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.arpitsingh.mydictionary.R;
import com.arpitsingh.mydictionary.model.SearchResult;

import java.util.ArrayList;

/**
 * Created by ARPIT SINGH
 * 8/10/19
 */


public class SearchItemAdapter extends ArrayAdapter<SearchResult.QueryResult> {
    ArrayList<SearchResult.QueryResult> mData;

    public SearchItemAdapter(@NonNull Context context, int resource, @NonNull ArrayList<SearchResult.QueryResult> objects) {
        super(context, resource, objects);
        mData = objects;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rootView=convertView;
        if (convertView==null){
            rootView = LayoutInflater.from(getContext()).inflate(R.layout.search_items,parent,false);
        }

        SearchResult.QueryResult queryResult = getItem(position);

        TextView wordTextView = rootView.findViewById(R.id.search_items_word);
        wordTextView.setText(queryResult.word);

        ImageView delView = rootView.findViewById(R.id.search_items_action);
        delView.setVisibility(View.GONE);

        return rootView;
    }

}


/**
 * FirebaseAuth,
 * NavigationDrawer,
 * Intent -> get[String]Extra(key), putExtra(key, val)
 * ArrayAdapter
 *  -> ArrayAdapter -> ListView -> getView, getItem
 *  -> ViewpageAdapter -> ViewPager -> getView
 *  -> CursorAdapter -> getView
 *
 *
 */
