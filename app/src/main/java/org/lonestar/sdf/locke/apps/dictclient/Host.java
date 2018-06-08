/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.lonestar.sdf.locke.libs.dict.JDictClient;

import java.util.Date;
import java.util.List;

@SuppressWarnings("unused")
@DatabaseTable(tableName = "hosts")
class Host {
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

    private List<Dictionary> dictionaries;
    private List<Strategy> strategies;

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

    public List<Dictionary> getDictionaries() {
        if (dictionaries == null)
          dictionaries = DatabaseManager.getInstance().getDictionaries(this);
        return dictionaries;
    }

    public void setDictionaries(List<Dictionary> list) {
        dictionaries = list;
    }

    public List<Strategy> getStrategies() {
        if (strategies == null)
            strategies = DatabaseManager.getInstance().getStrategies(this);
        return strategies;
    }

    public void setStrategies(List<Strategy> list) {
        strategies = list;
    }

    @Override
    public String toString() {
        if (port != JDictClient.DEFAULT_PORT)
          return name + ":" + port;
        else
          return name;
    }
}
