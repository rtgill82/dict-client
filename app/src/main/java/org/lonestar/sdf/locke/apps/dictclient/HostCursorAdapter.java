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
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

class HostCursorAdapter extends CursorAdapter {
    public HostCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(final Context context, Cursor cursor, ViewGroup parent) {
        final View view = View.inflate(context, R.layout.list_item_host, null);
        CompatCheckedTextView textView = view.findViewById(R.id.host);
        CheckMarkHolder holder = new CheckMarkHolder();
        Drawable checkMark = textView.getCheckMarkDrawable();

        if (checkMark != null)
          holder.checkMark = checkMark;

        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        bindItemText(view, cursor, null, false);
    }

    void bindItemText(View view, Cursor cursor, Drawable checkMark,
                      boolean checked) {
        HostCursor hostCursor = (HostCursor) cursor;
        CheckedTextView hostName = view.findViewById(R.id.host);
        hostName.setText(hostCursor.getHostName());
        hostName.setCheckMarkDrawable(checkMark);
        hostName.setChecked(checked);

        TextView descriptionView = view.findViewById(R.id.description);
        String description = hostCursor.getDescription();
        if (description.length() > 0) {
            descriptionView.setVisibility(VISIBLE);
            descriptionView.setText(description);
        } else {
            descriptionView.setVisibility(GONE);
        }
    }

    protected class CheckMarkHolder {
        Drawable checkMark;
    }
}
