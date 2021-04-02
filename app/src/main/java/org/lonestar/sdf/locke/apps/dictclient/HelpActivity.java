/*
 * Copyright (C) 2018 Robert Gill <rtgill82@gmail.com>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setTitle(getString(R.string.title_help));
        TextView helpView = findViewById(R.id.help_view);
        helpView.setMovementMethod(LinkMovementMethod.getInstance());
        helpView.setText(readHelpFile());
    }

    private Spanned readHelpFile() {
        String html = null;
        Resources resources = getResources();
        InputStream stream = resources.openRawResource(R.raw.help);
        try {
            byte[] buffer = new byte[stream.available()];
            //noinspection ResultOfMethodCallIgnored
            stream.read(buffer);
            html = new String(buffer);
        } catch (IOException e) {
            ErrorDialog.show(this,
                             "Unable to read file help.html: "
                             + e.getMessage());
        }
        return Html.fromHtml(html);
    }
}
