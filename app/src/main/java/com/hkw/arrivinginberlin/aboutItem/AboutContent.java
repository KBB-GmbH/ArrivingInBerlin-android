package com.hkw.arrivinginberlin.aboutItem;

/**
 * Created by anouk on 03/05/17.
 */

import android.content.res.Resources;

import com.hkw.arrivinginberlin.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.security.AccessController.getContext;


public class AboutContent {

    public static final List<com.hkw.arrivinginberlin.aboutItem.AboutContent.AboutItem> ITEMS = new ArrayList<AboutItem>();
    public static final Map<String, AboutItem> ITEM_MAP = new HashMap<String, AboutItem>();


    static {
        addItem(new AboutItem("0", "legal"));
        addItem(new AboutItem("1", "privacy"));
        addItem(new AboutItem("2", "terms"));

    }

    private static void addItem(AboutItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }


    public static class AboutItem {
        public final String id;
        public final String content;

        public AboutItem(String id, String content) {
            this.id = id;
            this.content = content;

        }

        @Override
        public String toString() {
            return content;
        }
    }
}
