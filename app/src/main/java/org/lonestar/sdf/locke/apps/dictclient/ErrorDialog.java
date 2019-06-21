/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import androidx.appcompat.app.AppCompatActivity;

public class ErrorDialog extends MessageDialog {
    public static void show(AppCompatActivity activity, String message) {
        ErrorDialog.show(
            activity,
            activity.getString(R.string.title_error),
            message
        );
    }
}
