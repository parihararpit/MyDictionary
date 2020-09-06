package com.arpitsingh.mydictionary.activities;

import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.arpitsingh.mydictionary.AppExecutor;
import com.arpitsingh.mydictionary.DictionaryApplication;
import com.arpitsingh.mydictionary.R;
import com.arpitsingh.mydictionary.adapters.SearchItemAdapter;
import com.arpitsingh.mydictionary.data.DictContract;
import com.arpitsingh.mydictionary.model.SearchResult;
import com.arpitsingh.mydictionary.networking.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.arpitsingh.mydictionary.DictionaryApplication.LOADING_STATE.LOAD_STARTED;
import static com.arpitsingh.mydictionary.networking.NetworkUtils.checkConnectivity;

/**
 * Created by ARPIT SINGH
 * 8/10/19
 */


public class SearchActivity extends AppCompatActivity implements Callback<SearchResult> {
    private final String LOG_TAG = SearchActivity.class.getSimpleName();

    @BindView(R.id.words_list_view)
    ListView mListView;

    private ConnectivityManager mConnectivityManager;
    private ArrayList<SearchResult.QueryResult> mWordsList;
    private String mSearchKey;
    private SearchItemAdapter adapter;
    private DictionaryApplication.LOADING_STATE state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        mConnectivityManager =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        //initial list initial empty list
        mWordsList = new ArrayList<>();
        //set onitemClick listener for list items
        //send an intent to word activity for showing word with word id
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(SearchActivity.this, WordActivity.class);
                intent.putExtra(DictContract.DictEntry.WORD_ID, mWordsList.get(i).id);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Bundle bundle = ActivityOptions.makeCustomAnimation(
                            SearchActivity.this,
                            R.anim.slide_from_right, R.anim.slide_to_left).toBundle();
                    startActivity(intent, bundle);
                } else {
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    startActivity(intent);
                }
            }
        });

        mListView.setDividerHeight(0);
        adapter = new SearchItemAdapter(SearchActivity.this, R.layout.search_items, mWordsList);
        mListView.setAdapter(adapter);
        //set empty view for list view
        if (savedInstanceState != null) {
            state = ((DictionaryApplication.LOADING_STATE) savedInstanceState
                    .get(DictionaryApplication.LOADING_STATE_KEY));

            DictionaryApplication.getInstance().setState(state, this);
            mSearchKey = savedInstanceState.getString(SearchManager.QUERY);
        } else {
            state = LOAD_STARTED;
            DictionaryApplication.getInstance().setState(LOAD_STARTED, this);
        }
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SearchManager.QUERY)) {
            mSearchKey = intent.getStringExtra(SearchManager.QUERY);
            performSearch(mSearchKey);
        }
    }

    //method for performing search
    public void performSearch(String searchKey) {
        adapter.clear();
        mSearchKey = searchKey;
        state = LOAD_STARTED;
        DictionaryApplication.getInstance().setState(state, this);

        /**
         * save searchKey to show in history
         */
        if (mSearchKey != null && !mSearchKey.isEmpty()) {
            mSearchKey = mSearchKey.trim();
            ContentValues cv = new ContentValues();
            cv.put(DictContract.DictEntry.WORD, mSearchKey);
            this.getContentResolver().insert(DictContract.DictEntry.HISTORY_CONTENT_URI, cv);
        }

        /**
         * make network call and fetch matching words
         */
        if (mSearchKey != null && !mSearchKey.trim().isEmpty() && checkConnectivity(mConnectivityManager)) {
            /**
             * if network is avail make network request, 'this' will listen to result in callback func.
             */
            if(checkConnectivity(mConnectivityManager)){
                NetworkUtils.queryWord(mSearchKey, this);
            }else{
                state = DictionaryApplication.LOADING_STATE.LOAD_FAILED;
                DictionaryApplication.getInstance().setState(state, this);
                Toast.makeText(this, getString(R.string.not_conn_msg), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     *
     * @param call the network call made
     * @param response search resulut
     */
    @Override
    public void onResponse(Call<SearchResult> call, @NonNull Response<SearchResult> response) {
        Log.v("result", response.body().toString());
        if (response.body() != null ) {
            List<SearchResult.QueryResult> searchWords = response.body().results;
            if (searchWords != null && !searchWords.isEmpty()) {
                state = DictionaryApplication.LOADING_STATE.LOAD_STOPPED;
                DictionaryApplication.getInstance().setState(state, this);
                adapter.clear();
                adapter.addAll(searchWords);

                AppExecutor.getInstance().diskIO().execute(() -> {
                    ContentValues[] values = new ContentValues[searchWords.size()];
                    for (int i = 0; i < searchWords.size(); i++) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(DictContract.DictEntry.WORD_ID, searchWords.get(i).id);
                        contentValues.put(DictContract.DictEntry.WORD, searchWords.get(i).word);
                        values[i] = contentValues;
                    }
                    this.getContentResolver().bulkInsert(DictContract.DictEntry.SEARCH_CONTENT_URI, values);
                });

            } else {
                state = DictionaryApplication.LOADING_STATE.LOAD_EMPTY;
                DictionaryApplication.getInstance().setState(state, this);
            }
        }
    }

    @Override
    public void onFailure(Call<SearchResult> call, Throwable t) {
        state = DictionaryApplication.LOADING_STATE.LOAD_FAILED;
        DictionaryApplication.getInstance().setState(state, this);
        Log.e(SearchActivity.class.getSimpleName(), "Error occured!");
        t.printStackTrace();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(DictionaryApplication.LOADING_STATE_KEY, state);
        outState.putString(SearchManager.QUERY, mSearchKey);
        super.onSaveInstanceState(outState);
    }

}
