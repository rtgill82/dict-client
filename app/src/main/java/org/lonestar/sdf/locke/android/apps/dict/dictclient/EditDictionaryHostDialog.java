package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RelativeLayout;

import org.lonestar.sdf.locke.apps.dict.dictclient.R;

import java.sql.SQLException;

/**
 * Created by locke on 10/2/16.
 */

public class EditDictionaryHostDialog extends DialogFragment
{
  private DictionaryHost host;
  private EditText edit_host_name;
  private EditText edit_port;
  private EditText edit_description;

  public static void show(FragmentActivity activity)
    {
      EditDictionaryHostDialog.show(activity, null);
    }

  public static void show(FragmentActivity activity, DictionaryHost host)
    {
      EditDictionaryHostDialog dialog = new EditDictionaryHostDialog();
      dialog.setDictionaryHost(host);
      dialog.show(activity.getSupportFragmentManager(),
          activity.getString(R.string.dialog_edit_tag));
    }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
    {
      LayoutInflater inflater = getActivity().getLayoutInflater();
      RelativeLayout layout = (RelativeLayout)
        inflater.inflate(R.layout.dialog_edit_dictionary_host, null);

      edit_host_name = (EditText) layout.findViewById(R.id.edit_host_name);
      edit_port = (EditText) layout.findViewById(R.id.edit_port);
      edit_description = (EditText) layout.findViewById(R.id.edit_description);

      if (host != null)
        {
          edit_host_name.setText(host.getHostName());
          edit_port.setText(host.getPort().toString());
          edit_description.setText(host.getDescription().toString());
        }

      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle(getTitle())
          .setNegativeButton("Cancel", null)
          .setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
              {
                if (host == null)
                  host = new DictionaryHost();

                String port_text = edit_port.getText().toString();
                if (port_text.length() > 0)
                  host.setPort(Integer.parseInt(port_text));

                host.setHostName(edit_host_name.getText().toString());
                host.setDescription(edit_description.getText().toString());

                try {
                  DatabaseManager.getInstance().saveHost(host);
                } catch (SQLException e) {
                  ErrorDialog.show(getActivity(), e.getMessage());
                }

                ((HostManagementActivity) getActivity()).refreshHostList();
              }
          })
          .setView(layout);
      return builder.create();
    }

  public String getTitle()
    {
      if (host == null)
        return getActivity().getString(R.string.dialog_add_title);
      else
        return getActivity().getString(R.string.dialog_edit_title);
    }

  public void setDictionaryHost(DictionaryHost host)
    {
      this.host = host;
    }
}
