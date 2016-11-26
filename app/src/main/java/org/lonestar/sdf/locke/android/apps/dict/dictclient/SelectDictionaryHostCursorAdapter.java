package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;

import org.lonestar.sdf.locke.apps.dict.dictclient.R;
import org.lonestar.sdf.locke.libs.dict.JDictClient;

public class SelectDictionaryHostCursorAdapter extends CursorAdapter
{

  public SelectDictionaryHostCursorAdapter(Context context, Cursor cursor,
                                           int flags)
    {
      super(context, cursor, flags);
    }

  public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
      LayoutInflater inflater = LayoutInflater.from(context);
      CheckedTextView view = (CheckedTextView) inflater.inflate(
          R.layout.select_host_list_item, null
        );
      String itemText = createItem(cursor);
      view.setText(Html.fromHtml(itemText));
      return view;
    }

  public void bindView(View view, Context context, Cursor cursor)
    {
      if (view instanceof TextView)
        {
          String itemText = createItem(cursor);
          ((TextView) view).setText(Html.fromHtml(itemText));
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