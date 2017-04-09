/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class DictClientApplication extends Application
{
  private Host currentHost;
  private OnSharedPreferenceChangeListener listener;

  @Override
  public void onCreate ()
  {
    super.onCreate ();
    DatabaseManager.initialize (getApplicationContext ());
    currentHost = DatabaseManager.getInstance ().getDefaultHost (this);

    listener =
      new SharedPreferences.OnSharedPreferenceChangeListener()
      {
        public void onSharedPreferenceChanged (SharedPreferences preferences,
                                               String key)
        {
          if (key == getString(R.string.pref_key_default_host))
            {
              int hostId = Integer.parseInt(preferences.getString(key, "1"));
              setCurrentHostById (hostId);
            }
        }
      };

    SharedPreferences prefs =
      PreferenceManager.getDefaultSharedPreferences (this);
    prefs.registerOnSharedPreferenceChangeListener (listener);
  }

  public Host getCurrentHost ()
  {
    return currentHost;
  }

  public void setCurrentHost (Host host)
  {
    /* Ensure new host is not the same as the old one. */
    if (currentHost == null || currentHost.getId () != host.getId ())
      currentHost = host;
  }

  public void setCurrentHostById (int hostId)
  {
    if (currentHost == null || currentHost.getId() != hostId)
      currentHost = DatabaseManager.getInstance ().getHostById (hostId);
  }
}
