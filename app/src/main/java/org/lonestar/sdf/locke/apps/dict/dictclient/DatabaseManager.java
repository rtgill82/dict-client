/*
 * Copyright (C) 2016 Robert Gill <locke@sdf.lonestar.org>
 *
 * This file is part of DictClient
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager extends OrmLiteSqliteOpenHelper
{
  final private static String DATABASE_NAME    = "dictclient.db";
  final private static int    DATABASE_VERSION = 1;

  private static DatabaseManager instance = null;
  private Context context;

  private DatabaseManager(Context context)
  {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    this.context = context;
  }

  static public void initialize(Context context)
  {
    if (instance == null)
      instance = new DatabaseManager(context);
  }

  static public DatabaseManager getInstance()
  {
    return instance;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void onCreate(SQLiteDatabase db, ConnectionSource cs)
  {
    Resources resources = this.context.getResources();
    try
      {
        TableUtils.createTable(cs, DictionaryHost.class);
        loadData(resources, db, cs, 0, DATABASE_VERSION);
      }
    catch (SQLException e)
      {
        Log.e("DatabaseManager", "SQLException caught: " + e.toString());
        throw new RuntimeException(e);
      }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, ConnectionSource cs, int oldVersion,
                        int newVersion)
  {
    Resources resources = this.context.getResources();
    try
      {
        loadData(resources, db, cs, oldVersion, newVersion);
      }
    catch (SQLException e)
      {
        Log.e("DatabaseManager", "SQLException caught: " + e.toString());
        throw new RuntimeException(e);
      }
  }

  public DictionaryHost getPreferredHost(Context context)
    throws SQLException
  {
    SharedPreferences prefs =
      PreferenceManager.getDefaultSharedPreferences(context);
    Resources resources = context.getResources();
    int host = prefs.getInt(resources.getString(R.string.pref_key_dict_host),
        resources.getInteger(R.integer.pref_value_dict_host));
    Dao<DictionaryHost, Integer> dao = instance.getDao(DictionaryHost.class);
    return dao.queryForId(host);
  }

  public DictionaryHost getHostById(Integer id)
    throws SQLException
  {
    Dao<DictionaryHost, Integer> dao = instance.getDao(DictionaryHost.class);
    return dao.queryForId(id);
  }

  public boolean deleteHostById(int id)
    throws SQLException
  {
    Dao<DictionaryHost, Integer> dao = instance.getDao(DictionaryHost.class);
    return (dao.deleteById(id) == 1);
  }

  public DictionaryHostCursor getHostList()
    throws SQLException
  {
    Dao<DictionaryHost, Integer> dao = instance.getDao(DictionaryHost.class);
    QueryBuilder<DictionaryHost, Integer> qb = dao.queryBuilder();
    CloseableIterator<DictionaryHost> iterator = dao.iterator(qb.prepare());
    AndroidDatabaseResults results =
      (AndroidDatabaseResults) iterator.getRawResults();
    return new DictionaryHostCursor(results.getRawCursor());
  }

  public void saveHost(DictionaryHost host)
    throws SQLException
  {
    Dao<DictionaryHost, Integer> dao = getDao(DictionaryHost.class);

    if (host.getId() == null)
      {
        Map<String, Object> map = new HashMap();
        map.put("host_name", host.getHostName());
        map.put("port", host.getPort());
        if (!dao.queryForFieldValues(map).isEmpty())
          {
            SQLException exception =
              new SQLException("The host " + host.getHostName() + ":"
                               + host.getPort().toString() + " already exists.");
            throw exception;
          }
      }

    dao.createOrUpdate(host);
  }

  private void loadData(Resources resources, SQLiteDatabase db,
                        ConnectionSource cs, int oldVersion, int newVersion)
    throws SQLException
  {
    Yaml yaml = new Yaml();
    InputStream stream = resources.openRawResource(R.raw.dicthosts);
    for (Object data : yaml.loadAll(stream))
      {
        DatabaseRevision rev = (DatabaseRevision) data;
        if (rev.getVersion() > oldVersion && rev.getVersion() <= newVersion)
          rev.commit(db, cs);
      }
  }
}
