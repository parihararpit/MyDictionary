package com.arpitsingh.mydictionary.activities;

import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.arpitsingh.mydictionary.DictionaryApplication;
import com.arpitsingh.mydictionary.R;
import com.arpitsingh.mydictionary.adapters.WordAdapter;
import com.arpitsingh.mydictionary.data.DictContract;
import com.arpitsingh.mydictionary.model.WordDefinition;
import com.arpitsingh.mydictionary.networking.NetworkUtils;
import com.arpitsingh.mydictionary.utilities.DictionaryDBUtils;
import com.arpitsingh.mydictionary.utilities.DictionaryJsonUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ARPIT SINGH
 * 12/10/19
 */


public class WordActivity extends AppCompatActivity implements Callback<String>, LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = this.getClass().toString();

    @BindView(R.id.head_word_text_view)
    TextView mHeadWordTextView;

    @BindView(R.id.word_pronunciation)
    TextView mPronunciationTextView;

    @BindView(R.id.speak_head_word_ib)
    ImageButton pronounceWordIB;

    @BindView(R.id.word_activity_header_rl)
    RelativeLayout headerRL;

    @BindView(R.id.word_one_definition)
    TextView oneLineDefinition;

    @BindView((R.id.word_list_view))
    ListView mListView;

    @BindView(R.id.adView)
    AdView mAdView;

    String word_id;
    private WordDefinition mWord;
    int INITIAL_HEIGHT = 420;
    int TRANSLATION_FACTOR = 3;

    DictionaryApplication.LOADING_STATE state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(DictContract.DictEntry.WORD_ID)) {
            word_id = intent.getStringExtra(DictContract.DictEntry.WORD_ID);
        } else {
            Log.e(LOG_TAG, "You should must provide word id.");
            finish();
        }


        if (getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);
        else getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {


            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                View c = mListView.getChildAt(0);
                if (c != null) {
                    int scrolly = c.getTop();
                    animateLayout(scrolly);
                }
            }
        });


        pronounceWordIB.setOnClickListener((View view) -> {
            //speak word

        });

        state = DictionaryApplication.LOADING_STATE.LOAD_STARTED;
        if (savedInstanceState != null)
            state = (DictionaryApplication.LOADING_STATE) savedInstanceState.getSerializable
                    (DictionaryApplication.LOADING_STATE_KEY);
        DictionaryApplication.getInstance().setState(state, this);
        performLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.word_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mWord == null)
            return false;
        else if (mWord.isFav) {
            menu.getItem(0).setIcon(R.drawable.ic_bookmark_white_24dp);
            return true;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                return true;
            case R.id.word_bookmark:
                saveWord();
                return true;
            case R.id.word_copy:
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name),
                        mWord.word));
                Toast.makeText(this, getString(R.string.copied_msg), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.word_google:
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, "define " + mWord.word);
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivity(intent);
                return true;
            case R.id.word_translate:
                Intent translate = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://translate.google.com/?sl=sv#en/hi/" + Uri.encode(mWord.word)));
                if (translate.resolveActivity(getPackageManager()) != null)
                    startActivity(translate);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    private void saveWord() {
        //if it was favourite delete and unfavor it
        if (mWord != null && mWord.isFav) {
            mWord.isFav = false;
            invalidateOptionsMenu();
            getContentResolver().delete(DictContract.DictEntry.FAVOURITE_CONTENT_URI, DictContract.DictEntry.WORD_ID + " =?",
                    new String[]{word_id});
            Toast.makeText(this, getString(R.string.unsaved), Toast.LENGTH_SHORT).show();
            //else save and make it fav
        } else if (mWord != null) {
            DictionaryDBUtils.insertWordIntoDatabase(this, DictContract.DictEntry.FAVOURITE_CONTENT_URI, mWord);
            invalidateOptionsMenu();
            mWord.isFav = true;
            Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
        }
    }

    //helper method to update user interface
    private void updateUi() {
        state = DictionaryApplication.LOADING_STATE.LOAD_STOPPED;
        DictionaryApplication.getInstance().setState(state, this);

        mHeadWordTextView.setText(mWord.word);

        if (mWord.pronunciationText != null)
            mPronunciationTextView.setText(mWord.pronunciationText);
        else mPronunciationTextView.setVisibility(View.INVISIBLE);

        if (mWord.single_definition != null) {
            oneLineDefinition.setText(mWord.single_definition);
        }

        mListView.setDividerHeight(0);
        mListView.setAdapter(new WordAdapter(this, R.layout.word_entry, mWord));

    }

    void performLoad() {
        //show loading
        state = DictionaryApplication.LOADING_STATE.LOAD_STARTED;
        DictionaryApplication.getInstance().setState(state, this);
        getSupportLoaderManager().initLoader(101, null, this).startLoading();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(DictionaryApplication.LOADING_STATE_KEY, state);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResponse(Call<String> call, @NonNull Response<String> response) {
        //   Log.d("Response Body", response.body());
        mWord = DictionaryJsonUtils.getSingleWord(response.body());
        if (mWord != null) {
            invalidateOptionsMenu();
            updateUi();
            return;
        }
        state = DictionaryApplication.LOADING_STATE.LOAD_EMPTY;
        DictionaryApplication.getInstance().setState(state, this);
    }

    @Override
    public void onFailure(Call<String> call, Throwable t) {
        state = DictionaryApplication.LOADING_STATE.LOAD_FAILED;
        DictionaryApplication.getInstance().setState(state, this);
        Log.e(LOG_TAG, "Error occured in perfoming internet query");
        t.printStackTrace();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        return new CursorLoader(this,
                DictContract.DictEntry.FAVOURITE_CONTENT_URI,
                DictionaryDBUtils.WORD_COMPLETE_PROJECTION,
                DictContract.DictEntry.WORD_ID + " =?",
                new String[]{word_id},
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        //try loading locally
        mWord = DictionaryDBUtils.getWordFromCursor(cursor);
        if (mWord != null) {
            invalidateOptionsMenu();
            updateUi();
        }
        //not possible to load locally so perform net query
        else {
            NetworkUtils.queryDefinitions(word_id, WordActivity.this);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    void animateLayout(int scrollY) {
        float translateToY = (INITIAL_HEIGHT - scrollY) / TRANSLATION_FACTOR;
        headerRL.setTranslationY(-(translateToY));
        float alpha = Math.max(0, 1 - translateToY / 300);
        headerRL.setAlpha(alpha);
    }
}
