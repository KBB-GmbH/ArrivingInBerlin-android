package com.hkw.arrivinginberlin;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {
    public final static String ENGLISH_KEY = "english_data";
    public final static String GERMAN_KEY = "german_data";
    public final static String FARSI_KEY = "farsi_data";
    public final static String ARABIC_KEY = "arabic_data";
    public final static String KURDISH_KEY = "kurdish_data";
    public final static String FRENCH_KEY = "french_data";
    private static final String KEY = "Startup_Finished";
    private static final String KEY_LOAD = "Last_Load";
    Boolean didShowStartup;
    String lastLoaded;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        didShowStartup = prefs.getBoolean(KEY, false);
        lastLoaded = prefs.getString(KEY_LOAD, "none");
        Log.i(KEY, getTodayString());
        if (!lastLoaded.equals(getTodayString())){
            Toast.makeText(getApplicationContext(), R.string.loading, Toast.LENGTH_LONG).show();
            new FetchAllLocationsTask().execute();
        } else {
            proceedAfterDownload();
        }
    }



    public class FetchAllLocationsTask extends AsyncTask<Void, Void, Map<String, ArrayList<JSONObject>>> {
        @Override
        protected Map<String, ArrayList<JSONObject>> doInBackground(Void... params) {
            HashMap<String, ArrayList<JSONObject>> hm = new HashMap<>();
            ArrayList<JSONObject> jsonEn = new UmapDataRequest().getLocationsEnglish();
            ArrayList<JSONObject> jsonFr = new UmapDataRequest().getLocations(159184, 159198, 325432, "en");
            ArrayList<JSONObject> jsonDe = new UmapDataRequest().getLocations(226926, 226940, 325444, "de");
            ArrayList<JSONObject> jsonFa = new UmapDataRequest().getLocations(128475, 128489, 325455, "en");
            ArrayList<JSONObject> jsonAr = new UmapDataRequest().getLocations(128884, 128898, 325451, "en");
            ArrayList<JSONObject> jsonUr = new UmapDataRequest().getLocations(193257, 193270, 325457, "de");
            hm.put("en", jsonEn);
            hm.put("fr", jsonFr);
            hm.put("de", jsonDe);
            hm.put("fa", jsonFa);
            hm.put("ar", jsonAr);
            hm.put("ur", jsonUr);
            return hm;
        }

        @Override
        protected void onCancelled() {
            showOfflineMessage();
        }

        @Override
        protected void onPostExecute(Map<String, ArrayList<JSONObject>> locations) {
            //check if the map exists already
            for (Map.Entry<String, ArrayList<JSONObject>> entry : locations.entrySet()) {
                String language = entry.getKey();
                ArrayList<JSONObject> loc = entry.getValue();

                if ((loc != null) && (loc.size() > 0)) {
                    Log.i("SPLASH", "found locations" + loc.size());
                    storeLocations(loc, language);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove(KEY_LOAD);
                    editor.putString(KEY_LOAD, getTodayString());
                    editor.apply();
                    proceedAfterDownload();
                } else {
                    if(hasStoredLocations()){
                        proceedAfterDownload();
                    } else {
                        showOfflineMessage();
                    }
                }
            }

        }

    }

    private void proceedAfterDownload(){
        if(!didShowStartup) {
            Intent intent = new Intent(getApplicationContext(), StartupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }
    }

    private void showOfflineMessage() {
        String message = getString(R.string.offline_message);
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }


    private void storeLocations(ArrayList<JSONObject> locations, String language) {
        SaveArray save = new SaveArray(getApplicationContext());
        save.saveArray(getKeyForData(language), locations);
        Log.i("SPLASH", "Saved Locations");
    }

    private boolean hasStoredLocations(){
        String key = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(LocaleUtils.LANGUAGE, "en");
        SaveArray save = new SaveArray(getApplicationContext());
        ArrayList<JSONObject> locs = save.getArray(getKeyForData(key));

        if (locs != null & locs.size()>0){
            return true;
        } else {
            return false;
        }
    }

    private String getKeyForData(String language){
        switch (language){
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
            case "ur":
                return KURDISH_KEY;
            default:
                return ENGLISH_KEY;
        }
    }

    private String getTodayString(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
        return format1.format(cal.getTime());
    }
}
