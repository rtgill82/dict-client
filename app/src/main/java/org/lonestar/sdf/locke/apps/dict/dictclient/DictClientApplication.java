/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DictClient.
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.app.Application;

import java.sql.SQLException;

public class DictClientApplication extends Application
{
  private DictionaryHost currentHost;

  @Override
  public void onCreate ()
  {
    super.onCreate ();
    DatabaseManager.initialize (getApplicationContext ());
    try
      {
        currentHost = DatabaseManager.getInstance ().getDefaultHost (this);
      }
    catch (SQLException e)
      {
        throw new RuntimeException ("Unable to read host database.");
      }
  }

  public DictionaryHost getCurrentHost ()
  {
    return currentHost;
  }

  public void setCurrentHost (DictionaryHost host)
  {
    /* Ensure new host is not the same as the old one. */
    if (currentHost == null || currentHost.getId () != host.getId ())
      currentHost = host;
  }
}
