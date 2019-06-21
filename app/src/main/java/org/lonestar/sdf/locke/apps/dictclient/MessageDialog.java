/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;

public class MessageDialog extends AppCompatDialogFragment {
    private static final String TITLE = "title";
    private static final String MESSAGE = "message";

    public static void show(AppCompatActivity activity, String title,
                            String message) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        MessageDialog dialog = new MessageDialog();
        dialog.setArguments(args);
        dialog.show(
            activity.getSupportFragmentManager(),
            MessageDialog.class.getSimpleName()
        );
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String title = getArguments().getString(TITLE);
        String message = getArguments().getString(MESSAGE);
        builder.setTitle(title)
               .setMessage(message)
               .setPositiveButton(getString(R.string.button_ok), null);
        return builder.create();
    }
}
