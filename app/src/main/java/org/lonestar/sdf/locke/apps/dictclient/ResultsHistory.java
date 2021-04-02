/*
 * Copyright (C) 2017 Robert Gill <rtgill82@gmail.com>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Singleton class for maintaining and navigating a history of search Results.
 *
 * @author Robert Gill &lt;locke@sdf.lonestar.org&gt;
 *
 */
class ResultsHistory extends ArrayList<Results> {
    public enum Direction { BACK, FORWARD }

    /** Instance for singleton class. */
    static private ResultsHistory sInstance;

    /** The current position in the history list. */
    private int mCurrentPos = -1;

    /**
     * Default constructor.
     *
     */
    private ResultsHistory() {
        super();
    }

    /**
     * Construct new ResultsHistory from Collection.
     *
     */
    private ResultsHistory(Collection<? extends Results> c) {
        super(c);
        mCurrentPos = c.size() - 1;
    }

    /**
     * Initialize singleton instance with Collection.
     * Does nothing if instance has been previously initialized.
     *
     */
    @SuppressWarnings("unused")
    static public void initialize(Collection<? extends Results> c) {
        if (sInstance == null)
          sInstance = new ResultsHistory(c);
    }

    /**
     * Get instance of singleton class.
     *
     */
    static public ResultsHistory getInstance() {
        if (sInstance == null)
          sInstance = new ResultsHistory();
        return sInstance;
    }

    /**
     * Return previous Results in history list.
     *
     */
    public Results back() {
        Results rv = null;
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
     * Return next Results in history list.
     *
     */
    public Results forward() {
        Results rv = null;
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
     * Appends Results entry after current history position.
     *
     */
    @Override
    public boolean add(Results entry) {
        clearToEnd();
        mCurrentPos += 1;
        return super.add(entry);
    }

    /**
     * Inserts Results entry at specified position in history.
     *
     */
    @Override
    public void add(int index, Results entry) {
        if (index <= mCurrentPos)
          mCurrentPos += 1;
        super.add(index, entry);
    }

    /**
     * Appends all Results after the current history position.
     *
     */
    @Override
    public boolean addAll(Collection<? extends Results> c) {
        clearToEnd();
        mCurrentPos += c.size() - 1;
        return super.addAll(c);
    }

    /**
     * Insert all Results at specified position in history.
     *
     */
    @Override
    public boolean addAll(int index, Collection<? extends Results> c) {
        if (index <= mCurrentPos)
          mCurrentPos += c.size() - 1;
        return super.addAll(index, c);
    }

    /**
     * Clears Results history.
     *
     */
    @Override
    public void clear() {
        mCurrentPos = -1;
        super.clear();
    }

    /**
     * Removes the Results entry at the specified position in the definition
     * history.
     *
     */
    @Override
    public Results remove(int index) {
        Results rv = super.remove(index);

        if (this.size() == 0)
          mCurrentPos = -1;
        else if (rv != null && mCurrentPos > 0 && index <= mCurrentPos)
          mCurrentPos -= 1;
        return rv;
    }

    /**
     * Removes first occurrence of the specified Results entry from definition
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
     * Remove all Results entries from this history in the specified
     * Collection.
     *
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        Results entry = this.get(mCurrentPos);

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
     * Remove all Results entries specified between fromIndex and toIndex.
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
        if (mCurrentPos >= 0)
          subList(mCurrentPos + 1, size()).clear();
    }
}
