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
    final private String mWord;
    final private Dictionary mDictionary;
    final private Strategy mStrategy;
    final private CharSequence mText;

    public HistoryEntry(String word, Dictionary dictionary,
                        Strategy strategy, CharSequence text) {
        mWord = word;
        mDictionary = dictionary;
        mStrategy = strategy;
        mText = text;
    }

    public String getWord() {
        return mWord;
    }

    public Dictionary getDictionary() {
        return mDictionary;
    }

    public Strategy getStrategy() {
        return mStrategy;
    }

    public CharSequence getText () {
        return mText;
    }
}
