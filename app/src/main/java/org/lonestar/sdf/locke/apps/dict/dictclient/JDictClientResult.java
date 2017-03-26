/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DictClient.
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

import org.lonestar.sdf.locke.libs.dict.Definition;
import org.lonestar.sdf.locke.libs.dict.Dictionary;

import java.util.List;

public class JDictClientResult
{
  private final JDictClientRequest request;
  private final List<Definition> definitions;
  private final List<Dictionary> dictionaries;
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
        break;
      case DICT_LIST:
        dictionaries = (List<Dictionary>) list;
        definitions = null;
        break;
      default:
        dictionaries = null;
        definitions = null;
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

  public String getDictionaryInfo ()
  {
    return dictionaryInfo;
  }
}
