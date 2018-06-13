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
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

class ManageHostsCursorAdapter extends HostCursorAdapter {
    private ArrayList<Boolean> toggles;

    public ManageHostsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public void setToggleList(ArrayList<Boolean> list) {
        toggles = list;
        notifyDataSetChanged();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        HostCursor hostCursor = (HostCursor) cursor;
        CompatCheckedTextView textView = (CompatCheckedTextView)
          View.inflate(context, R.layout.list_item_host, null);
        CheckMarkHolder holder = new CheckMarkHolder();
        holder.checkMark = textView.getCheckMarkDrawable();
        textView.setTag(holder);
        return setupItemView(textView, hostCursor);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        CompatCheckedTextView textView = (CompatCheckedTextView) view;
        HostCursor hostCursor = (HostCursor) cursor;
        setupItemView(textView, hostCursor);
    }

    @Override
    public boolean isEnabled(int position) {
        HostCursor hostCursor = (HostCursor) getItem(position);
        return !hostCursor.isReadonly();
    }

    private CompatCheckedTextView setupItemView(CompatCheckedTextView view,
                                                HostCursor cursor) {
        CheckMarkHolder holder = (CheckMarkHolder) view.getTag();
        view.setText(Html.fromHtml(buildItemText(cursor)));
        view.setCheckMarkDrawable(holder.checkMark);
        view.setChecked(false);
        view.setChecked(toggles.get(cursor.getPosition()));
        if (cursor.isReadonly()) {
            view.setCheckMarkDrawable(null);
        }
        return view;
    }

    private class CheckMarkHolder {
        Drawable checkMark;
    }
}
