package com.hkw.arrivinginberlin;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by anouk on 02/09/16.
 */
public class SaveArray {

    Context context;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    public SaveArray(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = prefs.edit();
    }


    public void saveArray(String key, ArrayList<JSONObject> array) {
        JSONArray jArray = new JSONArray(array);
        editor.remove(key);
        editor.putString(key, jArray.toString());
        editor.commit();
    }


    public ArrayList<JSONObject> getArray(String key) {
        ArrayList<JSONObject> array = new ArrayList<JSONObject>();
        String jArrayString = prefs.getString(key, "NOPREFSAVED");
        if (jArrayString.matches("NOPREFSAVED")) return getDefaultArray();
        else {
            try {
                JSONArray jArray = new JSONArray(jArrayString);
                for (int i = 0; i < jArray.length(); i++) {
                    String result = jArray.getString(i);
                    JSONObject jsonBody = new JSONObject(result);
                    array.add(jsonBody);
                }
                return array;
            } catch (JSONException e) {
                return getDefaultArray();
            }
        }
    }

    private ArrayList<JSONObject> getDefaultArray() {
        ArrayList<JSONObject> array = new ArrayList<JSONObject>();
        return array;
    }
}
