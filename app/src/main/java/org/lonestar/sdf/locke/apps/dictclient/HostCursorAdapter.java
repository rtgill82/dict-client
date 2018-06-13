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
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.TextView;

class HostCursorAdapter extends CursorAdapter {
    public HostCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        CheckedTextView view = (CheckedTextView)
          View.inflate(context, R.layout.list_item_host, null);
        HostCursor hostCursor = (HostCursor) cursor;
        view.setText(Html.fromHtml(buildItemText(hostCursor)));
        view.setCheckMarkDrawable(null);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        HostCursor hostCursor = (HostCursor) cursor;
        String itemText = buildItemText(hostCursor);
        TextView textView = (TextView) view;
        textView.setText(Html.fromHtml(itemText));
    }

    protected String buildItemText(HostCursor cursor) {
        String host = cursor.getHostName();
        String description =
          cursor.getString(cursor.getColumnIndex("description"));

        String itemText = "<b>" + host + "</b>";
        if (description.length() > 0) {
            itemText += "<br><i>" + description + "</i>";
        }
        return itemText;
    }
}
