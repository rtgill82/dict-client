/*
 * Copyright (C) 2017 Robert Gill <rtgill82@gmail.com>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("WeakerAccess")
public class AboutDialog extends DialogFragment {
    public static void show(Activity activity) {
        new AboutDialog().show(activity.getFragmentManager(),
                               activity.getString(R.string.title_about));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String html;
        Activity activity = getActivity();
        Resources resources = activity.getResources();
        InputStream stream = resources.openRawResource(R.raw.about);

        try {
            byte[] buffer = new byte[stream.available()];
            //noinspection ResultOfMethodCallIgnored
            stream.read(buffer);
            html = new String(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int pad = (int) resources.getDimension(R.dimen.default_margins);
        TextView textView = new TextView(activity);
        textView.setPadding(pad, pad, pad, pad);
        textView.setText(Html.fromHtml(replaceVersion(html)));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.title_about))
               .setView(textView);
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        Button button = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        ViewGroup.LayoutParams layoutParams = button.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        button.setLayoutParams(layoutParams);
    }

    private String replaceVersion(String html) {
        String version = DictClient.getVersionString();
        return html.replaceAll("@VERSION@", version);
    }
}
