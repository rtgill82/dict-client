/*
 * Copyright (C) 2016 Robert Gill <locke@sdf.lonestar.org>
 *
 * This file is part of DictClient
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.sql.SQLException;

public class HostManagementActivity extends FragmentActivity
{
  private ListView hostList;
  private DictionaryHostCursor cursor;
  private ManageHostCursorAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_host_management);
    setTitle(getString(R.string.app_name) + " - Host Management");
    refreshHostList();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    getMenuInflater().inflate(R.menu.activity_host_management, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch(item.getItemId())
      {
      case R.id.menu_add:
        EditDictionaryHostDialog.show(this);
        break;

      default:
        return super.onOptionsItemSelected(item);
      }

    return true;
  }

  public void delete(View view)
  {
    final DictionaryHost host = getSelectedHost(view);

    // Confirm Dialog
    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    dialog.setTitle("Confirm")
      .setMessage("Are you sure you want to delete " + host.toString() + "?")
      .setCancelable(false)
      .setNegativeButton("No", null)
      .setPositiveButton("Yes", new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface dialog, int which)
      {
        try
          {
            DatabaseManager.getInstance().deleteHostById(host.getId());
            refreshHostList();
          }
        catch (SQLException e)
          {
            ErrorDialog.show(HostManagementActivity.this, e.getMessage());
          }
      }
    });
    dialog.create().show();
  }

  public void edit(View view)
  {
    final DictionaryHost host = getSelectedHost(view);
    EditDictionaryHostDialog.show(this, host);
  }

  public boolean refreshHostList()
  {
    boolean rv = true;
    try
      {
        cursor = DatabaseManager.getInstance().getHostList();
        adapter = new ManageHostCursorAdapter(this, cursor, 0);
        hostList = (ListView) findViewById(R.id.manage_host_list);
        hostList.setAdapter(adapter);
      }
    catch (SQLException e)
      {
        ErrorDialog.show(this, e.getMessage());
        rv = false;
      }
    return rv;
  }

  private DictionaryHost getSelectedHost(View view)
  {
    int pos = hostList.getPositionForView((View) view.getParent());
    return ((DictionaryHostCursor) adapter.getItem(pos)).getDictionaryHost();
  }
}
