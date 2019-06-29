/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

class ManageHostsCursorAdapter extends HostCursorAdapter {
    private ArrayList<Boolean> mToggles;

    public ManageHostsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public void setToggleList(ArrayList<Boolean> list) {
        mToggles = list;
        notifyDataSetChanged();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        return setupItemView(view, cursor);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        setupItemView(view, cursor);
    }

    @Override
    public boolean isEnabled(int position) {
        HostCursor hostCursor = (HostCursor) getItem(position);
        return !hostCursor.isReadonly();
    }

    private View setupItemView(View view, Cursor cursor) {
        Drawable checkMark = null;
        HostCursor hostCursor = (HostCursor) cursor;
        CheckMarkHolder holder = (CheckMarkHolder) view.getTag();
        boolean checked = mToggles.get(hostCursor.getPosition());

        if (holder != null && !hostCursor.isReadonly())
          checkMark = holder.checkMark;

        bindItemText(view, hostCursor, checkMark, checked);
        return view;
    }
}
