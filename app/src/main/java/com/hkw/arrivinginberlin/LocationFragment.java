package com.hkw.arrivinginberlin;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class LocationFragment extends SuperFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private static final String TAG = "LocationFragment";
    private ArrayList<LocationItem> locationItems = new ArrayList<LocationItem>();
    private ArrayList<JSONObject> locationData = new ArrayList<JSONObject>();
    private static final String DATA = "data";
    private RecyclerView.Adapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LocationFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static LocationFragment newInstance(int columnCount, ArrayList<JSONObject> data) {
        LocationFragment fragment = new LocationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);

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

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_list, container, false);
        updateLocationPoints(locationData);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            adapter = new MyLocationRecyclerViewAdapter(context, locationItems, mListener);
            recyclerView.setAdapter(adapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(LocationItem item);
    }

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
                addLocationForCategory(categoryID, location);

            } catch (Exception e) {
                Log.e(TAG, "Exception Loading GeoJSON: " + e.toString());
            }
        }
    }

    public void addLocationForCategory(int categoryID, JSONObject json) {
        try {
            JSONArray features = json.getJSONArray("features");

            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject properties = feature.getJSONObject("properties");
                String name = properties.getString("name");
                String beschreibung = properties.getString("beschreibung").replace("*", "");
                String adresse = properties.getString("adresse").replace("*", "");
                if (adresse.length() != 0) {
                    adresse = adresse.substring(0, 1).toUpperCase() + adresse.substring​(1);
                }
                String telefon = properties.getString("telefon").replace("*", "");
                String medium = properties.getString("medium").replace("*", "").replace("[[", "").replace("]]", "");
                String text = beschreibung + "\n" + "\n" + adresse + "\n" + telefon + "\n" + medium;
                LocationItem loc = new LocationItem(categoryID, name, text, getIconForCategory(categoryID));
                locationItems.add(loc);
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "Exception Loading GeoJSON: " + e.toString());
        }
    }

    public class LocationItem extends Object {

        public String title;
        public String iconPNG;
        public String text;
        public int categoryID;

        public LocationItem(int categoryID, String title, String text, String iconPNG) {
            validateCategoryID(categoryID);
            this.text = text;
            this.categoryID = categoryID;
            this.title = title;
            this.iconPNG = iconPNG;
        }

        public void validateCategoryID(int categoryID) {
            if (categoryID > 13) {
                throw new RuntimeException("This category does not exist.");
            }
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getIconPNG() {
            return iconPNG;
        }

        public void setIconPNG(String iconPNG) {
            this.iconPNG = iconPNG;
        }


        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getCategoryID() {
            return categoryID;
        }

        public void setCategoryID(int categoryID) {
            this.categoryID = categoryID;
        }
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

}
