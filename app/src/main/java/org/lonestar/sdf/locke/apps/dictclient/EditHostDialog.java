/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 * FIXME: Look for a way to refresh the list of hosts in
 *        ManageHostsListFragment without tying these two classes as tightly
 *        together as they are.
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RelativeLayout;

import org.lonestar.sdf.locke.libs.dict.JDictClient;

import java.sql.SQLException;

public class EditHostDialog extends DialogFragment {
    private Host host;
    private ManageHostsListFragment fragment;
    private EditText editHostName;
    private EditText editPort;
    private EditText editDescription;

    public static void show(ManageHostsListFragment fragment) {
        EditHostDialog.show(fragment, null);
    }

    public static void show(ManageHostsListFragment fragment, Host host) {
        EditHostDialog dialog = new EditHostDialog();
        dialog.setDictionaryHost(host);
        dialog.setManageHostsListFragment(fragment);
        dialog.show(fragment.getActivity().getFragmentManager(),
                    fragment.getString(R.string.dialog_edit_tag));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        RelativeLayout layout = (RelativeLayout)
          inflater.inflate(R.layout.dialog_edit_dictionary_host, null);

        editHostName = (EditText) layout.findViewById(R.id.edit_host_name);
        editPort = (EditText) layout.findViewById(R.id.edit_port);
        editPort.setText(Integer.toString(JDictClient.DEFAULT_PORT));
        editDescription = (EditText)
          layout.findViewById(R.id.edit_description);

        if (host != null) {
            editHostName.setText(host.getHostName());
            editPort.setText(host.getPort().toString());
            editDescription.setText(host.getDescription().toString());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getTitle())
          .setNegativeButton(getString(R.string.button_cancel), null)
          .setPositiveButton(getString(R.string.button_save),
              new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                      if (host == null)
                        host = new Host();

                      String portText = editPort.getText().toString();
                      if (portText.length() > 0)
                        host.setPort(Integer.parseInt(portText));

                      host.setHostName(editHostName.getText().toString());
                      host.setDescription(editDescription.getText()
                                                         .toString());

                      try {
                          DatabaseManager.getInstance().saveHost(host);
                      } catch (SQLException e) {
                          ErrorDialog.show(getActivity(), e.getMessage());
                      }
                      fragment.refreshHostList();
                  }
              }).setView(layout);
        return builder.create();
    }

    public String getTitle() {
        if (host == null)
          return getActivity().getString(R.string.dialog_add_title);
        else
          return getActivity().getString(R.string.dialog_edit_title);
    }

    public void setDictionaryHost(Host host) {
        this.host = host;
    }

    public void setManageHostsListFragment(ManageHostsListFragment fragment) {
        this.fragment = fragment;
    }
}
