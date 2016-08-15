package com.hkw.arrivinginberlin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ubuntu on 15.08.16.
 */
public class UmapDataRequest {

    public JSONObject getData() throws IOException, JSONException {
        URL url = new URL("http://json-schema.org/draft-04/schema#");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String jsontext = readStream(in);
            JSONObject json =  new JSONObject(jsontext);
            return json;
        } finally {
            urlConnection.disconnect();
        }


    }

    private String readStream(InputStream in) {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = in.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();

    }
    public static void main (string [] args) {
        JSONObject returnsdata = getData();


    }

}
