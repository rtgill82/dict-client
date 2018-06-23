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
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import org.lonestar.sdf.locke.libs.jdictclient.Definition;
import org.lonestar.sdf.locke.libs.jdictclient.Match;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.lonestar.sdf.locke.apps.dictclient.DonateNotificationService.DONATE_ACTION;
import static org.lonestar.sdf.locke.apps.dictclient.DonateNotificationService.DONATE_SEEN;

public class MainActivity extends Activity {
    final private static String SELECTED_DICTIONARY = "SELECTED_DICTIONARY";
    final private static String SELECTED_STRATEGY = "SELECTED_STRATEGY";

    final private DefinitionHistory mHistory = DefinitionHistory.getInstance();

    private Host mHost;
    private ClientTask mRunningTask;

    private ResultView mResultView;
    private EditText mSearchText;
    private Spinner mDictionarySpinner;
    private Spinner mStrategySpinner;
    private ImageButton mInfoButton;
    private ImageButton mSearchButton;

    @SuppressWarnings("FieldCanBeLocal")
    private OnSharedPreferenceChangeListener mPreferenceChangeListener;

    private int mSelectedDictionary = -1;
    private int mSelectedStrategy = -1;
    private boolean mWordWrap;
    private boolean mInfoButtonState;
    private boolean mSearchButtonState;
    private boolean mHostChanged;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mResultView = findViewById(R.id.result_view);
        mSearchText = setupSearchText();
        mDictionarySpinner = setupDictionarySpinner();
        mStrategySpinner = setupStrategySpinner();
        mInfoButton = findViewById(R.id.dictionary_info_button);
        mSearchButton = findViewById(R.id.search_button);

        DictClient app = (DictClient) getApplication();
        app.setOnHostChangeListener(
          new DictClient.OnHostChangeListener() {
              @Override
              public void onHostChange(Host host) {
                  mHostChanged = true;
              }
          }
        );

        mPreferenceChangeListener =
          new OnSharedPreferenceChangeListener() {
              public void onSharedPreferenceChanged(
                SharedPreferences preferences,
                String key
              ) {
                  String prefKey = getString(R.string.pref_key_word_wrap);
                  boolean value =
                    getResources().getBoolean(R.bool.pref_value_word_wrap);
                  if (key.equals(prefKey)) {
                      mWordWrap = preferences.getBoolean(prefKey, value);
                      mResultView.setWordWrap(mWordWrap);
                      mResultView.invalidate();
                  }
              }
          };
        PreferenceManager.getDefaultSharedPreferences(this)
          .registerOnSharedPreferenceChangeListener(
                  mPreferenceChangeListener
          );

        if (savedInstanceState != null) {
            mSelectedDictionary = savedInstanceState.getInt(SELECTED_DICTIONARY);
            mSelectedStrategy = savedInstanceState.getInt(SELECTED_STRATEGY);
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
        if (mRunningTask != null) {
            mRunningTask.cancel(true);
            mRunningTask = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DictClient app = (DictClient) getApplication();
        mHost = app.getCurrentHost();
        SharedPreferences preferences =
          PreferenceManager.getDefaultSharedPreferences(this);

        mWordWrap = preferences.getBoolean(
          getString(R.string.pref_key_word_wrap),
          getResources().getBoolean(R.bool.pref_value_word_wrap)
        );

        if (mHost != null) {
            Collection dictionaries = mHost.getDictionaries();
            Collection strategies = mHost.getStrategies();
            int cacheTime = Integer.parseInt(
              preferences.getString(
                getString(R.string.pref_key_cache_time),
                getString(R.string.pref_value_cache_time)
              )
            );

            Calendar expireTime = Calendar.getInstance();
            expireTime.setTime(mHost.getLastRefresh());
            expireTime.add(Calendar.DATE, cacheTime);

            if (dictionaries.isEmpty() ||
                  expireTime.before(Calendar.getInstance())) {
                refreshDictionaries();
            } else {
                setDictionarySpinnerData(dictionaries);
                setStrategySpinnerData(strategies);
            }

            if (mHostChanged) {
                mSearchButtonState = false;
                mSearchButton.setEnabled(false);
                mHistory.clear();
                invalidateOptionsMenu();
                mSearchText.setText("");
                mResultView.setText("");
                mHostChanged = false;
            }

            setTitle(mHost.getName());
            mDictionarySpinner.setSelection(mSelectedDictionary);
            mStrategySpinner.setSelection(mSelectedStrategy);
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

        if (mHistory.isEmpty()) {
            backButton.setEnabled(false);
            forwardButton.setEnabled(false);
        } else {
            if (mHistory.canGoBack())
              backButton.setEnabled(true);
            else
              backButton.setEnabled(false);

            if (mHistory.canGoForward())
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
                      mDictionarySpinner.getSelectedItemPosition());
        bundle.putInt(SELECTED_STRATEGY,
                      mStrategySpinner.getSelectedItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectedDictionary = savedInstanceState.getInt(SELECTED_DICTIONARY);
        mSelectedStrategy = savedInstanceState.getInt(SELECTED_STRATEGY);
    }

    public void onTaskFinished(ClientTask.Result result,
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

        ClientTask.Request request = result.getRequest();
        mRunningTask = null;

        CharSequence text;
        HistoryEntry entry;
        switch (request.getCommand()) {
          case DEFINE:
            text = displayDefinitions(result.getDefinitions());
            entry = new HistoryEntry(
              request.getWord(),
              ((Dictionary) mDictionarySpinner.getSelectedItem()),
              ((Strategy) mStrategySpinner.getSelectedItem()),
              text
            );
            mHistory.add(entry);
            invalidateOptionsMenu();
            break;

          case MATCH:
            text = displayMatches(result.getMatches());
            entry = new HistoryEntry(
              request.getWord(),
              ((Dictionary) mDictionarySpinner.getSelectedItem()),
              ((Strategy) mStrategySpinner.getSelectedItem()),
              text
            );
            mHistory.add(entry);
            invalidateOptionsMenu();
            break;

          case DICT_INFO:
            displayDictionaryInfo(result.getDictionaryInfo());
            break;

          case DICT_STRAT_LIST:
            Host host = request.getHost();
            host.setDictionaries(result.getDictionaries());
            host.setStrategies(result.getStrategies());
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
        Strategy strategy = (Strategy) mStrategySpinner.getSelectedItem();
        Dictionary dict = (Dictionary) mDictionarySpinner.getSelectedItem();
        String word = mSearchText.getText().toString();
        if (!(word.isEmpty())) {
            if (!strategy.getName().equals("define")) {
                executeTask(ClientTask.MATCH(mHost, word, dict, strategy));
            } else {
                if (dict != null)
                  executeTask(ClientTask.DEFINE(mHost, word, dict));
                else
                  executeTask(ClientTask.DEFINE(mHost, word));
            }
        }
    }

    public void getDictionaryInfo(View view) {
        Dictionary dictionary = (Dictionary)
          mDictionarySpinner.getSelectedItem();
        mSearchText.setText("");
        executeTask(ClientTask.DICT_INFO(mHost, dictionary));
    }

    public boolean traverseHistory(DefinitionHistory.Direction direction) {
        HistoryEntry entry;
        if (DefinitionHistory.Direction.BACK == direction)
          entry = mHistory.back();
        else
          entry = mHistory.forward();
        if (entry != null)
          displayHistoryEntry(entry);
        invalidateOptionsMenu();
        return (entry != null);
    }

    public void setDictionarySpinnerData(Collection<Dictionary> collection) {
        ArrayList<Dictionary> list = new ArrayList<>(collection);
        list.add(0, Dictionary.ALL_DICTIONARIES);
        ArrayAdapter<Dictionary> adapter = new ArrayAdapter<>(
          this, android.R.layout.simple_spinner_item, list
        );
        mDictionarySpinner.setAdapter(adapter);
    }

    public void setStrategySpinnerData(Collection<Strategy> collection) {
        ArrayList<Strategy> list = new ArrayList<>(collection);
        mStrategySpinner.setAdapter(
          new StrategySpinnerAdapter(
            this, android.R.layout.simple_spinner_item, list
          ));
    }

    public void setSelectedDictionary(Dictionary dictionary) {
        if (dictionary == null || dictionary.getName() == null)
          mSelectedDictionary = 0;
        else {
            SpinnerAdapter adapter = mDictionarySpinner.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                Dictionary item = (Dictionary) adapter.getItem(i);
                if (item.getName() == null) continue;
                if (item.getName().equals(dictionary.getName())) {
                    mSelectedDictionary = i;
                    break;
                }
            }
        }
        mDictionarySpinner.setSelection(mSelectedDictionary);
    }

    public void setSelectedStrategy(Strategy strategy) {
        if (strategy == null)
          mSelectedStrategy = 0;
        else {
            SpinnerAdapter adapter = mStrategySpinner.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                Strategy item = (Strategy) adapter.getItem(i);
                if (item.getName().equals(strategy.getName())) {
                    mSelectedStrategy = i;
                    break;
                }
            }
        }
        mStrategySpinner.setSelection(mSelectedStrategy);
    }

    private void executeTask(ClientTask.Request request) {
        disableInput();
        mRunningTask = new ClientTask(this, request);
        mRunningTask.execute();
    }

    private void refreshDictionaries() {
        executeTask(ClientTask.DICT_LIST(mHost));
    }

    private void displayHistoryEntry(HistoryEntry entry) {
        Strategy strategy = entry.getStrategy();
        setSelectedDictionary(entry.getDictionary());
        setSelectedStrategy(strategy);
        if (strategy.getName().equals("define"))
          mResultView.setWordWrap(mWordWrap);
        else
          mResultView.setWordWrap(true);
        mSearchText.setText(entry.getWord());
        mResultView.setText(entry.getText());
    }

    private CharSequence displayDefinitions(List<Definition> definitions) {
        mResultView.setWordWrap(mWordWrap);
        if (definitions == null)
          mResultView.setText(getString(R.string.result_definitions));
        else {
            SpannableStringBuilder stringBuilder =
              new SpannableStringBuilder();
            for (Definition definition : definitions) {
                stringBuilder.append(Html.fromHtml(
                    "<b>" +
                        definition.getDatabase().getDescription() +
                        "</b><br>"
                ));
                stringBuilder.append(DefinitionParser.parse(definition));
                stringBuilder.append("\n");
            }
            mResultView.setText(stringBuilder);
        }
        return mResultView.getText();
    }

    private CharSequence displayMatches(List<Match> matches) {
        mResultView.setWordWrap(true);
        if (matches == null)
          mResultView.setText(getString(R.string.result_matches));
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
            mResultView.setText(stringBuilder);
        }
        return mResultView.getText();
    }

    private Map<Dictionary, List<String>> buildMatchMap(List<Match> matches) {
        HashMap<Dictionary, List<String>> map = new HashMap<>();
        Adapter adapter = mDictionarySpinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            Dictionary dictionary = (Dictionary) adapter.getItem(i);
            if (dictionary == Dictionary.ALL_DICTIONARIES) continue;
            LinkedList<String> list = new LinkedList<>();
            map.put(dictionary, list);
            for (Match match : matches) {
                if (dictionary.getName().equals(match.getDatabase()))
                  list.add(match.getWord());
            }
            if (list.size() == 0)
              map.remove(dictionary);
        }
        return map;
    }

    private void displayDictionaryInfo(String dictionaryInfo) {
        mResultView.setText("");
        if (dictionaryInfo == null)
          mResultView.setText(getString(R.string.result_dict_info));
        else
          mResultView.setText(dictionaryInfo);
    }

    private void disableInput() {
        mSearchText.setEnabled(false);
        mDictionarySpinner.setEnabled(false);
        mStrategySpinner.setEnabled(false);
        mInfoButtonState = mInfoButton.isEnabled();
        mInfoButton.setEnabled(false);
        mSearchButtonState = mSearchButton.isEnabled();
        mSearchButton.setEnabled(false);
    }

    private void enableInput() {
        mSearchText.setEnabled(true);
        mDictionarySpinner.setEnabled(true);
        mStrategySpinner.setEnabled(true);
        mInfoButton.setEnabled(mInfoButtonState);
        mSearchButton.setEnabled(mSearchButtonState);
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

                if (currentDictionary.getName() != null)
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
        strategySpinner.setAdapter(
          new StrategySpinnerAdapter(
            this, android.R.layout.simple_spinner_item,
            new ArrayList<Strategy>()
          ));
        return strategySpinner;
    }
}
