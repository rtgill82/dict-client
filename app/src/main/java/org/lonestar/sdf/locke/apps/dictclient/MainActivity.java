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
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
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
import android.widget.SpinnerAdapter;

import org.lonestar.sdf.locke.libs.dict.Definition;
import org.lonestar.sdf.locke.libs.dict.Match;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.lonestar.sdf.locke.apps.dictclient.DonateNotificationService.DONATE_ACTION;
import static org.lonestar.sdf.locke.apps.dictclient.DonateNotificationService.DONATE_SEEN;

public class MainActivity extends Activity {
    private static final String SELECTED_DICTIONARY = "SELECTED_DICTIONARY";
    private static final String SELECTED_STRATEGY = "SELECTED_STRATEGY";

    private Host host;
    private DefinitionHistory history = DefinitionHistory.getInstance();
    private JDictClientTask runningTask;

    private ResultView resultView;
    private EditText searchText;
    private Spinner dictionarySpinner;
    private Spinner strategySpinner;
    private ImageButton infoButton;
    private ImageButton searchButton;

    private int selectedDictionary = -1;
    private int selectedStrategy = -1;
    private boolean infoButtonState;
    private boolean searchButtonState;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultView = findViewById(R.id.result_view);
        searchText = setupSearchText();
        dictionarySpinner = setupDictionarySpinner();
        strategySpinner = setupStrategySpinner();
        infoButton = findViewById(R.id.dictionary_info_button);
        searchButton = findViewById(R.id.search_button);

        if (savedInstanceState != null) {
            selectedDictionary = savedInstanceState.getInt(SELECTED_DICTIONARY);
            selectedStrategy = savedInstanceState.getInt(SELECTED_STRATEGY);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if (intent.getBooleanExtra(DONATE_ACTION, false)) {
            intent.putExtra(DONATE_ACTION, false);
            intent.putExtra(DONATE_SEEN, true);
            DonateDialog.show(this);
        } else if (!intent.getBooleanExtra(DONATE_SEEN, false)) {
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
            dictionarySpinner.setSelection(selectedDictionary);
            strategySpinner.setSelection(selectedStrategy);
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
                      dictionarySpinner.getSelectedItemPosition());
        bundle.putInt(SELECTED_STRATEGY,
                      strategySpinner.getSelectedItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedDictionary = savedInstanceState.getInt(SELECTED_DICTIONARY);
        selectedStrategy = savedInstanceState.getInt(SELECTED_STRATEGY);
    }

    public void onTaskFinished(JDictClientResult result,
                               Exception exception) {
        enableInput();
        if (exception != null) {
            if (exception.getClass().equals(UnknownHostException.class)) {
                ErrorDialog.show(this, "Unknown host: " +
                                       exception.getMessage());
            } else {
                ErrorDialog.show(this, exception.toString());
            }
            return;
        }

        JDictClientRequest request = result.getRequest();
        runningTask = null;

        CharSequence text;
        HistoryEntry entry;
        switch (request.getCommand()) {
          case DEFINE:
            text = displayDefinitions(result.getDefinitions());
            entry = new HistoryEntry(
              request.getWord(),
              ((Dictionary) dictionarySpinner.getSelectedItem()),
              ((Strategy) strategySpinner.getSelectedItem()),
              text
            );
            history.add(entry);
            invalidateOptionsMenu();
            break;

          case MATCH:
            text = displayMatches(result.getMatches());
            entry = new HistoryEntry(
              request.getWord(),
              ((Dictionary) dictionarySpinner.getSelectedItem()),
              ((Strategy) strategySpinner.getSelectedItem()),
              text
            );
            history.add(entry);
            invalidateOptionsMenu();
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

    @Override
    public void onBackPressed() {
        if (!traverseHistory(DefinitionHistory.Direction.BACK))
          super.onBackPressed();
    }

    public void lookupWord(View view) {
        Strategy strategy = (Strategy) strategySpinner.getSelectedItem();
        Dictionary dict = (Dictionary) dictionarySpinner.getSelectedItem();
        String word = searchText.getText().toString();
        if (!(word.isEmpty())) {
            if (!strategy.getStrategy().equals("define")) {
                executeTask(JDictClientRequest.MATCH(host, strategy, dict, word));
            } else {
                if (dict != null)
                  executeTask(JDictClientRequest.DEFINE(host, dict, word));
                else
                  executeTask(JDictClientRequest.DEFINE(host, word));
            }
        }
    }

    public void getDictionaryInfo(View view) {
        Dictionary dictionary = (Dictionary)
          dictionarySpinner.getSelectedItem();
        searchText.setText("");
        executeTask(JDictClientRequest.DICT_INFO(host, dictionary));
    }

    public boolean traverseHistory(DefinitionHistory.Direction direction) {
        HistoryEntry entry;
        if (DefinitionHistory.Direction.BACK == direction)
          entry = history.back();
        else
          entry = history.forward();
        if (entry != null)
          displayHistoryEntry(entry);
        invalidateOptionsMenu();
        return (entry != null);
    }

    public void setDictionarySpinnerData(List<Dictionary> list) {
        dictionarySpinner.setAdapter(new DictionarySpinnerAdapter(this, list));
    }

    public void setStrategySpinnerData(List<Strategy> list) {
        strategySpinner.setAdapter(new StrategySpinnerAdapter(this, list));
    }

    public void setSelectedDictionary(Dictionary dictionary) {
        if (dictionary == null || dictionary.getDatabase() == null)
          selectedDictionary = 0;
        else {
            SpinnerAdapter adapter = dictionarySpinner.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                Dictionary item = (Dictionary) adapter.getItem(i);
                if (item.getDatabase() == null) continue;
                if (item.getDatabase().equals(dictionary.getDatabase())) {
                    selectedDictionary = i;
                    break;
                }
            }
        }
        dictionarySpinner.setSelection(selectedDictionary);
    }

    public void setSelectedStrategy(Strategy strategy) {
        if (strategy == null)
          selectedStrategy = 0;
        else {
            SpinnerAdapter adapter = strategySpinner.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                Strategy item = (Strategy) adapter.getItem(i);
                if (item.getStrategy().equals(strategy.getStrategy())) {
                    selectedStrategy = i;
                    break;
                }
            }
        }
        strategySpinner.setSelection(selectedStrategy);
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
        Strategy strategy = entry.getStrategy();
        setSelectedDictionary(entry.getDictionary());
        setSelectedStrategy(strategy);
        if (strategy.getStrategy().equals("define"))
          resultView.setWordWrap(false);
        else
          resultView.setWordWrap(true);
        searchText.setText(entry.getWord());
        resultView.setText(entry.getText());
    }

    private CharSequence displayDefinitions(List<Definition> definitions) {
        resultView.setWordWrap(false);
        if (definitions == null)
          resultView.setText(getString(R.string.result_definitions));
        else {
            SpannableStringBuilder stringBuilder =
              new SpannableStringBuilder();
            for (Definition definition : definitions) {
                stringBuilder.append(Html.fromHtml(
                    "<b>" +
                        definition.getDictionary().getDescription() +
                        "</b><br>"
                ));
                stringBuilder.append(DefinitionParser.parse(definition));
                stringBuilder.append("\n");
            }
            resultView.setText(stringBuilder);
        }
        return resultView.getText();
    }

    private CharSequence displayMatches(List<Match> matches) {
        resultView.setWordWrap(true);
        if (matches == null)
          resultView.setText(getString(R.string.result_matches));
        else {
            Map<Dictionary, List<String>> map = buildMatchMap(matches);
            SpannableStringBuilder stringBuilder =
              new SpannableStringBuilder();
            for (Dictionary dictionary : map.keySet()) {
                List<String> list = map.get(dictionary);
                stringBuilder.append(Html.fromHtml(
                        "<b>" + dictionary.getDescription() + "</b><br>"
                    ));
                int i = 0; int count = list.size();
                for (String word : list) {
                    stringBuilder.append(new WordSpan(word, dictionary)
                                                     .toCharSequence());
                    if (i != count - 1)
                      stringBuilder.append(", ");
                    i += 1;
                }
                stringBuilder.append(Html.fromHtml("<br><br>"));
            }
            resultView.setText(stringBuilder);
        }
        return resultView.getText();
    }

    private Map<Dictionary, List<String>> buildMatchMap(List<Match> matches) {
        HashMap<Dictionary, List<String>> map = new HashMap<>();
        Adapter adapter = dictionarySpinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            Dictionary dictionary = (Dictionary) adapter.getItem(i);
            if (dictionary.getHost() == null) continue;
            LinkedList<String> list = new LinkedList<>();
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

    private void displayDictionaryInfo(String dictionaryInfo) {
        resultView.setText("");
        if (dictionaryInfo == null)
          resultView.setText(getString(R.string.result_dict_info));
        else
          resultView.setText(dictionaryInfo);
    }

    private void disableInput() {
        searchText.setEnabled(false);
        dictionarySpinner.setEnabled(false);
        strategySpinner.setEnabled(false);
        infoButtonState = infoButton.isEnabled();
        infoButton.setEnabled(false);
        searchButtonState = searchButton.isEnabled();
        searchButton.setEnabled(false);
    }

    private void enableInput() {
        searchText.setEnabled(true);
        dictionarySpinner.setEnabled(true);
        strategySpinner.setEnabled(true);
        infoButton.setEnabled(infoButtonState);
        searchButton.setEnabled(searchButtonState);
    }

    private EditText setupSearchText() {
        final EditText searchText = findViewById(R.id.search_text);
        final ImageButton searchButton = findViewById(R.id.search_button);
        if (searchText.getText().length() == 0)
          searchButton.setEnabled(false);
        searchText.addTextChangedListener(
            new TextWatcher () {
                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    if (s.toString().trim().length() > 0)
                      searchButton.setEnabled(true);
                    else
                      searchButton.setEnabled(false);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) { }
            }
        );
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

    private Spinner setupDictionarySpinner() {
    Spinner spinner = findViewById(R.id.dictionary_spinner);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View
                                       selectedItemView, int position,
                                       long id) {
                ImageButton button =
                  findViewById(R.id.dictionary_info_button);
                Dictionary currentDictionary = (Dictionary)
                  parent.getSelectedItem();

                if (currentDictionary.getDatabase() != null)
                  button.setEnabled(true);
                else
                  button.setEnabled(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                 ImageButton button =
                   findViewById(R.id.dictionary_info_button);
                 button.setEnabled(false);
            }
        });
        return spinner;
    }

    private Spinner setupStrategySpinner() {
        final Spinner strategySpinner = findViewById(R.id.strategy_spinner);
        ArrayList<Strategy> list = new ArrayList<>();
        list.add(Strategy.DEFINE);
        strategySpinner.setAdapter(new StrategySpinnerAdapter(this, list));
        return strategySpinner;
    }
}
