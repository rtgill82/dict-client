/*
 * Copyright (C) 2016 Robert Gill <locke@sdf.lonestar.org>
 *
 * This file is part of DictClient
 *
 */

package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

public class ErrorDialog extends DialogFragment
{
  public static void show(Activity activity, String message)
  {
    Bundle args = new Bundle();
    args.putString("message", message);
    ErrorDialog dialog = new ErrorDialog();
    dialog.setArguments(args);
    dialog.show(
      ((FragmentActivity) activity).getSupportFragmentManager(),
      ErrorDialog.class.getSimpleName()
    );
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    String message = getArguments().getString("message");
    builder.setTitle("Error")
      .setMessage(message)
      .setPositiveButton("Ok", null);
    return builder.create();
  }
}
