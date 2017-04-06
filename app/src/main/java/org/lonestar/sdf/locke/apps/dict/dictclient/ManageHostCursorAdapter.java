/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.lonestar.sdf.locke.libs.dict.JDictClient;

public class ManageHostCursorAdapter extends CursorAdapter
{
  public ManageHostCursorAdapter (Context context, Cursor c, int flags)
  {
    super (context, c, flags);
  }

  public View newView (Context context, Cursor cursor, ViewGroup parent)
  {
    LayoutInflater inflater = LayoutInflater.from (context);
    TextView textview = (TextView) inflater.inflate (R.layout.list_item_host, null);
    textview.setText (Html.fromHtml (createItem (cursor)));
    return textview;
  }

  public void bindView (View view, Context context, Cursor cursor)
  {
    TextView textview = (TextView) view;
    textview.setText (Html.fromHtml (createItem (cursor)));
  }

  private String createItem (Cursor cursor)
  {
    String host = cursor.getString (cursor.getColumnIndex ("host_name"));
    Integer port = cursor.getInt (cursor.getColumnIndex ("port"));
    String description =
      cursor.getString (cursor.getColumnIndex ("description"));

    String itemText = "<b>" + host;
    if (port != JDictClient.DEFAULT_PORT)
      itemText += ":" + port.toString ();
    itemText += "</b>";
    if (description.length () > 0)
      itemText += "<br><i>" + description + "</i>";

    return itemText;
  }
}
