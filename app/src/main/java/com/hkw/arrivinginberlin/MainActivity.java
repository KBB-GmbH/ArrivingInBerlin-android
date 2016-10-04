package com.hkw.arrivinginberlin;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
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
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarFragment;
import com.roughike.bottombar.OnMenuTabSelectedListener;
import com.roughike.bottombar.OnTabSelectedListener;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, CustomMapFragment.OnFragmentInteractionListener, LanguageFragment.OnFragmentInteractionListener {
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    ExpandableMenuAdapter mMenuAdapter;
    ExpandableListView expandableList;
    List<ExpandedMenuItem> listDataHeader;
    private ActionBarDrawerToggle drawerToggle;
    private static final String TAG = "MainActivity";
    private static final String MapTag = "MAP";
    private CustomMapFragment mapFragment;
    private GoogleApiClient client;
    private BottomBar bottomBar;
    public ArrayList<JSONObject> mainLocations = new ArrayList<JSONObject>();


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ((savedInstanceState == null) || (mapFragment == null)){
            new FetchLocationsTask().execute();
            FragmentManager fragmentManager = getFragmentManager();
            mapFragment = CustomMapFragment.newInstance(mainLocations);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.content_container, mapFragment, MapTag);
            fragmentTransaction.commit();
        }

        bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.setItemsFromMenu(R.menu.bottom_navigation, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int itemId) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                switch (itemId) {
                    case R.id.map_item:
                        if (mapFragment == null) {
                            mapFragment = CustomMapFragment.newInstance(mainLocations);
                            updateLocations();
                        }
                        ft.replace(R.id.content_container, mapFragment, MapTag);
                        break;
                    case R.id.info_item:
                        break;
                    case R.id.lang_item:
                        LanguageFragment langFragment = new LanguageFragment();
                        if(mapFragment != null){
                        }
                        ft.replace(R.id.content_container, langFragment, "LANGUAGE");
                        break;
                    case R.id.contact_item:
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
        expandableList = (ExpandableListView) findViewById(R.id.navMenu);
        nvDrawer.setItemIconTintList(null);

        drawerToggle = setupDrawerToggle();
        // Tie DrawerLayout events to the ActionBarToggle

        mDrawer.addDrawerListener(drawerToggle);
        // Setup drawer view
        if (nvDrawer != null) {
            setupDrawerContent(nvDrawer);
        }

        setMenuItemsFromJSON(mainLocations);
        mMenuAdapter = new ExpandableMenuAdapter(this, listDataHeader, expandableList);

        // setting list adapter
        expandableList.setAdapter(mMenuAdapter);

        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long childId) {
                // Highlight the selected item has been done by NavigationView
                ExpandedMenuItem item = listDataHeader.get(groupPosition);
                setTitle(item.getIconName());

                if (mapFragment != null) {
                    if (groupPosition == 0){
                        mapFragment.displayAllMarkers();
                    } else if(childPosition == 0) {
                       mapFragment.displayMarkersForCategory(item.categorieId);
                    } else {
                        mapFragment.displayMarkersForSearchTerm(item.subItems.get(childPosition));
                    }
                }
            // Close the navigation drawer
            mDrawer.closeDrawers();
                return true;
            }
        });
        expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long groupId) {
                //Log.d("DEBUG", "heading clicked");
                if (mapFragment != null) {
                    if (groupPosition == 0){
                        setTitle("Arriving");
                        mapFragment.displayAllMarkers();
                        mDrawer.closeDrawers();
                        return true;
                    }
                }
                return false;
            }
        });

        // Hockeyapp
        checkForUpdates();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public ArrayList<ExpandedMenuItem> setMenuItemsFromJSON(List<JSONObject> locations) {
        listDataHeader = new ArrayList<ExpandedMenuItem>();
        ExpandedMenuItem item0 = new ExpandedMenuItem();
        item0.setIconName(getString(R.string.all));
        item0.setIconImg(R.drawable.favorite);
        listDataHeader.add(item0);

        for (JSONObject location : locations) {
            try {
                JSONArray features = location.getJSONArray("features");
                JSONObject props = features.getJSONObject(0).getJSONObject("properties");

                ExpandedMenuItem item = new ExpandedMenuItem();
                item.setCategorieId(Integer.parseInt(props.get("category_id").toString()));
                item.setIconName((String) props.get("category"));
                item.setIconImg(getIconForCategory(item.getCategorieId()));

                if (item.categorieId == 8) {
                    item.setSubItems(getPoliceFromJSON(features));
                } else {
                    item.setSubItems(getLocationsFromJSON(features));
                }
                listDataHeader.add(item);

            } catch (Exception e) {
                Log.e(TAG, "Exception Loading GeoJSON: " + e.toString());
            }
        }
        return null;
    }

    public ArrayList<String> getLocationsFromJSON(JSONArray features) {
        ArrayList<String> places = new ArrayList<String>();
        places.add("All");
        try {
            for (int i = 0; i < features.length(); i++) {
                JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
                String name = properties.getString("name");
                places.add(name);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Exception Loading GeoJSON: " + e.toString());
        }
        return places;
    }

    public ArrayList<String> getPoliceFromJSON(JSONArray features) {
        ArrayList<String> places = new ArrayList<String>();
        places.add(getString(R.string.all));
        return places;
    }

    public int getIconForCategory(int categoryID) {
        // Make Custom Icon
        int iconPng = R.drawable.favorite;
        switch (categoryID) {
            case 1:
                iconPng = R.drawable.counseling_services_for_refugees;
                break;
            case 2:
                iconPng = R.drawable.doctors_general_practitioner_arabic;
                break;
            case 3:
                iconPng = R.drawable.doctors_general_practitioner_farsi;
                break;
            case 4:
                iconPng = R.drawable.doctors_gynaecologist_arabic;
                break;
            case 5:
                iconPng = R.drawable.doctors_gynaecologist_farsi;
                break;
            case 6:
                iconPng = R.drawable.german_language_classes;
                break;
            case 7:
                iconPng = R.drawable.lawyers_residence_and_asylum_law;
                break;
            case 8:
                iconPng = R.drawable.police;
                break;
            case 9:
                iconPng = R.drawable.public_authorities;
                break;
            case 10:
                iconPng = R.drawable.public_libraries;
                break;
            case 11:
                iconPng = R.drawable.public_transport;
                break;
            case 12:
                iconPng = R.drawable.shopping_and_food;
                break;
            case 13:
                iconPng = R.drawable.sports_and_freetime;
                break;

        }

        return iconPng;
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
                if (mapFragment != null) {
                    mapFragment.displayAllMarkers();
                }
                return false;
            }
        });

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
    }


    private void updateLocations() {
        if (mainLocations.size() == 0) {
            new FetchLocationsTask().execute();
        }
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

//
    @Override
    public boolean onQueryTextSubmit(String query) {
        // User pressed the search button
        if (mapFragment != null) {
            mapFragment.displayMarkersForSearchTerm(query);
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.isEmpty()) {
            if (mapFragment != null) {
                mapFragment.displayAllMarkers();
            }
        }
        return false;
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
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


    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }


    /********* FETCHING AND SETTING LOCATION DATA**********************/
    public class FetchLocationsTask extends AsyncTask<Void, Void, List<JSONObject>> {
        @Override
        protected List<JSONObject> doInBackground(Void... params) {
            return new UmapDataRequest().fetchLocations();
        }

        private void processDataUpdate(ArrayList<JSONObject> locations) {
            mainLocations = locations;
            setMenuItemsFromJSON(mainLocations);

            if (mMenuAdapter != null) {
                setMenuItemsFromJSON(locations);
                mMenuAdapter.updateData(listDataHeader);
            }
            if (mapFragment != null){
                mapFragment.receiveDataFromActivity(locations);
            }
        }

        @Override
        protected void onCancelled() {
            ArrayList<JSONObject> locations = getStoredLocations();
            if ((locations != null) && (locations.size() > 0)) {
                processDataUpdate(locations);

            } else {
                showOfflineMessage();
            }
        }

        @Override
        protected void onPostExecute(List<JSONObject> locations) {
            //check if the map exists already
            Log.i("FETCH", "arrived at post exec with locations: " + locations);

            if ((locations != null) && (locations.size() > 0)) {
                processDataUpdate((ArrayList<JSONObject>) locations);
                storeLocations((ArrayList<JSONObject>) locations);
            } else {
                if (!showStoredLocations()){
                    showOfflineMessage();
                }
            }
        }

        private boolean showStoredLocations() {
            List<JSONObject> storedLocations = getStoredLocations();
            if ((storedLocations != null) && (storedLocations.size() > 0)) {
                processDataUpdate((ArrayList<JSONObject>) storedLocations);
                return true;
            } else {
                return false;
            }
        }

        private void showOfflineMessage() {
            String message = getString(R.string.offline_message);
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }


        private ArrayList<JSONObject> getStoredLocations() {
            SaveArray save = new SaveArray(MainActivity.this.getApplicationContext());
            ArrayList<JSONObject> locations = save.getArray("locations");
            return locations;
        }

        private void storeLocations(ArrayList<JSONObject> locations) {
            SaveArray save = new SaveArray(MainActivity.this.getApplicationContext());
            save.saveArray("locations", locations);
            Log.i(TAG, "Saved Locations");
        }
    }

    @Override
    public void onLanguageSelection(boolean selected) {
        //Move to next fragment
        if (selected) {

        }
    }

}
