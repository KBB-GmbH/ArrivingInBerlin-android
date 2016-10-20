package com.hkw.arrivinginberlin;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;


import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabSelectedListener;

public class SettingActivity extends AppCompatActivity implements LanguageSettingFragment.OnFragmentInteractionListener, InfoFragment.OnFragmentInteractionListener, ContactFragment.OnFragmentInteractionListener, AboutFragment.OnFragmentInteractionListener {
    private BottomBar bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.setItemsFromMenu(R.menu.bottom_navigation, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int itemId) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                switch (itemId) {
                    case R.id.map_item:
                        finish();
                        break;
                    case R.id.info_item:

                        break;
                    case R.id.lang_item:

                        break;
                    case R.id.contact_item:

                        break;
                    case R.id.about_item:

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
