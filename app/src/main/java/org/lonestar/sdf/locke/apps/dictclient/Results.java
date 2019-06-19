/*
 * Copyright (C) 2019 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

class Results {
    final private String mWord;
    final private Dictionary mDictionary;
    final private Strategy mStrategy;
    final private CharSequence mText;

    public Results() {
        this(null, null, null, "");
    }

    public Results(String word, Dictionary dictionary,
                   Strategy strategy, CharSequence text) {
        mWord = word;
        mDictionary = dictionary;
        mStrategy = strategy;
        mText = text;
    }

    public boolean defaultStrategy() {
        return mStrategy == null || mStrategy == Strategy.DEFAULT;

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
