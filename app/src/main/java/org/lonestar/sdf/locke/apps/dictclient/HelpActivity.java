package org.lonestar.sdf.locke.apps.dictclient;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class HelpActivity extends Activity
{
  private String html;
  private TextView helpText;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_help);
    setTitle (getString (R.string.title_help));
    helpText = (TextView) findViewById (R.id.help_text);
    helpText.setMovementMethod (LinkMovementMethod.getInstance ());
    readHelpFile ();
  }

  private void readHelpFile ()
  {
    if (html == null)
      {
        Resources resources = getResources ();
        InputStream stream = resources.openRawResource (R.raw.help);

        try
          {
            byte[] buffer = new byte[stream.available ()];
            stream.read (buffer);
            html = new String (buffer);
          }
        catch (IOException e)
          {
            ErrorDialog.show (this,
                "Unable to read file help.html: "
                    + e.getMessage ());
          }

        helpText.setText (Html.fromHtml (html));
      }
  }
}
