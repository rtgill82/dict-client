package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.database.Cursor;
import android.database.CursorWrapper;

import org.lonestar.sdf.locke.libs.dict.JDictClient;

/**
 * Created by locke on 11/8/15.
 */
public class HostListCursor extends CursorWrapper {
    private Cursor cursor;

    public HostListCursor(Cursor cursor) {
        super(cursor);
        this.cursor = cursor;
    }

    public String getString(int columnIndex) {
        if (cursor.getColumnName(columnIndex).equals("port")) {
            if (cursor.getInt(columnIndex) == JDictClient.DEFAULT_PORT) {
                return "";
            } else {
                return ":" + cursor.getString(columnIndex);
            }
        } else {
            return cursor.getString(columnIndex);
        }
    }
}
