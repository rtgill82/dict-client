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
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.TextView;

import static android.util.TypedValue.COMPLEX_UNIT_PX;

public class ResultTextView extends TextView {
    private static final float MIN_TEXT_SIZE = 8.0f;
    private static final float MAX_TEXT_SIZE = 60.0f;

    private static final String SUPER_STATE = "SUPER_STATE";
    private static final String TEXT_SIZE = "TEXT_SIZE";
    private static final String SCROLL_POS = "SCROLL_POS";

    private ScaleGestureDetector scaleGesture;
    private float textSize;

    public ResultTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setHorizontallyScrolling(true);

        // Use LinkMovementMethod for scrolling to allow for both clickable
        // links and scrolling.
        setMovementMethod(LinkMovementMethod.getInstance());
        setHighlightColor(Color.BLUE);
        addTextChangedListener(createTextWatcher());
        scaleGesture = createScaleGestureDetector();
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        textSize = getTextSize();
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
    public boolean onTouchEvent(MotionEvent event) {
        scaleGesture.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private TextWatcher createTextWatcher() {
        return new TextWatcher() {
            public void afterTextChanged(Editable s) {
                setTextSize(COMPLEX_UNIT_PX, textSize);
                scrollTo(0, 0);
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) { }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) { }
        };
    }

    private ScaleGestureDetector createScaleGestureDetector() {
        return new ScaleGestureDetector(this.getContext(),
            new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public void onScaleEnd(ScaleGestureDetector detector) {
                    float newSize =
                      getTextSize() * detector.getScaleFactor();
                    if (newSize < MIN_TEXT_SIZE)
                      newSize = MIN_TEXT_SIZE;
                    else if (newSize > MAX_TEXT_SIZE)
                      newSize = MAX_TEXT_SIZE;
                    setTextSize(COMPLEX_UNIT_PX, newSize);
                }
            });
    }

    private void saveState(Bundle outState) {
        float textSize = getTextSize();
        int scrollPos[] = { getScrollX(), getScrollY() };

        outState.putFloat(TEXT_SIZE, textSize);
        outState.putIntArray(SCROLL_POS, scrollPos);
    }

    private void restoreState(Bundle savedInstanceState) {
        int scrollPos[] = savedInstanceState.getIntArray(SCROLL_POS);
        scrollTo(scrollPos[0], scrollPos[1]);
        setTextSize(COMPLEX_UNIT_PX,
                    savedInstanceState.getFloat(TEXT_SIZE));
    }
}
