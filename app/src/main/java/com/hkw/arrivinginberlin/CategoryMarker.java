package com.hkw.arrivinginberlin;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;


public class CategoryMarker extends Object {
    public MarkerViewOptions markerViewOptions;
    public int categoryID;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean active;

    public CategoryMarker(int categoryID, Boolean active, MarkerViewOptions markerViewOptions) {
        validateCategoryID(categoryID);
        this.active = active;
        this.categoryID = categoryID;
        this.markerViewOptions = markerViewOptions;
    }

    public void validateCategoryID(int categoryID) {
        if (categoryID > 14) {
            throw new RuntimeException("This category does not exist.");
        }
    }


    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }
}
