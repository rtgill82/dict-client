/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import org.lonestar.sdf.locke.libs.jdictclient.Definition;
import org.lonestar.sdf.locke.libs.jdictclient.Match;

import java.util.List;

class JDictClientResult {
    private final JDictClientRequest request;
    private final List<Definition> definitions;
    private final List<Dictionary> dictionaries;
    private final List<Strategy> strategies;
    private final List<Match> matches;
    private final String dictionaryInfo;

    private JDictClientResult(JDictClientRequest request, List<?> list1,
                              List<?> list2, String dictionaryInfo) {
        this.request = request;
        this.dictionaryInfo = dictionaryInfo;

        switch (this.request.getCommand()) {
          case DEFINE:
            definitions = (List<Definition>) list1;
            dictionaries = null;
            strategies = null;
            matches = null;
            break;
          case MATCH:
            definitions = null;
            dictionaries = null;
            strategies = null;
            matches = (List<Match>) list1;
            break;
          case DICT_LIST:
            dictionaries = (List<Dictionary>) list1;
            definitions = null;
            strategies = (List<Strategy>) list2;
            matches = null;
            break;
          default:
            definitions = null;
            dictionaries = null;
            strategies = null;
            matches = null;
            break;
        }
    }

    public JDictClientResult(JDictClientRequest request, List<?> list) {
        this(request, list, null, null);
    }

    public JDictClientResult(JDictClientRequest request,
                             String dictionaryInfo) {
        this(request, null, null, dictionaryInfo);
    }

    public JDictClientResult(JDictClientRequest request,
                             List<Dictionary> dictionaries,
                             List<Strategy> strategies) {
        this(request, dictionaries, strategies, null);
    }

    public JDictClientRequest getRequest() {
        return request;
    }

    public List<Dictionary> getDictionaries() {
        return dictionaries;
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public List<Strategy> getStrategies() {
        return strategies;
    }

    public String getDictionaryInfo() {
        return dictionaryInfo;
    }
}
