package com.hkw.arrivinginberlin;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anouk on 10/09/16.
 */
public class LocationsStore {
    private static LocationsStore locationsStore;
    private static  Context context;

    public static LocationsStore get(Context context) {
        context = context;
        if (locationsStore == null) {

        }

        return locationsStore;
    }

    private LocationsStore(Context context) {

    }

    public class FetchLocationsTask extends AsyncTask<Void, Void, List<JSONObject>> {
        @Override
        protected List<JSONObject> doInBackground(Void... params) {
            return new UmapDataRequest().fetchLocations();
        }

        @Override
        protected void onCancelled() {
            ArrayList<JSONObject> locations = getStoredLocations(context);
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
                storeLocations((ArrayList<JSONObject>) locations, context);
            } else {
                List<JSONObject> storedLocations = getStoredLocations(context);
                if ((storedLocations != null) && (storedLocations.size() > 0)) {
                    updateLocationPoints(locations);
                } else {
                    showOfflineMessage();
                }
            }
        }

        private void showOfflineMessage() {

        }

        private void updateLocationPoints(List<JSONObject> locations) {


        }

        private ArrayList<JSONObject> getStoredLocations(Context context) {
            SaveArray save = new SaveArray(context);
            ArrayList<JSONObject> locations = save.getArray("locations");
            return locations;
        }

        private void storeLocations(ArrayList<JSONObject> locations, Context context) {
            SaveArray save = new SaveArray(context);
            save.saveArray("locations", locations);
        }
    }
}
