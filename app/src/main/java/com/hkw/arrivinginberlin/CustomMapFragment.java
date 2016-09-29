package com.hkw.arrivinginberlin;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CustomMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CustomMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomMapFragment extends SuperFragment {

    private MapView mapView;
    private MapboxMap mapBox;
    private List<CategoryMarker> allMarkers = new ArrayList<>();
    FloatingActionButton floatingActionButton;
    LocationServices locationServices;
    ProgressDialog progressDialog;

    // JSON encoding/decoding
    public final static String JSON_CHARSET = "UTF-8";
    public final static String JSON_FIELD_REGION_NAME = "BERLIN_REGION";
    private static final int PERMISSIONS_LOCATION = 0;
    private boolean isEndNotified;
    private ProgressBar progressBar;
    private static final String TAG = "MapFragment";
    private static final String DATA = "data";
    private OnFragmentInteractionListener mListener;
    private ArrayList<JSONObject> locationData;

    public CustomMapFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static CustomMapFragment newInstance(ArrayList<JSONObject> data) {
        CustomMapFragment fragment = new CustomMapFragment();
        Bundle args = new Bundle();

        ArrayList<String> newData = new ArrayList<String>();

        for (JSONObject object: data) {
            newData.add(object.toString());
        }
        args.putStringArrayList(DATA, newData);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationData = new ArrayList<JSONObject>();
        if (getArguments() != null) {
            ArrayList<String> oldData = getArguments().getStringArrayList(DATA);
            for(String str: oldData) {
                try {
                    JSONObject json = new JSONObject(str);
                    locationData.add(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                } {

                }
            }
        } else {
            Log.i(TAG, "no arguments found");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout =  inflater.inflate(R.layout.fragment_custom_map, container, false);

        // Mapbox access token only needs to be configured once in your app
        MapboxAccountManager.start(getActivity(), getString(R.string.access_token));
        // This contains the MapView in XML and needs to be called after the account manager

        locationServices = LocationServices.getLocationServices(getActivity());

        mapView = (MapView) layout.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                startDownloadingMap();
                mapBox = mapboxMap;
                enableLocation(true);
                updateLocationPoints(locationData);
            }
        });

        floatingActionButton = (FloatingActionButton) layout.findViewById(R.id.location_toggle_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapBox != null) {
                    toggleGps(!mapBox.isMyLocationEnabled());
                }
            }
        });


        return layout;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /********* FETCHING AND SETTING LOCATION DATA**********************/
    @Override
    public void receiveDataFromActivity(ArrayList<JSONObject> data){
        Log.i(TAG, "received data from activity");
        locationData = data;
        updateLocationPoints(locationData);
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
        String uri = getIconForCategory(categoryID);
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

                int imageResource = getResources().getIdentifier(uri, null, getActivity().getPackageName());
                Log.i("IMAGE RESOURCE", String.valueOf(imageResource));
                IconFactory iconFactory = IconFactory.getInstance(getActivity());
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
        OfflineManager offlineManager = OfflineManager.getInstance(getActivity());

        // Create a bounding box for the offline region
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(52.56000, 13.306689)) // Northeast
                .include(new LatLng(52.464649, 13.555756)) // Southwest
                .build();

        // Define the offline region
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                mapView.getStyleUrl(),
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
                progressBar = (ProgressBar) getView().findViewById(R.id.progress_bar);
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
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @UiThread
    public void toggleGps(boolean enableGps) {
        if (enableGps) {
            // Check if user has granted location permission
            if (!locationServices.areLocationPermissionsGranted()) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{
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
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
