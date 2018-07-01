/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

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

        TextView textView = (TextView) View.inflate(activity,
                                                    R.layout.dialog_about,
                                                    null);
        textView.setText(Html.fromHtml(replaceVersion(activity, html)));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.title_about))
               .setView(textView)
               .setNeutralButton(getString(R.string.button_donate),
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         DonateDialog.show(getActivity());
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

    private String replaceVersion(Activity activity, String html) {
        DictClient app = (DictClient) activity.getApplication();
        String version = app.getVersionString();
        return html.replaceAll("@VERSION@", version);
    }
}
