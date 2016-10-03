package com.hkw.arrivinginberlin;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class StartupActivity extends AppCompatActivity implements LanguageFragment.OnFragmentInteractionListener {
    private LanguageFragment lang;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        if (savedInstanceState == null) {

            FragmentManager fragmentManager = getFragmentManager();
            lang = new LanguageFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.content_container, lang, "Language");
            fragmentTransaction.commit();
        }

    }
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

}
