package org.lonestar.sdf.locke.apps.dict.dictclient;

import org.lonestar.sdf.locke.libs.dict.Dictionary;
import org.lonestar.sdf.locke.libs.dict.JDictClient;

import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

public class DictionaryInfoTask extends
		JDictClientTask<Dictionary, Void, String> {
	
	public DictionaryInfoTask(FragmentActivity context) {
		super(context);
	}
	
	@Override
	protected String doInBackground(Dictionary... dicts) {
		String dictinfo = null;

		try {
			JDictClient dictClient = JDictClient.connect(host, port);
			dictinfo = dictClient.getDictionaryInfo(dicts[0].getDatabase());
			dictClient.close();
		} catch (Exception e) {
			exception = e;
		}

		return dictinfo;
	}
	
	@Override
	protected void onPostExecute(String dictinfo) {
		super.onPostExecute(dictinfo);
		
		TextView textView = (TextView) context.findViewById(R.id.definition_view);
		textView.setText("");

		if (dictinfo == null) {
			textView.setText("No dictionary info received.");
		} else {
			textView.setText(dictinfo);
		}
	}
}
