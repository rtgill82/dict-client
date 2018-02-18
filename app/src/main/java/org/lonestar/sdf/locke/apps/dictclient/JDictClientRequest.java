/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

class JDictClientRequest
{
  public enum JDictClientCommand
  {
    DEFINE,
    MATCH,
    DICT_INFO,
    DICT_LIST,
    STRAT_LIST
  }

  private final Host host;
  private final JDictClientCommand command;
  private final Dictionary dictionary;
  private final Strategy strategy;
  private final String word;

  boolean displayWaitMessage = true;

  private JDictClientRequest (Host host, JDictClientCommand command,
                              Dictionary dictionary, Strategy strategy,
                              String word)
  {
    this.host = host;
    this.command = command;
    this.dictionary = dictionary;
    this.strategy = strategy;
    this.word = word;
  }

  private JDictClientRequest (Host host, JDictClientCommand command,
                              Dictionary dictionary, String word)
  {
    this (host, command, dictionary, null, word);
  }

  private JDictClientRequest (Host host, JDictClientCommand command,
                              Strategy strategy, String word)
  {
    this (host, command, null, strategy, word);
  }

  public static JDictClientRequest DEFINE (Host host, String word)
  {
    return new JDictClientRequest (host, JDictClientCommand.DEFINE,
                                   new Dictionary (), word);
  }

  public static JDictClientRequest MATCH (Host host, Strategy strategy,
                                          String word)
  {
    return new JDictClientRequest (host, JDictClientCommand.MATCH,
                                   strategy, word);
  }

  public static JDictClientRequest DEFINE (Host host, Dictionary dictionary,
                                           String word)
  {
    return new JDictClientRequest (host, JDictClientCommand.DEFINE,
                                   dictionary, word);
  }

  public static JDictClientRequest DICT_LIST (Host host)
  {
    return new JDictClientRequest (host, JDictClientCommand.DICT_LIST,
                                   null, null, null);
  }

  public static JDictClientRequest DICT_INFO (Host host, Dictionary dictionary)
  {
    return new JDictClientRequest (host, JDictClientCommand.DICT_INFO,
                                   dictionary, null);
  }

  public static JDictClientRequest STRAT_LIST (Host host)
  {
    JDictClientRequest request =
        new JDictClientRequest (host, JDictClientCommand.STRAT_LIST,
            null, null, null);
    request.displayWaitMessage(false);
    return request;
  }

  public void displayWaitMessage(boolean value)
  {
    this.displayWaitMessage = value;
  }

  public boolean displayWaitMessage()
  {
    return this.displayWaitMessage;
  }

  public Host getHost ()
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

  public Strategy getStrategy ()
  {
    return strategy;
  }

  public String getWord ()
  {
    return word;
  }
}
