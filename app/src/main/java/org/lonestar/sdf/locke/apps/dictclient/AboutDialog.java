/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.io.IOException;
import java.io.InputStream;

class AboutDialog extends AppCompatDialogFragment {
    public static void show(AppCompatActivity activity) {
        new AboutDialog().show(activity.getSupportFragmentManager(),
                               activity.getString(R.string.title_about));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String html;
        AppCompatActivity activity = (AppCompatActivity) getActivity();
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
        textView.setText(Html.fromHtml(replaceVersion(activity, html)));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.title_about))
               .setView(textView)
               .setNeutralButton(getString(R.string.button_donate),
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         AppCompatActivity activity =
                           (AppCompatActivity) getActivity();
                         DonateDialog.show(activity);
                     }
                 });
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

    private String replaceVersion(AppCompatActivity activity, String html) {
        DictClient app = (DictClient) activity.getApplication();
        String version = app.getVersionString();
        return html.replaceAll("@VERSION@", version);
    }
}
