package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.app.Activity;
import android.widget.Spinner;

import org.lonestar.sdf.locke.apps.dict.dictclient.R;
import org.lonestar.sdf.locke.libs.dict.Dictionary;
import org.lonestar.sdf.locke.libs.dict.JDictClient;

import java.util.ArrayList;
import java.util.List;

public class ListDictionariesTask extends JDictClientTask<String, Void, List<Dictionary>> {

    public ListDictionariesTask(Activity context)
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
            JDictClient dictClient = JDictClient.connect(host.getHostName(), host.getPort());
            dictionaries.addAll(dictClient.getDictionaries());
            dictClient.close();
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                exception = e;
            } else {
                throw (NullPointerException) e;
            }
        }

        return dictionaries;
    }

    @Override
    protected void onPostExecute(List<Dictionary> dictionaries)
    {
        super.onPostExecute(dictionaries);

        Spinner dict_spinner = (Spinner) context.findViewById(R.id.dictionary_spinner);
        dict_spinner.setAdapter(
                new DictionarySpinnerAdapter(context, dictionaries)
        );
    }
}
