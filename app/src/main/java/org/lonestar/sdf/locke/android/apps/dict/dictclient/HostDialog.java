package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import org.lonestar.sdf.locke.apps.dict.dictclient.R;

import java.sql.SQLException;

public class HostDialog extends DialogFragment {
    private HostListCursor cursor;
    private HostListCursorAdapter ca;

    public static void show(FragmentActivity activity) {
        new HostDialog().show(activity.getSupportFragmentManager(), HostDialog.class.getName());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        try {
            cursor = DatabaseManager.getInstance().getHostList();
            ca = new HostListCursorAdapter(context, cursor, 0);
        } catch (SQLException e) {
            this.dismiss();
            ErrorDialog.show(this.getActivity(), e.getMessage());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Integer hostId = Integer.parseInt(
                prefs.getString(getString(R.string.pref_key_dict_host),
                context.getResources().getString(R.string.pref_value_dict_host))
            );

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.host_text))
                .setSingleChoiceItems(ca, getSelectedHost(cursor, hostId),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog alertDialog = (AlertDialog) dialog;
                                HostListCursor c = (HostListCursor) alertDialog.getListView().getItemAtPosition(which);
                                String hostId = c.getString(c.getColumnIndex("_id"));
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(alertDialog.getContext()).edit();
                                editor.putString(getString(R.string.pref_key_dict_host), hostId);
                                editor.apply();
                                ((MainActivity) alertDialog.getOwnerActivity()).refreshDictionaries();
                                ((MainActivity) alertDialog.getOwnerActivity()).reset();
                                alertDialog.dismiss();
                            }
                        });
        return builder.create();
    }

    private int getSelectedHost(HostListCursor cursor, Integer hostId) {
        int rv = -1;

        cursor.moveToFirst();
        do {
            if (cursor.getInt(cursor.getColumnIndex("_id")) == hostId)
                rv = cursor.getPosition();
        } while (cursor.moveToNext());

        return rv;
    }
}
