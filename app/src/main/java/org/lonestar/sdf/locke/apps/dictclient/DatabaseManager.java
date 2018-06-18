/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

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
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

class DatabaseManager extends OrmLiteSqliteOpenHelper {
    final private static String DATABASE_NAME    = "dict-client.db";
    final private static int    DATABASE_VERSION = 3;

    private static DatabaseManager instance = null;
    private Context context;

    private DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    static public void initialize(Context context) {
        if (instance == null)
          instance = new DatabaseManager(context);
    }

    static public DatabaseManager getInstance() {
        return instance;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(SQLiteDatabase db, ConnectionSource cs) {
        Resources resources = this.context.getResources();
        try {
            TableUtils.createTable(cs, Host.class);
            TableUtils.createTable(cs, Dictionary.class);
            TableUtils.createTable(cs, Strategy.class);
            loadData(resources, db, cs, 0, DATABASE_VERSION);
        } catch (SQLException e) {
            Log.e("DatabaseManager", "SQLException caught: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource cs,
                          int oldVersion, int newVersion) {
    }

    public Host getDefaultHost(Context context) {
        SharedPreferences prefs =
          PreferenceManager.getDefaultSharedPreferences(context);
        Resources resources = context.getResources();
        int hostId = Integer.parseInt(prefs.getString(
            resources.getString(R.string.pref_key_default_host),
            resources.getString(R.string.pref_value_default_host))
        );

        Host host;
        try {
            Dao<Host, Integer> dao = instance.getDao(Host.class);
            host = dao.queryForId(hostId);
        } catch (SQLException e) {
            Log.e("DatabaseManager", "SQLException caught: " + e.toString());
            throw new RuntimeException(e);
        }
        return host;
    }

    public Host getHostById(Integer id) {
        try {
            Dao<Host, Integer> dao;
            dao = instance.getDao(Host.class);
            return dao.queryForId(id);
        } catch (SQLException e) {
            Log.e("DatabaseManager", "SQLException caught: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    public boolean deleteHost(Host host) {
        try {
            Dao<Host, Integer> dao = instance.getDao(Host.class);

            // Ignore readonly hosts
            if (host.isReadonly())
              return true;

            // Remove preconfigured hosts from list
            else if (!host.isUserDefined()) {
                host.setHidden(true);
                return saveHost(host);
            }

            // Otherwise delete from database
            else
              return (dao.deleteById(host.getId()) == 1);
        } catch (SQLException e) {
            Log.e("DatabaseManager", "SQLException caught: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    public HostCursor getHostList() {
        CloseableIterator<Host> iterator;
        try {
            Dao<Host, Integer> dao = instance.getDao(Host.class);
            QueryBuilder<Host, Integer> qb = dao.queryBuilder();
            iterator = dao.iterator(qb.where().eq("hidden", false)
                                    .prepare());
        } catch(SQLException e) {
            Log.e("DatabaseManager", "SQLException caught: " + e.toString());
            throw new RuntimeException(e);
        }

        AndroidDatabaseResults results =
          (AndroidDatabaseResults) iterator.getRawResults();
        return new HostCursor(results.getRawCursor());
    }

    public boolean saveHost(Host host)
          throws SQLException {
        Dao<Host, Integer> dao = getDao(Host.class);

        if (host.getId() == null) {
            Map<String, Object> map = new HashMap();
            map.put("name", host.getName());
            map.put("port", host.getPort());
            if (!dao.queryForFieldValues(map).isEmpty()) {
                SQLException exception =
                  new SQLException("The host " + host.getName() + ":"
                                   + host.getPort().toString()
                                   + " already exists.");
                throw exception;
            }
        }

        Dao.CreateOrUpdateStatus status = dao.createOrUpdate(host);
        return (status.isCreated() | status.isUpdated());
    }

    public List<Dictionary> getDictionaries(Host host) {
        try {
            ArrayList<Dictionary> dictionaries = null;
            Dao<Dictionary, Void> dictDao =
              DatabaseManager.getInstance().getDao(Dictionary.class);
            List<Dictionary> dbdicts = dictDao.queryForEq("host_id", host);

            if (dbdicts.size() > 0) {
                dictionaries = new ArrayList<>();
                dictionaries.add(Dictionary.ALL_DICTIONARIES);
                dictionaries.addAll(dbdicts);
            }
            return dictionaries;
        }
        catch (SQLException e) {
            Log.e("DatabaseManager", "SQLException caught: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    public void saveDictionaries(final Host host) {
        try {
            final Dao<Dictionary, Void> dictDao = getDao(Dictionary.class);

            // Delete all old dictionaries first
            PreparedDelete<Dictionary> statement = (PreparedDelete<Dictionary>)
              dictDao.deleteBuilder().where().eq("host_id", host).prepare();
            dictDao.delete(statement);
            getWritableDatabase().execSQL("VACUUM");

            /*
             * Save new dictionaries, but rollback if there's not enough disk
             * space. This should force the dictionaries to be refreshed on
             * every usage when the disk is full.
             */
            TransactionManager.callInTransaction(connectionSource,
                new Callable<Void>() {
                    public Void call() throws Exception {
                        Dao<Host, Integer> hostDao = getDao(Host.class);
                        for (Dictionary dict : host.getDictionaries()) {
                            if (dict.getHost() != null)
                              dictDao.create(dict);
                        }
                        host.setLastRefresh(Calendar.getInstance()
                                                    .getTime());
                        hostDao.update(host);
                        return null;
                    }
                });
        } catch (SQLException e) {
            Log.e("DatabaseManager", "SQLException caught: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    public List<Strategy> getStrategies(Host host) {
        try {
            ArrayList<Strategy> strategies = null;
            Dao<Strategy, Void> dictDao =
                    DatabaseManager.getInstance().getDao(Strategy.class);
            List<Strategy> dbstrats = dictDao.queryForEq("host_id", host);

            if (dbstrats.size() > 0) {
                strategies = new ArrayList<>();
                strategies.add(Strategy.DEFINE);
                strategies.addAll(dbstrats);
            }
            return strategies;
        }
        catch (SQLException e) {
            Log.e("DatabaseManager", "SQLException caught: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    public void saveStrategies(final Host host) {
        try {
            final Dao<Strategy, Void> stratDao = getDao(Strategy.class);

            // Delete all old dictionaries first
            PreparedDelete<Strategy> statement = (PreparedDelete<Strategy>)
                    stratDao.deleteBuilder().where().eq("host_id", host).prepare();
            stratDao.delete(statement);
            getWritableDatabase().execSQL("VACUUM");

            /*
             * Save new dictionaries, but rollback if there's not enough disk
             * space. This should force the dictionaries to be refreshed on
             * every usage when the disk is full.
             */
            TransactionManager.callInTransaction(connectionSource,
                    new Callable<Void>() {
                        public Void call() throws Exception {
                            Dao<Host, Integer> hostDao = getDao(Host.class);
                            for (Strategy strat : host.getStrategies()) {
                                if (strat.getHost() != null)
                                  stratDao.create(strat);
                            }
                            host.setLastRefresh(Calendar.getInstance()
                                    .getTime());
                            hostDao.update(host);
                            return null;
                        }
                    });
        } catch (SQLException e) {
            Log.e("DatabaseManager", "SQLException caught: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    private void loadData(Resources resources, SQLiteDatabase db,
                          ConnectionSource cs, int oldVersion, int newVersion)
          throws SQLException {
        Yaml yaml = new Yaml();
        InputStream stream = resources.openRawResource(R.raw.dicthosts);
        for (Object data : yaml.loadAll(stream)) {
            DatabaseRevision rev = (DatabaseRevision) data;
            if (rev.getVersion() > oldVersion &&
                rev.getVersion() <= newVersion)
              rev.commit(db, cs);
        }
    }
}
