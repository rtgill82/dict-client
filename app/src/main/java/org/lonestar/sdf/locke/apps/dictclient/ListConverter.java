/*
 * Copyright (C) 2017 Robert Gill <rtgill82@gmail.com>
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
class ListConverter {
    public static List<Dictionary> convertDictionaryList(
        List<com.github.rtgill82.libs.jdictclient.Database> databases,
        Host host
    ) {
        ArrayList<Dictionary> list = new ArrayList<>();
        for (com.github.rtgill82.libs.jdictclient.Database database : databases) {
            list.add(new Dictionary(host, database));
        }
        return list;
    }

    public static List<Strategy> convertStrategyList(
        List<com.github.rtgill82.libs.jdictclient.Strategy> strategies,
        Host host
    ) {
        ArrayList<Strategy> list = new ArrayList<>();
        for (com.github.rtgill82.libs.jdictclient.Strategy strategy : strategies) {
            list.add(new Strategy(host, strategy));
        }
        return list;
    }
}
