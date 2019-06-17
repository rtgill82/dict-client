/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatTextView;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static android.util.TypedValue.COMPLEX_UNIT_PX;

public class ResultsView extends AppCompatTextView {
    private static final String SUPER_STATE = "SUPER_STATE";
    private static final String TEXT_SIZE = "TEXT_SIZE";
    private static final String DISPLAY_OPTION = "DISPLAY_OPTION";

    private boolean mScrolling = false;
    private DisplayOption mDisplayOption = DisplayOption.SCROLL;

    private float mDefaultTextSize = getTextSize();
    private SharedPreferences mPrefs;
    private GestureDetector mGestureDetector;
    private OnSharedPreferenceChangeListener mPrefListener;
    private Results mResults;

    public ResultsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mResults = new Results();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        setMovementMethod(LinkMovementMethod.getInstance());
        initGestureDetector();
        initPreferenceChangeListener(context);
    }

    public void setResults(Results results) {
        setText(results.getText());
        if (!results.defaultStrategy()) {
            mDisplayOption = DisplayOption.LINE_WRAP;
            refresh();
        }
        mResults = results;
        refresh();
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

    @Override
    protected void onLayout(boolean changed, int left, int top,
                            int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed)
          refresh();
    }

    @Override
    protected void onTextChanged(CharSequence text, int start,
                                 int lengthBefore, int lengthAfter) {
        if (mPrefs == null)
          return;

        Context c = getContext();
        String key = c.getString(R.string.pref_key_display_option);
        String value = c.getString(R.string.pref_display_option_fit_width);
        mDisplayOption = DisplayOption.valueOf(mPrefs.getString(key, value));
        refresh();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mScrolling == true) {
                mScrolling = false;
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private void refresh() {
        if (!mResults.defaultStrategy())
          return;

        restoreTextSize();
        switch (mDisplayOption) {
          case FIT_WIDTH:
            scaleToFitWidth();
            /* fall through */
          case LINE_WRAP:
            setHorizontallyScrolling(false);
            break;
          case SCROLL:
            setHorizontallyScrolling(true);
            break;
        }
    }

    private void restoreTextSize() {
        setTextSize(COMPLEX_UNIT_PX, mDefaultTextSize);
    }

    private void scaleToFitWidth() {
        int measuredWidth = getMeasuredWidth();
        if (measuredWidth <= 0)
          return;

        int availableWidth =
          measuredWidth - getTotalPaddingLeft() - getTotalPaddingRight();

        if (availableWidth <= 0)
          return;

        float textSize = findOptimalTextSize(availableWidth);
        setTextSize(COMPLEX_UNIT_PX, textSize);
    }

    private float findOptimalTextSize(int availableWidth) {
        String text = getText().toString();
        BufferedReader reader = new BufferedReader(new StringReader(text));
        String longestLine = "";
        int longestLineLength = 0;

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (longestLineLength < line.length()) {
                    longestLine = line;
                    longestLineLength = longestLine.length();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Rect rect = new Rect();
        TextPaint paint = new TextPaint(getPaint());
        int size = Math.round(paint.getTextSize());
        rect.right = Integer.MAX_VALUE;
        while (rect.right >= availableWidth) {
            paint.getTextBounds(longestLine, 0, longestLineLength, rect);
            if (rect.right < availableWidth) break;
            size -= 1;
            paint.setTextSize((float) size);
        }
        return (float) size;
    }

    private void saveState(Bundle outState) {
        outState.putFloat(TEXT_SIZE, getTextSize());
        outState.putInt(DISPLAY_OPTION, mDisplayOption.ordinal());
    }

    private void restoreState(Bundle savedInstanceState) {
        float textSize = savedInstanceState.getFloat(TEXT_SIZE);
        setTextSize(COMPLEX_UNIT_PX, textSize);
        int ordinal = savedInstanceState.getInt(DISPLAY_OPTION);
        mDisplayOption = DisplayOption.values()[ordinal];
    }

    private void initGestureDetector() {
        mGestureDetector = new GestureDetector(this.getContext(),
          new SimpleOnGestureListener() {
              @Override
              public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                      float distanceX, float distanceY) {
                  mScrolling = true;
                  Spannable text = (Spannable) ResultsView.this.getText();
                  Selection.removeSelection(text);
                  return true;
              }
          });
    }

    private void initPreferenceChangeListener(final Context c) {
        mPrefListener = new OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(
                SharedPreferences preferences, String key
            ) {
                String prefKey;
                String value;

                prefKey = c.getString(R.string.pref_key_display_option);
                value = c.getString(R.string.pref_display_option_fit_width);
                if (key.equals(prefKey)) {
                    mDisplayOption = DisplayOption.valueOf(
                        preferences.getString(prefKey, value)
                    );
                    refresh();
                }
            }};

        mPrefs.registerOnSharedPreferenceChangeListener(mPrefListener);
    }
}
