package com.hkw.arrivinginberlin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anouk on 01/10/16.
 */

public class ExpandedMenuItem {
    String iconName = "";
    int iconImg = -1; // menu icon resource id
    int categorieId = 0;
    public List<String> subItems = new ArrayList<String>();

    public String getIconName() {
        return iconName;
    }
    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
    public int getIconImg() {
        return iconImg;
    }

    public void setIconImg(int img) {
        this.iconImg = img;
    }

    public void setCategorieId(int catId) {
        this.categorieId = catId;
    }

    public int getCategorieId() {
        return categorieId;
    }

    public List<String> getSubItems() {
        return subItems;
    }

    public void setSubItems(List<String> subs) {
        this.subItems = subs;
    }
}

