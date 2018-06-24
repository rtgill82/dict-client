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
    final private String mWord;
    final private Dictionary mDictionary;

    public WordSpan(String word, Dictionary dictionary) {
        mWord = word.replace("\n", "").replaceAll("\\s+", " ");
        mDictionary = dictionary;
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
            Host host = app.getCurrentHost();
            new ClientTask(activity,
                           ClientTask.DEFINE(host, mWord, mDictionary),
                           activity.getOnTaskFinishedHandler(true))
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
        spannedString.append(mWord);
        spannedString.setSpan(
          this,
          spannedString.length() - mWord.length(),
          spannedString.length(),
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        return spannedString;
    }
}
