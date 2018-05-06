/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import java.util.ArrayList;
import java.util.List;

/**
 * Convert JDictClient Dictionaries into Dictionaries internally used by
 * Dict-Client Application.
 */
class ClassConvert {
    public static List<Dictionary> convertDictionaryList(
        List<org.lonestar.sdf.locke.libs.dict.Dictionary> dictionaries,
        Host host
    ) {
        ArrayList<Dictionary> list = new ArrayList<Dictionary>();
        for (org.lonestar.sdf.locke.libs.dict.Dictionary dict : dictionaries) {
            list.add(new Dictionary(host, dict));
        }
        return list;
    }

    public static List<Strategy> convertStrategyList(
        List<org.lonestar.sdf.locke.libs.dict.Strategy> strategies,
        Host host
    ) {
        ArrayList<Strategy> list = new ArrayList<Strategy>();
        for (org.lonestar.sdf.locke.libs.dict.Strategy strat : strategies) {
            list.add(new Strategy(host, strat));
        }
        return list;
    }
}
