package com.arpitsingh.mydictionary.activities;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.arpitsingh.mydictionary.R;
import com.arpitsingh.mydictionary.data.DictContract;
import com.arpitsingh.mydictionary.fragments.DictionaryFragment;
import com.arpitsingh.mydictionary.fragments.FavouriteFragment;
import com.arpitsingh.mydictionary.fragments.WordOfDayFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ARPIT SINGH
 * 14/10/19
 */


public class DrawerListener extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String LOG_TAG = DrawerListener.class.getSimpleName();

    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    @BindView(R.id.search_view)
    SearchView mSearchView;

    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    private String mSearchKey;
    private InterstitialAd mInterstitialAd;
    private MainPagerAdapter mPagerAdapter;
    public static final String APP_UNIT_AD_ID = "ca-app-pub-3940256099942544/1033173712";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_listner);

        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);

        mNavigationView.setNavigationItemSelectedListener(this);
        //for setting up status bar color
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.status_bar_color));
        //working with viewPager with tabLayout
        //manage navigation menu selected on scroll
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Toast.makeText(getBaseContext(), "Hi", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPageSelected(int position) {
                setNavigationItemSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        //working with ads
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(APP_UNIT_AD_ID);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        final ImageView toggleicon = findViewById(R.id.menu_drawer);
        toggleicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });

        // working with search view at here
        final EditText editText = findViewById(R.id.search_src_text);
        final InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        //going to work here
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchKey = query;
                imm.hideSoftInputFromWindow(DrawerListener.this.getCurrentFocus().getWindowToken(), 0);
                startSearchActivity();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        ImageView close = mSearchView.findViewById(R.id.search_close_btn);
        close.setImageResource(R.drawable.ic_clear_black_24dp);
        //hide search icon of search view
        ImageView magImage = mSearchView.findViewById(R.id.search_mag_icon);
        magImage.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        //edit text field of search view
        editText.requestFocus();
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    editText.setCursorVisible(true);
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
            }
        });
        editText.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
        editText.setHint(Html.fromHtml("<b>" + "Dictionary" + "</b>"));
        editText.setTextColor(getResources().getColor(R.color.colorPrimaryText));
    }

    //jsut a helper method to be invoked on scroll of view Pager to manage state of navigation view
    private void setNavigationItemSelected(int index) {
        mNavigationView.getMenu().getItem(index).setChecked(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        if (intent != null && intent.hasExtra(SearchManager.QUERY)) {
            mSearchView.setQuery(intent.getStringExtra(SearchManager.QUERY), true);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    void showAdsAndStartSearch() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    startSearchActivity();
                }
            });
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
            startSearchActivity();
        }
    }

    void startSearchActivity() {
        startActivity(new Intent(
                DrawerListener.this, SearchActivity.class)
                .putExtra(SearchManager.QUERY, mSearchKey));

        // insert query into history database in background
        ContentValues cv = new ContentValues();
        cv.put(DictContract.DictEntry.WORD, mSearchKey);
        cv.put(DictContract.DictEntry.WORD_ID, mSearchKey);
        this.getContentResolver().insert(DictContract.DictEntry.SEARCH_CONTENT_URI, cv);

        // start word activity
//        Intent intent = new Intent(DrawerListener.this, WordActivity.class);
//        intent.putExtra(DictContract.DictEntry.WORD_ID, mSearchKey);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//            Bundle bundle = ActivityOptions.makeCustomAnimation(
//                    DrawerListener.this,
//                    R.anim.slide_from_right, R.anim.slide_to_left).toBundle();
//            startActivity(intent, bundle);
//        } else {
//            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
//            startActivity(intent);
//        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.nav_fav:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.nav_word_for_day:
                mViewPager.setCurrentItem(2);
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.nav_history:
                startActivity(new Intent(this, HistoryActivity.class));
                break;
            case R.id.nav_sign_in:
                startSignInFlow();
                break;
            case R.id.nav_sign_out:
                signOut();
                break;

        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class MainPagerAdapter extends FragmentPagerAdapter {

        private MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.app_name);
                case 1:
                    return getString(R.string.favourite);
                case 2:
                    return getString(R.string.word_of_day);

            }
            return super.getPageTitle(position);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new DictionaryFragment();
                case 1:
                    return new FavouriteFragment();
                case 2:
                    Log.d("Word of Day instance", "Created");
                    return new WordOfDayFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    void startSignInFlow() {
        startActivity(new Intent(this, SignInActivity.class));
    }

    void signOut() {
        FirebaseAuth.getInstance().signOut();
        AuthUI.getInstance().signOut(this).addOnCompleteListener((@NonNull Task<Void> task) -> {
            //after signed out
            Toast.makeText(DrawerListener.this, getString(R.string.sign_out_msg), Toast.LENGTH_SHORT).show();
            userIsLoggedOut();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) userIsLoggedIn();
        else userIsLoggedOut();
    }

    void userIsLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        View headerLayout = mNavigationView.getHeaderView(0);
        TextView nameTV = headerLayout.findViewById(R.id.drawer_header_name_tv);
        TextView emailTV = headerLayout.findViewById(R.id.drawer_header_email_tv);
        nameTV.setText(user.getDisplayName());
        emailTV.setText(user.getEmail());
        mNavigationView.getMenu().findItem(R.id.nav_sign_in).setVisible(false);
        mNavigationView.getMenu().findItem(R.id.nav_sign_out).setVisible(true);
    }

    void userIsLoggedOut() {
        View headerLayout = mNavigationView.getHeaderView(0);
        TextView nameTV = headerLayout.findViewById(R.id.drawer_header_name_tv);
        TextView emailTV = headerLayout.findViewById(R.id.drawer_header_email_tv);
        nameTV.setVisibility(View.INVISIBLE);
        emailTV.setText(getString(R.string.sign_in_msg));
        mNavigationView.getMenu().findItem(R.id.nav_sign_out).setVisible(false);
        mNavigationView.getMenu().findItem(R.id.nav_sign_in).setVisible(true);
    }


}
