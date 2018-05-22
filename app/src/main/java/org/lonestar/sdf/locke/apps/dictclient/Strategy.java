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
class Strategy {
    @DatabaseField(canBeNull = false, foreign = true)
    private Host host;
    @DatabaseField(canBeNull = false)
    private String strategy;
    @DatabaseField(canBeNull = false)
    private String description;

    private static final String DEFINE_KEY = "define";
    private static final String DEFINE_DESCRIPTION = "Define the entered word";
    private static final String MATCH_KEY = "match";
    private static final String MATCH_DESCRIPTION = "Match the entered word";

    public static final Strategy DEFINE =
      new Strategy(DEFINE_KEY, DEFINE_DESCRIPTION);
    public static final Strategy MATCH =
      new Strategy(MATCH_KEY, MATCH_DESCRIPTION);

    public Strategy() {
        this.strategy = DEFINE_KEY;
        this.description = DEFINE_DESCRIPTION;
    }

    public Strategy(String strategy, String description) {
        this.strategy = strategy;
        this.description = description;
    }

    public Strategy(Host host,
                    org.lonestar.sdf.locke.libs.dict.Strategy strategy) {
        this.host = host;
        this.strategy = strategy.getName();
        this.description = strategy.getDescription();
    }

    public Host getHost() {
        return host;
    }

    public String getStrategy() {
        return strategy;
    }

    public String getDescription() {
        return description;
    }
}
