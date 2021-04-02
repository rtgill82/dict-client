/*
 * Copyright (C) 2017 Robert Gill <rtgill82@gmail.com>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.github.rtgill82.libs.jdictclient.JDictClient;

class HostCursor extends CursorWrapper {
    public HostCursor(Cursor cursor) {
        super(cursor);
    }

    public Host getHost() {
        return (Host) DatabaseManager.find(Host.class, getId());
    }

    public int getId() {
        Cursor cursor = getWrappedCursor();
        return (cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
    }

    public String getHostName() {
        Cursor cursor = getWrappedCursor();
        int port = cursor.getInt(cursor.getColumnIndexOrThrow("port"));
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        if (port != JDictClient.DEFAULT_PORT) {
            name = name + ":" + port;
        }
        return name;
    }

    public String getDescription() {
        Cursor cursor = getWrappedCursor();
        return cursor.getString(cursor.getColumnIndexOrThrow("description"));
    }

    public boolean isReadonly() {
        Cursor cursor = getWrappedCursor();
        return (cursor.getInt(cursor.getColumnIndexOrThrow("readonly")) != 0);
    }
}
