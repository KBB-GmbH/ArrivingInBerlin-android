package com.hkw.arrivinginberlin;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Locale;
import java.util.Objects;
import java.util.prefs.Preferences;

public class StartupActivity extends AppCompatActivity implements LanguageFragment.OnFragmentInteractionListener, StartupTextFragment.OnFragmentInteractionListener, TermsStartupFragment.OnFragmentInteractionListener {
    private LanguageFragment lang;
    private  TermsStartupFragment termsFragment;
    private static final String KEY = "Startup_Finished";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        View decorView = getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        if (savedInstanceState == null) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean didShowStartup = prefs.getBoolean(KEY, false);

            if (didShowStartup) {
                onTermsAgreed();
            } else {
                FragmentManager fragmentManager = getSupportFragmentManager();
                lang = new LanguageFragment();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.content_container, lang, "LANGUAGE");
                fragmentTransaction.commit();
            }
        }

    }
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    @Override
    public void onLanguageSelection(boolean selected) {
        //Move to next fragment
        if (selected) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            StartupTextFragment textFragment = new StartupTextFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_container, textFragment, "TEXT");
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onTermsAgreed() {
        //Move to next main
        LocaleUtils.setLanguageFromPreference(getApplicationContext());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY);
        editor.putBoolean(KEY, true);
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStartupFlowFinished() {
        //Move to terms & conditions
        FragmentManager fragmentManager = getSupportFragmentManager();
        termsFragment = new TermsStartupFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_container, termsFragment, "TERMS");
        fragmentTransaction.commit();

    }
}
