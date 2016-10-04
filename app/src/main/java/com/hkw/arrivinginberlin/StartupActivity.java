package com.hkw.arrivinginberlin;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.view.View;
import android.widget.Button;

import java.util.prefs.Preferences;

public class StartupActivity extends AppCompatActivity implements LanguageFragment.OnFragmentInteractionListener, StartupTextFragment.OnFragmentInteractionListener {
    private LanguageFragment lang;
    private static final String KEY = "Startup_Finished";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        if (savedInstanceState == null) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Boolean didShowStartup = prefs.getBoolean(KEY, false);

            if (didShowStartup) {
                onStartupFlowFinished();
            } else {
                FragmentManager fragmentManager = getFragmentManager();
                lang = new LanguageFragment();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.content_container, lang, "Language");
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
            FragmentManager fragmentManager = getFragmentManager();
            StartupTextFragment textFragment = new StartupTextFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_container, textFragment, "TEXT");
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onStartupFlowFinished() {
        //Move to main activity
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY);
        editor.putBoolean(KEY, true);
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }
}
