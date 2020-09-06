package com.arpitsingh.mydictionary.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.arpitsingh.mydictionary.DictionaryApplication;
import com.arpitsingh.mydictionary.R;
import com.arpitsingh.mydictionary.activities.SignInActivity;
import com.arpitsingh.mydictionary.adapters.WordAdapter;
import com.arpitsingh.mydictionary.data.DictContract;
import com.arpitsingh.mydictionary.model.WordDefinition;
import com.arpitsingh.mydictionary.networking.SyncService;
import com.arpitsingh.mydictionary.utilities.DictionaryDBUtils;
import com.arpitsingh.mydictionary.utilities.DictionaryDateUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ARPIT SINGH
 * 14/10/19
 */


public class WordOfDayFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    @BindView(R.id.head_word_text_view)
    TextView mHeadWordTextView;

    @BindView(R.id.word_pronunciation)
    TextView mPronunciationTextView;

    @BindView(R.id.speak_head_word_ib)
    ImageButton pronounceWordIB;

    @BindView(R.id.word_one_definition)
    TextView oneLineDefinition;

    @BindView(R.id.word_list_view)
    ListView listView;

    @BindView(R.id.word_activity_header_rl)
    RelativeLayout headerRL;

    @BindView(R.id.adView)
    AdView mAdView;

    WordDefinition mWord;
    private static final int WOD_LOADER_ID = 4012;
    public static final String LOG_TAG = WordOfDayFragment.class.getSimpleName();
    View mRootView;
    private String mWordId;
    private static final String DEVICE_ID = "8790CF1C2B9700EB5589C664107C2D23";
    BroadcastReceiver mMessageReceiver;
    int INITIAL_HEIGHT = 420;
    int TRANSLATION_FACTOR = 3;

    public WordOfDayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.activity_word, container, false);
        ButterKnife.bind(this, mRootView);

        AdRequest adRequest = new AdRequest.Builder().addTestDevice(DEVICE_ID).build();
        mAdView.loadAd(adRequest);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {


            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                View c = listView.getChildAt(0);
                if (c != null) {
                    int scrolly = c.getTop();
                    animateLayout(scrolly);
                }
            }
        });

        pronounceWordIB.setOnClickListener((View view) -> {
            //speak word

        });

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpView();
    }

    public void setUpView() {
        boolean signedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        if (signedIn) {
            showLoading();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String lastSyncDate = preferences.getString(getString(R.string.WOD_DATE_KEY), DictionaryDateUtils.DEFAULT_DATE);
            if (lastSyncDate.equals(DictionaryDateUtils.getTodayInStringFormat())) {
                mWordId = preferences.getString(getString(R.string.WOD_WORD_ID_KEY), "");
                if (getActivity() != null) {
                    getActivity().getSupportLoaderManager().initLoader(WOD_LOADER_ID,
                            null, this).forceLoad();
                }
            } else {
                //time to start sync
                startSyncService();
            }
        } else {
            // modify retry button to send to log in activity
            showLoadingFailed();
            ImageView imageView = mRootView.findViewById(R.id.loading_fail_iv);
            //set image veiw a msg to sign in
            Button retryButton = mRootView.findViewById(R.id.load_retry_bv);
            retryButton.setText(getString(R.string.sign_in));
            retryButton.setOnClickListener((View v) -> {
                startActivity(new Intent(getActivity(), SignInActivity.class));
            });
        }

    }

    //helper method to update user interface
    private void updateUi() {
        if (mWord != null) {
            showLoadingStopped();
            mHeadWordTextView.setText(mWord.word);

            if (mWord.pronunciationText != null)
                mPronunciationTextView.setText(mWord.pronunciationText);
            else mPronunciationTextView.setVisibility(View.INVISIBLE);

            if (mWord.single_definition != null) {
                oneLineDefinition.setText(mWord.single_definition);
            }

            listView.setDividerHeight(0);
            listView.setAdapter(new WordAdapter(DictionaryApplication.getInstance().getBaseContext(), R.layout.word_entry, mWord));
        } else {
            showLoadingFailed();
            Button button = mRootView.findViewById(R.id.load_retry_bv);
            button.setText(getString(R.string.retry));
            button.setOnClickListener((View view) -> {
                showLoading();
                getLoaderManager().initLoader(WOD_LOADER_ID, null, this).forceLoad();
            });
        }
    }

    void startSyncService() {

        Log.d(LOG_TAG, "Sync Service started.");

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onReceiveBroadCast(context, intent);
            }
        };

        Context context = DictionaryApplication.getInstance().getBaseContext();
        Intent syncIntent = new Intent(context, SyncService.class);
        syncIntent.setAction(SyncService.ACTION_SYNC_WOD);
        context.startService(syncIntent);

        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver,
                new IntentFilter(SyncService.ACTION_SYNC_WOD_FINISHED));
    }

    private void onReceiveBroadCast(Context context, Intent intent) {
        if (SyncService.ACTION_SYNC_WOD_FINISHED.equals(intent.getAction())) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);
            mMessageReceiver = null;

            Intent syncIntent = new Intent(context, SyncService.class);
            context.stopService(syncIntent);

            setUpView();
        }
    }

    void showLoading() {
        mRootView.findViewById(R.id.content_view).setVisibility(View.INVISIBLE);
        mRootView.findViewById(R.id.loading_empty_ll).setVisibility(View.GONE);
        mRootView.findViewById(R.id.loading_fail_ll).setVisibility(View.GONE);
        mRootView.findViewById(R.id.loading_ll).setVisibility(View.VISIBLE);
    }

    void showLoadingStopped() {
        mRootView.findViewById(R.id.loading_empty_ll).setVisibility(View.GONE);
        mRootView.findViewById(R.id.loading_fail_ll).setVisibility(View.GONE);
        mRootView.findViewById(R.id.loading_ll).setVisibility(View.GONE);
        mRootView.findViewById(R.id.content_view).setVisibility(View.VISIBLE);

    }

    void showLoadingFailed() {
        mRootView.findViewById(R.id.content_view).setVisibility(View.INVISIBLE);
        mRootView.findViewById(R.id.loading_empty_ll).setVisibility(View.GONE);
        mRootView.findViewById(R.id.loading_ll).setVisibility(View.GONE);
        mRootView.findViewById(R.id.loading_fail_ll).setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        return new CursorLoader(DictionaryApplication.getInstance().getApplicationContext(),
                DictContract.DictEntry.DEFINITION_CONTENT_URI,
                DictionaryDBUtils.WORD_COMPLETE_PROJECTION,
                DictContract.DictEntry.WORD_ID + " =?",
                new String[]{mWordId},
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        //try loading locally
        mWord = DictionaryDBUtils.getWordFromCursor(cursor);
        updateUi();
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
