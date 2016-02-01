package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import org.lonestar.sdf.locke.libs.dict.Definition;

public final class DefinitionParser {

    private static class WordSpan extends ClickableSpan {
        private String word;

        public WordSpan(String word) {
            this.word = word.replace("\n", "").replaceAll("\\s+", " ");
        }

        @Override
        public void onClick(View textView) {
            Activity activity = null;
            Context context = textView.getContext();

            while (context instanceof ContextWrapper) {
                if (context instanceof Activity) {
                    activity = (Activity) context;
                }
                context = ((ContextWrapper) context).getBaseContext();
            }

            if (activity != null)
                new JDictClientTask(activity, JDictClientRequest.DEFINE(word)).execute();
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(true);
        }
    }

    public static CharSequence parse(Definition definition) {
        String def_string = definition.getDefinition();
        SpannableStringBuilder spanned_string = new SpannableStringBuilder();
        boolean in_braces = false;
        int brace_pos = 0;

        int i = 0;
        for (int n = def_string.length(); i < n; i++) {
            char c = def_string.charAt(i);

            if (c == '{') {
                if (in_braces != true) {
                    spanned_string.append(def_string.substring(brace_pos, i));
                    brace_pos = i;
                }
                in_braces = true;
            }

            if (c == '}') {
                if (in_braces == true) {
                    String word = def_string.substring(brace_pos + 1, i);
                    spanned_string.append(word);
                    spanned_string.setSpan(
                            new WordSpan(word),
                            spanned_string.length() - word.length(),
                            spanned_string.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                    brace_pos = i + 1;
                }

                in_braces = false;
            }
        }

        spanned_string.append(def_string.substring(brace_pos, i));
        return spanned_string;
    }

    private DefinitionParser() {
        throw new RuntimeException();
    }
}
