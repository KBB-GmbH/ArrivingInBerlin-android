package com.hkw.arrivinginberlin;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;




/**
 * Created by anouk on 05/06/17.
 */

public class TextviewPlus extends android.support.v7.widget.AppCompatTextView {

    private static final String TAG = "Textview";

    public TextviewPlus(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TextviewPlus(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextviewPlus(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Medium.ttf");
            setTypeface(tf);
        }
    }
}
