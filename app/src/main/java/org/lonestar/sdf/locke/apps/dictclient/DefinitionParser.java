/*
 * Copyright (C) 2017 Robert Gill <rtgill82@gmail.com>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.github.rtgill82.libs.jdictclient.Definition;

final class DefinitionParser {
    public static CharSequence parse(Definition definition) {
        String string = definition.getDefinition();
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        boolean inBraces = false;
        int bracePos = 0;

        int i = 0;
        for (int n = string.length(); i < n; i++) {
            char c = string.charAt(i);

            if (c == '{') {
                if (!inBraces) {
                    stringBuilder.append(string.substring(bracePos, i));
                    bracePos = i;
                }
                inBraces = true;
            }

            if (c == '}') {
                if (inBraces) {
                    String word = string.substring(bracePos + 1, i);
                    stringBuilder.append(word);
                    stringBuilder.setSpan(
                        new WordSpan(word),
                        stringBuilder.length() - word.length(),
                        stringBuilder.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                    bracePos = i + 1;
                }
                inBraces = false;
            }
        }

        stringBuilder.append(string.substring(bracePos, i));
        return stringBuilder;
    }

    private DefinitionParser() {
        throw new RuntimeException();
    }
}
