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
class DefinitionHistory extends ArrayList<HistoryEntry> {
    public enum Direction { BACK, FORWARD }

    /** Instance for singleton class. */
    static private DefinitionHistory sInstance;

    /** The current position in the history list. */
    private int mCurrentPos = -1;

    /**
     * Default constructor.
     *
     */
    private DefinitionHistory() {
        super();
    }

    /**
     * Construct new DefinitionHistory from Collection.
     *
     */
    private DefinitionHistory(Collection<? extends HistoryEntry> c) {
        super(c);
        mCurrentPos = c.size() - 1;
    }

    /**
     * Initialize singleton instance with Collection.
     * Does nothing if instance has been previously initialized.
     *
     */
    @SuppressWarnings("unused")
    static public void initialize(Collection<? extends HistoryEntry> c) {
        if (sInstance == null)
          sInstance = new DefinitionHistory(c);
    }

    /**
     * Get instance of singleton class.
     *
     */
    static public DefinitionHistory getInstance() {
        if (sInstance == null)
          sInstance = new DefinitionHistory();
        return sInstance;
    }

    /**
     * Return previous Definition in history list.
     *
     */
    public HistoryEntry back() {
        HistoryEntry rv = null;
        if (mCurrentPos > 0) {
            mCurrentPos -= 1;
            rv = this.get(mCurrentPos);
        }
        return rv;
    }

    public boolean canGoBack() {
        return (mCurrentPos > 0);
    }

    /**
     * Return next Definition in history list.
     *
     */
    public HistoryEntry forward() {
        HistoryEntry rv = null;
        if (mCurrentPos < this.size() - 1) {
            mCurrentPos += 1;
            rv = this.get(mCurrentPos);
        }
        return rv;
    }

    public boolean canGoForward() {
        return (mCurrentPos < (this.size() - 1));
    }

    /**
     * Appends definition after current history position.
     *
     */
    @Override
    public boolean add(HistoryEntry entry) {
        clearToEnd();
        mCurrentPos += 1;
        return super.add(entry);
    }

    /**
     * Inserts definition at specified position in history.
     *
     */
    @Override
    public void add(int index, HistoryEntry entry) {
        if (index <= mCurrentPos)
          mCurrentPos += 1;
        super.add(index, entry);
    }

    /**
     * Appends all definitions after the current history position.
     *
     */
    @Override
    public boolean addAll(Collection<? extends HistoryEntry> c) {
        clearToEnd();
        mCurrentPos += c.size() - 1;
        return super.addAll(c);
    }

    /**
     * Insert all definitions at specified position in history.
     *
     */
    @Override
    public boolean addAll(int index, Collection<? extends HistoryEntry> c) {
        if (index <= mCurrentPos)
          mCurrentPos += c.size() - 1;
        return super.addAll(index, c);
    }

    /**
     * Clears definition history.
     *
     */
    @Override
    public void clear() {
        mCurrentPos = -1;
        super.clear();
    }

    /**
     * Removes the history entry at the specified position in the definition
     * history.
     *
     */
    @Override
    public HistoryEntry remove(int index) {
        HistoryEntry rv = super.remove(index);

        if (this.size() == 0)
          mCurrentPos = -1;
        else if (rv != null && mCurrentPos > 0 && index <= mCurrentPos)
          mCurrentPos -= 1;
        return rv;
    }

    /**
     * Removes first occurrence of the specified history entry from definition
     * history.
     *
     */
    @Override
    public boolean remove(Object o) {
        int index = this.indexOf(o);
        boolean rv = super.remove(o);

        if (rv && mCurrentPos > 0 && index <= mCurrentPos)
          mCurrentPos -= 1;
        return rv;
    }

    /**
     * Remove all history entries from this history in the specified
     * Collection.
     *
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        HistoryEntry entry = this.get(mCurrentPos);

        while (true) {
            if (c.contains(entry) && mCurrentPos > 0) {
                mCurrentPos -= 1;
                entry = this.get(mCurrentPos);
                continue;
            }
            break;
        }
        return super.removeAll(c);
    }

    /**
     * Remove all history entries specified between fromIndex and toIndex.
     *
     */
    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        if (mCurrentPos > toIndex)
          mCurrentPos = mCurrentPos - (toIndex - fromIndex);
        else if (mCurrentPos > fromIndex)
          mCurrentPos = fromIndex;
        super.removeRange(fromIndex, toIndex);
    }

    /**
     * Clears from current position in history until end of list.
     *
     */
    private void clearToEnd() {
        for (int i = this.size() - 1; i > mCurrentPos; i--)
          super.remove(i);
    }
}
