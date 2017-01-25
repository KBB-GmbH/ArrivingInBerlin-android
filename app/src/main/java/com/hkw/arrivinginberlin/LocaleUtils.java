package com.hkw.arrivinginberlin;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.StringDef;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

public class LocaleUtils {
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ENGLISH, FRENCH, GERMAN, FARSI, ARABIC, KURDISH})
    public @interface LocaleDef {
        String[] SUPPORTED_LOCALES = {ENGLISH, FRENCH, GERMAN, FARSI, ARABIC, KURDISH};
    }

    public static final String ENGLISH = "en";
    public static final String FRENCH = "fr";
    public static final String GERMAN = "de";
    public static final String FARSI = "fa";
    public static final String ARABIC = "ar";
    public static final String KURDISH = "ur";
    public  static final String LANGUAGE = "LANGUAGE";

    public static void initialize(Context context) {
    }

    public static void initialize(Context context, @LocaleDef String defaultLanguage) {
        setLocale(context, defaultLanguage);
    }

    public static void setLanguageFromPreference(Context context){
        String lang = PreferenceManager.getDefaultSharedPreferences(context).getString(LocaleUtils.LANGUAGE, "en");
        LocaleUtils.setLocaleNonStrict(context,lang);
        Log.i("UTILS", "default language on create: " + Locale.getDefault());
    }

    public static boolean setLocale(Context context, @LocaleDef String language) {
        return updateResources(context, language);
    }

    public static boolean setLocaleNonStrict(Context context, String language) {
        return updateResources(context, language);
    }

    private static boolean updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return true;
    }

    public static String getLocale() {
        return Locale.getDefault().toString();
    }
}