/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class SelectHostActivity extends ListActivity
{
  @Override
  protected void onCreate (Bundle savedInstanceState)
  {
    super.onCreate (savedInstanceState);
    HostCursor cursor = DatabaseManager.getInstance ().getHostList ();
    SelectHostCursorAdapter ca = new SelectHostCursorAdapter(this, cursor, 0);
    getListView ().setAdapter (ca);
  }

  @Override
  public void onListItemClick (ListView l, View v, int pos, long id)
  {
    HostCursor c = (HostCursor) l.getItemAtPosition (pos);
    DictClientApplication app = (DictClientApplication) getApplication ();
    app.setCurrentHost (c.getDictionaryHost ());
    finish ();
  }
}
