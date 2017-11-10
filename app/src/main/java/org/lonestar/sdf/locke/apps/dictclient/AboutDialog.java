/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class AboutDialog extends DialogFragment
{
  private String html;

  public static void show (Activity activity)
  {
    new AboutDialog ().show (activity.getFragmentManager (),
                             activity.getString (R.string.title_about));
  }

  @Override
  public Dialog onCreateDialog (Bundle savedInstanceState)
  {
    Context context = getActivity ();
    if (html == null)
      {
        Resources resources = context.getResources ();
        InputStream stream = resources.openRawResource (R.raw.about);

        try
          {
            byte[] buffer = new byte[stream.available ()];
            stream.read (buffer);
            html = new String (buffer);
          }
        catch (IOException e)
          {
            this.dismiss ();
            ErrorDialog.show (this.getActivity (),
                              "Unable to read file about.html: "
                              + e.getMessage ());
          }
      }

    LayoutInflater inflater = getActivity ().getLayoutInflater ();
    TextView textView = (TextView) inflater.inflate (R.layout.dialog_about,
                                                     null);
    textView.setText (Html.fromHtml (html));
    textView.setMovementMethod (LinkMovementMethod.getInstance ());
    AlertDialog.Builder builder = new AlertDialog.Builder (context);
    builder.setTitle (context.getString (R.string.title_about))
           .setView (textView)
           .setNeutralButton (getString (R.string.button_donate),
             new DialogInterface.OnClickListener ()
             {
               public void onClick (DialogInterface dialog, int which)
               {
                 DonateDialog.show (getActivity ());
               }
             })
           .setPositiveButton (getString (R.string.button_ok), null);
    return builder.create ();
  }
}
