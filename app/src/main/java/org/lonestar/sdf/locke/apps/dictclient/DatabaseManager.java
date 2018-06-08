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
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;

class DatabaseManager extends OrmLiteSqliteOpenHelper {
    final private static String DATABASE_NAME    = "dict-client.db";
    final private static int    DATABASE_VERSION = 2;

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
        try {
            if (oldVersion < 2) {
                TableUtils.dropTable(cs, Dictionary.class, false);
                TableUtils.createTable(cs, Dictionary.class);
                TableUtils.dropTable(cs, Strategy.class, false);
                TableUtils.createTable(cs, Strategy.class);
            }
        } catch (SQLException e) {
            Log.e("DatabaseManager", "SQLException caught: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    public static Object find(Class<? extends BaseModel> clazz, Integer id) {
        try {
            Dao dao = instance.getDao(clazz);
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static CursorWrapper find(Class<? extends BaseModel> clazz, Map map) {
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
