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
import android.widget.CursorAdapter;

import org.lonestar.sdf.locke.libs.jdictclient.JDictClient;

import java.util.ArrayList;

class ManageHostCursorAdapter extends CursorAdapter {
    private LayoutInflater inflater;
    private ArrayList<Boolean> toggles;

    public ManageHostCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        inflater = LayoutInflater.from(context);
    }

    public void setToggleList(ArrayList<Boolean> list) {
        toggles = list;
        notifyDataSetChanged();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        HostCursor hostCursor = (HostCursor) cursor;
        CompatCheckedTextView textView = (CompatCheckedTextView)
          inflater.inflate(R.layout.list_item_host, null);
        CheckMarkHolder holder = new CheckMarkHolder();
        holder.checkMark = textView.getCheckMarkDrawable();
        textView.setTag(holder);
        return setupListItemView(textView, hostCursor);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CompatCheckedTextView textView;
        HostCursor cursor = (HostCursor) getItem(position);
        if (convertView != null) {
            textView = (CompatCheckedTextView) convertView;
            setupListItemView(textView, cursor);
        } else {
            Context context = parent.getContext();
            textView = (CompatCheckedTextView)
              newView(context, cursor, parent);
        }
        return textView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        CompatCheckedTextView textView = (CompatCheckedTextView) view;
        HostCursor hostCursor = (HostCursor) cursor;
        setupListItemView(textView, hostCursor);
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

    private CompatCheckedTextView setupListItemView(
      CompatCheckedTextView view,
      HostCursor cursor
    ) {
        CheckMarkHolder holder = (CheckMarkHolder) view.getTag();
        view.setText(Html.fromHtml(createItem(cursor)));
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
