/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import static org.lonestar.sdf.locke.apps.dictclient.DonateNotificationService.DONATE_ACTION;
import static org.lonestar.sdf.locke.apps.dictclient.DonateNotificationService.DONATE_SEEN;

public class MainActivity extends AppCompatActivity {
    private static final String SELECTED_DICTIONARY = "SELECTED_DICTIONARY";
    private static final String SELECTED_STRATEGY = "SELECTED_STRATEGY";

    private final ResultsHistory mHistory = ResultsHistory.getInstance();

    private Host mHost;
    private ClientTask mRunningTask;

    private ResultsView mResultsView;
    private EditText mSearchText;
    private Spinner mDictionarySpinner;
    private Spinner mStrategySpinner;
    private ImageButton mInfoButton;
    private ImageButton mSearchButton;
    private ShareActionProvider mShareActionProvider;

    private int mSelectedDictionary = -1;
    private int mSelectedStrategy = -1;
    private boolean mInfoButtonState;
    private boolean mSearchButtonState;
    private boolean mHostChanged;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mResultsView = findViewById(R.id.results_view);
        mSearchText = createSearchText();
        mDictionarySpinner = createDictionarySpinner();
        mStrategySpinner = createStrategySpinner();
        mInfoButton = findViewById(R.id.dictionary_info_button);
        mSearchButton = findViewById(R.id.search_button);
        mSearchButton.setEnabled(false);

        DictClient app = (DictClient) getApplication();
        app.setOnHostChangeListener(
          new DictClient.OnHostChangeListener() {
              @Override
              public void onHostChange() {
                  mHostChanged = true;
              }
          }
        );

        if (savedInstanceState != null) {
            mSelectedDictionary =
              savedInstanceState.getInt(SELECTED_DICTIONARY);
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
                viewResults(new Results());
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
        MenuItem item = menu.findItem(R.id.menu_share);
        mShareActionProvider = (ShareActionProvider)
          MenuItemCompat.getActionProvider(item);
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
            if (mHistory.canGoBack()) {
                backButton.setEnabled(true);
            } else {
                backButton.setEnabled(false);
            }

            if (mHistory.canGoForward()) {
                forwardButton.setEnabled(true);
            } else {
                forwardButton.setEnabled(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
          case R.id.menu_back:
            traverseHistory(ResultsHistory.Direction.BACK);
            break;

          case R.id.menu_forward:
            traverseHistory(ResultsHistory.Direction.FORWARD);
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

    @Override
    public void onBackPressed() {
        if (!traverseHistory(ResultsHistory.Direction.BACK)) {
            super.onBackPressed();
        }
    }

    public void lookupWord(@SuppressWarnings("unused") View view) {
        String word = mSearchText.getText().toString();
        if (word.isEmpty()) return;

        Strategy strategy = (Strategy) mStrategySpinner.getSelectedItem();
        Dictionary dictionary = (Dictionary)
          mDictionarySpinner.getSelectedItem();
        if (!strategy.getName().equals("define")) {
            executeTask(ClientTask.MATCH(mHost, word, dictionary, strategy));
        } else {
            if (dictionary != null) {
                executeTask(ClientTask.DEFINE(mHost, word, dictionary));
            } else {
                executeTask(ClientTask.DEFINE(mHost, word));
            }
        }
    }

    public void getDictionaryInfo(@SuppressWarnings("unused") View view) {
        Dictionary dictionary = (Dictionary)
          mDictionarySpinner.getSelectedItem();
        mSearchText.setText("");
        executeTask(ClientTask.DICT_INFO(mHost, dictionary));
    }

    public OnTaskFinishedHandler getOnTaskFinishedHandler(boolean setFields) {
        return new OnTaskFinishedHandler(setFields);
    }

    private void executeTask(ClientTask.Request request) {
        disableInput();
        mRunningTask = new ClientTask(this, request,
                                      getOnTaskFinishedHandler(false));
        mRunningTask.execute();
    }

    private void refreshDictionaries() {
        executeTask(ClientTask.DICT_LIST(mHost));
    }

    private void viewResults(Results results) {
        setShareIntent(results.getText());
        mResultsView.setResults(results);
    }

    private void setShareIntent(CharSequence text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text.toString());
        mShareActionProvider.setShareIntent(intent);
    }

    private void setSelectedDictionary(Dictionary dictionary) {
        if (dictionary == null || dictionary == Dictionary.DEFAULT) {
            mSelectedDictionary = 0;
        } else {
            SpinnerAdapter adapter = mDictionarySpinner.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                Dictionary item = (Dictionary) adapter.getItem(i);
                if (item == Dictionary.DEFAULT) continue;
                if (item.getName().equals(dictionary.getName())) {
                    mSelectedDictionary = i;
                    break;
                }
            }
        }
        mDictionarySpinner.setSelection(mSelectedDictionary);
    }

    private void setSelectedStrategy(Strategy strategy) {
        if (strategy == null) {
            mSelectedStrategy = 0;
        } else {
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

    private void setDictionarySpinnerData(Collection<Dictionary> collection) {
        ArrayList<Dictionary> list = new ArrayList<>(collection);
        list.add(0, Dictionary.DEFAULT);
        ArrayAdapter<Dictionary> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, list
        );
        mDictionarySpinner.setAdapter(adapter);
    }

    private void setStrategySpinnerData(Collection<Strategy> collection) {
        ArrayList<Strategy> list = new ArrayList<>(collection);
        mStrategySpinner.setAdapter(
                new StrategySpinnerAdapter(
                    this, android.R.layout.simple_spinner_item, list
                ));
    }

    private boolean traverseHistory(ResultsHistory.Direction direction) {
        Results results;
        if (ResultsHistory.Direction.BACK == direction) {
            results = mHistory.back();
        } else {
            results = mHistory.forward();
        }
        if (results != null) {
            displayHistoryEntry(results);
        }
        invalidateOptionsMenu();
        return (results != null);
    }

    private void displayHistoryEntry(Results entry) {
        Strategy strategy = entry.getStrategy();
        setSelectedDictionary(entry.getDictionary());
        setSelectedStrategy(strategy);
        mSearchText.setText(entry.getWord());
        viewResults(entry);
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

    private EditText createSearchText() {
        EditText searchText = findViewById(R.id.search_text);
        searchText.addTextChangedListener(
            new TextWatcher () {
                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    ImageButton button = findViewById(R.id.search_button);
                    if (s.toString().trim().length() > 0) {
                        button.setEnabled(true);
                    } else {
                        button.setEnabled(false);
                    }
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

    private Spinner createDictionarySpinner() {
        Spinner spinner = findViewById(R.id.dictionary_spinner);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View
                                       selectedItemView, int position,
                                       long id) {
                ImageButton button = findViewById(R.id.dictionary_info_button);
                Dictionary currentDictionary = (Dictionary)
                  parent.getSelectedItem();

                if (currentDictionary != Dictionary.DEFAULT) {
                    button.setEnabled(true);
                } else {
                    button.setEnabled(false);
                }
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

    private Spinner createStrategySpinner() {
        Spinner strategySpinner = findViewById(R.id.strategy_spinner);
        strategySpinner.setAdapter(
          new StrategySpinnerAdapter(
            this, android.R.layout.simple_spinner_item,
            new ArrayList<Strategy>()
          ));
        return strategySpinner;
    }

    public class OnTaskFinishedHandler implements
      ClientTask.OnTaskFinishedHandler {
        private final boolean mSetFields;
        OnTaskFinishedHandler (boolean setFields) {
            mSetFields = setFields;
        }

        public void onTaskFinished(ClientTask.Result result,
                                   Exception exception) {
            mRunningTask = null;
            enableInput();

            if (exception != null) {
                showException(exception);
                return;
            }

            ClientTask.Request request = result.getRequest();
            if (mSetFields) setFields(result);

            Results results;
            switch (request.getCommand()) {
              case DEFINE:
                results = new DefinitionResults(
                    request.getWord(),
                    ((Dictionary) mDictionarySpinner.getSelectedItem()),
                    ((Strategy) mStrategySpinner.getSelectedItem()),
                    result.getDefinitions()
                );
                mHistory.add(results);
                viewResults(results);
                invalidateOptionsMenu();
                break;

              case MATCH:
                results = new MatchResults(
                    request.getWord(),
                    ((Dictionary) mDictionarySpinner.getSelectedItem()),
                    ((Strategy) mStrategySpinner.getSelectedItem()),
                    result.getMatches()
                );
                mHistory.add(results);
                viewResults(results);
                invalidateOptionsMenu();
                break;

              case DICT_INFO:
                results = new DictionaryInfoResults(result.getDictionaryInfo());
                viewResults(results);
                break;

              case DICT_STRATEGY_LIST:
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

        private void showException(Exception exception) {
            if (exception.getClass().equals(UnknownHostException.class)) {
                ErrorDialog.show(MainActivity.this, "Unknown host: " +
                                 exception.getMessage());
            } else {
                ErrorDialog.show(MainActivity.this, exception.toString());
            }
        }

        private void setFields(ClientTask.Result result) {
            ClientTask.Request request = result.getRequest();
            mSearchText.setText(request.getWord());
            mSearchText.selectAll();
            setSelectedDictionary(request.getDictionary());
            setSelectedStrategy(request.getStrategy());
        }
    }
}
