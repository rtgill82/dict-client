/*
 * Copyright (C) 2018 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import com.github.xelkarin.libs.jdictclient.Database;
import com.github.xelkarin.libs.jdictclient.Response;
import com.github.xelkarin.libs.jdictclient.ResponseHandler;
import com.github.xelkarin.libs.jdictclient.Strategy;

import java.util.ArrayList;
import java.util.List;

class DictStrategyListResponseHandler implements ResponseHandler {
    final private Host mHost;
    final private ArrayList<Database> mDatabases;
    final private ArrayList<Strategy> mStrategies;

    public DictStrategyListResponseHandler(Host host) {
        mHost = host;
        mDatabases = new ArrayList<>();
        mStrategies = new ArrayList<>();
    }

    @Override
    public boolean handle(Response response) {
        switch (response.getStatus()) {
          case 110:
            mDatabases.addAll((List<Database>) response.getData());
            break;

          case 111:
            mStrategies.addAll((List<Strategy>) response.getData());
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
          ListConverter.convertDictionaryList(mDatabases, mHost),
          ListConverter.convertStrategyList(mStrategies, mHost)
        );
    }
}
