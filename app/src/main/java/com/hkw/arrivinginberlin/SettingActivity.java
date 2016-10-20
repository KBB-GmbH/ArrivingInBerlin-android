package com.hkw.arrivinginberlin;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContentResolverCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;


import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabSelectedListener;

public class SettingActivity extends AppCompatActivity implements LanguageSettingFragment.OnFragmentInteractionListener, InfoFragment.OnFragmentInteractionListener, ContactFragment.OnFragmentInteractionListener, AboutFragment.OnFragmentInteractionListener {
    private BottomBar bottomBar;
    private AboutFragment aboutFragment;
    private ContactFragment contactFragment;
    private InfoFragment infoFragment;
    private LanguageFragment languageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        languageFragment =  new LanguageFragment();
        aboutFragment = new AboutFragment();
        infoFragment = new InfoFragment();
        contactFragment = new ContactFragment();


        bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.setItemsFromMenu(R.menu.bottom_navigation, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int itemId) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.content_container, contactFragment, "CONTACT");
                fragmentTransaction.hide(contactFragment);
                fragmentTransaction.add(R.id.content_container, languageFragment, "LANGUAGE");
                fragmentTransaction.hide(languageFragment);
                fragmentTransaction.add(R.id.content_container, infoFragment, "INFO");
                fragmentTransaction.hide(infoFragment);
                fragmentTransaction.add(R.id.content_container, aboutFragment, "ABOUT");
                fragmentTransaction.hide(aboutFragment);
                fragmentTransaction.commit();

                switch (itemId) {
                    case R.id.map_item:
                        finish();
                        break;
                    case R.id.info_item:
                        fragmentTransaction.hide(contactFragment);
                        fragmentTransaction.hide(languageFragment);
                        fragmentTransaction.hide(aboutFragment);
                        fragmentTransaction.show(infoFragment);
                        break;
                    case R.id.lang_item:
                        fragmentTransaction.hide(contactFragment);
                        fragmentTransaction.hide(infoFragment);
                        fragmentTransaction.hide(aboutFragment);
                        fragmentTransaction.show(languageFragment);
                        break;
                    case R.id.contact_item:
                        fragmentTransaction.hide(languageFragment);
                        fragmentTransaction.hide(infoFragment);
                        fragmentTransaction.hide(aboutFragment);
                        fragmentTransaction.show(contactFragment);
                        break;
                    case R.id.about_item:
                        fragmentTransaction.hide(contactFragment);
                        fragmentTransaction.hide(infoFragment);
                        fragmentTransaction.hide(languageFragment);
                        fragmentTransaction.show(aboutFragment);
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
}
