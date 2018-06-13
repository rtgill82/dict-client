/*
 * Copyright (C) 2018 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;
import android.content.ContextWrapper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.EditText;

class WordSpan extends ClickableSpan {
    final private String word;
    final private Dictionary dictionary;

    public WordSpan(String word, Dictionary dictionary) {
        this.word = word.replace("\n", "").replaceAll("\\s+", " ");
        this.dictionary = dictionary;
    }

    public WordSpan(String word) {
        this(word, null);
    }

    @Override
    public void onClick(View textView) {
        MainActivity activity = null;
        Context context = textView.getContext();

        while (context instanceof ContextWrapper) {
            if (context instanceof MainActivity) {
                activity = (MainActivity) context;
                break;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }

        if (activity != null) {
            DictClient app = (DictClient) activity.getApplication();
            EditText searchText = activity.findViewById(R.id.search_text);
            Host host = app.getCurrentHost();
            setSelectedDictionary(activity, dictionary);
            searchText.setText(word);
            searchText.selectAll();
            setDefineStrategy(activity);
            new ClientTask(activity,
                           ClientRequest.DEFINE(host, word, dictionary))
              .execute();
        }
    }

    @Override
    public void updateDrawState(TextPaint paint) {
        paint.setUnderlineText(true);
        super.updateDrawState(paint);
    }

    public CharSequence toCharSequence() {
        SpannableStringBuilder spannedString = new SpannableStringBuilder();
        spannedString.append(word);
        spannedString.setSpan(
          this,
          spannedString.length() - word.length(),
          spannedString.length(),
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        return spannedString;
    }

    private void setSelectedDictionary(MainActivity activity,
                                       Dictionary dictionary) {
        activity.setSelectedDictionary(dictionary);
    }

    private void setDefineStrategy(MainActivity activity) {
        activity.setSelectedStrategy(null);
    }
}
