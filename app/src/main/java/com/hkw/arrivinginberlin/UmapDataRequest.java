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

    public List<JSONObject> fetchLocations() {
        List<JSONObject> locations = new ArrayList<>();
        for (int i = 226926; i < 226940; i++)
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
