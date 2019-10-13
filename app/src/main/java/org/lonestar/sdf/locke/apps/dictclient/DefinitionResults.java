/*
 * Copyright (C) 2019 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.text.Html;
import android.text.SpannableStringBuilder;

import com.github.xelkarin.libs.jdictclient.Definition;

import java.util.List;

class DefinitionResults extends Results {
    public DefinitionResults(String word, Dictionary dictionary,
                             Strategy strategy,
                             List<Definition> definitions) {
        super(word, dictionary, strategy, formatDefinitions(definitions));
    }

    private static CharSequence
    formatDefinitions(List<Definition> definitions) {
        Context context = DictClient.getContext();
        if (definitions == null) {
            return context.getString(R.string.result_definitions);
        }
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        for (Definition definition : definitions) {
            stringBuilder.append(Html.fromHtml(
                "<b>" + definition.getDatabase().getDescription() + "</b><br>"
            ));
            stringBuilder.append(DefinitionParser.parse(definition));
            stringBuilder.append("\n");
        }
        return stringBuilder;
    }
}
