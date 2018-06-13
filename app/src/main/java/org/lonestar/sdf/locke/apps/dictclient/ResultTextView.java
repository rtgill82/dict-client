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
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static android.util.TypedValue.COMPLEX_UNIT_PX;

public class ResultTextView extends AppCompatTextView {
    private static final String SUPER_STATE = "SUPER_STATE";
    private static final String TEXT_SIZE = "TEXT_SIZE";
    private final float origTextSize = getTextSize();
    private float textSize;

    public ResultTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Use LinkMovementMethod for scrolling to allow for both clickable
        // links and scrolling.
        setMovementMethod(LinkMovementMethod.getInstance());
        setHighlightColor(Color.BLUE);
    }

    public void restoreTextSize() {
        textSize = origTextSize;
        setTextSize(COMPLEX_UNIT_PX, origTextSize);
    }

    public void scaleToFitWidth() {
        if (getMeasuredWidth() <= 0)
          return;

        int availableWidth =
          getMeasuredWidth() - getTotalPaddingLeft() - getTotalPaddingRight();

        if (availableWidth <= 0)
          return;

        textSize = findOptimalTextSize(availableWidth);
        setTextSize(COMPLEX_UNIT_PX, textSize);
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

    private float findOptimalTextSize(int availableWidth) {
        String text = getText().toString();
        BufferedReader reader = new BufferedReader(new StringReader(text));
        String longestLine = "";
        int longestLineLength = 0;

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
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
        float textSize = getTextSize();
        outState.putFloat(TEXT_SIZE, textSize);
    }

    private void restoreState(Bundle savedInstanceState) {
        setTextSize(COMPLEX_UNIT_PX,
                    savedInstanceState.getFloat(TEXT_SIZE));
    }
}
