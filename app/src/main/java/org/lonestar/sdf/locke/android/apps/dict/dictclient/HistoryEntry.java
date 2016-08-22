/*
 * Created:  Wed 17 Aug 2016 03:01:19 PM PDT
 * Modified: Mon 22 Aug 2016 01:59:13 PM PDT
 * Copyright © 2016 Robert Gill <locke@sdf.lonestar.org>
 *
 * This file is part of DictClient
 *
 */
package org.lonestar.sdf.locke.android.apps.dict.dictclient;

/**
 * Class containing the defined word and full definition text for use in
 * DefinitionHistory.
 *
 * @author Robert Gill &lt;locke@sdf.lonestar.org&gt;
 *
 */
public class HistoryEntry {
    private String _word;
    private CharSequence _definition_text;

    public HistoryEntry(String word, CharSequence definition_text)
    {
        _word = word;
        _definition_text = definition_text;
    }

    public String getWord() {
        return _word;
    }

    public CharSequence getDefinitionText() {
        return _definition_text;
    }

    public String toString()
    {
        return String.format("Definitions for %s", _word);
    }
}
