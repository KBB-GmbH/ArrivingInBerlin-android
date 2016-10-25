package com.hkw.arrivinginberlin;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LanguageSettingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LanguageSettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LanguageSettingFragment extends Fragment {
    private LanguageSettingFragment.OnFragmentInteractionListener mListener;
    private Button english;
    private Button french;
    private Button german;
    private Button farsi;
    private Button arabic;

    public LanguageSettingFragment() {
        // Required empty public constructor
    }

    public static LanguageSettingFragment newInstance() {
        LanguageSettingFragment fragment = new LanguageSettingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout =  inflater.inflate(R.layout.fragment_language_setting, container, false);
        german = (Button) layout.findViewById(R.id.german);
        english = (Button) layout.findViewById(R.id.english);
        farsi = (Button) layout.findViewById(R.id.farsi);
        arabic = (Button) layout.findViewById(R.id.arabic);
        french = (Button) layout.findViewById(R.id.french);
        german.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                languageSelected(LocaleUtils.GERMAN, german);
            }
        });

        english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                languageSelected(LocaleUtils.ENGLISH, english);
            }
        });

        farsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                languageSelected(LocaleUtils.FARSI, farsi);
            }
        });
        arabic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                languageSelected(LocaleUtils.ARABIC, arabic);
            }
        });

        french.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                languageSelected(LocaleUtils.FRENCH, french);
            }
        });

        setLanguageSelected();

        return layout;
    }

    private void setLanguageSelected(){
        String loc = Locale.getDefault().getLanguage();
        Log.i("LANGUAGE", loc);
        english.setPressed(false);
        french.setPressed(false);
        german.setPressed(false);
        arabic.setPressed(false);
        farsi.setPressed(false);

        switch (loc) {
            case LocaleUtils.ENGLISH:
                english.setPressed(true);
                break;
            case LocaleUtils.FRENCH:
                french.setPressed(true);
                break;
            case LocaleUtils.GERMAN:
                german.setPressed(true);
                break;
            case LocaleUtils.FARSI:
                farsi.setPressed(true);
                break;
            case LocaleUtils.ARABIC:
                arabic.setPressed(true);
                break;
        }
    }

    public void languageSelected(String language, Button button){

        LocaleUtils.setLocale(getActivity().getApplicationContext(), language);
        button.setSelected(true);
        //store as user preference:
        PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putString(LocaleUtils.LANGUAGE, language).commit();

        //Restart activity:
        getActivity().setResult(Activity.RESULT_OK, null);
        getActivity().finish();
        if (mListener != null){
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i("SET LISTENER", String.valueOf(context));
        if (context instanceof LanguageSettingFragment.OnFragmentInteractionListener) {
            mListener = (LanguageSettingFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
