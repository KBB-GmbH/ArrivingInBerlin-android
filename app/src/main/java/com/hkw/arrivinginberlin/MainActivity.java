package com.hkw.arrivinginberlin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Button;
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
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
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
import com.mapbox.services.Constants;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v5.DirectionsCriteria;
import com.mapbox.services.directions.v5.MapboxDirections;
import com.mapbox.services.directions.v5.models.DirectionsResponse;
import com.mapbox.services.directions.v5.models.DirectionsRoute;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabSelectedListener;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, MapboxMap.OnMarkerClickListener, MapboxMap.OnMapClickListener {
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
    private PolylineOptions polyLine;
    private MapView mapView;
    private MapboxMap mapBox;
    private DirectionsRoute currentRoute;
    private List<CategoryMarker> allMarkers = new ArrayList<>();
    FloatingActionButton floatingActionButton;
    FloatingActionButton walkButton;
    FloatingActionButton driveButton;
    FloatingActionButton publicTransportButton;
    LocationServices locationServices;

    private Position destination;

    // JSON encoding/decoding
    public final static String JSON_CHARSET = "UTF-8";
    public final static String ENGLISH_KEY = "english_data";
    public final static String GERMAN_KEY = "german_data";
    public final static String FARSI_KEY = "farsi_data";
    public final static String ARABIC_KEY = "arabic_data";
    public final static String KURDISH_KEY = "kurdish_data";
    public final static String FRENCH_KEY = "french_data";
    public final static String JSON_FIELD_REGION_NAME = "BERLIN_REGION";

    private static final int PERMISSIONS_LOCATION = 0;
    private boolean isEndNotified;
    private ProgressBar progressBar;
    private Boolean didDownload = false;
    static final int CHANGE_LANGUAGE = 1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapboxAccountManager.start(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);

        //Language:
        LocaleUtils.setLanguageFromPreference(getApplicationContext());
        Log.i(TAG, "default language on create: " + Locale.getDefault());

        locationServices = LocationServices.getLocationServices(this);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapBox = mapboxMap;
                mapBox.setOnMarkerClickListener(MainActivity.this);
                mapBox.setOnMapClickListener(MainActivity.this);
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

        walkButton = (FloatingActionButton) findViewById(R.id.walk);
        walkButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(destination != null){
                    try {
                        getRoute(destination, DirectionsCriteria.PROFILE_WALKING );
                    } catch (ServicesException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        driveButton = (FloatingActionButton) findViewById(R.id.drive);
        driveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(destination != null){
                    try {
                        getRoute(destination, DirectionsCriteria.PROFILE_DRIVING);
                    } catch (ServicesException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        publicTransportButton = (FloatingActionButton) findViewById(R.id.public_transport);
        publicTransportButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (destination != null){
                    String loc = "http://maps.google.com/maps?daddr="+ destination.getLatitude()+ ","
                            + destination.getLongitude()+ "&directionsmode=transit";

                    Uri gmmIntentUri = Uri.parse(loc);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                }
            }
        });

        bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.setItemsFromMenu(R.menu.bottom_navigation, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int itemId) {
                Log.i(TAG, "menu tapped");
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                switch (itemId) {
                    case R.id.map_item:
                        break;
                    case R.id.info_item:
                        intent.putExtra("startFragment", 1);
                        startActivityForResult(intent, CHANGE_LANGUAGE);
                        break;
                    case R.id.lang_item:
                        intent.putExtra("startFragment", 2);
                        startActivityForResult(intent, CHANGE_LANGUAGE);
                        break;
                    case R.id.contact_item:
                        intent.putExtra("startFragment", 3);
                        startActivityForResult(intent, CHANGE_LANGUAGE);
                        break;
                    case R.id.about_item:
                        intent.putExtra("startFragment", 4);
                        startActivityForResult(intent, CHANGE_LANGUAGE);
                        break;
                    default:
                        break;
                }
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
                showTransportButtons(false);
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
                showTransportButtons(false);
                if (groupPosition == 0) {
                    setTitle("Arriving");
                    displayAllMarkers();
                    mDrawer.closeDrawers();
                    return false;
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
                String medium = properties.getString("link").replace("*", "").replace("[[", "").replace("]]", "");

                int imageResource = getResources().getIdentifier(uri, null, MainActivity.this.getPackageName());
                Log.i("IMAGE RESOURCE", String.valueOf(imageResource));
                IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                Drawable iconDrawable = getResources().getDrawable(imageResource);
                Icon icon = iconFactory.fromDrawable(iconDrawable);

                MarkerViewOptions marker = new MarkerViewOptions()
                        .position(latLng)
                        .title(name)
                        .icon(icon)
                        .snippet(beschreibung + "\n" + adresse + "\n" + telefon + "\n" + medium);
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
        removePolyline();
        removeAllMarkers();
        mapBox.removeAnnotations();
        for (CategoryMarker cm : allMarkers) {
            cm.marker = mapBox.addMarker(cm.markerViewOptions);
        }
    }

    public void removeAllMarkers() {
        removePolyline();
        for (Marker m : mapBox.getMarkers()) {
            mapBox.removeMarker(m);
            mapBox.removeAnnotations();
        }
    }

    public void displayMarkersForCategory(final int categoryId) {
        removePolyline();
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
                    if (location != null) {
                        // Move the map camera to where the user location is
// Update directions
                    }
                }
            });
            floatingActionButton.setImageResource(R.drawable.ic_my_location_24dp);
        } else {
            floatingActionButton.setImageResource(R.drawable.ic_location_disabled_24dp);
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

    //MAP HANDLERS
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        //show direction buttons
        showTransportButtons(true);
        removePolyline();

        destination = Position.fromCoordinates(marker.getPosition().getLongitude(), marker.getPosition().getLatitude());

        double lat = marker.getPosition().getLatitude() + 0.012;
        double lng = marker.getPosition().getLongitude();
        mapBox.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(lat,lng))
                .zoom(12)
                .build());
        return false;
    }

    @Override
    public void onMapClick(@NonNull LatLng point) {
        //remove direction buttons
        showTransportButtons(false);
    }

    public void showTransportButtons(Boolean show){
        if (show){
            walkButton.setVisibility(Button.VISIBLE);
            driveButton.setVisibility(Button.VISIBLE);
            publicTransportButton.setVisibility(Button.VISIBLE);
        } else {
            walkButton.setVisibility(Button.INVISIBLE);
            driveButton.setVisibility(Button.INVISIBLE);
            publicTransportButton.setVisibility(Button.INVISIBLE);
        }
    }


    /********* FETCHING AND SETTING LOCATION DATA**********************/
    public class FetchLocationsTask extends AsyncTask<Void, Void, List<JSONObject>> {
        private String key = "en";
        @Override
        protected List<JSONObject> doInBackground(Void... params) {
            key = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(LocaleUtils.LANGUAGE, "en");

            switch (key){
                case "en":
                    return new UmapDataRequest().getLocationsEnglish();
                case "fr":
                    return new UmapDataRequest().getLocations(159184, 159198);
                case "de":
                    return new UmapDataRequest().getLocations(226926, 226940);
                case "fa":
                    return new UmapDataRequest().getLocations(128475, 128489);
                case "ar":
                    return new UmapDataRequest().getLocations(128884, 128897);
                case "ku":
                    return new UmapDataRequest().getLocations(128475, 128497);
                default:
                    return new UmapDataRequest().getLocationsEnglish();

            }

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
            ArrayList<JSONObject> locations = save.getArray(getKeyForData());
            return locations;
        }

        private void storeLocations(ArrayList<JSONObject> locations) {
            SaveArray save = new SaveArray(MainActivity.this.getApplicationContext());
            save.saveArray(getKeyForData(), locations);
            Log.i(TAG, "Saved Locations");
        }

        private String getKeyForData(){
            switch (key){
                case "en":
                    return ENGLISH_KEY;
                case "fr":
                    return FRENCH_KEY;
                case "de":
                    return GERMAN_KEY;
                case "fa":
                    return FARSI_KEY;
                case "ar":
                    return ARABIC_KEY;
                case "ku":
                    return KURDISH_KEY;
                default:
                    return ENGLISH_KEY;
            }
        }
    }

    //*************** ROUTE ******************************//

    private void getRoute(Position destination, String profile) throws ServicesException {

        Position origin;
        if (mapBox.getMyLocation() != null) {
            origin = Position.fromCoordinates(mapBox.getMyLocation().getLongitude(), mapBox.getMyLocation().getLatitude());
        }else {
            origin = Position.fromCoordinates(13.388389, 52.516889);
        }

        MapboxDirections client = new MapboxDirections.Builder()
                .setOrigin(origin)
                .setDestination(destination)
                .setProfile(profile)
                .setAccessToken(getString(R.string.access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                Log.d(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                }

                // Print some info about the route
                currentRoute = response.body().getRoutes().get(0);
                Log.d(TAG, "Distance: " + currentRoute.getDistance());
                Toast.makeText(MainActivity.this, "Route is " +  currentRoute.getDistance() + " meters long.", Toast.LENGTH_SHORT).show();

                // Draw the route on the map
                drawRoute(currentRoute);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removePolyline() {
        if (polyLine != null && mapBox != null) {
            mapBox.removePolyline(polyLine.getPolyline());
        }
    }

    private void drawRoute(DirectionsRoute route) {
        removePolyline();
        // Convert LineString coordinates into LatLng[]
        LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.OSRM_PRECISION_V5);
        List<Position> coordinates = lineString.getCoordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).getLatitude(),
                    coordinates.get(i).getLongitude());
        }

        // Draw Points on MapView
        polyLine = new PolylineOptions()
                .add(points)
                .color(Color.parseColor("#009688"))
                .width(5);
        mapBox.addPolyline(polyLine);
    }

    @Override
    public void onResume() {
        super.onResume();
        bottomBar.selectTabAtPosition(0, false);
        mapView.onResume();
        LocaleUtils.setLanguageFromPreference(getApplicationContext());
        Log.i(TAG, "default language on resume: " + Locale.getDefault());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHANGE_LANGUAGE) {
            if (resultCode == RESULT_OK) {
                this.finish();
                startActivity(this.getIntent());
            }
        }
    }

}
