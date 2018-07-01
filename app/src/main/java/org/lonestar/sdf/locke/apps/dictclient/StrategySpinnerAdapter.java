/*
 * Copyright (C) 2018 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

class StrategySpinnerAdapter extends ArrayAdapter<Strategy> {
    public StrategySpinnerAdapter(Context context, int resource, List<Strategy> objects) {
        super(context, resource, objects);
        objects.add(0, Strategy.DEFINE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = super.getView(position, null, parent);
        }
        TextView view = (TextView) convertView;
        view.setText(getItem(position).getName());
        return view;
    }
}
