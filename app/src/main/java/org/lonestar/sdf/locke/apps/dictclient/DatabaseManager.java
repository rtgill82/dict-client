/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.content.res.Resources;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

class DatabaseManager extends OrmLiteSqliteOpenHelper {
    final private static String DATABASE_NAME    = "dict-client.db";
    final private static int    DATABASE_VERSION = 1;

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
        Resources resources = this.context.getResources();
        try {
            loadData(resources, db, cs, oldVersion, newVersion);
        } catch (SQLException e) {
            Log.e("DatabaseManager", "SQLException caught: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    public static Object find(Class<? extends ModelBase> clazz, Integer id) {
        try {
            Dao dao = instance.getDao(clazz);
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static CursorWrapper find(Class<? extends ModelBase> clazz, Map map) {
        CloseableIterator iterator;
        try {
            Dao dao = instance.getDao(clazz);
            QueryBuilder qb = dao.queryBuilder();
            Where where = qb.where();
            for (Object key : map.keySet()) {
                where = where.eq(key.toString(), map.get(key));
            }
            iterator = dao.iterator(where.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            Method method =
              clazz.getMethod("cursorWrapper", CloseableIterator.class);
            return (CursorWrapper) method.invoke(null, iterator);
        } catch (Exception e) {
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
