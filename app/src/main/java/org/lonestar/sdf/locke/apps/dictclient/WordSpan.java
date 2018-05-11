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
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

class WordSpan extends ClickableSpan {
    private String word;
    private String database;

    public WordSpan(String word, String database) {
        this.word = word.replace("\n", "").replaceAll("\\s+", " ");
        this.database = database;
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
            }
            context = ((ContextWrapper) context).getBaseContext();
        }

        if (activity != null) {
            DictClient app = (DictClient)
              activity.getApplication();
            EditText searchText = (EditText)
              activity.findViewById(R.id.search_text);
            Host host = app.getCurrentHost();

            if (database != null)
              setSelectedDictionary(activity, database);

            searchText.setText(word);
            searchText.selectAll();
            new JDictClientTask(activity,
                                JDictClientRequest.DEFINE(host, word))
              .execute();
        }
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(true);
    }

    private void setSelectedDictionary(MainActivity activity, String database) {
        Spinner spinner = (Spinner) activity.findViewById(R.id.dict_spinner);
        SpinnerAdapter adapter = spinner.getAdapter();

        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            Dictionary dictionary = (Dictionary) adapter.getItem(i);
            if (dictionary.getDatabase().equals(database))
              spinner.setSelection(i);
        }
    }
}