/*
 * Copyright (C) 2017 Robert Gill <rtgill82@gmail.com>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 * FIXME: Look for a way to refresh the list of hosts in
 *        ManageHostsListFragment without tying these two classes as tightly
 *        together as they are.
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.github.rtgill82.libs.jdictclient.JDictClient;

import java.sql.SQLException;

public class EditHostDialog extends DialogFragment {
    private Host mHost;
    private ManageHostsListFragment mFragment;
    private EditText mEditHostName;
    private EditText mEditPort;
    private EditText mEditDescription;

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

    @SuppressLint("SetTextI18n")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout)
          View.inflate(getActivity(), R.layout.dialog_edit_dictionary_host,
                       null);

        mEditHostName = layout.findViewById(R.id.edit_host_name);
        mEditPort = layout.findViewById(R.id.edit_port);
        mEditPort.setText(Integer.toString(JDictClient.DEFAULT_PORT));
        mEditDescription = layout.findViewById(R.id.edit_description);

        if (mHost != null) {
            mEditHostName.setText(mHost.getName());
            mEditPort.setText(mHost.getPort().toString());
            mEditDescription.setText(mHost.getDescription());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getTitle())
          .setNegativeButton(getString(R.string.button_cancel), null)
          .setPositiveButton(getString(R.string.button_save),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (mHost == null) mHost = new Host();
                    String portText = mEditPort.getText().toString();
                    if (portText.trim().length() > 0) {
                        mHost.setPort(Integer.parseInt(portText));
                    }

                    String hostText = mEditHostName.getText().toString();
                    if (hostText.trim().length() == 0) {
                        ErrorDialog.show(getActivity(),
                          getString(R.string.error_host_name_required));
                        return;
                    }

                    mHost.setUserDefined(true);
                    mHost.setName(mEditHostName.getText().toString());
                    mHost.setDescription(mEditDescription.getText()
                                                         .toString());

                    try {
                        mHost.create();
                    } catch (SQLException e) {
                        ErrorDialog.show(getActivity(), e.getMessage());
                    }
                    mFragment.refreshHostList();
                }
            }).setView(layout);
        return builder.create();
    }

    private String getTitle() {
        if (mHost == null) {
            return getActivity().getString(R.string.dialog_add_title);
        } else {
            return getActivity().getString(R.string.dialog_edit_title);
        }
    }

    private void setDictionaryHost(Host host) {
        mHost = host;
    }

    private void setManageHostsListFragment(ManageHostsListFragment fragment) {
        mFragment = fragment;
    }
}
