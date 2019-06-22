/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import org.lonestar.sdf.locke.libs.jdictclient.JDictClient;

public class DictClient extends Application {
    public static final String CHANNEL = "dict-client";

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;  // Refers to 'this'
    private static Host sCurrentHost;

    private HostCache mCache;
    private OnHostChangeListener mOnHostChangeListener;

    private final OnSharedPreferenceChangeListener mPreferenceChangeListener =
      createOnSharedPreferenceChangeListener();

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        DatabaseManager.initialize(getApplicationContext());
        JDictClient.setClientString(buildClientString());
        mCache = new HostCache();
        sCurrentHost = getDefaultHost();
        mCache.add(sCurrentHost);
        PreferenceManager.getDefaultSharedPreferences(this)
          .registerOnSharedPreferenceChangeListener(
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

    public void setCurrentHost(Host host) {
        /* Ensure new host is not the same as the old one. */
        if (sCurrentHost != null && sCurrentHost.getId().equals(host.getId()))
          return;
        sCurrentHost = findCachedHost(host.getId(), host);
        if (mOnHostChangeListener != null)
          mOnHostChangeListener.onHostChange();
    }

    public static Host getCurrentHost() {
        return sCurrentHost;
    }

    private void setCurrentHostById(int hostId) {
        if (sCurrentHost == null || sCurrentHost.getId() != hostId) {
            sCurrentHost = findCachedHost(hostId, null);
            if (mOnHostChangeListener != null)
              mOnHostChangeListener.onHostChange();
        }
    }

    public String getVersionString() {
        try {
            PackageInfo pInfo = getPackageManager()
              .getPackageInfo(getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildClientString() {
        String name = getString(R.string.app_name);
        return name + " " + getVersionString();
    }

    private Host findCachedHost(int hostId, Host defaultHost) {
        Host host = mCache.getHostById(hostId);
        if (host == null) {
            if (defaultHost == null)
              defaultHost = (Host) DatabaseManager.find(Host.class, hostId);
            host = defaultHost;
            mCache.add(host);
        }
        return host;
    }

    public static Context getContext() {
        return sContext;
    }

    private OnSharedPreferenceChangeListener
    createOnSharedPreferenceChangeListener() {
        return new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(
              SharedPreferences preferences, String key
            ) {
                if (key.equals(getString(R.string.pref_key_default_host))) {
                    int hostId = Integer.parseInt(preferences
                                                  .getString(key, "1"));
                    setCurrentHostById(hostId);
                }
              }};
    }

    public static abstract class OnHostChangeListener {
        public abstract void onHostChange();
    }
}
