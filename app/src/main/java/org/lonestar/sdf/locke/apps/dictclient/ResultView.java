/*
 * Copyright (C) 2018 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

class ResultView extends NestedScrollView {
    private HorizontalScrollView horizontalScrollView;
    private ResultTextView resultTextView;
    private boolean wordWrap;

    public ResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        horizontalScrollView = createHorizontalScrollView();
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        resultTextView = findViewById(R.id.result_text_view);
        removeAllViews();
        horizontalScrollView.addView(resultTextView);
        addView(horizontalScrollView);
    }

    public CharSequence getText() {
        return resultTextView.getText();
    }

    public void setText(CharSequence text) {
        resultTextView.setText(text);
    }

    public void setWordWrap(boolean value) {
        if (wordWrap != value) {
            wordWrap = value;
            removeAllViews();
            horizontalScrollView.removeAllViews();
            if (wordWrap) {
                addView(resultTextView);
            } else {
                horizontalScrollView.addView(resultTextView);
                addView(horizontalScrollView);
            }
        }
    }

    private HorizontalScrollView createHorizontalScrollView() {
        HorizontalScrollView horizontalScrollView =
          new HorizontalScrollView(getContext());
        LayoutParams params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        horizontalScrollView.setLayoutParams(params);
        horizontalScrollView.setFillViewport(true);
        return horizontalScrollView;
    }
}
