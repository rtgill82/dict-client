/*
 * Copyright (C) 2018 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "strategies")
class Strategy extends BaseModel {
    @DatabaseField(canBeNull = false, foreign = true)
    private Host host;
    @DatabaseField(canBeNull = false)
    private String name;
    @DatabaseField(canBeNull = false)
    private String description;

    public static final Strategy DEFAULT = new Strategy();

    public Strategy() {
        name = "define";
        description = "Define the entered word";
    }

    public Strategy(Host host,
                    org.lonestar.sdf.locke.libs.jdictclient.Strategy strategy) {
        this.host = host;
        this.name = strategy.getName();
        this.description = strategy.getDescription();
    }

    public Host getHost() {
        return host;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
