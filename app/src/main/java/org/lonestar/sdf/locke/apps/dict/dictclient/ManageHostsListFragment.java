/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.sql.SQLException;

public class ManageHostsListFragment extends ListFragment
{
  @Override
  public void onCreate (Bundle savedInstanceState)
  {
    super.onCreate (savedInstanceState);
    setHasOptionsMenu (true);
    refreshHostList ();
  }

  @Override
  public void onCreateOptionsMenu (Menu menu, MenuInflater inflater)
  {
    inflater.inflate (R.menu.fragment_host_management, menu);
    super.onCreateOptionsMenu (menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected (MenuItem item)
  {
    switch (item.getItemId ())
      {
      case R.id.menu_add_host:
        EditDictionaryHostDialog.show (this);
        break;

      case R.id.menu_delete_host:
        confirmDeleteSelectedHosts ();
        break;

      default:
        return super.onOptionsItemSelected (item);
      }

    return true;
  }

  @Override
  public void onViewCreated (View view, Bundle savedInstanceState)
  {
    getListView ().setChoiceMode (AbsListView.CHOICE_MODE_MULTIPLE);
    getListView ().setOnItemLongClickListener (
        new AbsListView.OnItemLongClickListener ()
        {
          public boolean onItemLongClick (AdapterView<?> parent, View view, int pos, long id)
          {
            editSelectedHost (pos);
            return true;
          }
        }
    );
  }

  public void refreshHostList ()
  {
    DictionaryHostCursor cursor = DatabaseManager.getInstance ()
                                                 .getHostList ();
    ManageHostCursorAdapter ca =
        new ManageHostCursorAdapter (this.getActivity (), cursor, 0);
    setListAdapter (ca);
  }

  private void confirmDeleteSelectedHosts ()
  {
    ListView view = getListView ();
    int itemCount = view.getCheckedItemCount ();
    if (itemCount == 1)
      {
        int pos = view.getCheckedItemPositions ().keyAt (0);
        DictionaryHost host = getHostAtPosition (pos);
        showConfirmDeleteDialog ("Are you sure you want to delete " +
                                 host.toString () + "?");
      }
    else if (itemCount > 1)
      {
        showConfirmDeleteDialog ("Are you sure you want to delete " +
                                 itemCount + " hosts?");
      }
  }

  private void deleteSelectedHosts ()
  {
    SparseBooleanArray selected = getListView ().getCheckedItemPositions ();
    for (int i = 0; i < selected.size (); i++)
      {
        int pos = selected.keyAt (i);
        getListView ().setItemChecked (pos, false);
        DictionaryHost host = getHostAtPosition (pos);

        try
          {
            DatabaseManager.getInstance ().deleteHostById (host.getId ());
          }
        catch (SQLException e)
          {
            throw new RuntimeException (e);
          }
      }
    refreshHostList ();
  }

  private void editSelectedHost (int pos)
  {
    final DictionaryHost host = getHostAtPosition (pos);
    EditDictionaryHostDialog.show (this, host);
  }

  private DictionaryHost getHostAtPosition (int pos)
  {
    if (pos == -1) return null;
    return ((DictionaryHostCursor) getListAdapter ().getItem (pos))
        .getDictionaryHost ();
  }

  private void showConfirmDeleteDialog (String message)
  {
    new AlertDialog.Builder (getActivity ())
        .setTitle ("Confirm")
        .setMessage (message)
        .setNegativeButton ("No", null)
        .setPositiveButton ("Yes", new DialogInterface.OnClickListener ()
        {
          public void onClick (DialogInterface dialog, int which)
          {
            deleteSelectedHosts ();
          }
        }).create ().show ();
  }
}
