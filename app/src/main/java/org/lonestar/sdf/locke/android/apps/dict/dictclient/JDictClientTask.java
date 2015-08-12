package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import java.net.URI;
import java.net.URISyntaxException;

import org.lonestar.sdf.locke.android.support.v4.app.ErrorDialogFragment;
import org.lonestar.sdf.locke.apps.dict.dictclient.R;

import android.app.ProgressDialog;
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
	protected int port = -1;
	protected String progressMessage = null;
	
	private SharedPreferences prefs;
	private ProgressDialog progDialog;
	
	public JDictClientTask(FragmentActivity context) {		
		super();
		
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		port = context.getResources().getInteger(R.integer.dict_port);
		String hostString = prefs.getString("host", context.getString(R.string.pref_default_host));

		try {
			URI uri = new URI("dict://" + hostString);
			host = uri.getHost();
			if (uri.getPort() != -1) {
				port = uri.getPort();
			}
		} catch (URISyntaxException ex) {
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