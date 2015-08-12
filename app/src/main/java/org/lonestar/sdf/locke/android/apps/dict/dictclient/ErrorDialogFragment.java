package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ErrorDialogFragment extends DialogFragment {
	
	public static void show(FragmentManager fragmentManager, String tag, String message) {
		Bundle args = new Bundle();
		args.putString("message", message);
		ErrorDialogFragment dialog = new ErrorDialogFragment();
		dialog.setArguments(args);
		dialog.show(fragmentManager, tag);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		String message = getArguments().getString("message");
		builder.setTitle("Error")
		       .setMessage(message)
		       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		    	   public void onClick(DialogInterface dialog, int id) {
		    		   // ok pressed
		    	   }
		       });
		return builder.create();
	}

}
