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
import android.os.Bundle;

public class ErrorDialog extends DialogFragment
{
  private static final String MESSAGE = "message";

  public static void show (Activity activity, String message)
  {
    Bundle args = new Bundle ();
    args.putString (MESSAGE, message);
    ErrorDialog dialog = new ErrorDialog ();
    dialog.setArguments (args);
    dialog.show (
      activity.getFragmentManager (),
      ErrorDialog.class.getSimpleName ()
    );
  }

  @Override
  public Dialog onCreateDialog (Bundle savedInstanceState)
  {
    AlertDialog.Builder builder = new AlertDialog.Builder (getActivity ());
    String message = getArguments ().getString (MESSAGE);
    builder.setTitle (getString (R.string.title_error))
           .setMessage (message)
           .setPositiveButton (getString (R.string.button_ok), null);
    return builder.create ();
  }
}
