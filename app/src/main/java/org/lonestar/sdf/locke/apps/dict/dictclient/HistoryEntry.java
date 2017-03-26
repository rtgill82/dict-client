/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DictClient.
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

/**
 * Class containing the defined word and full definition text for use in
 * DefinitionHistory.
 *
 * @author Robert Gill &lt;locke@sdf.lonestar.org&gt;
 *
 */
public class HistoryEntry
{
  private String word;
  private CharSequence definitionText;

  public HistoryEntry (String word, CharSequence definitionText)
  {
    this.word = word;
    this.definitionText = definitionText;
  }

  public String getWord ()
  {
    return word;
  }

  public CharSequence getDefinitionText ()
  {
    return definitionText;
  }

  public String toString ()
  {
    return String.format ("Definitions for %s", word);
  }
}
