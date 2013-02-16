package org.lonestar.sdf.locke.apps.dict.dictclient;

import java.util.List;

import org.lonestar.sdf.locke.libs.dict.Dictionary;
import org.lonestar.sdf.locke.libs.dict.JDictClient;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Spinner;

public class ListDictionariesTask extends JDictClientTask<String, Void, List<Dictionary>> {
	
	public ListDictionariesTask(FragmentActivity context)
	{
		super(context);
	}

	@Override
	protected List<Dictionary> doInBackground(String...strings) {
		List<Dictionary> dictionaries = null;
		
		try {
			JDictClient dictClient = JDictClient.connect(host, port);
			dictionaries = dictClient.getDictionaries();
			dictClient.close();
		} catch (Exception e) {
			exception = e;
		}
		
		return dictionaries;
	}

	@Override
	protected void onPostExecute(List<Dictionary> dictionaries) {
		super.onPostExecute(dictionaries);
		
		Spinner dictionary_spinner = (Spinner) context.findViewById(R.id.dictionary_spinner);

		if (dictionaries == null) {
			Log.d("ListDictionariesTask", "No dictionaries found.");
		} else {
			Log.d("ListDictionariesTask", "Dictionaries found.");
			DictionarySpinnerAdapter adapter = new DictionarySpinnerAdapter(context, dictionaries);
			Log.d("ListDictionariesTask", "" + adapter.getCount());
			dictionary_spinner.setAdapter(adapter);
		}
	}
}
