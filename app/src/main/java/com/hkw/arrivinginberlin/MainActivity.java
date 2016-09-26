package com.hkw.arrivinginberlin;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mapbox.mapboxsdk.maps.MapFragment;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabSelectedListener;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, CustomMapFragment.OnFragmentInteractionListener {
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private static final String TAG = "MainActivity";
    private static final String MapTag = "MAP";
    private CustomMapFragment mapFragment;
    private GoogleApiClient client;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomBar bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.setItemsFromMenu(R.menu.bottom_navigation, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int itemId) {
                switch (itemId) {
                    case R.id.map_item:
                        mapFragment = new CustomMapFragment();
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.add(R.id.content_container, mapFragment, MapTag);
                        fragmentTransaction.commit();
                        android.app.Fragment mapFragment = (CustomMapFragment) getFragmentManager().findFragmentByTag(MapTag);
                        break;
                    case R.id.info_item:
                        break;
                    case R.id.list_item:
                        break;

                }
            }
        });
        // Set the color for the active tab. Ignored on mobile when there are more than three tabs.
        bottomBar.setActiveTabColor("#C2185B");
        bottomBar.selectTabAtPosition(0, false);

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        nvDrawer.setItemIconTintList(null);
        drawerToggle = setupDrawerToggle();
        // Tie DrawerLayout events to the ActionBarToggle

        mDrawer.addDrawerListener(drawerToggle);
        // Setup drawer view
        setupDrawerContent(nvDrawer);
        // Hockeyapp
        checkForUpdates();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.hkw.arrivinginberlin/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.hkw.arrivinginberlin/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    // Hockeyapp methods
    private void checkForCrashes() {
        CrashManager.register(this);
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this);
    }

    private void unregisterManagers() {
        UpdateManager.unregister();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        final MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
//                displayAllMarkers();
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // User pressed the search button
//        displayMarkersForSearchTerm(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.isEmpty()) {
//            displayAllMarkers();
        }
        return false;
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        Log.i(TAG, "item drawer selected with id " + item);
        return super.onOptionsItemSelected(item);
    }


    @Override

    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();

    }


    @Override

    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);

    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }


    public void selectDrawerItem(MenuItem menuItem) {
        Log.i(TAG, "drawer selected");
//        mapFragment = (CustomMapFragment) getFragmentManager().findFragmentByTag(MapTag);
        switch (menuItem.getItemId()) {
            case R.id.nav_all_categories:
                mapFragment.displayAllMarkers();
                break;
            case R.id.nav_counseling:
                mapFragment.displayMarkersForCategory(1);
                break;
            case R.id.nav_doctor_gp_arabic:
                mapFragment.displayMarkersForCategory(2);
                break;
            case R.id.nav_doctor_gp_farsi:
                mapFragment.displayMarkersForCategory(3);
                break;
            case R.id.nav_doctor_gyn_arabic:
                mapFragment.displayMarkersForCategory(4);
                break;
            case R.id.nav_doctor_gyn_farsi:
                mapFragment.displayMarkersForCategory(5);
                break;
            case R.id.nav_german_language_classes:
                mapFragment.displayMarkersForCategory(6);
                break;
            case R.id.nav_lawyers:
                mapFragment.displayMarkersForCategory(7);
                break;
            case R.id.nav_police:
                mapFragment.displayMarkersForCategory(8);
                break;
            case R.id.nav_authorities:
                mapFragment.displayMarkersForCategory(9);
                break;
            case R.id.nav_libraries:
                mapFragment.displayMarkersForCategory(10);
                break;
            case R.id.nav_transport:
                mapFragment.displayMarkersForCategory(11);
                break;
            case R.id.nav_shopping_food:
                mapFragment.displayMarkersForCategory(12);
                break;
            case R.id.nav_sports_freetime:
                mapFragment.displayMarkersForCategory(13);
                break;

            default:
                break;
        }


        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }

    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }
}
