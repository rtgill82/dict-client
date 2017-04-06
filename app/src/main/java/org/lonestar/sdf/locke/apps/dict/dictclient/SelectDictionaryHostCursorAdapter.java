/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DictClient.
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

public class SelectDictionaryHostCursorAdapter extends CursorAdapter
{

  public SelectDictionaryHostCursorAdapter (Context context, Cursor cursor,
      int flags)
  {
    super (context, cursor, flags);
  }

  public View newView (Context context, Cursor cursor, ViewGroup parent)
  {
    LayoutInflater inflater = LayoutInflater.from (context);
    TextView view = (TextView) inflater.inflate (R.layout.list_item_host, null);
    view.setText (Html.fromHtml (createItem (cursor)));
    return view;
  }

  public void bindView (View view, Context context, Cursor cursor)
  {
    if (view instanceof TextView)
      {
        String itemText = createItem (cursor);
        ((TextView) view).setText (Html.fromHtml (itemText));
      }
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
