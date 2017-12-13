/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.database.Cursor;
import android.database.CursorWrapper;

import org.lonestar.sdf.locke.libs.dict.JDictClient;

import java.util.Date;

class HostCursor extends CursorWrapper
{
  private Cursor cursor;

  public HostCursor(Cursor cursor)
  {
    super (cursor);
    this.cursor = cursor;
  }

  public Host getDictionaryHost ()
  {
    Host host = new Host(getId (), getHostName (), getPort ());
    host.setDescription (getDescription ());
    host.setLastRefresh (getLastRefresh ());
    host.setReadonly (isReadonly ());
    host.setUserDefined (isUserDefined ());
    return host;
  }

  public Integer getId ()
  {
    return (cursor.getInt (cursor.getColumnIndexOrThrow ("_id")));
  }

  public String getHostName ()
  {
    return (cursor.getString (cursor.getColumnIndexOrThrow ("host_name")));
  }

  public Integer getPort ()
  {
    return (cursor.getInt (cursor.getColumnIndexOrThrow ("port")));
  }

  public String getDescription ()
  {
    return (cursor.getString (cursor.getColumnIndexOrThrow ("description")));
  }

  public boolean isReadonly ()
  {
    return (cursor.getInt (cursor.getColumnIndexOrThrow ("readonly")) != 0);
  }

  public boolean isUserDefined ()
  {
    return (cursor.getInt (cursor.getColumnIndexOrThrow ("user_defined")) != 0);
  }

  public Date getLastRefresh ()
  {
    return (new Date(cursor.getLong(cursor.getColumnIndexOrThrow ("last_refresh"))));
  }

  public String getString (int columnIndex)
  {
    if (cursor.getColumnName (columnIndex).equals ("port"))
      {
        if (cursor.getInt (columnIndex) == JDictClient.DEFAULT_PORT)
          {
            return "";
          }
        else
          {
            return ":" + cursor.getString (columnIndex);
          }
      }
    else
      {
        return cursor.getString (columnIndex);
      }
  }
}