/*
 * Copyright (C) 2019 Robert Gill <rtgill82@gmail.com>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.text.Html;
import android.text.SpannableStringBuilder;

import com.github.rtgill82.libs.jdictclient.Match;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MatchResults extends Results {
    public MatchResults(String word, Dictionary dictionary,
                        Strategy strategy, List<Match> matches) {
        super(word, dictionary, strategy, formatMatches(matches));
    }

    private static CharSequence formatMatches(List<Match> matches) {
        Context context = DictClient.getContext();
        if (matches == null) {
            return context.getString(R.string.result_matches);
        }
        Map<Dictionary, List<String>> map = buildMatchMap(matches);
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        for (Dictionary dictionary : map.keySet()) {
            List<String> list = map.get(dictionary);
            stringBuilder.append(Html.fromHtml(
                "<b>" + dictionary.getDescription() + "</b><br>"
            ));
            int i = 0; int count = list.size();
            for (String word : list) {
                stringBuilder.append(new WordSpan(word, dictionary)
                             .toCharSequence());
                if (i != count - 1) stringBuilder.append(", ");
                i += 1;
            }
            stringBuilder.append(Html.fromHtml("<br><br>"));
        }
        return stringBuilder;
    }

    private static Map<Dictionary, List<String>>
    buildMatchMap(List<Match> matches) {
        HashMap<Dictionary, List<String>> map = new HashMap<>();
        Collection<Dictionary> dictionaries =
          DictClient.getCurrentHost().getDictionaries();
        for (Dictionary dictionary : dictionaries) {
            if (dictionary == Dictionary.DEFAULT) continue;
            ArrayList<String> list = new ArrayList<>();
            map.put(dictionary, list);
            for (Match match : matches) {
                if (dictionary.getName().equals(match.getDatabase())) {
                    list.add(match.getWord());
                }
            }
            if (list.size() == 0) {
                map.remove(dictionary);
            }
        }
        return map;
    }
}
