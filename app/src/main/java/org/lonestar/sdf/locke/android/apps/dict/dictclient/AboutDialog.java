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

    public AboutDialog(Context context) {
        super(context);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        if (html == null) {
            Resources resources = this.getContext().getResources();
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
