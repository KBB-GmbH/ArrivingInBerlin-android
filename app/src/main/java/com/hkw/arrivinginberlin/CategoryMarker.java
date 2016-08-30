package com.hkw.arrivinginberlin;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;


public class CategoryMarker extends Object {
    public Marker marker;
    public MarkerViewOptions markerViewOptions;
    public int categoryID;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean active;

    public CategoryMarker(Marker marker, int categoryID, Boolean active, MarkerViewOptions markerViewOptions) {
        validateCategoryID(categoryID);
        this.active = active;
        this.categoryID = categoryID;
        this.marker = marker;
        this.markerViewOptions = markerViewOptions;

    }

    public void validateCategoryID(int categoryID) {
        if (categoryID > 13) {
            throw new RuntimeException("This category does not exist.");
        }
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }
}
