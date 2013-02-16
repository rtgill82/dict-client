package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;

public abstract class JDictClientTask<Params, Progress, Result>
                    extends AsyncTask<Params, Progress, Result>
{
	protected FragmentActivity context = null;
	protected Exception exception = null;
	protected String host = null;
	protected int port = 0;
	
	private SharedPreferences prefs;
	
	public JDictClientTask(FragmentActivity context) {
		super();
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	protected void onPreExecute() {
		Button search_button = (Button) context.findViewById(R.id.search_button);
		Button dictinfo_button = (Button) context.findViewById(R.id.dictinfo_button);
		search_button.setEnabled(false);
		dictinfo_button.setEnabled(false);
		host = prefs.getString("host", context.getString(R.string.pref_default_host));
		port = Integer.parseInt(prefs.getString("port", context.getString(R.string.pref_default_port)));
	}
	
	@Override
	protected void onPostExecute(Result result) {
		Button search_button = (Button) context.findViewById(R.id.search_button);
		Button dictinfo_button = (Button) context.findViewById(R.id.dictinfo_button);
		search_button.setEnabled(true);
		dictinfo_button.setEnabled(true);
	
		if (exception != null) {
			Bundle args = new Bundle();
			args.putString("message", exception.getMessage());
			ErrorDialogFragment dialog = new ErrorDialogFragment();
			dialog.setArguments(args);
			dialog.show(context.getSupportFragmentManager(), "DefineException");
		}
	}
}