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

class ResultView extends NestedScrollView {
    private ResultTextView resultTextView;
    private boolean wordWrap;

    public ResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        resultTextView = findViewById(R.id.result_text_view);
    }

    public CharSequence getText() {
        return resultTextView.getText();
    }

    public void setText(CharSequence text) {
        resultTextView.setText(text);
    }

    public void setWordWrap(boolean value) {
        wordWrap = value;
    }
}
