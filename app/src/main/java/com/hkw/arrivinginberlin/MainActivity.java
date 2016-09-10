package com.hkw.arrivinginberlin;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Button;
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
import com.roughike.bottombar.OnMenuTabSelectedListener;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
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
    private static final String TAG = "MainActivity";
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mapbox access token only needs to be configured once in your app
        MapboxAccountManager.start(this, getString(R.string.access_token));
        // This contains the MapView in XML and needs to be called after the account manager
        setContentView(R.layout.activity_main);
        locationServices = LocationServices.getLocationServices(MainActivity.this);

        BottomBar bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.setItemsFromMenu(R.menu.bottom_navigation, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int itemId) {
                switch (itemId) {
                    case R.id.map_item:
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


        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                startDownloadingMap();
                mapBox = mapboxMap;
                mapBox.setMyLocationEnabled(true);
                new FetchLocationsTask().execute();
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
                displayAllMarkers();
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // User pressed the search button
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

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
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
    }


    public class FetchLocationsTask extends AsyncTask<Void, Void, List<JSONObject>> {
        @Override
        protected List<JSONObject> doInBackground(Void... params) {
            return new UmapDataRequest().fetchLocations();
        }

        @Override
        protected void onCancelled() {
            ArrayList<JSONObject> locations = getStoredLocations();
            if ((locations != null) && (locations.size() > 0)) {
                updateLocationPoints(locations);
            } else {
                showOfflineMessage();
            }
        }

        @Override
        protected void onPostExecute(List<JSONObject> locations) {
            //check if the map exists already
            Log.i("FETCH", "arrived at post exec with location: " + locations);

            if ((locations != null) && (locations.size() > 0)) {
                //store locations
                updateLocationPoints(locations);
                storeLocations((ArrayList<JSONObject>) locations);
            } else {
                List<JSONObject> storedLocations = getStoredLocations();
                if ((storedLocations != null) && (storedLocations.size() > 0)) {
                    updateLocationPoints(locations);
                } else {
                    showOfflineMessage();
                }
            }
        }

        private void showOfflineMessage() {
            String message = getString(R.string.offline_message);
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }

        private void updateLocationPoints(List<JSONObject> locations) {
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

        private ArrayList<JSONObject> getStoredLocations() {
            SaveArray save = new SaveArray(getApplicationContext());
            ArrayList<JSONObject> locations = save.getArray("locations");
            return locations;
        }

        private void storeLocations(ArrayList<JSONObject> locations) {
            SaveArray save = new SaveArray(getApplicationContext());
            save.saveArray("locations", locations);
            Log.i(TAG, "Saved Locations");
        }
    }


    public void addGeoPointsForCategory(int categoryID, JSONObject json, MapboxMap mapboxMap) {
        ArrayList<LatLng> points = new ArrayList<>();
        String uri = getIconForCategory(categoryID);

        try {
            JSONArray features = json.getJSONArray("features");

            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONArray coord = geometry.getJSONArray("coordinates");
                LatLng latLng = new LatLng(coord.getDouble(1), coord.getDouble(0));
                points.add(latLng);

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

                int imageResource = getResources().getIdentifier(uri, null, getPackageName());
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

    public String getIconForCategory(int categoryID) {
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

    public void selectDrawerItem(MenuItem menuItem) {
        Log.i(TAG, "drawer selected");
        switch (menuItem.getItemId()) {
            case R.id.nav_all_categories:
                displayAllMarkers();
                break;
            case R.id.nav_counseling:
                displayMarkersForCategory(1);
                break;
            case R.id.nav_doctor_gp_arabic:
                displayMarkersForCategory(2);
                break;
            case R.id.nav_doctor_gp_farsi:
                displayMarkersForCategory(3);
                break;
            case R.id.nav_doctor_gyn_arabic:
                displayMarkersForCategory(4);
                break;
            case R.id.nav_doctor_gyn_farsi:
                displayMarkersForCategory(5);
                break;
            case R.id.nav_german_language_classes:
                displayMarkersForCategory(6);
                break;
            case R.id.nav_lawyers:
                displayMarkersForCategory(7);
                break;
            case R.id.nav_police:
                displayMarkersForCategory(8);
                break;
            case R.id.nav_authorities:
                displayMarkersForCategory(9);
                break;
            case R.id.nav_libraries:
                displayMarkersForCategory(10);
                break;
            case R.id.nav_transport:
                displayMarkersForCategory(11);
                break;
            case R.id.nav_shopping_food:
                displayMarkersForCategory(12);
                break;
            case R.id.nav_sports_freetime:
                displayMarkersForCategory(13);
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
        OfflineManager offlineManager = OfflineManager.getInstance(this);

        // Create a bounding box for the offline region
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(52.506509, 13.359811)) // Northeast
                .include(new LatLng(52.275972, 13.697205)) // Southwest
                .build();

        // Define the offline region
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                mapView.getStyleUrl(),
                latLngBounds,
                10,
                15,
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
                            endProgress("Region downloaded successfully.");

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
        progressBar.setVisibility(View.VISIBLE);
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
        progressBar.setVisibility(View.GONE);

        // Show a toast
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @UiThread
    public void toggleGps(boolean enableGps) {
        if (enableGps) {
            // Check if user has granted location permission
            if (!locationServices.areLocationPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, new String[]{
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
                    if (location != null) {
                        // Move the map camera to where the user location is
                        mapBox.setCameraPosition(new CameraPosition.Builder()
                                .target(new LatLng(location))
                                .zoom(16)
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
}
