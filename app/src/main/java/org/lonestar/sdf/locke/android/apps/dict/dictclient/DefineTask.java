package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.app.Activity;
import android.text.Html;
import android.widget.TextView;

import org.lonestar.sdf.locke.apps.dict.dictclient.R;
import org.lonestar.sdf.locke.libs.dict.Definition;
import org.lonestar.sdf.locke.libs.dict.Dictionary;
import org.lonestar.sdf.locke.libs.dict.JDictClient;

import java.util.Iterator;
import java.util.List;

public class DefineTask extends
        JDictClientTask<String, Void, List<Definition>> {

    public DefineTask(Activity context)
    {
        super(context);
        progressMessage = "Looking up word...";
    }

    @Override
    protected List<Definition> doInBackground(String... words) {
        List<Definition> definitions = null;

        try {
            JDictClient dictClient = JDictClient.connect(server.getHost(), server.getPort());
            if (words.length == 2) {
                definitions = dictClient.define(words[0], words[1]);
            } else {
                definitions = dictClient.define(words[0]);
            }
            dictClient.close();
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                exception = e;
            } else {
                throw (NullPointerException) e;
            }
        }

        return definitions;
    }

    @Override
    protected void onPostExecute(List<Definition> definitions) {
        super.onPostExecute(definitions);

        TextView textView = (TextView) context.findViewById(R.id.definition_view);
        textView.setText("");

        if (definitions == null) {
            textView.setText("No definitions found.");
        } else {
            Iterator<Definition> itr = definitions.iterator();
            while (itr.hasNext()) {
                Definition definition = (Definition) itr.next();
                Dictionary dictionary = definition.getDictionary();

                textView.append(Html.fromHtml("<b>" + dictionary.getDescription() + "</b><br>"));
                textView.append(definition.getDefinition() + "\n");
            }
        }
    }
}
