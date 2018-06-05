/*
 * Copyright (C) 2018 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.database.CursorWrapper;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

class ModelBase {
    private Dao dao;

    public ModelBase() {
        try {
            dao = DatabaseManager.getInstance().getDao(this.getClass());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static CursorWrapper cursorWrapper(CloseableIterator iterator) {
        throw new UnsupportedOperationException();
    }

    protected Dao getDao() {
        return dao;
    }

    public boolean delete() throws SQLException {
        return (dao.delete(this) == 1);
    }

    public boolean save() throws SQLException {
        Dao.CreateOrUpdateStatus status = dao.createOrUpdate(this);
        return (status.isCreated() | status.isUpdated());
    }
}
