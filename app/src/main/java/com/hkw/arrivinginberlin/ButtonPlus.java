package com.hkw.arrivinginberlin;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by anouk on 05/06/17.
 */

public class ButtonPlus extends Button {

    private static final String TAG = "Button";

    public ButtonPlus(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ButtonPlus(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonPlus(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Medium.ttf");
            setTypeface(tf);
            setTextSize(18);
        }
    }
}
