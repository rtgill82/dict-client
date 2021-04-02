/*
 * Copyright (C) 2018 Robert Gill <rtgill82@gmail.com>
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
    private Drawable mDrawable;

    public CompatCheckedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setCheckMarkDrawable(Drawable d) {
        super.setCheckMarkDrawable(d);
        mDrawable = d;
    }

    @Override
    public Drawable getCheckMarkDrawable() {
        return mDrawable;
    }
}
