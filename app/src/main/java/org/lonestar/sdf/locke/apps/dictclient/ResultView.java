/*
 * Copyright (C) 2018 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;

class ResultView extends NestedScrollView {
    private static final String SUPER_STATE = "SUPER_STATE";
    private static final String WORD_WRAP = "WORD_WRAP";

    private ResultTextView mResultTextView;
    private boolean mWordWrap;

    public ResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        bundle.putBoolean(WORD_WRAP, mWordWrap);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        Bundle bundle;
        if (state instanceof Bundle) {
            bundle = (Bundle) state;
            setWordWrap(bundle.getBoolean(WORD_WRAP));
            state = bundle.getParcelable(SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        mResultTextView.restoreTextSize();
        if (!mWordWrap)
          mResultTextView.scaleToFitWidth();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mResultTextView.restoreTextSize();
        if (!mWordWrap)
          mResultTextView.scaleToFitWidth();
    }

    public CharSequence getText() {
        return mResultTextView.getText();
    }

    public void setText(CharSequence text) {
        mResultTextView.setText(text);
        mResultTextView.restoreTextSize();
        if (!mWordWrap)
          mResultTextView.scaleToFitWidth();
        scrollTo(0, 0);
    }

    public void setWordWrap(boolean value) {
        mWordWrap = value;
    }
}
