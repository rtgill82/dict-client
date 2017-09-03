/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
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

import org.lonestar.sdf.locke.libs.dict.Definition;

final class DefinitionParser
{
  private static class WordSpan extends ClickableSpan
  {
    private String word;

    public WordSpan (String word)
    {
      this.word = word.replace ("\n", "").replaceAll ("\\s+", " ");
    }

    @Override
    public void onClick (View textView)
    {
      MainActivity activity = null;
      Context context = textView.getContext ();

      while (context instanceof ContextWrapper)
        {
          if (context instanceof MainActivity)
            {
              activity = (MainActivity) context;
            }
          context = ((ContextWrapper) context).getBaseContext ();
        }

      if (activity != null)
        {
          DictClient app = (DictClient)
            activity.getApplication ();
          EditText searchText = (EditText)
            activity.findViewById (R.id.search_text);
          Host host = app.getCurrentHost ();

          searchText.setText (word);
          searchText.selectAll ();
          new JDictClientTask (activity,
                               JDictClientRequest.DEFINE (host, word))
            .execute ();
        }
    }

    @Override
    public void updateDrawState (TextPaint ds)
    {
      super.updateDrawState (ds);
      ds.setUnderlineText (true);
    }
  }

  public static CharSequence parse (Definition definition)
  {
    String defString = definition.getDefinition ();
    SpannableStringBuilder spannedString = new SpannableStringBuilder ();
    boolean inBraces = false;
    int bracePos = 0;

    int i = 0;
    for (int n = defString.length (); i < n; i++)
      {
        char c = defString.charAt (i);

        if (c == '{')
          {
            if (inBraces != true)
              {
                spannedString.append (defString.substring (bracePos, i));
                bracePos = i;
              }
            inBraces = true;
          }

        if (c == '}')
          {
            if (inBraces == true)
              {
                String word = defString.substring (bracePos + 1, i);
                spannedString.append (word);
                spannedString.setSpan (
                  new WordSpan (word),
                  spannedString.length () - word.length (),
                  spannedString.length (),
                  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                bracePos = i + 1;
              }

            inBraces = false;
          }
      }

    spannedString.append (defString.substring (bracePos, i));
    return spannedString;
  }

  private DefinitionParser ()
  {
    throw new RuntimeException ();
  }
}
