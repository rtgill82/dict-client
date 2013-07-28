package org.lonestar.sdf.locke.apps.dict.dictclient;

import java.util.ArrayList;
import java.util.List;

import org.lonestar.sdf.locke.libs.dict.Dictionary;
import org.lonestar.sdf.locke.libs.dict.JDictClient;

import android.support.v4.app.FragmentActivity;
import android.widget.Spinner;

public class ListDictionariesTask extends JDictClientTask<String, Void, List<Dictionary>> {
	
	public ListDictionariesTask(FragmentActivity context)
	{
		super(context);
		progressMessage = "Retrieving available dictionaries...";
	}
	
	@Override
	protected List<Dictionary> doInBackground(String...strings)
	{
		List<Dictionary> dictionaries = null;
		
		dictionaries = new ArrayList<Dictionary>();
		dictionaries.add(new Dictionary(null, "All Dictionaries"));
		try {
			JDictClient dictClient = JDictClient.connect(host, port);
			dictionaries.addAll(dictClient.getDictionaries());
			dictClient.close();
		} catch (Exception e) {
			exception = e;
		}
		
		return dictionaries;
	}

	@Override
	protected void onPostExecute(List<Dictionary> dictionaries)
	{
		super.onPostExecute(dictionaries);
		
		Spinner dictionary_spinner = (Spinner) context.findViewById(R.id.dictionary_spinner);
		DictClientState state = DictClientState.getInstance();
		state.dictAdapter = new DictionarySpinnerAdapter(context, dictionaries);
		dictionary_spinner.setAdapter(state.dictAdapter);

	}
}