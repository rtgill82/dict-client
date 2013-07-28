package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class AboutDialog extends Dialog {
	
	public AboutDialog(Context context) {
		super(context);
	}

	@Override
	public void onCreate(Bundle savedInstance) {
		setContentView(R.layout.dialog_about);
		
		TextView textView = (TextView) findViewById(R.id.about_text);
		textView.setText(Html.fromHtml("<h2>DictClient</h2> Version 1.0.0<br>Copyright &copy 2013 Robert Gill<br/><br/><p>Icon by <a href='http://www.boomgraphics.se'>Robin Weatherall</a></p>"));
	}
}
