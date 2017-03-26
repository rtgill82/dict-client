/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DictClient.
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.sql.SQLException;

public class EditDictionaryHostDialog extends DialogFragment
{
  private DictionaryHost host;
  private EditText editHostName;
  private EditText editPort;
  private EditText editDescription;

  public static void show (Activity activity)
  {
    EditDictionaryHostDialog.show (activity, null);
  }

  public static void show (Activity activity, DictionaryHost host)
  {
    EditDictionaryHostDialog dialog = new EditDictionaryHostDialog ();
    dialog.setDictionaryHost (host);
    dialog.show (activity.getFragmentManager (),
                 activity.getString (R.string.dialog_edit_tag));
  }

  @Override
  public Dialog onCreateDialog (Bundle savedInstanceState)
  {
    LayoutInflater inflater = getActivity ().getLayoutInflater ();
    RelativeLayout layout = (RelativeLayout)
                        inflater.inflate (R.layout.dialog_edit_dictionary_host,
                                          null);

    editHostName = (EditText) layout.findViewById (R.id.edit_host_name);
    editPort = (EditText) layout.findViewById (R.id.edit_port);
    editDescription = (EditText) layout.findViewById (R.id.edit_description);

    if (host != null)
      {
        editHostName.setText (host.getHostName ());
        editPort.setText (host.getPort ().toString ());
        editDescription.setText (host.getDescription ().toString ());
      }

    AlertDialog.Builder builder = new AlertDialog.Builder (getActivity ());
    builder.setTitle (getTitle ())
      .setNegativeButton ("Cancel", null)
      .setPositiveButton ("Save", new DialogInterface.OnClickListener ()
    {
      public void onClick (DialogInterface dialog, int which)
      {
        if (host == null)
          host = new DictionaryHost ();

        String portText = editPort.getText ().toString ();
        if (portText.length () > 0)
          host.setPort (Integer.parseInt (portText));

        host.setHostName (editHostName.getText ().toString ());
        host.setDescription (editDescription.getText ().toString ());

        try
          {
            DatabaseManager.getInstance ().saveHost (host);
          }
        catch (SQLException e)
          {
            ErrorDialog.show (getActivity (), e.getMessage ());
          }

        ((HostManagementActivity) getActivity ()).refreshHostList ();
      }
    }).setView (layout);
    return builder.create ();
  }

  public String getTitle ()
  {
    if (host == null)
      return getActivity ().getString (R.string.dialog_add_title);
    else
      return getActivity ().getString (R.string.dialog_edit_title);
  }

  public void setDictionaryHost (DictionaryHost host)
  {
    this.host = host;
  }
}
