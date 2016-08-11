package com.hkw.arrivinginberlin;

import android.icu.text.StringPrepParseException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.services.commons.utils.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private MapView mapView;

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
                addGeoPoints("_arriving_in_berlin___a_map_made_by_refugees___german_version_.geojson", mapboxMap);
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

    public void addGeoPoints(String fileName, MapboxMap mapboxMap) {
        ArrayList<LatLng> points = new ArrayList<>();
        try {
            // Load GeoJSON file
            InputStream inputStream = getAssets().open(fileName);
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            StringBuilder stringBuilder = new StringBuilder();
            int charPointer;
            while ((charPointer = bufferReader.read()) != -1) {
                stringBuilder.append((char) charPointer);
            }
            inputStream.close();

            // Parse JSON
            JSONObject json = new JSONObject(stringBuilder.toString());
            JSONArray features = json.getJSONArray("features");

            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONArray coord = geometry.getJSONArray("coordinates");
                LatLng latLng = new LatLng(coord.getDouble(1), coord.getDouble(0));
                points.add(latLng);

                JSONObject properties = feature.getJSONObject("properties");
                String name = properties.getString("name");


                MarkerViewOptions marker = new MarkerViewOptions()
                        .position(latLng)
                        .title(name);
                mapboxMap.addMarker(marker);

            }
        } catch (Exception e) {
            Log.e("MainActivity", "Exception Loading GeoJSON: " + e.toString());
        }

    }
}