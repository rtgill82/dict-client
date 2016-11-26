/*
 * Modified: Sat 26 Nov 2016 02:42:29 PM PST
 * Copyright (C) 2016 Robert Gill <locke@sdf.lonestar.org>
 *
 * This file is part of DictClient
 *
 */

package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.lonestar.sdf.locke.apps.dict.dictclient.R;
import org.lonestar.sdf.locke.libs.dict.JDictClient;

public class ManageHostCursorAdapter extends CursorAdapter
{

  public ManageHostCursorAdapter(Context context, Cursor c, int flags)
  {
    super(context, c, flags);
  }

  public View newView(Context context, Cursor cursor, ViewGroup parent)
  {
    LayoutInflater inflater = LayoutInflater.from(context);
    RelativeLayout view = (RelativeLayout) inflater.inflate(
                            R.layout.manage_host_list_item, null
                          );
    String itemText = createItem(cursor);

    TextView textview = (TextView) view.findViewById(R.id.manage_host_list_item_textview);
    textview.setText(Html.fromHtml(itemText));
    return view;
  }

  public void bindView(View view, Context context, Cursor cursor)
  {
    TextView textview = (TextView) view.findViewById(R.id.manage_host_list_item_textview);
    if (textview != null)
      {
        String itemText = createItem(cursor);
        textview.setText(Html.fromHtml(itemText));
      }
  }

  private String createItem(Cursor cursor)
  {
    String host = cursor.getString(cursor.getColumnIndex("host_name"));
    Integer port = cursor.getInt(cursor.getColumnIndex("port"));
    String description =
      cursor.getString(cursor.getColumnIndex("description"));

    String itemText = "<b>" + host;
    if (port != JDictClient.DEFAULT_PORT)
      itemText += ":" + port.toString();
    itemText += "</b>";
    if (description.length() > 0)
      itemText += "<br><i>" + description + "</i>";

    return itemText;
  }
}
