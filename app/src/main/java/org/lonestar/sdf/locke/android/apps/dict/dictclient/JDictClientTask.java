package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.lonestar.sdf.locke.apps.dict.dictclient.R;

import java.sql.SQLException;

public abstract class JDictClientTask<Params, Progress, Result>
                    extends AsyncTask<Params, Progress, Result>
{
    protected Activity context = null;
    protected Exception exception = null;
    protected DictionaryHost server = null;
    protected String progressMessage = null;

    private ProgressDialog progDialog;

    public JDictClientTask(Activity context) {
        super();
        this.context = context;

        try {
            server = DictClientApplication.getDatabaseManager().getCurrentServer(context);
        } catch (SQLException e) {
            exception = e;
        }
    }

    @Override
    protected void onPreExecute() {
        Button search_button = (Button) context.findViewById(R.id.search_button);
        Button dictinfo_button = (Button) context.findViewById(R.id.dictinfo_button);
        search_button.setEnabled(false);
        dictinfo_button.setEnabled(false);
        progDialog = ProgressDialog.show(context, "Waiting", progressMessage, true);
    }

    @Override
    protected void onPostExecute(Result result) {
        EditText search_text = (EditText) context.findViewById(R.id.search_text);
        Button search_button = (Button) context.findViewById(R.id.search_button);
        Spinner dict_spinner = (Spinner) context.findViewById(R.id.dictionary_spinner);
        Button dictinfo_button = (Button) context.findViewById(R.id.dictinfo_button);
        search_button.setEnabled(true);
        dictinfo_button.setEnabled(true);
        progDialog.dismiss();

        if (this instanceof ListDictionariesTask) {
            search_text.setEnabled(true);
            dict_spinner.setEnabled(true);
        }

        if (exception != null) {
            if (this instanceof ListDictionariesTask) {
                search_text.setEnabled(false);
                search_button.setEnabled(false);
                dict_spinner.setEnabled(false);
                dictinfo_button.setEnabled(false);
            }
            ErrorDialogFragment.show(context, exception.getMessage());
        }
    }
}
