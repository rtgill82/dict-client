/*
 * Copyright (C) 2018 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import org.lonestar.sdf.locke.libs.jdictclient.Database;
import org.lonestar.sdf.locke.libs.jdictclient.Response;
import org.lonestar.sdf.locke.libs.jdictclient.ResponseHandler;
import org.lonestar.sdf.locke.libs.jdictclient.Strategy;

import java.util.ArrayList;
import java.util.List;

class DictStratListResponseHandler implements ResponseHandler {
    final private Host host;
    final private ArrayList<Database> databases;
    final private ArrayList<Strategy> strategies;

    public DictStratListResponseHandler(Host host) {
        this.host = host;
        databases = new ArrayList<>();
        strategies = new ArrayList<>();
    }

    @Override
    public boolean handle(Response response) {
        switch (response.getStatus()) {
          case 110:
            databases.addAll((List<Database>) response.getData());
            break;

          case 111:
            strategies.addAll((List<Strategy>) response.getData());
            break;

          case 250:
            break; /* Do nothing */

          default:
            throw new RuntimeException(response.getMessage());
        }
        return true;
    }

    public Pair<?, ?> getResults() {
        return new Pair<>(
          ListConverter.convertDictionaryList(databases, host),
          ListConverter.convertStrategyList(strategies, host)
        );
    }
}
