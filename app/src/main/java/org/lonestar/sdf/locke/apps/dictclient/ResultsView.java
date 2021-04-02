/*
 * Copyright (C) 2017 Robert Gill <rtgill82@gmail.com>
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
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static android.util.TypedValue.COMPLEX_UNIT_PX;

public class ResultsView extends AppCompatTextView {
    private static final String SUPER_STATE = "SUPER_STATE";
    private static final String DISPLAY_OPTION = "DISPLAY_OPTION";
    private static final String SCALE_FACTOR = "SCALE_FACTOR";
    private static final String TEXT_SIZE = "TEXT_SIZE";

    private final float MIN_TEXT_SIZE = 2.0f;
    private final float MAX_TEXT_SIZE = 80.0f;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final OnSharedPreferenceChangeListener mPrefListener;

    private final MovementMethod mLinkMethod = LinkMovementMethod.getInstance();
    private final SharedPreferences mPrefs;
    private final GestureDetector mGestureDetector;
    private final ScaleGestureDetector mScaleGestureDetector;

    private float mScaleFactor = 1.0f;
    private DisplayOption mDisplayOption = DisplayOption.SCROLL;

    private Results mResults;
    private float mDefaultTextSize;

    public ResultsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mResults = new Results();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mDefaultTextSize = getDefaultTextSize();
        setMovementMethod(ScrollingMovementMethod.getInstance());
        mGestureDetector = createGestureDetector();
        mScaleGestureDetector = createScaleGestureDetector();
        mPrefListener = createPreferenceChangeListener(context);
        mPrefs.registerOnSharedPreferenceChangeListener(mPrefListener);
    }

    public void setResults(Results results) {
        setText(results.getText());
        if (!results.defaultStrategy()) {
            mDisplayOption = DisplayOption.LINE_WRAP;
            refresh();
        }
        mResults = results;
        refresh();
        scrollTo(0, 0);
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
        restoreTextSize();
        refresh();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private boolean sendLinkClick(MotionEvent event) {
        return mLinkMethod.onTouchEvent(this, (Spannable) getText(), event);
    }

    private void refresh() {
        if (!mResults.defaultStrategy())
          return;

        switch (mDisplayOption) {
          case FIT_WIDTH:
            restoreTextSize();
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
        mScaleFactor = 1.0f;
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

    private float getDefaultTextSize() {
        Context c = getContext();
        String key = c.getString(R.string.pref_key_font_size);
        String value = c.getString(R.string.pref_value_font_size);
        float size = Integer.parseInt(mPrefs.getString(key, value));
        setTextSize(size);
        return getTextSize();
    }

    private void saveState(Bundle outState) {
        outState.putInt(DISPLAY_OPTION, mDisplayOption.ordinal());
        outState.putFloat(SCALE_FACTOR, mScaleFactor);
        outState.putFloat(TEXT_SIZE, getTextSize());
    }

    private void restoreState(Bundle savedInstanceState) {
        int ordinal = savedInstanceState.getInt(DISPLAY_OPTION);
        mDisplayOption = DisplayOption.values()[ordinal];
        mScaleFactor = savedInstanceState.getFloat(SCALE_FACTOR);
        float textSize = savedInstanceState.getFloat(TEXT_SIZE);
        setTextSize(COMPLEX_UNIT_PX, textSize);
    }

    private GestureDetector createGestureDetector() {
        return new GestureDetector(this.getContext(),
          new SimpleOnGestureListener() {
              @Override
              public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                      float distanceX, float distanceY) {
                  Spannable text = (Spannable) ResultsView.this.getText();
                  Selection.removeSelection(text);
                  return true;
              }

              @Override
              public boolean onDown(MotionEvent event) {
                  return sendLinkClick(event);
              }

              @Override
              public boolean onSingleTapConfirmed(MotionEvent event) {
                  event.setAction(MotionEvent.ACTION_UP);
                  return sendLinkClick(event);
              }

              @Override
              public boolean onDoubleTap(MotionEvent event) {
                  if (mDisplayOption != DisplayOption.FIT_WIDTH)
                    restoreTextSize();

                  return true;
              }
          });
    }

    private ScaleGestureDetector createScaleGestureDetector() {
        return new ScaleGestureDetector(this.getContext(),
          new SimpleOnScaleGestureListener() {
              @Override
              public boolean onScale(ScaleGestureDetector detector) {
                  if (mDisplayOption == DisplayOption.FIT_WIDTH)
                    return true;

                  float scaleFactor = mScaleFactor * detector.getScaleFactor();
                  float textSize = mDefaultTextSize * scaleFactor;
                  if (exceedsTextSizeLimits(textSize))
                    return true;

                  mScaleFactor = scaleFactor;
                  setTextSize(COMPLEX_UNIT_PX, textSize);
                  return true;
              }

              private boolean exceedsTextSizeLimits(float value) {
                  return value < MIN_TEXT_SIZE || value > MAX_TEXT_SIZE;
              }
          });
    }

    private OnSharedPreferenceChangeListener
    createPreferenceChangeListener(final Context c) {
        OnSharedPreferenceChangeListener listener =
          new OnSharedPreferenceChangeListener() {
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
                    restoreTextSize();
                    refresh();
                }

                prefKey = c.getString(R.string.pref_key_font_size);
                value = c.getString(R.string.pref_value_font_size);
                if (key.equals(prefKey)) {
                    String size = preferences.getString(prefKey, value);
                    setTextSize(TypedValue.COMPLEX_UNIT_SP,
                                (float) Integer.parseInt(size));
                    mDefaultTextSize = getTextSize();
                }
            }};

        return listener;
    }
}
