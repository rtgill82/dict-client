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
import android.content.res.Resources;
import android.preference.PreferenceManager;

public class DictClient extends Application {
    public static final String CHANNEL = "dict-client";

    private Host currentHost;
    private HostCache cache;
    private OnHostChangeListener onHostChangeListener;

    @SuppressWarnings("FieldCanBeLocal")
    private OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseManager.initialize(getApplicationContext());
        DonationManager.initialize(getApplicationContext());
        cache = new HostCache();

        currentHost = getDefaultHost();
        cache.add(currentHost);

        preferenceChangeListener =
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
        preferences.registerOnSharedPreferenceChangeListener(
          preferenceChangeListener
        );
    }

    public void setOnHostChangeListener(OnHostChangeListener listener) {
        onHostChangeListener = listener;
    }

    public Host getDefaultHost() {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        Resources resources = this.getResources();
        int hostId = Integer.parseInt(preferences.getString(
                resources.getString(R.string.pref_key_default_host),
                resources.getString(R.string.pref_value_default_host))
        );
        return (Host) DatabaseManager.find(Host.class, hostId);
    }

    public Host getCurrentHost() {
        return currentHost;
    }

    public void setCurrentHost(Host host) {
        /* Ensure new host is not the same as the old one. */
        if (currentHost == null || !currentHost.getId().equals(host.getId())) {
            currentHost = findCachedHost(host.getId(), host);
            if (onHostChangeListener != null)
              onHostChangeListener.onHostChange(currentHost);
        }
    }

    public void setCurrentHostById(int hostId) {
        if (currentHost == null || currentHost.getId() != hostId) {
            currentHost = findCachedHost(hostId, null);
            if (onHostChangeListener != null)
              onHostChangeListener.onHostChange(currentHost);
        }
    }

    private Host findCachedHost(int hostId, Host defaultHost) {
        Host host = cache.getHostById(hostId);
        if (host == null) {
            if (defaultHost == null)
              defaultHost = (Host) DatabaseManager.find(Host.class, hostId);
            host = defaultHost;
            cache.add(defaultHost);
        }
        return host;
    }

    public static abstract class OnHostChangeListener {
        public abstract void onHostChange(Host host);
    }
}
