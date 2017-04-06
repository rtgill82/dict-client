/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

import org.lonestar.sdf.locke.libs.dict.Dictionary;

public class JDictClientRequest
{
  public enum JDictClientCommand
  {
    DEFINE,
    DICT_INFO,
    DICT_LIST
  }

  private final DictionaryHost host;
  private final JDictClientCommand command;
  private final Dictionary dictionary;
  private final String word;

  private JDictClientRequest (DictionaryHost host, JDictClientCommand command,
                              Dictionary dictionary, String word)
  {
    this.host = host;
    this.command = command;
    this.dictionary = dictionary;
    this.word = word;
  }

  public static JDictClientRequest DEFINE (DictionaryHost host, String word)
  {
    return new JDictClientRequest (host,
                                   JDictClientCommand.DEFINE,
                                   new Dictionary (null, "All Dictionaries"),
                                   word);
  }

  public static JDictClientRequest DEFINE (DictionaryHost host,
                                           Dictionary dictionary, String word)
  {
    return new JDictClientRequest (host,
                                   JDictClientCommand.DEFINE,
                                   dictionary,
                                   word);
  }

  public static JDictClientRequest DICT_LIST (DictionaryHost host)
  {
    return new JDictClientRequest (host,
                                   JDictClientCommand.DICT_LIST,
                                   null,
                                   null);
  }

  public static JDictClientRequest DICT_INFO (DictionaryHost host,
                                              Dictionary dictionary)
  {
    return new JDictClientRequest (host,
                                   JDictClientCommand.DICT_INFO,
                                   dictionary,
                                   null);
  }

  public DictionaryHost getHost ()
  {
    return host;
  }

  public JDictClientCommand getCommand ()
  {
    return command;
  }

  public Dictionary getDictionary ()
  {
    return dictionary;
  }

  public String getWord ()
  {
    return word;
  }
}
