/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dict.dictclient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

class MainActivity extends Activity
{
  private static final String SELECTED_DICTIONARY = "SELECTED_DICTIONARY";

  private Host host;
  private DefinitionHistory history = DefinitionHistory.getInstance ();

  private TextView dictView;
  private EditText searchText;
  private Spinner dictSpinner;

  private int selectedDictionary = -1;

  @SuppressLint("NewApi")
  @Override
  protected void onCreate (Bundle savedInstanceState)
  {
    super.onCreate (savedInstanceState);
    setContentView (R.layout.activity_main);
    dictView = (TextView) findViewById (R.id.dict_view);
    searchText = setupSearchText ();
    dictSpinner = setupDictSpinner ();

    if (savedInstanceState != null)
      selectedDictionary = savedInstanceState.getInt (SELECTED_DICTIONARY);
  }

  @Override
  public void onResume ()
  {
    super.onResume ();
    DictClientApplication app = (DictClientApplication) getApplication ();
    host = app.getCurrentHost ();

    if (host != null)
      {
        List dictionaries = host.getDictionaries ();
        if (dictionaries == null)
          refreshDictionaries ();
        else
          setDictionarySpinnerData (dictionaries);

        setTitle (host.getHostName ());
        dictSpinner.setSelection (selectedDictionary);
      }
  }

  @Override
  public boolean onCreateOptionsMenu (Menu menu)
  {
    getMenuInflater ().inflate (R.menu.activity_main, menu);
    return super.onCreateOptionsMenu (menu);
  }

  @Override
  public boolean onPrepareOptionsMenu (Menu menu)
  {
    MenuItem backButton = menu.findItem (R.id.menu_back);
    MenuItem forwardButton = menu.findItem (R.id.menu_forward);

    if (history.isEmpty ())
      {
        backButton.setEnabled (false);
        forwardButton.setEnabled (false);
      }
    else
      {
        if (history.canGoBack ())
            backButton.setEnabled (true);
        else
            backButton.setEnabled (false);

        if (history.canGoForward ())
            forwardButton.setEnabled (true);
        else
            forwardButton.setEnabled (false);
      }

    return super.onPrepareOptionsMenu (menu);
  }

  @Override
  public boolean onOptionsItemSelected (MenuItem item)
  {
    switch (item.getItemId ())
      {
      case R.id.menu_back:
        traverseHistory (DefinitionHistory.Direction.BACK);
        break;

      case R.id.menu_forward:
        traverseHistory (DefinitionHistory.Direction.FORWARD);
        break;

      case R.id.menu_host_select:
        startActivity (new Intent (this, SelectHostActivity.class));
        break;

      case R.id.menu_preferences:
        startActivity (new Intent (this, SettingsActivity.class));
        break;

      case R.id.menu_about:
        AboutDialog.show (this);
        break;

      default:
        return super.onOptionsItemSelected (item);
      }

    return true;
  }

  @Override
  protected void onSaveInstanceState (Bundle bundle)
  {
    bundle.putInt (SELECTED_DICTIONARY,
                   dictSpinner.getSelectedItemPosition ());
  }

  @Override
  protected void onRestoreInstanceState (Bundle savedInstanceState)
  {
    dictSpinner.setSelection (savedInstanceState.getInt (SELECTED_DICTIONARY));
  }

  public void lookupWord (View view)
  {
    Dictionary dict = (Dictionary) dictSpinner.getSelectedItem ();
    String word = searchText.getText ().toString ();
    if (!(word.isEmpty ()))
      {
        searchText.selectAll ();
        if (dict != null)
          {
            new JDictClientTask (this,
                JDictClientRequest.DEFINE (host, dict, word))
              .execute ();
          }
        else
          {
            new JDictClientTask (this,
                JDictClientRequest.DEFINE (host, word))
              .execute ();
          }
      }
  }

  public void getDictionaryInfo (View view)
  {
    Dictionary dictionary = (Dictionary) dictSpinner.getSelectedItem ();

    searchText.setText ("");
    new JDictClientTask (this,
        JDictClientRequest.DICT_INFO (host, dictionary))
      .execute ();
  }

  public void traverseHistory (DefinitionHistory.Direction direction)
  {
    HistoryEntry entry;

    if (DefinitionHistory.Direction.BACK == direction)
      entry = history.back ();
    else
      entry = history.forward ();

    if (entry != null)
      displayHistoryEntry (entry);

    invalidateOptionsMenu ();
  }

  public void setDictionarySpinnerData (List<Dictionary> list)
  {
    dictSpinner.setAdapter (new DictionarySpinnerAdapter (this, list));
  }

  private void refreshDictionaries ()
  {
    new JDictClientTask (this,
        JDictClientRequest.DICT_LIST (host))
      .execute ();
  }

  private void displayHistoryEntry (HistoryEntry entry)
  {
    searchText.setText (entry.getWord ());
    dictView.setText (entry.getDefinitionText ());
  }

  private EditText setupSearchText ()
  {
    EditText searchText = (EditText) findViewById (R.id.search_text);
    searchText.setOnKeyListener (new View.OnKeyListener ()
    {
      public boolean onKey (View v, int keyCode, KeyEvent event)
        {
          switch (keyCode)
            {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
              lookupWord (v);
              break;

            default:
              break;
            }
          return false;
        }
    });
    return searchText;
  }

  private Spinner setupDictSpinner ()
  {
    Spinner dictSpinner = (Spinner) findViewById (R.id.dict_spinner);
    dictSpinner.setOnItemSelectedListener (new OnItemSelectedListener ()
    {
      @Override
      public void onItemSelected (AdapterView<?> parent, View
                                  selectedItemView, int position, long id)
      {
        ImageButton dictinfoButton = (ImageButton)
            findViewById (R.id.dictinfo_button);
        Dictionary currentDictionary = (Dictionary) parent.getSelectedItem ();

        if (currentDictionary.getDatabase () != null)
            dictinfoButton.setEnabled (true);
        else
            dictinfoButton.setEnabled (false);
      }

      @Override
      public void onNothingSelected (AdapterView<?> parent)
      {
        ImageButton dictinfoButton = (ImageButton)
            findViewById (R.id.dictinfo_button);
        dictinfoButton.setEnabled (false);
      }
    });
    return dictSpinner;
  }
}
