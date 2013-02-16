package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ErrorDialogFragment extends DialogFragment {
	
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
