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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;

import org.lonestar.sdf.locke.libs.dict.JDictClient;

class ManageHostCursorAdapter extends CursorAdapter {
    private Drawable checkMarkDrawable;

    public ManageHostCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        checkMarkDrawable =
          context.getResources()
                 .getDrawable(R.drawable.checkbox_background);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        HostCursor hostCursor = (HostCursor) cursor;
        LayoutInflater inflater = LayoutInflater.from(context);
        CheckedTextView textView = (CheckedTextView)
          inflater.inflate(R.layout.list_item_host, null);
        textView.setText(Html.fromHtml(createItem(hostCursor)));
        if (hostCursor.isReadonly()) {
            textView.setCheckMarkDrawable(null);
        }
        return textView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        CheckedTextView textView = (CheckedTextView) view;
        textView.setText(Html.fromHtml(createItem(cursor)));
        textView.setCheckMarkDrawable(checkMarkDrawable);
        HostCursor hostCursor = (HostCursor) cursor;
        if (hostCursor.isReadonly()) {
            textView.setCheckMarkDrawable(null);
        }
    }

    @Override
    public boolean isEnabled(int position) {
        HostCursor hostCursor = (HostCursor) getItem(position);
        return !hostCursor.isReadonly();
    }

    private String createItem(Cursor cursor) {
        String host = cursor.getString(cursor.getColumnIndex("name"));
        Integer port = cursor.getInt(cursor.getColumnIndex("port"));
        String description =
          cursor.getString(cursor.getColumnIndex("description"));

        String itemText = "<b>" + host;
        if (port != JDictClient.DEFAULT_PORT) {
            itemText += ":" + port.toString();
        }
        itemText += "</b>";
        if (description.length() > 0) {
            itemText += "<br><i>" + description + "</i>";
        }
        return itemText;
    }
}
