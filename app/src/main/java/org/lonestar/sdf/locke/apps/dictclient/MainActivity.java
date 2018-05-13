/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.lonestar.sdf.locke.libs.dict.Definition;
import org.lonestar.sdf.locke.libs.dict.Match;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.lonestar.sdf.locke.apps.dictclient.DonateNotificationService.DONATE_ACTION;

public class MainActivity extends Activity {
    private static final String SELECTED_DICTIONARY = "SELECTED_DICTIONARY";

    private Host host;
    private DefinitionHistory history = DefinitionHistory.getInstance();
    private JDictClientTask runningTask;

    private TextView dictView;
    private EditText searchText;
    private Spinner dictSpinner;
    private Spinner stratSpinner;
    private ImageButton dictInfo;

    private int selectedDictionary = -1;
    private boolean dictInfoButtonState;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dictView = (TextView) findViewById(R.id.dict_view);
        searchText = setupSearchText();
        dictSpinner = setupDictSpinner();
        stratSpinner = setupStratSpinner();
        dictInfo = (ImageButton) findViewById(R.id.dictinfo_button);

        if (savedInstanceState != null)
          selectedDictionary = savedInstanceState.getInt(SELECTED_DICTIONARY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();

        if (intent.getBooleanExtra(DONATE_ACTION, false)) {
            DonateDialog.show(this);
        } else {
            DonateNotificationService.start(getApplicationContext());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (runningTask != null) {
            runningTask.cancel(true);
            runningTask = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DictClient app = (DictClient) getApplication();
        host = app.getCurrentHost();

        if (host != null) {
            List dictionaries = host.getDictionaries();
            List strategies = host.getStrategies();
            int cacheTime = Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(this)
                  .getString(getString(R.string.pref_key_cache_time),
                   getString(R.string.pref_value_cache_time))
            );

            Calendar expireTime = Calendar.getInstance();
            expireTime.setTime(host.getLastRefresh());
            expireTime.add(Calendar.DATE, cacheTime);

            if (dictionaries == null ||
                expireTime.before(Calendar.getInstance())) {
                refreshDictionaries();
            } else {
                setDictionarySpinnerData(dictionaries);
                setStrategySpinnerData(strategies);
            }

            setTitle(host.getHostName());
            dictSpinner.setSelection(selectedDictionary);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem backButton = menu.findItem(R.id.menu_back);
        MenuItem forwardButton = menu.findItem(R.id.menu_forward);

        if (history.isEmpty()) {
            backButton.setEnabled(false);
            forwardButton.setEnabled(false);
        } else {
            if (history.canGoBack())
              backButton.setEnabled(true);
            else
              backButton.setEnabled(false);

            if (history.canGoForward())
              forwardButton.setEnabled(true);
            else
              forwardButton.setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
          case R.id.menu_back:
            traverseHistory(DefinitionHistory.Direction.BACK);
            break;

          case R.id.menu_forward:
            traverseHistory(DefinitionHistory.Direction.FORWARD);
            break;

          case R.id.menu_host_select:
            startActivity(new Intent(this, SelectHostActivity.class));
            break;

          case R.id.menu_refresh_dictionaries:
            refreshDictionaries();
            break;

          case R.id.menu_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            break;

          case R.id.menu_help:
            startActivity(new Intent(this, HelpActivity.class));
            break;

          case R.id.menu_about:
            AboutDialog.show(this);
            break;

          default:
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt(SELECTED_DICTIONARY,
                      dictSpinner.getSelectedItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        dictSpinner.setSelection(
            savedInstanceState.getInt(SELECTED_DICTIONARY)
        );
    }

    public void onTaskFinished(JDictClientResult result,
                               Exception exception) {
        if (exception != null) {
            ErrorDialog.show(this, exception.getMessage());
            return;
        }

        JDictClientRequest request = result.getRequest();
        runningTask = null;
        enableInput();
        switch (request.getCommand()) {
          case DEFINE:
            CharSequence text = displayDefinitions(result.getDefinitions());
            HistoryEntry entry = new HistoryEntry(request.getWord(), text);
            history.add(entry);
            invalidateOptionsMenu();
            break;

          case MATCH:
            displayMatches(result.getMatches());
            break;

          case DICT_INFO:
            displayDictionaryInfo(result.getDictionaryInfo());
            break;

          case DICT_LIST:
            Host host = request.getHost();
            host.setDictionaries(result.getDictionaries());
            host.setStrategies(result.getStrategies());
            DatabaseManager.getInstance().saveDictionaries(host);
            DatabaseManager.getInstance().saveStrategies(host);
            setDictionarySpinnerData(result.getDictionaries());
            setStrategySpinnerData(result.getStrategies());
            break;

          default:
            break;
        }
    }

    public void lookupWord(View view) {
        Strategy strat = (Strategy) stratSpinner.getSelectedItem();
        Dictionary dict = (Dictionary) dictSpinner.getSelectedItem();
        String word = searchText.getText().toString();
        if (!(word.isEmpty())) {
            searchText.selectAll();
            if (!strat.getStrategy().equals("define")) {
                executeTask(JDictClientRequest.MATCH(host, strat, word));
            } else {
                if (dict != null)
                  executeTask(JDictClientRequest.DEFINE(host, dict, word));
                else
                  executeTask(JDictClientRequest.DEFINE(host, word));
            }
        }
    }

    public void getDictionaryInfo(View view) {
        Dictionary dictionary = (Dictionary) dictSpinner.getSelectedItem();
        searchText.setText("");
        executeTask(JDictClientRequest.DICT_INFO(host, dictionary));
    }

    public void traverseHistory(DefinitionHistory.Direction direction) {
        HistoryEntry entry;
        if (DefinitionHistory.Direction.BACK == direction)
          entry = history.back();
        else
          entry = history.forward();
        if (entry != null)
          displayHistoryEntry(entry);
        invalidateOptionsMenu();
    }

    public void setDictionarySpinnerData(List<Dictionary> list) {
        dictSpinner.setAdapter(new DictionarySpinnerAdapter(this, list));
    }

    public void setStrategySpinnerData(List<Strategy> list) {
        stratSpinner.setAdapter(new StrategySpinnerAdapter(this, list));
    }

    private void executeTask(JDictClientRequest request) {
        disableInput();
        runningTask = new JDictClientTask(this, request);
        runningTask.execute();
    }

    private void refreshDictionaries() {
        executeTask(JDictClientRequest.DICT_LIST(host));
    }

    private void displayHistoryEntry(HistoryEntry entry) {
        searchText.setText(entry.getWord());
        dictView.setText(entry.getDefinitionText());
    }

    private CharSequence displayDefinitions(List<Definition> definitions) {
        dictView.setText("");
        dictView.setHorizontallyScrolling(true);
        if (definitions == null)
          dictView.setText(getString(R.string.result_definitions));
        else {
            Iterator<Definition> itr = definitions.iterator();
            while (itr.hasNext()) {
                Definition definition = itr.next();
                dictView.append(Html.fromHtml(
                    "<b>" +
                        definition.getDictionary().getDescription() +
                        "</b><br>"
                ));
                dictView.append(DefinitionParser.parse(definition));
                dictView.append("\n");
            }
        }
        return dictView.getText();
    }

    private CharSequence displayMatches(List<Match> matches) {
        dictView.setText("");
        dictView.setHorizontallyScrolling(false);
        if (matches == null)
          dictView.setText(getString(R.string.result_matches));
        else {
            Map<Dictionary, List<String>> map = buildMatchMap(matches);
            for (Dictionary dictionary : map.keySet()) {
                List<String> list = map.get(dictionary);
                dictView.append(Html.fromHtml(
                        "<b>" + dictionary.getDescription() + "</b><br>"
                    ));
                int i = 0;
                int count = list.size();
                for (String word : list) {
                    dictView.append(new WordSpan(
                            word, dictionary.getDatabase()
                        ).toCharSequence());
                    if (i != count - 1)
                      dictView.append(", ");
                    i += 1;
                }
                dictView.append(Html.fromHtml("<br><br>"));
            }
        }
        return dictView.getText();
    }

    private Map<Dictionary, List<String>> buildMatchMap(List<Match> matches) {
        HashMap<Dictionary, List<String>> map =
          new HashMap<Dictionary, List<String>>();

        Adapter adapter = dictSpinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            Dictionary dictionary = (Dictionary) adapter.getItem(i);
            if (dictionary.getHost() == null) continue;
            LinkedList<String> list = new LinkedList<String>();
            map.put(dictionary, list);
            for (Match match : matches) {
                if (dictionary.getDatabase().equals(match.getDictionary()))
                  list.add(match.getWord());
            }
            if (list.size() == 0)
              map.remove(dictionary);
        }
        return map;
    }

    private void displayDictionaryInfo(String dictInfo) {
        dictView.setText("");
        if (dictInfo == null)
          dictView.setText(getString(R.string.result_dict_info));
        else
          dictView.setText(dictInfo);
    }

    private void disableInput() {
        searchText.setEnabled(false);
        dictSpinner.setEnabled(false);
        stratSpinner.setEnabled(false);
        dictInfoButtonState = dictInfo.isEnabled();
        dictInfo.setEnabled(false);
    }

    private void enableInput() {
        searchText.setEnabled(true);
        dictSpinner.setEnabled(true);
        stratSpinner.setEnabled(true);
        dictInfo.setEnabled(dictInfoButtonState);
    }

    private EditText setupSearchText() {
        EditText searchText = (EditText) findViewById(R.id.search_text);
        searchText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                  case KeyEvent.KEYCODE_DPAD_CENTER:
                  case KeyEvent.KEYCODE_ENTER:
                    lookupWord(v);
                    break;

                  default:
                    break;
                }
                return false;
            }
        });
        return searchText;
    }

    private Spinner setupDictSpinner() {
    Spinner dictSpinner = (Spinner) findViewById(R.id.dict_spinner);
        dictSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View
                                       selectedItemView, int position,
                                       long id) {
                ImageButton dictinfoButton = (ImageButton)
                  findViewById(R.id.dictinfo_button);
                Dictionary currentDictionary = (Dictionary)
                  parent.getSelectedItem();

                if (currentDictionary.getDatabase() != null)
                  dictinfoButton.setEnabled(true);
                else
                  dictinfoButton.setEnabled(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                 ImageButton dictinfoButton = (ImageButton)
                   findViewById(R.id.dictinfo_button);
                 dictinfoButton.setEnabled(false);
            }
        });
        return dictSpinner;
    }

    private Spinner setupStratSpinner() {
        Spinner stratSpinner = (Spinner) findViewById(R.id.strategy_spinner);
        ArrayList<Strategy> list = new ArrayList<Strategy>();
        list.add(Strategy.DEFINE);
        stratSpinner.setAdapter(new StrategySpinnerAdapter(this, list));
        return stratSpinner;
    }
}
