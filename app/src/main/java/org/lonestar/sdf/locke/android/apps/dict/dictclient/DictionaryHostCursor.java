package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.database.Cursor;
import android.database.CursorWrapper;

import org.lonestar.sdf.locke.libs.dict.JDictClient;

public class DictionaryHostCursor extends CursorWrapper
{
  private Cursor cursor;

  public DictionaryHostCursor(Cursor cursor)
    {
      super(cursor);
      this.cursor = cursor;
    }

  public DictionaryHost getDictionaryHost()
    {
      DictionaryHost host = new DictionaryHost(getId(), getHostName(), getPort());
      host.setDescription(getDescription());
      host.setReadonly(isReadonly());
      host.setUserDefined(isUserDefined());
      return host;
    }

  public Integer getId()
    {
      return (cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
    }

  public String getHostName()
    {
      return (cursor.getString(cursor.getColumnIndexOrThrow("host_name")));
    }

  public Integer getPort()
    {
      return (cursor.getInt(cursor.getColumnIndexOrThrow("port")));
    }

  public String getDescription()
    {
      return (cursor.getString(cursor.getColumnIndexOrThrow("description")));
    }

  public boolean isReadonly()
    {
      return (cursor.getInt(cursor.getColumnIndexOrThrow("readonly")) != 0);
    }

  public boolean isUserDefined()
    {
      return (cursor.getInt(cursor.getColumnIndexOrThrow("user_defined")) != 0);
    }

  public String getString(int columnIndex)
    {
      if (cursor.getColumnName(columnIndex).equals("port"))
        {
          if (cursor.getInt(columnIndex) == JDictClient.DEFAULT_PORT)
            {
              return "";
            } else {
              return ":" + cursor.getString(columnIndex);
            }
        } else {
          return cursor.getString(columnIndex);
        }
    }
}
