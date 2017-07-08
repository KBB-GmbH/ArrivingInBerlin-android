package com.hkw.arrivinginberlin;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContentResolverCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.hkw.arrivinginberlin.aboutItem.AboutContent;
import com.hkw.arrivinginberlin.aboutItem.AboutContent.AboutItem;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabSelectedListener;

import java.util.Locale;

public class SettingActivity extends AppCompatActivity implements LanguageSettingFragment.OnFragmentInteractionListener, InfoFragment.OnFragmentInteractionListener, ContactFragment.OnFragmentInteractionListener, AboutItemFragment.OnListFragmentInteractionListener, AboutDetailFragment.OnFragmentInteractionListener {
    private BottomBar bottomBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LocaleUtils.setLanguageFromPreference(getApplicationContext());
        Log.i("SETTINGS", "default language on create: " + Locale.getDefault());


        bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.setItemsFromMenu(R.menu.bottom_navigation, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int itemId) {
                FragmentManager fm = getSupportFragmentManager();
                final FragmentTransaction ft = fm.beginTransaction();
                switch (itemId) {
                    case R.id.map_item:
                        finish();
                        break;
                    case R.id.info_item:
                        InfoFragment info = new InfoFragment();
                        ft.replace(R.id.content_container, info, "INFO");
                        showSettingTitle(getString(R.string.info));
                        break;
                    case R.id.lang_item:
                        LanguageSettingFragment language = new LanguageSettingFragment();
                        ft.replace(R.id.content_container, language, "LANG");
                        showSettingTitle(getString(R.string.language));
                        break;
                    case R.id.contact_item:
                        ContactFragment contact = new ContactFragment();
                        ft.replace(R.id.content_container, contact, "CONTACT");
                        showSettingTitle(getString(R.string.contact));
                        break;
                    case R.id.about_item:
                        AboutItemFragment about = new AboutItemFragment();
                        ft.replace(R.id.content_container, about, "ABOUT");
                        showSettingTitle(getString(R.string.about));
                        break;
                    default:
                        break;
                }
                ft.commit();
            }
        });

        bottomBar.mapColorForTab(0, "#438D8D");
        bottomBar.mapColorForTab(1, "#438D8D");
        bottomBar.mapColorForTab(2, "#438D8D");
        bottomBar.mapColorForTab(3, "#438D8D");
        bottomBar.mapColorForTab(4, "#438D8D");

        // Set the color for the active tab. Ignored on mobile when there are more than three tabs.
        bottomBar.setActiveTabColor("#C2185B");

        int fragment = getIntent().getExtras().getInt("startFragment");
        bottomBar.selectTabAtPosition(fragment, false);
    }

    public void onFragmentInteraction(Uri uri) {

    }

    public void onBackButtonPressed() {
        Log.i("SETTING", "listening");
        hideSettingTitle(false);
        showSettingTitle(getString(R.string.about));
        AboutItemFragment about = new AboutItemFragment();
        FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content_container, about, getString(R.string.about));
        ft.commit();
    }


    public void onListFragmentInteraction(AboutItem item){
        showSettingTitle("");

        Bundle bundle = new Bundle();
        bundle.putString("detail_text", item.content);
        bundle.putString("detail_title", getTitleItem(item.content));

        Log.i("SETTINGS", item.content);
        AboutDetailFragment detail = new AboutDetailFragment();
        detail.setArguments(bundle);
        FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(0,0);
        ft.replace(R.id.content_container, detail, "DETAIL");
        ft.commit();
        hideSettingTitle(true);
    }

    private void showSettingTitle(String title) {
        TextView tv = (TextView) findViewById(R.id.title_settings);
        tv.setText(title);
    }

    private void hideSettingTitle(Boolean hide) {
        TextView tv = (TextView) findViewById(R.id.title_settings);
        if (hide){
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
        }
    }

    private String getTitleItem(String itemName){
        switch (itemName){
            case "privacy":
                return getApplicationContext().getString(R.string.privacy_title);
            case "legal":
                return getApplicationContext().getString(R.string.legal_title);
            case "terms":
                return getApplicationContext().getString(R.string.terms_title);
            case "project":
                return getApplicationContext().getString(R.string.project_title);
            default:
                return getApplicationContext().getString(R.string.privacy_title);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

}
