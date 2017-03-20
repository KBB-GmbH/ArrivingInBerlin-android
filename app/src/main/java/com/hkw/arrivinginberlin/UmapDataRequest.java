package com.hkw.arrivinginberlin;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.primitives.Shorts.toArray;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.concat;

/**
 * Created by ubuntu on 15.08.16.
 */
public class UmapDataRequest {

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                out.close();
                throw new IOException(connection.getResponseMessage() + ": with" + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return  new String(getUrlBytes(urlSpec));
    }

//    public List<JSONObject> fetchLocations(String language) {
//        Log.i("LOAD", language);
//
//        if (language == null){
//            return getLocationsEnglish();
//        }
//        switch (language){
//            case "fr":
//                return getLocations(french.get(0), french.get(1));
//            case "de":
//                return getLocations(french.get(0), french.get(1));
//            case "ar":
//                return getLocations(french.get(0), french.get(1));
//            case "fa":
//                return getLocations(french.get(0), french.get(1));
//            case "ku":
//                return getLocations(french.get(0), french.get(1));
//            case "en":
//                return getLocationsEnglish();
//            default:
//                return getLocationsEnglish();
//
//        }
//
//    }

    public List<JSONObject>getLocations(int start, int end, int extra){
        List<JSONObject> locations = new ArrayList<>();
        List<Integer> myInts = getIntegerListForInts(start, end, extra);
        for (int i = 0; i < myInts.size(); i++) {
            try {
                String url = Uri.parse("http://umap.openstreetmap.fr/en/datalayer/" + myInts.get(i) + "/")
                        .buildUpon()
                        .appendQueryParameter("format", "json")
                        .build().toString();

                String result = getUrlString(url);
                JSONObject jsonBody = new JSONObject(result);
                locations.add(jsonBody);
                Log.i("JSON Loader", "Downloading json: " + myInts.get(i));
            } catch (JSONException je) {
                Log.e("JSON Loader", "JSON failed to get contents: " + je);
            } catch (IOException ioe) {
                Log.e("JSON Loader", "Failed to get contents: " + ioe);
            }
        }
        return locations;
    }
    public List<Integer> getIntegerListForInts(int start, int end, int extra) {
        List<Integer> ints = new ArrayList<>();
        ints.add(extra);
        for (int i = start; i < end; i++){
            ints.add(i);
        }
        return  ints;
    }

    public List<JSONObject>getLocationsEnglish() {
        List<JSONObject> locations = new ArrayList<>();
        List<Integer> ints = new ArrayList<>();
        ints.add(119513);
        ints.add(103122);
        ints.add(103125);
        ints.add(103130);
        ints.add(119468);
        ints.add(115921);
        ints.add(93689);
        ints.add(93512);
        ints.add(93515);
        ints.add(119511);
        ints.add(119437);
        ints.add(115892);
        ints.add(259820);
        ints.add(320118);

        for (int i:ints)
            try {
                String url = Uri.parse("http://umap.openstreetmap.fr/en/datalayer/"+i+"/")
                        .buildUpon()
                        .appendQueryParameter("format", "json")
                        .build().toString();

                String result = getUrlString(url);
                JSONObject jsonBody = new JSONObject(result);
                locations.add(jsonBody);
                Log.i("JSON Loader", "Downloading json: " + i);
            } catch (JSONException je) {
                Log.e("JSON Loader", "JSON failed to get contents: " + je);
            } catch (IOException ioe) {
                Log.e("JSON Loader", "Failed to get contents: " + ioe);
            }
        return locations;
    }

}
