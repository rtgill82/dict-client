package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;

import com.j256.ormlite.dao.Dao;

import org.lonestar.sdf.locke.android.support.v4.app.ErrorDialogFragment;
import org.lonestar.sdf.locke.apps.dict.dictclient.R;

import java.sql.SQLException;

public abstract class JDictClientTask<Params, Progress, Result>
                    extends AsyncTask<Params, Progress, Result>
{
    protected FragmentActivity context = null;
    protected Exception exception = null;
    protected DictionaryServer server = null;
    protected String progressMessage = null;

    private SharedPreferences prefs;
    private ProgressDialog progDialog;

    public JDictClientTask(FragmentActivity context) {
        super();

        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int default_host = Integer.parseInt(prefs.getString("default_host", context.getResources().getString(R.string.pref_value_default_host)));

        try {
            Dao<DictionaryServer, Integer> dao = DictClientApplication.getDatabaseManager().getDao(DictionaryServer.class);
            server = dao.queryForId(default_host);
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
        Button search_button = (Button) context.findViewById(R.id.search_button);
        Button dictinfo_button = (Button) context.findViewById(R.id.dictinfo_button);
        search_button.setEnabled(true);
        dictinfo_button.setEnabled(true);
        progDialog.dismiss();

        if (exception != null) {
            Bundle args = new Bundle();
            args.putString("message", exception.getMessage());
            ErrorDialogFragment dialog = new ErrorDialogFragment();
            dialog.setArguments(args);
            dialog.show(context.getSupportFragmentManager(), "DefineException");
        }
    }
}
