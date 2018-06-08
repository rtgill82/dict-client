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

@SuppressWarnings("unused")
@DatabaseTable(tableName = "dictionaries")
class Dictionary extends BaseModel {
    @DatabaseField(canBeNull = false, foreign = true)
    private Host host;
    @DatabaseField(canBeNull = false)
    private String database;
    @DatabaseField(canBeNull = false)
    private String description;

    public static final Dictionary ALL_DICTIONARIES = new Dictionary();

    public Dictionary() {
        description = "All Dictionaries";
    }

    public Dictionary(
        Host host,
        org.lonestar.sdf.locke.libs.jdictclient.Database database
    ) {
        this.host = host;
        this.database = database.getName();
        this.description = database.getDescription();
    }

    public Host getHost() {
        return host;
    }

    public String getDatabase() {
        return database;
    }

    public String getDescription() {
        return description;
    }
}
