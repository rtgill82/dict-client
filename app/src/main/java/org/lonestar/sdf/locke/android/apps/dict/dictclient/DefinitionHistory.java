/*
 * Created:  Sun 14 Aug 2016 05:52:31 PM PDT
 * Modified: Thu 18 Aug 2016 06:39:00 PM PDT
 * Copyright © 2016 Robert Gill <locke@sdf.lonestar.org>
 *
 * This file is part of DictClient
 *
 */
package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Singleton class for maintaining and navigating a history of looked up
 * Definitions.
 *
 * @author Robert Gill &lt;locke@sdf.lonestar.org&gt;
 *
 */
public class DefinitionHistory extends ArrayList<HistoryEntry> {
    public enum Direction { BACK, FORWARD };

    /** Instance for singleton class. */
    static private DefinitionHistory instance;

    /** The current position in the history list. */
    private int _current_pos = -1;

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
        _current_pos = c.size() - 1;
    }

    /**
     * Initialize singleton instance with Collection.
     * Does nothing if instance has been previously initialized.
     *
     */
    @SuppressWarnings("unused")
    static public void initialize(Collection<? extends HistoryEntry> c) {
        if (instance == null)
          instance = new DefinitionHistory(c);
    }

    /**
     * Get instance of singleton class.
     *
     */
    static public DefinitionHistory getInstance() {
        if (instance == null)
          instance = new DefinitionHistory();

        return instance;
    }

    /**
     * Return previous Definition in history list.
     *
     */
    public HistoryEntry back() {
        HistoryEntry rv = null;
        if (_current_pos > 0) {
            _current_pos -= 1;
            rv = this.get(_current_pos);
        }
        return rv;
    }

    public boolean canGoBack() {
        return (_current_pos > 0);
    }

    /**
     * Return next Definition in history list.
     *
     */
    public HistoryEntry forward() {
        HistoryEntry rv = null;
        if (_current_pos < this.size() - 1) {
            _current_pos += 1;
            rv = this.get(_current_pos);
        }
        return rv;
    }

    public boolean canGoForward() {
        return (_current_pos < (this.size() - 1));
    }

    /**
     * Appends definition after current history position.
     *
     */
    @Override
    public boolean add(HistoryEntry entry) {
        clearToEnd();
        _current_pos += 1;
        return super.add(entry);
    }

    /**
     * Inserts definition at specified position in history.
     *
     */
    @Override
    public void add(int index, HistoryEntry entry) {
        if (index <= _current_pos)
            _current_pos += 1;
        super.add(index, entry);
    }

    /**
     * Appends all definitions after the current history position.
     *
     */
    @Override
    public boolean addAll(Collection<? extends HistoryEntry> c) {
        clearToEnd();
        _current_pos += c.size() - 1;
        return super.addAll(c);
    }

    /**
     * Insert all definitions at specified position in history.
     *
     */
    @Override
    public boolean addAll(int index, Collection<? extends HistoryEntry> c) {
        if (index <= _current_pos)
            _current_pos += c.size() - 1;
        return super.addAll(index, c);
    }

    /**
     * Clears definition history.
     *
     */
    @Override
    public void clear() {
        _current_pos = -1;
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
            _current_pos = -1;
        else if (rv != null && _current_pos > 0 && index <= _current_pos)
            _current_pos -= 1;

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

        if (rv && _current_pos > 0 && index <= _current_pos)
            _current_pos -= 1;

        return rv;
    }

    /**
     * Remove all history entries from this history in the specified
     * Collection.
     *
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        HistoryEntry entry = this.get(_current_pos);

        while (true) {
            if (c.contains(entry) && _current_pos > 0) {
                _current_pos -= 1;
                entry = this.get(_current_pos);
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
        if (_current_pos > toIndex)
            _current_pos = _current_pos - (toIndex - fromIndex);
        else if (_current_pos > fromIndex)
            _current_pos = fromIndex;

        super.removeRange(fromIndex, toIndex);
    }

    /**
     * Clears from current position in history until end of list.
     *
     */
    private void clearToEnd() {
        for (int i = this.size() - 1; i > _current_pos; i--)
            super.remove(i);
    }
}
