/*
 * Modified: Sat 26 Nov 2016 02:41:26 PM PST
 * Copyright (C) 2016 Robert Gill <locke@sdf.lonestar.org>
 *
 * This file is part of DictClient
 *
 */

package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.app.Application;

public class DictClientApplication extends Application
{
  @Override
  public void onCreate()
  {
    super.onCreate();
    DatabaseManager.initialize(getApplicationContext());
  }
}
