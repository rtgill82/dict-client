/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.app.Activity;

public class ErrorDialog extends MessageDialog {
    public static void show(Activity activity, String message) {
        ErrorDialog.show(
            activity,
            activity.getString(R.string.title_error),
            message
        );
    }
}
