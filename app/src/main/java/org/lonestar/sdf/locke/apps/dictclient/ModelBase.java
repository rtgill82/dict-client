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
import com.j256.ormlite.misc.BaseDaoEnabled;

import java.sql.SQLException;

class ModelBase extends BaseDaoEnabled {
    public static CursorWrapper cursorWrapper(CloseableIterator iterator) {
        throw new UnsupportedOperationException();
    }

    public boolean save() throws SQLException {
        Dao.CreateOrUpdateStatus status = dao.createOrUpdate(this);
        return (status.isCreated() | status.isUpdated());
    }
}
