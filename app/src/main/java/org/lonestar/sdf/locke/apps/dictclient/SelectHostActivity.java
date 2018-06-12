/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.HashMap;
import java.util.Map;

public class SelectHostActivity extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Map<String, Object> map = new HashMap();
        map.put("hidden", false);
        HostCursor cursor = (HostCursor) DatabaseManager.find(Host.class, map);
        HostCursorAdapter ca = new HostCursorAdapter(this, cursor, 0);
        getListView().setAdapter(ca);
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        HostCursor cursor = (HostCursor) l.getItemAtPosition(pos);
        DictClient app = (DictClient) getApplication();
        app.setCurrentHost(cursor.getHost());
        finish();
    }
}
