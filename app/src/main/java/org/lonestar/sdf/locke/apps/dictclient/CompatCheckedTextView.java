/*
 * Copyright (C) 2018 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.util.AttributeSet;

class CompatCheckedTextView extends AppCompatCheckedTextView {
    private Drawable checkMarkDrawable;

    public CompatCheckedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setCheckMarkDrawable(Drawable d) {
        super.setCheckMarkDrawable(d);
        checkMarkDrawable = d;
    }

    @Override
    public Drawable getCheckMarkDrawable() {
        return checkMarkDrawable;
    }
}
