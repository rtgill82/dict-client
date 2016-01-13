package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.app.Activity;
import android.widget.TextView;

import org.lonestar.sdf.locke.apps.dict.dictclient.R;
import org.lonestar.sdf.locke.libs.dict.Dictionary;
import org.lonestar.sdf.locke.libs.dict.JDictClient;

public class DictionaryInfoTask extends
        JDictClientTask<Dictionary, Void, String> {

    public DictionaryInfoTask(Activity context)
    {
        super(context);
        progressMessage = "Retrieving dictionary information...";
    }

    @Override
    protected String doInBackground(Dictionary... dicts) {
        String dictinfo = null;

        try {
            JDictClient dictClient = JDictClient.connect(server.getHost(), server.getPort());
            dictinfo = dictClient.getDictionaryInfo(dicts[0].getDatabase());
            dictClient.close();
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                exception = e;
            } else {
                throw (NullPointerException) e;
            }
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
