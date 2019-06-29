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
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.SQLException;

class DatabaseManager extends OrmLiteSqliteOpenHelper {
    final private static String DATABASE_NAME    = "dict-client.db";
    final private static int    DATABASE_VERSION = 3;

    private static DatabaseManager sInstance = null;
    final private Resources mResources;

    private DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mResources = context.getResources();
    }

    static public void initialize(Context context) {
        if (sInstance == null)
          sInstance = new DatabaseManager(context);
    }

    static public DatabaseManager getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource cs) {
        try {
            TableUtils.createTable(cs, Host.class);
            TableUtils.createTable(cs, Dictionary.class);
            TableUtils.createTable(cs, Strategy.class);
            loadData(mResources, db, cs, 0, DATABASE_VERSION);
        } catch (SQLException e) {
            Log.e("DatabaseManager", "SQLException caught: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource cs,
                          int oldVersion, int newVersion) {
    }

    public static Object find(Class<? extends BaseModel> clazz, Integer id) {
        try {
            Dao dao = sInstance.getDao(clazz);
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static CursorWrapper find(Class<? extends BaseModel> clazz,
                                     PreparedQuery query) {
        CloseableIterator iterator;
        try {
            Dao dao = sInstance.getDao(clazz);
            iterator = dao.iterator(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return wrapCursor(clazz, iterator);
    }

    public static CursorWrapper find(Class<? extends BaseModel> clazz,
                                     String fieldName, Object value) {
        CloseableIterator iterator;
        try {
            QueryBuilder qb = sInstance.getDao(clazz).queryBuilder();
            PreparedQuery query = qb.where().eq(fieldName, value).prepare();
            iterator = sInstance.getDao(clazz).iterator(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return wrapCursor(clazz, iterator);
    }

    private static CursorWrapper wrapCursor(Class clazz,
                                            CloseableIterator iterator) {
        try {
            Method method = clazz.getMethod("cursorWrapper",
                                            CloseableIterator.class);
            return (CursorWrapper) method.invoke(null, iterator);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("SameParameterValue")
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
