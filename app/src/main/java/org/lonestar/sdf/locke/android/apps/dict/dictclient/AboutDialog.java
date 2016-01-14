package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import org.lonestar.sdf.locke.apps.dict.dictclient.R;

import java.io.IOException;
import java.io.InputStream;

public class AboutDialog extends Dialog {
    private String html;

    public static void show(Context context) {
        new AboutDialog(context).show();
    }

    public AboutDialog(Context context) {
        super(context);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        Context context = this.getContext();
        this.setTitle(context.getString(R.string.about_text));
        if (html == null) {
            Resources resources = context.getResources();
            InputStream stream = resources.openRawResource(R.raw.about);

            try {
                byte[] buffer = new byte[stream.available()];
                stream.read(buffer);
                html = new String(buffer);
            } catch (IOException e) {
                // FIXME: Display dialog error?
                Log.e("AboutDialog", String.format("Unable to read file about.html: %s", e.getMessage()));
            }
        }
        setContentView(R.layout.dialog_about);

        TextView textView = (TextView) findViewById(R.id.about_text);
        textView.setText(Html.fromHtml(html));
    }
}
