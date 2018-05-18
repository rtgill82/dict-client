/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

public class ResultTextView extends TextView {
    private static final String SUPER_STATE = "SUPER_STATE";
    private static final String SCROLL_POS = "SCROLL_POS";

    public ResultTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Use LinkMovementMethod for scrolling to allow for both clickable
        // links and scrolling.
        setMovementMethod(LinkMovementMethod.getInstance());
        setHighlightColor(Color.BLUE);
        addTextChangedListener(createTextWatcher());
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SUPER_STATE, super.onSaveInstanceState());
        saveState(bundle);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = null;
        if (state instanceof Bundle) {
            bundle = (Bundle) state;
            state = bundle.getParcelable(SUPER_STATE);
        }
        super.onRestoreInstanceState(state);

        if (bundle != null)
          restoreState(bundle);
    }

    private TextWatcher createTextWatcher() {
        return new TextWatcher() {
            public void afterTextChanged(Editable s) {
                scrollTo(0, 0);
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) { }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) { }
        };
    }

    private void saveState(Bundle outState) {
        int scrollPos[] = { getScrollX(), getScrollY() };
        outState.putIntArray(SCROLL_POS, scrollPos);
    }

    private void restoreState(Bundle savedInstanceState) {
        int scrollPos[] = savedInstanceState.getIntArray(SCROLL_POS);
        scrollTo(scrollPos[0], scrollPos[1]);
    }
}
