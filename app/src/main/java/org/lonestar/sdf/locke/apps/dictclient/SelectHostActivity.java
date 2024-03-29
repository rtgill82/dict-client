/*
 * Copyright (C) 2017 Robert Gill <rtgill82@gmail.com>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class SelectHostActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HostCursor cursor = (HostCursor)
          DatabaseManager.find(Host.class, "hidden", false);
        HostCursorAdapter ca = new HostCursorAdapter(this, cursor, 0);
        ListView listView = new ListView(this);
        setContentView(listView);
        listView.setAdapter(ca);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HostCursor cursor = (HostCursor)
                  parent.getItemAtPosition(position);
                Host host = cursor.getHost();
                DictClient.setCurrentHost(host);
                finish();
            }
        });
    }
}
