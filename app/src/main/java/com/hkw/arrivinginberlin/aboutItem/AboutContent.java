package com.hkw.arrivinginberlin.aboutItem;

/**
 * Created by anouk on 03/05/17.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class AboutContent {

    public static final List<com.hkw.arrivinginberlin.aboutItem.AboutContent.AboutItem> ITEMS = new ArrayList<AboutItem>();
    public static final Map<String, AboutItem> ITEM_MAP = new HashMap<String, AboutItem>();


    static {
        addItem(createAboutItem("Legal Notice", 0));
        addItem(createAboutItem("Privacy Policy", 1));
        addItem(createAboutItem("Terms of Use", 2));
    }

    private static void addItem(AboutItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static AboutItem createAboutItem(String title, int position) {
        return new AboutItem(String.valueOf(position), title , makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class AboutItem {
        public final String id;
        public final String content;
        public final String details;

        public AboutItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
