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
