/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;

class DatabaseRevision {
    private Integer version;
    private List<Host> add_hosts;
    private List<Host> remove_hosts;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setAddHosts(List<Host> hosts) {
        add_hosts = hosts;
    }

    public void setRemoveHosts(List<Host> hosts) {
        remove_hosts = hosts;
    }

    public void commit(SQLiteDatabase db, ConnectionSource cs)
          throws SQLException {
        if (db.getVersion() < version) {
            db.setVersion(version);
            Dao<Host, Integer> hostDao = DaoManager.createDao(cs, Host.class);
            Dao<Dictionary, Void> dictDao =
              DaoManager.createDao(cs, Dictionary.class);

            // Delete old hosts
            if (remove_hosts != null) {
                for (Host host : remove_hosts) {
                    List<Host> rows = hostDao.queryForMatching(host);
                    for (Host row : rows) {
                        dictDao.deleteBuilder().where().eq("host_id", row);
                        hostDao.delete(row);
                    }
                }
            }

            // Add new hosts
            if (add_hosts != null) {
                for (Host host : add_hosts) {
                    hostDao.create(host);
                }
            }
        }
    }
}
