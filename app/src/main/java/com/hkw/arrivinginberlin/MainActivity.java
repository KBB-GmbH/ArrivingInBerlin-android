package com.hkw.arrivinginberlin;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
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

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, MapboxMap.OnMarkerClickListener {
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    ExpandableMenuAdapter mMenuAdapter;
    ExpandableListView expandableList;
    List<ExpandedMenuItem> listDataHeader;
    private ActionBarDrawerToggle drawerToggle;
    private static final String TAG = "MainActivity";
    private GoogleApiClient client;
    private BottomBar bottomBar;
    public ArrayList<JSONObject> mainLocations = new ArrayList<JSONObject>();

    private MapView mapView;
    private MapboxMap mapBox;
    private List<CategoryMarker> allMarkers = new ArrayList<>();
    FloatingActionButton floatingActionButton;
    LocationServices locationServices;

    // JSON encoding/decoding
    public final static String JSON_CHARSET = "UTF-8";
    public final static String JSON_FIELD_REGION_NAME = "BERLIN_REGION";
    private static final int PERMISSIONS_LOCATION = 0;
    private boolean isEndNotified;
    private ProgressBar progressBar;
    private Boolean didDownload = false;
    private static final String DOWNLOADTAG = "download";
    private static final String LOCDATA = "location_data";


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);

        locationServices = LocationServices.getLocationServices(this);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);;
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapBox = mapboxMap;
                mapBox.setOnMarkerClickListener(MainActivity.this);
                new FetchLocationsTask().execute();
                enableLocation(true);
                if (!didDownload) {
                    startDownloadingMap();
                }
            }
        });

        floatingActionButton = (FloatingActionButton) findViewById(R.id.location_toggle_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapBox != null) {
                    toggleGps(!mapBox.isMyLocationEnabled());
                }
            }
        });


        bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.setItemsFromMenu(R.menu.bottom_navigation, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int itemId) {
                Log.i(TAG, "menu tapped");
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                switch (itemId) {
                    case R.id.map_item:
                        break;
                    case R.id.info_item:
                        intent.putExtra("startFragment", 1);
                        startActivity(intent);
                        break;
                    case R.id.lang_item:
                        intent.putExtra("startFragment", 2);
                        startActivity(intent);
                        break;
                    case R.id.contact_item:
                        intent.putExtra("startFragment", 3);
                        startActivity(intent);
                        break;
                    case R.id.about_item:
                        intent.putExtra("startFragment", 4);
                        startActivity(intent);
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

                if (groupPosition == 0) {
                    displayAllMarkers();
                } else if (childPosition == 0) {
                    displayMarkersForCategory(item.categorieId);
                } else {
                    displayMarkersForSearchTerm(item.subItems.get(childPosition));
                }
                 //Close the navigation drawer
                mDrawer.closeDrawers();
                return false;
            }
        });
        expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long groupId) {
                if (groupPosition == 0) {
                    setTitle("Arriving");
                    displayAllMarkers();
                    mDrawer.closeDrawers();
                    return true;
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

    private void hideNavBarItems(Boolean shouldHide) {
        if (shouldHide) {
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toolbar.setVisibility(View.GONE);

        } else {
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED);
            toolbar.setVisibility(View.VISIBLE);

        }
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
                displayAllMarkers();
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
//        // User pressed the search button
        displayMarkersForSearchTerm(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.isEmpty()) {
            displayAllMarkers();
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

    public void updateLocationPoints(List<JSONObject> locations) {
        for (JSONObject location : locations) {
            try {
                JSONObject feature = location.getJSONArray("features").getJSONObject(0);
                JSONObject properties = feature.getJSONObject("properties");
                int categoryID = Integer.parseInt(properties.get("category_id").toString());
                addGeoPointsForCategory(categoryID, location, mapBox);

            } catch (Exception e) {
                Log.e(TAG, "Exception Loading GeoJSON: " + e.toString());
            }
        }
    }

    public void addGeoPointsForCategory(int categoryID, JSONObject json, MapboxMap mapboxMap) {
        ArrayList<LatLng> points = new ArrayList<>();
        String uri = getIconStringForCategory(categoryID);
        try {
            JSONArray features = json.getJSONArray("features");

            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONArray coord = geometry.getJSONArray("coordinates");
                LatLng latLng = new LatLng(coord.getDouble(1), coord.getDouble(0));
                points.add(latLng);

                Log.i("JSON_FEATURE", feature.getJSONObject("properties").toString());
                // Information in Each point
                JSONObject properties = feature.getJSONObject("properties");
                String name = properties.getString("name");
                String beschreibung = properties.getString("beschreibung").replace("*", "");
                String adresse = properties.getString("adresse").replace("*", "");
                if (adresse.length() != 0) {
                    adresse = adresse.substring(0, 1).toUpperCase() + adresse.substring​(1);
                }
                String telefon = properties.getString("telefon").replace("*", "");
                String medium = properties.getString("medium").replace("*", "").replace("[[", "").replace("]]", "");
                String transport = properties.getString("transport").replace("*", "").replace("[[", "").replace("]]", "");

                int imageResource = getResources().getIdentifier(uri, null, MainActivity.this.getPackageName());
                Log.i("IMAGE RESOURCE", String.valueOf(imageResource));
                IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                Drawable iconDrawable = getResources().getDrawable(imageResource);
                Icon icon = iconFactory.fromDrawable(iconDrawable);

                MarkerViewOptions marker = new MarkerViewOptions()
                        .position(latLng)
                        .title(name)
                        .icon(icon)
                        .snippet(beschreibung + "\n" + adresse + "\n" + telefon + "\n" + transport + "\n" + medium);
                CategoryMarker catMarker = new CategoryMarker(mapboxMap.addMarker(marker), categoryID, true, marker);
                allMarkers.add(catMarker);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Exception Loading GeoJSON: " + e.toString());
        }
        Log.i("MainActivity", "my markers:" + allMarkers);

    }

    public String getIconStringForCategory(int categoryID) {
        // Make Custom Icon
        String uri = "@drawable/";
        String iconPng = "";
        switch (categoryID) {
            case 1:
                iconPng = "counseling_services_for_refugees";
                break;
            case 2:
                iconPng = "doctors_general_practitioner_arabic";
                break;
            case 3:
                iconPng = "doctors_general_practitioner_farsi";
                break;
            case 4:
                iconPng = "doctors_gynaecologist_arabic";
                break;
            case 5:
                iconPng = "doctors_gynaecologist_farsi";
                break;
            case 6:
                iconPng = "german_language_classes";
                break;
            case 7:
                iconPng = "lawyers_residence_and_asylum_law";
                break;
            case 8:
                iconPng = "police";
                break;
            case 9:
                iconPng = "public_authorities";
                break;
            case 10:
                iconPng = "public_libraries";
                break;
            case 11:
                iconPng = "public_transport";
                break;
            case 12:
                iconPng = "shopping_and_food";
                break;
            case 13:
                iconPng = "sports_and_freetime";
                break;

        }
        uri = uri + iconPng;

        return uri;
    }


    public void displayAllMarkers() {
        removeAllMarkers();
        mapBox.removeAnnotations();
        for (CategoryMarker cm : allMarkers) {
            cm.marker = mapBox.addMarker(cm.markerViewOptions);
        }
    }

    public void removeAllMarkers() {
        for (Marker m : mapBox.getMarkers()) {
            mapBox.removeMarker(m);
            mapBox.removeAnnotations();
        }
    }

    public void displayMarkersForCategory(final int categoryId) {
        removeAllMarkers();
        for (CategoryMarker cm : allMarkers) {
            if (cm.categoryID == categoryId) {
                cm.marker = mapBox.addMarker(cm.markerViewOptions);
            }
        }
    }

    public void displayMarkersForSearchTerm(String searchTerm) {
        removeAllMarkers();
        for (CategoryMarker cm : allMarkers) {
            if ((cm.marker.getTitle().contains(searchTerm)) || (cm.marker.getSnippet().contains(searchTerm))) {
                cm.marker = mapBox.addMarker(cm.markerViewOptions);
            }
        }
    }


    private void startDownloadingMap() {
        // Set up the OfflineManager
        Log.i(TAG, "start downloading");
        didDownload = true;
        OfflineManager offlineManager = OfflineManager.getInstance(MainActivity.this);

        // Create a bounding box for the offline region
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(52.56000, 13.306689)) // Northeast
                .include(new LatLng(52.464649, 13.555756)) // Southwest
                .build();

        // Define the offline region
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                Style.MAPBOX_STREETS,
                latLngBounds,
                12,
                12,
                this.getResources().getDisplayMetrics().density);

        // Set the metadata
        byte[] metadata;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_FIELD_REGION_NAME, "BERLIN");
            String json = jsonObject.toString();
            metadata = json.getBytes(JSON_CHARSET);
            Log.i(TAG, "metadata created");
        } catch (Exception e) {
            Log.e(TAG, "Failed to encode metadata: " + e.getMessage());
            metadata = null;
        }

        // Create the region asynchronously
        offlineManager.createOfflineRegion(definition, metadata, new OfflineManager.CreateOfflineRegionCallback() {
            @Override
            public void onCreate(OfflineRegion offlineRegion) {
                offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);

                // Display the download progress bar
                progressBar = (ProgressBar) findViewById(R.id.progress_bar);
                progressBar.setVisibility(ProgressBar.VISIBLE);
                didDownload = true;
                startProgress();

                // Monitor the download progress using setObserver
                offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
                    @Override
                    public void onStatusChanged(OfflineRegionStatus status) {

                        // Calculate the download percentage and update the progress bar
                        double percentage = status.getRequiredResourceCount() >= 0 ?
                                (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                                0.0;


                        if (status.isComplete()) {
                            // Download complete
                            Log.i(TAG, "download complete");
                            endProgress(getString(R.string.region_loaded));

                        } else if (status.isRequiredResourceCountPrecise()) {
                            // Switch to determinate state
                            setPercentage((int) Math.round(percentage));
                        }
                    }

                    @Override
                    public void onError(OfflineRegionError error) {
                        // If an error occurs, print to logcat
                        Log.e(TAG, "onError reason: " + error.getReason());
                        Log.e(TAG, "onError message: " + error.getMessage());
                    }

                    @Override
                    public void mapboxTileCountLimitExceeded(long limit) {
                        // Notify if offline region exceeds maximum tile count
                        Log.e(TAG, "Mapbox tile count limit exceeded: " + limit);
                    }
                });
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error: " + error);
            }
        });
    }

    // Progress bar methods
    private void startProgress() {
        // Start and show the progress bar
        isEndNotified = false;
        progressBar.setIndeterminate(true);
    }

    private void setPercentage(final int percentage) {
        progressBar.setIndeterminate(false);
        progressBar.setProgress(percentage);
    }

    private void endProgress(final String message) {
        // Don't notify more than once
        if (isEndNotified) return;

        // Stop and hide the progress bar
        isEndNotified = true;
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        // Show a toast
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @UiThread
    public void toggleGps(boolean enableGps) {
        if (enableGps) {
            // Check if user has granted location permission
            if (!locationServices.areLocationPermissionsGranted()) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
            } else {
                enableLocation(true);
            }
        } else {
            enableLocation(false);
        }
    }

    private void enableLocation(boolean enabled) {
        if (enabled) {
            locationServices.addLocationListener(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if ((location != null) && (location.getLatitude() >= 52.3 && location.getLatitude() < 52.7) &&
                            (location.getLongitude() >= 13.1 && location.getLongitude() < 13.7)) {
                        Log.i(TAG, "latlon: " + location.getLatitude() + location.getLongitude());
                        // Move the map camera to where the user location is
                        mapBox.setCameraPosition(new CameraPosition.Builder()
                                .target(new LatLng(location))
                                .zoom(12)
                                .build());
                    }
                }
            });
            floatingActionButton.setImageResource(R.drawable.favorite2);
        } else {
            floatingActionButton.setImageResource(R.drawable.favorite);
        }
        // Enable or disable the location layer on the map
        mapBox.setMyLocationEnabled(enabled);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_LOCATION: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableLocation(true);
                }
            }
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        double lat = marker.getPosition().getLatitude() + 0.012;
        double lng = marker.getPosition().getLongitude();
        mapBox.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(lat,lng))
                .zoom(12)
                .build());
        return false;
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
            updateLocationPoints(mainLocations);
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
                if (!showStoredLocations()) {
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
    public void onResume() {
        super.onResume();
        bottomBar.selectTabAtPosition(0, false);
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
        outState.putBoolean("didDownload", didDownload);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        didDownload = savedInstanceState.getBoolean("didDownload");
    }


}
