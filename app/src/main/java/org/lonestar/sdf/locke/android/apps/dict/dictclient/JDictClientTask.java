package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.Html;
import android.widget.Spinner;
import android.widget.TextView;

import org.lonestar.sdf.locke.apps.dict.dictclient.R;
import org.lonestar.sdf.locke.libs.dict.Definition;
import org.lonestar.sdf.locke.libs.dict.Dictionary;
import org.lonestar.sdf.locke.libs.dict.JDictClient;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JDictClientTask
        extends AsyncTask<Void, Void, JDictClientResult>
{
    private Activity context;
    private JDictClientRequest request;
    private Exception exception;
    private DictionaryHost host;

    private ProgressDialog progressDialog;

    private static final Map<JDictClientCommand, String> messages = new EnumMap<JDictClientCommand, String>(JDictClientCommand.class);
    static {
        messages.put(JDictClientCommand.DEFINE, "Looking up word...");
        messages.put(JDictClientCommand.DICT_INFO, "Retrieving dictionary information...");
        messages.put(JDictClientCommand.DICT_LIST, "Retrieving available dictionaries...");
    }

    public JDictClientTask(Activity context, JDictClientRequest request) {
        super();
        this.context = context;
        this.request = request;

        try {
            host = DictClientApplication.getDatabaseManager().getCurrentHost(this.context);
        } catch (SQLException e) {
            exception = e;
        }
    }

    @Override
    protected void onPreExecute() {
        disableInput();
        progressDialog = ProgressDialog.show(context, "Waiting", messages.get(request.getCommand()), true);
    }

    protected JDictClientResult doInBackground(Void... voids) {
        try {
            switch (request.getCommand()) {
                case DEFINE:
                    return new JDictClientResult(request, getDefinitions(request.getWord(), request.getDictionary()));
                case DICT_INFO:
                    return new JDictClientResult(request, getDictionaryInfo(request.getDictionary()));
                case DICT_LIST:
                    return new JDictClientResult(request, getDictionaries());
                default:
                    break;
            }
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                exception = e;
            } else {
                throw (NullPointerException) e;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(JDictClientResult result) {
        enableInput();
        progressDialog.dismiss();

        switch(result.getRequest().getCommand()) {
            case DEFINE:
                displayDefinitions(result.getDefinitions());
                break;
            case DICT_INFO:
                displayDictionaryInfo(result.getDictionaryInfo());
                break;
            case DICT_LIST:
                ((Spinner) context.findViewById(R.id.dictionary_spinner)).setAdapter(
                        new DictionarySpinnerAdapter(context, result.getDictionaries())
                );
                break;
            default:
                break;
        }

        if (exception != null) {
            if (request.getCommand() == JDictClientCommand.DICT_LIST) disableInput();
            ErrorDialogFragment.show(context, exception.getMessage());
        }
    }

    private List<Dictionary> getDictionaries()
            throws Exception {
        JDictClient dictClient = JDictClient.connect(host.getHostName(), host.getPort());
        List<Dictionary> dictionaries = new ArrayList<Dictionary>();
        dictionaries.add(new Dictionary(null, "All Dictionaries"));
        dictionaries.addAll(dictClient.getDictionaries());
        dictClient.close();
        return dictionaries;
    }

    private List<Definition> getDefinitions(String word, Dictionary dictionary)
            throws Exception {
        JDictClient dictClient = JDictClient.connect(host.getHostName(), host.getPort());
        List<Definition> definitions;
        if (dictionary.getDatabase() != null) {
            definitions = dictClient.define(dictionary.getDatabase(), word);
        } else {
            definitions = dictClient.define(word);
        }
        dictClient.close();

        return definitions;
    }

    private String getDictionaryInfo(Dictionary dictionary)
            throws Exception {
        JDictClient dictClient = JDictClient.connect(host.getHostName(), host.getPort());
        String dictInfo = dictClient.getDictionaryInfo(dictionary.getDatabase());
        dictClient.close();
        return dictInfo;
    }

    private void displayDefinitions(List<Definition> definitions) {
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

    private void displayDictionaryInfo(String dictInfo) {
        TextView textView = (TextView) context.findViewById(R.id.definition_view);
        textView.setText("");
        if (dictInfo == null) {
            textView.setText("No dictionary info received.");
        } else {
            textView.setText(dictInfo);
        }
    }

    private void disableInput() {
        context.findViewById(R.id.search_text).setEnabled(false);
        context.findViewById(R.id.search_button).setEnabled(false);
        context.findViewById(R.id.dictionary_spinner).setEnabled(false);
        context.findViewById(R.id.dictinfo_button).setEnabled(false);
    }

    private void enableInput() {
        context.findViewById(R.id.search_text).setEnabled(true);
        context.findViewById(R.id.search_button).setEnabled(true);
        context.findViewById(R.id.dictionary_spinner).setEnabled(true);
        context.findViewById(R.id.dictinfo_button).setEnabled(true);
    }
}
