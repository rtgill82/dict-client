/*
 * Copyright (C) 2017 Robert Gill <rtgill82@gmail.com>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.database.CursorWrapper;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.table.DatabaseTable;

import com.github.rtgill82.libs.jdictclient.JDictClient;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
@DatabaseTable(tableName = "hosts")
class Host extends BaseModel {
    @DatabaseField(generatedId = true, columnName = "_id")
    private Integer id;
    @DatabaseField(canBeNull = false, uniqueIndexName = "name_port_idx")
    private String name;
    @DatabaseField(canBeNull = false, uniqueIndexName = "name_port_idx", defaultValue = "2628")
    private Integer port;
    @DatabaseField()
    private String description;
    @DatabaseField(canBeNull = false)
    private Date last_refresh;
    @DatabaseField(defaultValue = "false")
    private boolean readonly;
    @DatabaseField(defaultValue = "false")
    private boolean user_defined;
    @DatabaseField(defaultValue = "false")
    private boolean hidden;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<Dictionary> dictionaries;
    @ForeignCollectionField(eager = true)
    private ForeignCollection<Strategy> strategies;

    public Host() {
        this(null, null, JDictClient.DEFAULT_PORT);
    }

    public Host(String name) {
        this(null, name, JDictClient.DEFAULT_PORT);
    }

    public Host(String name, Integer port) {
        this(null, name, port);
    }

    public Host(Integer id, String name, Integer port) {
        super();
        this.id = id;
        this.name = name;
        this.port = port;
        this.last_refresh = new Date();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastRefresh() {
        return last_refresh;
    }

    public void setLastRefresh(Date date) {
        last_refresh = date;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isUserDefined() {
        return user_defined;
    }

    public void setUserDefined(boolean userDefined) {
        this.user_defined = userDefined;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public Collection<Dictionary> getDictionaries() {
        return dictionaries;
    }

    public void setDictionaries(Collection<Dictionary> list) {
        Dao dao = dictionaries.getDao();
        DeleteBuilder deleteBuilder = dao.deleteBuilder();
        try {
            deleteBuilder.where().eq("host_id", getId());
            dao.delete(deleteBuilder.prepare());
            dictionaries.refreshCollection();
            dictionaries.addAll(list);
            setLastRefresh(Calendar.getInstance().getTime());
            update();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<Strategy> getStrategies() {
        return strategies;
    }

    public void setStrategies(Collection<Strategy> list) {
        Dao dao = strategies.getDao();
        DeleteBuilder deleteBuilder = dao.deleteBuilder();
        try {
            deleteBuilder.where().eq("host_id", getId());
            dao.delete(deleteBuilder.prepare());
            strategies.refreshCollection();
            strategies.addAll(list);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static CursorWrapper cursorWrapper(CloseableIterator iterator) {
        AndroidDatabaseResults results =
          (AndroidDatabaseResults) iterator.getRawResults();
        return new HostCursor(results.getRawCursor());
    }

    @Override
    public int delete() throws SQLException {
        if (isReadonly()) {
            return 0;
        } else if (!isUserDefined()) {
            setHidden(true);
            return update();
        } else {
            return super.delete();
        }
    }

    @Override
    public int create() throws SQLException {
        if (getId() == null) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", getName());
            map.put("port", getPort());
            if (!getDao().queryForFieldValues(map).isEmpty()) {
                throw new SQLException("The host " + toString() +
                                       " already exists.");
            }
        }
        return super.create();
    }

    @Override
    public String toString() {
        if (port != JDictClient.DEFAULT_PORT)
          return name + ":" + port;
        else
          return name;
    }
}
