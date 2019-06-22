package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.util.AttributeSet;

class DefaultHostListPreference extends CustomListPreference {
    public DefaultHostListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        bindHosts();
    }

    private void bindHosts() {
        HostCursor cursor = (HostCursor)
          DatabaseManager.find(Host.class, "hidden", false);
        CharSequence[] entries = new CharSequence[cursor.getCount()];
        CharSequence[] entryValues = new CharSequence[cursor.getCount()];

        try {
            for (int i = 0; !cursor.isAfterLast(); cursor.moveToNext(), i++) {
                entries[i] = cursor.getHostName();
                entryValues[i] = Integer.toString(cursor.getId());
            }
        } finally {
            cursor.close();
        }

        Host defaultHost = DictClient.getDefaultHost();
        setEntries(entries);
        setEntryValues(entryValues);
        setValue(defaultHost.getId().toString());
    }
}
