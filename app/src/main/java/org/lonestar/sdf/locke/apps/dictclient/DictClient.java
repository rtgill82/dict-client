/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class DictClient extends Application {
    private Host currentHost;
    private HostCache cache;
    private OnSharedPreferenceChangeListener listener;

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseManager.initialize(getApplicationContext());
        DonationManager.initialize(getApplicationContext());
        cache = new HostCache();

        currentHost = getDefaultHost();
        cache.add(currentHost);

        listener =
          new SharedPreferences.OnSharedPreferenceChangeListener() {
              public void onSharedPreferenceChanged(
                  SharedPreferences preferences,
                  String key
              ) {
                  if (key.equals(getString(R.string.pref_key_default_host))) {
                      int hostId = Integer.parseInt(preferences
                                                    .getString(key, "1"));
                      setCurrentHostById(hostId);
                  }
              }
          };

        SharedPreferences preferences =
          PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public Host getDefaultHost() {
        return DatabaseManager.getInstance().getDefaultHost(this);
    }

    public void useDefaultHost() {
        setCurrentHost(getDefaultHost());
    }

    public Host getCurrentHost() {
        return currentHost;
    }

    public void setCurrentHost(Host host) {
        /* Ensure new host is not the same as the old one. */
        if (currentHost == null || currentHost.getId() != host.getId())
          currentHost = findCachedHost(host.getId(), host);
    }

    public void setCurrentHostById(int hostId) {
        if (currentHost == null || currentHost.getId() != hostId)
          currentHost = findCachedHost(hostId, null);
    }

    private Host findCachedHost(int hostId, Host defaultHost) {
        Host host = cache.getHostById(hostId);
        if (host == null) {
            if (defaultHost == null)
              defaultHost = DatabaseManager.getInstance()
                                           .getHostById(hostId);
            host = defaultHost;
            cache.add(defaultHost);
        }
        return host;
    }
}
