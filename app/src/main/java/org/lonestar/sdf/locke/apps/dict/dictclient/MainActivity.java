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
import android.preference.PreferenceManager;
import android.text.Html;
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

import org.lonestar.sdf.locke.libs.dict.Definition;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

class MainActivity extends Activity
{
  private static final String SELECTED_DICTIONARY = "SELECTED_DICTIONARY";

  private Host host;
  private DefinitionHistory history = DefinitionHistory.getInstance ();
  private JDictClientTask runningTask;

  private TextView dictView;
  private EditText searchText;
  private Spinner dictSpinner;
  private ImageButton dictInfo;

  private int selectedDictionary = -1;
  private boolean dictInfoButtonState;

  @SuppressLint("NewApi")
  @Override
  protected void onCreate (Bundle savedInstanceState)
  {
    super.onCreate (savedInstanceState);
    setContentView (R.layout.activity_main);
    dictView = (TextView) findViewById (R.id.dict_view);
    searchText = setupSearchText ();
    dictSpinner = setupDictSpinner ();
    dictInfo = (ImageButton) findViewById (R.id.dictinfo_button);

    if (savedInstanceState != null)
      selectedDictionary = savedInstanceState.getInt (SELECTED_DICTIONARY);
  }

  @Override
  public void onPause ()
  {
    super.onPause ();
    if (runningTask != null)
      {
        runningTask.cancel (true);
        runningTask = null;
      }
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
        int cacheTime = Integer.parseInt (
            PreferenceManager.getDefaultSharedPreferences (this)
            .getString (getString (R.string.pref_key_cache_time),
                        getString (R.string.pref_value_cache_time))
        );

        Calendar expireTime = Calendar.getInstance ();
        expireTime.setTime (host.getLastRefresh ());
        expireTime.add (Calendar.DATE, cacheTime);

        if (dictionaries == null || expireTime.before (Calendar.getInstance ()))
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

      case R.id.menu_refresh_dictionaries:
        refreshDictionaries ();
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
    super.onSaveInstanceState (bundle);
    bundle.putInt (SELECTED_DICTIONARY,
                   dictSpinner.getSelectedItemPosition ());
  }

  @Override
  protected void onRestoreInstanceState (Bundle savedInstanceState)
  {
    super.onRestoreInstanceState (savedInstanceState);
    dictSpinner.setSelection (savedInstanceState.getInt (SELECTED_DICTIONARY));
  }

  public void onTaskFinished (JDictClientResult result, Exception exception)
  {
    if (exception != null)
      {
        ErrorDialog.show (this, exception.getMessage ());
        return;
      }

    JDictClientRequest request = result.getRequest ();
    runningTask = null;
    enableInput ();
    switch (request.getCommand ())
      {
      case DEFINE:
        CharSequence text = displayDefinitions (result.getDefinitions ());
        HistoryEntry entry = new HistoryEntry (request.getWord (), text);
        history.add (entry);
        invalidateOptionsMenu ();
        break;

      case DICT_INFO:
        displayDictionaryInfo (result.getDictionaryInfo ());
        break;

      case DICT_LIST:
        Host host = request.getHost ();
        host.setDictionaries (result.getDictionaries ());
        DatabaseManager.getInstance ().saveDictionaries (host);
        setDictionarySpinnerData (result.getDictionaries ());
        break;

      default:
        break;
      }
  }

  public void lookupWord (View view)
  {
    Dictionary dict = (Dictionary) dictSpinner.getSelectedItem ();
    String word = searchText.getText ().toString ();
    if (!(word.isEmpty ()))
      {
        searchText.selectAll ();
        if (dict != null)
          executeTask (JDictClientRequest.DEFINE (host, dict, word));
        else
          executeTask (JDictClientRequest.DEFINE (host, word));
      }
  }

  public void getDictionaryInfo (View view)
  {
    Dictionary dictionary = (Dictionary) dictSpinner.getSelectedItem ();

    searchText.setText ("");
    executeTask (JDictClientRequest.DICT_INFO (host, dictionary));
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

  private void executeTask (JDictClientRequest request)
  {
    disableInput ();
    runningTask = new JDictClientTask (this, request);
    runningTask.execute ();
  }

  private void refreshDictionaries ()
  {
    executeTask (JDictClientRequest.DICT_LIST (host));
  }

  private void displayHistoryEntry (HistoryEntry entry)
  {
    searchText.setText (entry.getWord ());
    dictView.setText (entry.getDefinitionText ());
  }

  private CharSequence displayDefinitions (List<Definition> definitions)
  {
    dictView.setText ("");
    if (definitions == null)
      dictView.setText ("No definitions found.");
    else
      {
        Iterator<Definition> itr = definitions.iterator ();
        while (itr.hasNext ())
          {
            Definition definition = itr.next ();

            dictView.append (Html.fromHtml (
                "<b>" +
                    definition.getDictionary ().getDescription () +
                    "</b><br>"
            ));
            dictView.append (DefinitionParser.parse (definition));
            dictView.append ("\n");
          }
      }

    return dictView.getText ();
  }

  private void displayDictionaryInfo (String dictInfo)
  {
    dictView.setText ("");

    if (dictInfo == null)
      dictView.setText ("No dictionary info received.");
    else
      dictView.setText (dictInfo);
  }

  private void disableInput ()
  {
    searchText.setEnabled (false);
    dictSpinner.setEnabled (false);

    dictInfoButtonState = dictInfo.isEnabled ();
    dictInfo.setEnabled (false);
  }

  private void enableInput ()
  {
    searchText.setEnabled (true);
    dictSpinner.setEnabled (true);
    dictInfo.setEnabled (dictInfoButtonState);
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
