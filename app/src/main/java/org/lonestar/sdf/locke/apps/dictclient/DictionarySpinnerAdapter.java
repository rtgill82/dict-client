/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class DictionarySpinnerAdapter implements SpinnerAdapter {
    final private Context context;
    final private List<Dictionary> data;

    public DictionarySpinnerAdapter(Context context, Collection<Dictionary> data) {
        this.context = context;
        this.data = new ArrayList<>(data);
        this.data.add(0, Dictionary.ALL_DICTIONARIES);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Dictionary getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return android.R.layout.simple_spinner_dropdown_item;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView v = new TextView(context.getApplicationContext());
        v.setTextColor(Color.BLACK);
        v.setText(data.get(position).getDescription());
        v.setMaxLines(1);
        v.setEllipsize(TextUtils.TruncateAt.END);
        return v;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        // TODO Auto-generated method stub
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        // TODO Auto-generated method stub
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return this.getView(position, convertView, parent);
    }
}
