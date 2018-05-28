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
    public static final String CHANNEL = "dict-client";

    private Host currentHost;
    private HostCache cache;
    private OnSharedPreferenceChangeListener listener;
    private OnHostChangedListener onHostChangedListener;

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

    public void setOnHostChangedListener(OnHostChangedListener listener) {
        onHostChangedListener = listener;
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
        if (currentHost == null || currentHost.getId() != host.getId()) {
            currentHost = findCachedHost(host.getId(), host);
            if (onHostChangedListener != null)
              onHostChangedListener.onHostChanged(currentHost);
        }
    }

    public void setCurrentHostById(int hostId) {
        if (currentHost == null || currentHost.getId() != hostId) {
            currentHost = findCachedHost(hostId, null);
            if (onHostChangedListener != null)
              onHostChangedListener.onHostChanged(currentHost);
        }
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

    public static abstract class OnHostChangedListener {
        public abstract void onHostChanged(Host host);
    }
}
