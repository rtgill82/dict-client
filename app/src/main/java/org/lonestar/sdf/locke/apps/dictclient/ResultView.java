/*
 * Copyright (C) 2018 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;

class ResultView extends NestedScrollView {
    private static final String SUPER_STATE = "SUPER_STATE";
    private static final String DISPLAY_OPTION = "DISPLAY_OPTION";

    private ResultTextView mResultTextView;
    private DisplayOption mDisplayOption;

    public ResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setInitialDisplayOption();
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mResultTextView = findViewById(R.id.result_text_view);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SUPER_STATE, super.onSaveInstanceState());
        bundle.putString(DISPLAY_OPTION, mDisplayOption.toString());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        Bundle bundle;
        if (state instanceof Bundle) {
            bundle = (Bundle) state;
            setDisplayOption(
                DisplayOption.valueOf(bundle.getString(DISPLAY_OPTION))
            );
            state = bundle.getParcelable(SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        updateDisplay();
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateDisplay();
    }

    public CharSequence getText() {
        return mResultTextView.getText();
    }

    public void setText(CharSequence text) {
        mResultTextView.setText(text);
        updateDisplay();
        scrollTo(0, 0);
    }

    public void setDisplayOption(DisplayOption value) {
        mDisplayOption = value;
    }

    private void setInitialDisplayOption() {
        Context context = this.getContext();
        SharedPreferences prefs = PreferenceManager
          .getDefaultSharedPreferences(context);
        mDisplayOption = DisplayOption.valueOf(
            prefs.getString(
                context.getString(R.string.pref_key_display_option),
                context.getString(R.string.pref_display_option_fit_width)
            ));
    }

    private void updateDisplay() {
        mResultTextView.restoreTextSize();
        if (mDisplayOption == DisplayOption.FIT_WIDTH)
          mResultTextView.scaleToFitWidth();
    }
}
