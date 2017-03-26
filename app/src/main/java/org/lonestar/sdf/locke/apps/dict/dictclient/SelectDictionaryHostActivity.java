/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DictClient.
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.sql.SQLException;

public class SelectDictionaryHostActivity extends ListActivity
{
  @Override
  protected void onCreate (Bundle savedInstanceState)
  {
    super.onCreate (savedInstanceState);
    try
      {
        DictionaryHostCursor cursor = DatabaseManager.getInstance ()
                                                     .getHostList ();
        SelectDictionaryHostCursorAdapter ca =
          new SelectDictionaryHostCursorAdapter (this, cursor, 0);
        getListView ().setAdapter (ca);
      }
    catch (SQLException e)
      {
        ErrorDialog.show (this, e.getMessage ());
      }
  }

  @Override
  public void onListItemClick (ListView l, View v, int pos, long id)
  {
    DictionaryHostCursor c = (DictionaryHostCursor) l.getItemAtPosition (pos);
    DictClientApplication app = (DictClientApplication) getApplication ();
    app.setCurrentHost (c.getDictionaryHost ());
    finish ();
  }
}
