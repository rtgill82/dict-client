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

    private Host mCurrentHost;
    private HostCache mCache;
    private OnHostChangeListener mOnHostChangeListener;

    @SuppressWarnings("FieldCanBeLocal")
    private OnSharedPreferenceChangeListener mPreferenceChangeListener;

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseManager.initialize(getApplicationContext());
        DonationManager.initialize(getApplicationContext());
        mCache = new HostCache();

        mCurrentHost = getDefaultHost();
        mCache.add(mCurrentHost);

        mPreferenceChangeListener =
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
          mPreferenceChangeListener
        );
    }

    public void setOnHostChangeListener(OnHostChangeListener listener) {
        mOnHostChangeListener = listener;
    }

    public Host getDefaultHost() {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        Resources resources = getResources();
        int hostId = Integer.parseInt(preferences.getString(
                resources.getString(R.string.pref_key_default_host),
                resources.getString(R.string.pref_value_default_host))
        );
        return (Host) DatabaseManager.find(Host.class, hostId);
    }

    public Host getCurrentHost() {
        return mCurrentHost;
    }

    public void setCurrentHost(Host host) {
        /* Ensure new host is not the same as the old one. */
        if (mCurrentHost == null
              || !mCurrentHost.getId().equals(host.getId())) {
            mCurrentHost = findCachedHost(host.getId(), host);
            if (mOnHostChangeListener != null)
              mOnHostChangeListener.onHostChange(mCurrentHost);
        }
    }

    public void setCurrentHostById(int hostId) {
        if (mCurrentHost == null || mCurrentHost.getId() != hostId) {
            mCurrentHost = findCachedHost(hostId, null);
            if (mOnHostChangeListener != null)
              mOnHostChangeListener.onHostChange(mCurrentHost);
        }
    }

    private Host findCachedHost(int hostId, Host defaultHost) {
        Host host = mCache.getHostById(hostId);
        if (host == null) {
            if (defaultHost == null)
              defaultHost = (Host) DatabaseManager.find(Host.class, hostId);
            host = defaultHost;
            mCache.add(defaultHost);
        }
        return host;
    }

    public static abstract class OnHostChangeListener {
        public abstract void onHostChange(Host host);
    }
}
