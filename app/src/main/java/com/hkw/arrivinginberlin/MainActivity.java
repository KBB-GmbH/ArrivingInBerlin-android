package com.hkw.arrivinginberlin;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.icu.text.StringPrepParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.GeoJSON;
import com.mapbox.services.commons.utils.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private MapView mapView;
    private MapboxMap mapBox;
    private List<JSONObject> locations = new ArrayList<>();
    private List<CategoryMarker> allMarkers = new ArrayList<>();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    // JSON encoding/decoding
    public final static String JSON_CHARSET = "UTF-8";
    public final static String JSON_FIELD_REGION_NAME = "BERLIN_REGION";
    private boolean isEndNotified;
    private ProgressBar progressBar;

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mapbox access token only needs to be configured once in your app
        MapboxAccountManager.start(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the account manager
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                startDownloadingMap();
                mapBox = mapboxMap;

            }

        });

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        // Hockeyapp
        checkForUpdates();


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        Log.i(TAG, "item drawer selected with id "+ item);
        return super.onOptionsItemSelected(item);
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE! Make sure to override the method with only a single `Bundle` argument
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
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
        checkForCrashes();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        unregisterManagers();
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
        unregisterManagers();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public class FetchLocationsTask extends AsyncTask<Void, Void, List<JSONObject>>{
        @Override
        protected List<JSONObject> doInBackground(Void... params) {
            Log.i(TAG, "fetching locations");
            return new UmapDataRequest().fetchLocations();
        }

        @Override
        protected void onPostExecute(List<JSONObject> newLocations){
            locations = newLocations;
            //check if the map exists already
            Log.i("FETCH", "arrived at post exec with location: " + newLocations);

            if (locations != null){
                for (JSONObject location : locations) {
                    try {
                        JSONObject feature = location.getJSONArray("features").getJSONObject(0);
                        JSONObject properties = feature.getJSONObject("properties");
                        int categoryID = Integer.parseInt(properties.get("category_id").toString());
                        addGeoPointsForCategory(categoryID, location, mapBox);

                    }catch (Exception e) {
                        Log.e("MainActivity", "Exception Loading GeoJSON: " + e.toString());
                    }
                }
            }
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
                if (adresse.length() != 0 ) {
                    adresse = adresse.substring(0,1).toUpperCase() + adresse.substring​(1);
                }
                String telefon = properties.getString("telefon").replace("*", "");
                String medium = properties.getString("medium").replace("*", "").replace("[[","").replace("]]", "");
                String transport = properties.getString("transport").replace("*", "").replace("[[","").replace("]]", "");

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

    public String getIconForCategory(int categoryID){
        // Make Custom Icon
        String uri = "@drawable/";
        String iconPng ="";
        switch (categoryID){
            case 1: iconPng ="counseling_services_for_refugees";
                break;
            case 2: iconPng ="doctors_general_practitioner_arabic";
                break;
            case 3: iconPng ="doctors_general_practitioner_farsi";
                break;
            case 4: iconPng ="doctors_gynaecologist_arabic";
                break;
            case 5: iconPng = "doctors_gynaecologist_farsi";
                break;
            case 6: iconPng = "german_language_classes";
                break;
            case 7: iconPng = "lawyers_residence_and_asylum_law";
                break;
            case 8: iconPng = "police";
                break;
            case 9: iconPng = "public_authorities";
                break;
            case 10: iconPng = "public_libraries";
                break;
            case 11: iconPng = "public_transport";
                break;
            case 12: iconPng = "shopping_and_food";
                break;
            case 13: iconPng = "sports_and_freetime";
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

    public void displayAllMarkers(){
        removeAllMarkers();
        mapBox.removeAnnotations();
        for (CategoryMarker cm:allMarkers) {
            cm.marker= mapBox.addMarker(cm.markerViewOptions);
        }
    }

    public void removeAllMarkers() {
        for (Marker m:mapBox.getMarkers()) {
            mapBox.removeMarker(m);
            mapBox.removeAnnotations();
        }
    }

    public void displayMarkersForCategory(final int categoryId) {
        removeAllMarkers();
        for (CategoryMarker cm:allMarkers) {
            if (cm.categoryID == categoryId) {
                cm.marker = mapBox.addMarker(cm.markerViewOptions);
            }
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

    private void startDownloadingMap() {
        // Set up the OfflineManager
        Log.i(TAG, "start downloading");
        OfflineManager offlineManager = OfflineManager.getInstance(this);

// BERLIN COORDINATES
// .include(new LatLng(52.606509, 13.259811))
//                .include(new LatLng(52.275972, 13.697205))

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

                        Log.i(TAG, "Percentage done: " + percentage);
                        if (status.isComplete()) {
                            // Download complete
                            Log.i(TAG, "download complete");
                            new FetchLocationsTask().execute();
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

}

