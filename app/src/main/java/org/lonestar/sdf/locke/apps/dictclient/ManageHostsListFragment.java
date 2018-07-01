/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

public class ManageHostsListFragment extends ListFragment {
    ManageHostsCursorAdapter mCursorAdapter;
    ArrayList<Boolean> mToggles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshHostList();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_host_management, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem deleteButton = menu.findItem(R.id.menu_delete_host);
        deleteButton.setEnabled(false);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteButton = menu.findItem(R.id.menu_delete_host);
        if (getListView().getCheckedItemCount() > 0)
          deleteButton.setEnabled(true);
        else
          deleteButton.setEnabled(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
          case R.id.menu_add_host:
            EditHostDialog.show(this);
            break;

          case R.id.menu_delete_host:
            confirmDeleteSelectedHosts();
            break;

          default:
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ListView listView = getListView();
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemLongClickListener(
            new AbsListView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> parent,
                                               View view, int pos, long id) {
                    editSelectedHost(pos);
                    return true;
                }
            }
        );
        listView.setOnItemClickListener(
            new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int pos, long id) {
                    mToggles.set(pos, ((CheckedTextView) view).isChecked());
                    getActivity().invalidateOptionsMenu();
                }
            }
        );
    }

    public void refreshHostList() {
        getListView().clearChoices();
        PreparedQuery query;
        try {
            Dao dao = DatabaseManager.getInstance().getDao(Host.class);
            QueryBuilder qb = dao.queryBuilder();
            query =
              qb.where().eq("hidden", false)
                  .and().eq("readonly", false)
                  .prepare();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        HostCursor cursor = (HostCursor)
          DatabaseManager.find(Host.class, query);
        mCursorAdapter =
          new ManageHostsCursorAdapter(getActivity(), cursor, 0);
        mToggles = new ArrayList<>(Collections.nCopies(
                                    mCursorAdapter.getCount(),
                                    false));
        mCursorAdapter.setToggleList(mToggles);
        setListAdapter(mCursorAdapter);
    }

    private void confirmDeleteSelectedHosts() {
        ListView view = getListView();
        int itemCount = view.getCheckedItemCount();
        if (itemCount == 1) {
            SparseBooleanArray checkedItems = view.getCheckedItemPositions();
            int index = checkedItems.indexOfValue(true);
            Host host = getHostAtPosition(checkedItems.keyAt(index));
            showConfirmDeleteDialog("Are you sure you want to delete " +
                                    host.toString() + "?");
        } else if (itemCount > 1) {
            showConfirmDeleteDialog("Are you sure you want to delete " +
                                    itemCount + " hosts?");
        }
    }

    private void deleteSelectedHosts() {
        SparseBooleanArray selected = getListView().getCheckedItemPositions();
        Host defaultHost = ((DictClient) getActivity().getApplication())
                                                      .getDefaultHost();
        try {
            for (int i = 0; i < selected.size(); i++) {
                int pos = selected.keyAt(i);
                if (selected.get(pos, false)) {
                    getListView().setItemChecked(pos, false);
                    Host host = getHostAtPosition(pos);
                    if (host.getId().equals(defaultHost.getId())) {
                        SharedPreferences prefs = PreferenceManager
                          .getDefaultSharedPreferences(getActivity());
                        prefs.edit().putString(
                          getString(R.string.pref_key_default_host),
                          getString(R.string.pref_value_default_host)
                        ).apply();
                    }
                    host.delete();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        getActivity().invalidateOptionsMenu();
        refreshHostList();
    }

    private void editSelectedHost(int pos) {
        final Host host = getHostAtPosition(pos);

        if (!host.isUserDefined())
          ErrorDialog.show(getActivity(),
                           getString(R.string.error_host_readonly));
        else
          EditHostDialog.show(this, host);
    }

    private Host getHostAtPosition(int pos) {
        if (pos == -1) return null;
        return ((HostCursor) getListAdapter().getItem(pos))
          .getHost();
    }

    private void showConfirmDeleteDialog(String message) {
        new AlertDialog.Builder(getActivity())
          .setTitle(getString(R.string.title_confirm))
          .setMessage(message)
          .setNegativeButton(getString(R.string.button_no), null)
          .setPositiveButton(getString(R.string.button_yes),
              new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                      deleteSelectedHosts();
                  }
              }).create().show();
    }
}
