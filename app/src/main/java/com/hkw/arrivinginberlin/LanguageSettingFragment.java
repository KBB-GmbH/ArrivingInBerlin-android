package com.hkw.arrivinginberlin;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
        final Button german = (Button) layout.findViewById(R.id.german);
        german.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                languageSelected("de", german);
            }
        });
        final Button english = (Button) layout.findViewById(R.id.english);
        english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                languageSelected("en", english);
            }
        });
        final Button farsi = (Button) layout.findViewById(R.id.farsi);
        farsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                languageSelected("en", farsi);
            }
        });
        final Button arabic = (Button) layout.findViewById(R.id.arabic);
        arabic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                languageSelected("en", arabic);
            }
        });

        final Button french = (Button) layout.findViewById(R.id.french);
        french.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                languageSelected("fr", french);
            }
        });

        return layout;
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    public void languageSelected(String lang, Button button){
        setLocale(lang);
        button.setTextColor(getActivity().getResources().getColor(R.color.colorSelected));
        if (mListener != null){
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
