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
    final private String word;
    final private Dictionary dictionary;
    final private Strategy strategy;
    final private CharSequence text;

    public HistoryEntry(String word, Dictionary dictionary,
                        Strategy strategy, CharSequence text) {
        this.word = word;
        this.dictionary = dictionary;
        this.strategy = strategy;
        this.text = text;
    }

    public String getWord() {
        return word;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public CharSequence getText () {
        return text;
    }
}
