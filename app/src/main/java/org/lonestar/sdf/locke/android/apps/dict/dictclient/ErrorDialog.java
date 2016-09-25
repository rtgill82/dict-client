package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ErrorDialog extends DialogFragment
{
  public static void show(Activity activity, String message)
    {
      Bundle args = new Bundle();
      args.putString("message", message);
      ErrorDialog dialog = new ErrorDialog();
      dialog.setArguments(args);
      dialog.show(
          activity.getFragmentManager(),
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
