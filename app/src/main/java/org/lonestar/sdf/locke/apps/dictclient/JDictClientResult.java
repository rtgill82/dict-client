/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import org.lonestar.sdf.locke.libs.dict.Definition;
import org.lonestar.sdf.locke.libs.dict.Match;

import java.util.List;

class JDictClientResult
{
  private final JDictClientRequest request;
  private final List<Definition> definitions;
  private final List<Dictionary> dictionaries;
  private final List<Strategy> strategies;
  private final List<Match> matches;
  private final String dictionaryInfo;

  private JDictClientResult (JDictClientRequest request, List<?> list,
                             String dictionaryInfo)
  {
    this.request = request;
    this.dictionaryInfo = dictionaryInfo;

    switch (this.request.getCommand ())
      {
      case DEFINE:
        definitions = (List<Definition>) list;
        dictionaries = null;
        strategies = null;
        matches = null;
        break;
      case MATCH:
        definitions = null;
        dictionaries = null;
        strategies = null;
        matches = (List<Match>) list;
        break;
      case DICT_LIST:
        dictionaries = (List<Dictionary>) list;
        definitions = null;
        strategies = null;
        matches = null;
        break;
      case STRAT_LIST:
        strategies = (List<Strategy>) list;
        definitions = null;
        dictionaries = null;
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

  public JDictClientResult (JDictClientRequest request, List<?> list)
  {
    this (request, list, null);
  }

  public JDictClientResult (JDictClientRequest request, String dictionaryInfo)
  {
    this (request, null, dictionaryInfo);
  }

  public JDictClientRequest getRequest ()
  {
    return request;
  }

  public List<Dictionary> getDictionaries ()
  {
    return dictionaries;
  }

  public List<Definition> getDefinitions ()
  {
    return definitions;
  }

  public List<Match> getMatches ()
  {
    return matches;
  }

  public List<Strategy> getStrategies ()
  {
    return strategies;
  }

  public String getDictionaryInfo ()
  {
    return dictionaryInfo;
  }
}
