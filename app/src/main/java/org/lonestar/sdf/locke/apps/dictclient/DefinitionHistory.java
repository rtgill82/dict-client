/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Singleton class for maintaining and navigating a history of looked up
 * Definitions.
 *
 * @author Robert Gill &lt;locke@sdf.lonestar.org&gt;
 *
 */
class DefinitionHistory extends ArrayList<HistoryEntry>
{
  public enum Direction { BACK, FORWARD };

  /** Instance for singleton class. */
  static private DefinitionHistory instance;

  /** The current position in the history list. */
  private int currentPos = -1;

  /**
   * Default constructor.
   *
   */
  private DefinitionHistory ()
  {
    super ();
  }

  /**
   * Construct new DefinitionHistory from Collection.
   *
   */
  private DefinitionHistory (Collection<? extends HistoryEntry> c)
  {
    super (c);
    currentPos = c.size () - 1;
  }

  /**
   * Initialize singleton instance with Collection.
   * Does nothing if instance has been previously initialized.
   *
   */
  @SuppressWarnings("unused")
  static public void initialize (Collection<? extends HistoryEntry> c)
  {
    if (instance == null)
      instance = new DefinitionHistory (c);
  }

  /**
   * Get instance of singleton class.
   *
   */
  static public DefinitionHistory getInstance ()
  {
    if (instance == null)
      instance = new DefinitionHistory ();

    return instance;
  }

  /**
   * Return previous Definition in history list.
   *
   */
  public HistoryEntry back ()
  {
    HistoryEntry rv = null;
    if (currentPos > 0)
      {
        currentPos -= 1;
        rv = this.get (currentPos);
      }
    return rv;
  }

  public boolean canGoBack ()
  {
    return (currentPos > 0);
  }

  /**
   * Return next Definition in history list.
   *
   */
  public HistoryEntry forward ()
  {
    HistoryEntry rv = null;
    if (currentPos < this.size () - 1)
      {
        currentPos += 1;
        rv = this.get (currentPos);
      }
    return rv;
  }

  public boolean canGoForward ()
  {
    return (currentPos < (this.size () - 1));
  }

  /**
   * Appends definition after current history position.
   *
   */
  @Override
  public boolean add (HistoryEntry entry)
  {
    clearToEnd ();
    currentPos += 1;
    return super.add (entry);
  }

  /**
   * Inserts definition at specified position in history.
   *
   */
  @Override
  public void add (int index, HistoryEntry entry)
  {
    if (index <= currentPos)
      currentPos += 1;
    super.add (index, entry);
  }

  /**
   * Appends all definitions after the current history position.
   *
   */
  @Override
  public boolean addAll (Collection<? extends HistoryEntry> c)
  {
    clearToEnd ();
    currentPos += c.size () - 1;
    return super.addAll (c);
  }

  /**
   * Insert all definitions at specified position in history.
   *
   */
  @Override
  public boolean addAll (int index, Collection<? extends HistoryEntry> c)
  {
    if (index <= currentPos)
      currentPos += c.size () - 1;
    return super.addAll (index, c);
  }

  /**
   * Clears definition history.
   *
   */
  @Override
  public void clear ()
  {
    currentPos = -1;
    super.clear ();
  }

  /**
   * Removes the history entry at the specified position in the definition
   * history.
   *
   */
  @Override
  public HistoryEntry remove (int index)
  {
    HistoryEntry rv = super.remove (index);

    if (this.size () == 0)
      currentPos = -1;
    else if (rv != null && currentPos > 0 && index <= currentPos)
      currentPos -= 1;

    return rv;
  }

  /**
   * Removes first occurrence of the specified history entry from definition
   * history.
   *
   */
  @Override
  public boolean remove (Object o)
  {
    int index = this.indexOf (o);
    boolean rv = super.remove (o);

    if (rv && currentPos > 0 && index <= currentPos)
      currentPos -= 1;

    return rv;
  }

  /**
   * Remove all history entries from this history in the specified
   * Collection.
   *
   */
  @Override
  public boolean removeAll (Collection<?> c)
  {
    HistoryEntry entry = this.get (currentPos);

    while (true)
      {
        if (c.contains (entry) && currentPos > 0)
          {
            currentPos -= 1;
            entry = this.get (currentPos);
            continue;
          }

        break;
      }

    return super.removeAll (c);
  }

  /**
   * Remove all history entries specified between fromIndex and toIndex.
   *
   */
  @Override
  protected void removeRange (int fromIndex, int toIndex)
  {
    if (currentPos > toIndex)
      currentPos = currentPos - (toIndex - fromIndex);
    else if (currentPos > fromIndex)
      currentPos = fromIndex;

    super.removeRange (fromIndex, toIndex);
  }

  /**
   * Clears from current position in history until end of list.
   *
   */
  private void clearToEnd ()
  {
    for (int i = this.size () - 1; i > currentPos; i--)
      super.remove (i);
  }
}
