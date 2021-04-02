/*
 * Copyright (C) 2018 Robert Gill <rtgill82@gmail.com>
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
    public static final Strategy DEFAULT = new Strategy();

    @DatabaseField(canBeNull = false, foreign = true)
    private Host host;
    @DatabaseField(canBeNull = false)
    private final String name;
    @DatabaseField(canBeNull = false)
    private final String description;

    public Strategy() {
        name = "define";
        description = "Define the entered word";
    }

    public Strategy(Host host,
                    com.github.rtgill82.libs.jdictclient.Strategy strategy) {
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
