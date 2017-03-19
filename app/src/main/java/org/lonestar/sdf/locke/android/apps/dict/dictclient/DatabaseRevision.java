/*
 * Copyright (C) 2016 Robert Gill <locke@sdf.lonestar.org>
 *
 * This file is part of DictClient
 *
 */

package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by locke on 7/24/16.
 */

public class DatabaseRevision
{
  private Integer version;
  private List<DictionaryHost> add_hosts;
  private List<DictionaryHost> remove_hosts;

  public Integer getVersion()
  {
    return this.version;
  }

  public void setVersion(Integer version)
  {
    this.version = version;
  }

  public void setAddHosts(List<DictionaryHost> hosts)
  {
    this.add_hosts = hosts;
  }

  public void setRemoveHosts(List<DictionaryHost> hosts)
  {
    this.remove_hosts = hosts;
  }

  public void commit(SQLiteDatabase db, ConnectionSource cs)
    throws SQLException
  {
    if (db.getVersion() < this.version)
      {
        db.setVersion(this.version);
        Dao<DictionaryHost, Integer> dao =
          DaoManager.createDao(cs, DictionaryHost.class);

        // Delete old hosts
        if (remove_hosts != null)
          {
            for (DictionaryHost host : remove_hosts)
              {
                List<DictionaryHost> rows = dao.queryForMatching(host);
                for (DictionaryHost row : rows)
                  {
                    dao.delete(row);
                  }
              }
          }

        // Add new hosts
        if (add_hosts != null)
          {
            for (DictionaryHost host : add_hosts)
              {
                dao.create(host);
              }
          }
      }
  }
}
