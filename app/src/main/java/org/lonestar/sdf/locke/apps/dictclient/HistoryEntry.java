/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

/**
 * Class containing the defined word and full definition text for use in
 * DefinitionHistory.
 *
 * @author Robert Gill &lt;locke@sdf.lonestar.org&gt;
 *
 */
class HistoryEntry {
    private String word;
    private String strategy;
    private CharSequence text;

    public HistoryEntry(String word, String strategy, CharSequence text) {
        this.word = word;
        this.strategy = strategy;
        this.text = text;
    }

    public String getWord() {
        return word;
    }

    public String getStrategy() {
        return strategy;
    }

    public CharSequence getText () {
        return text;
    }
}
