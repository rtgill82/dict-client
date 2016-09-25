package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.lonestar.sdf.locke.apps.dict.dictclient.R;
import org.lonestar.sdf.locke.libs.dict.Dictionary;

import java.sql.SQLException;
import java.util.List;

public class MainActivity extends FragmentActivity
{
  private DictionaryHost currentHost;
  private DictionaryHostCache hostCache;
  private DefinitionHistory history = DefinitionHistory.getInstance();

  @SuppressLint("NewApi")
  @Override
  protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      EditText search_text = (EditText) findViewById(R.id.search_text);
      search_text.setOnKeyListener(new View.OnKeyListener() {
          public boolean onKey(View v, int keyCode, KeyEvent event)
            {
              switch (keyCode)
                {
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

      Spinner dictionary_spinner = (Spinner) findViewById(R.id.dictionary_spinner);
      dictionary_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View
                                     selectedItemView, int position, long id)
            {
              Button dictinfo_button =
                (Button) findViewById(R.id.dictinfo_button);
              TextView definition_view =
                (TextView) findViewById(R.id.definition_view);
              Dictionary currentDictionary =
                (Dictionary) parent.getSelectedItem();

              if (currentDictionary.getDatabase() != null)
                {
                  dictinfo_button.setEnabled(true);
                } else {
                  dictinfo_button.setEnabled(false);
                }

                definition_view.setText("");
            }

          @Override
          public void onNothingSelected(AdapterView<?> parent)
            {
              Button dictinfo_button =
                (Button) findViewById(R.id.dictinfo_button);
              TextView definition_view =
                (TextView) findViewById(R.id.definition_view);

              dictinfo_button.setEnabled(false);
              definition_view.setText("");
            }
      });

      hostCache = new DictionaryHostCache();
      try {
          setCurrentHost(DatabaseManager.getInstance().getCurrentHost(this));
      } catch (SQLException e) {
          ErrorDialog.show(this, e.getMessage());
      }

      refreshDictionaries();
    }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
    {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.activity_main, menu);
      return super.onCreateOptionsMenu(menu);
    }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu)
    {
      MenuItem backButton = menu.findItem(R.id.menu_back);
      MenuItem forwardButton = menu.findItem(R.id.menu_forward);

      if (history.isEmpty())
        {
          backButton.setEnabled(false);
          forwardButton.setEnabled(false);
        } else {
          if (history.canGoBack())
            {
              backButton.setEnabled(true);
            } else {
              backButton.setEnabled(false);
            }

          if (history.canGoForward())
            {
              forwardButton.setEnabled(true);
            } else {
              forwardButton.setEnabled(false);
            }
        }

      return super.onPrepareOptionsMenu(menu);
    }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
    {
      switch(item.getItemId())
        {
        case R.id.menu_back:
          traverseHistory(DefinitionHistory.Direction.BACK);
          break;

        case R.id.menu_forward:
          traverseHistory(DefinitionHistory.Direction.FORWARD);
          break;

        case R.id.menu_host:
          HostDialog.show(this);
          break;

        case R.id.menu_about:
          AboutDialog.show(this);
          break;

        default:
          return super.onOptionsItemSelected(item);
        }

      return true;
    }
  public DictionaryHost getCurrentHost()
    {
      return currentHost;
    }

  public void setCurrentHost(DictionaryHost host)
    {
      String hostname = "No Host Currently Selected";

      hostCache.add(currentHost);
      currentHost = host;
      if (host != null)
        {
          hostname = host.getHostName();
          if (host.getDictionaries() == null)
            refreshDictionaries();
          else
            setDictionarySpinnerData(host.getDictionaries());
        }
      setTitle(getString(R.string.app_name) + " - " + hostname);
      reset();
    }

  public void setCurrentHostById(Integer id)
    {
      DictionaryHost host = hostCache.getHostById(id);

      if (host == null)
        {
          try {
              host = DatabaseManager.getInstance().getHostById(id);
          } catch (SQLException e) {
              ErrorDialog.show(this, e.getMessage());
          }
        }

      setCurrentHost(host);
    }

  public void setDictionaries(List<Dictionary> list)
    {
      if (currentHost != null)
        {
          currentHost.setDictionaries(list);
          setDictionarySpinnerData(list);
        }
    }

  public void lookupWord(View view)
    {
      EditText editText = (EditText) findViewById(R.id.search_text);
      Spinner dictionary_spinner =
        (Spinner) findViewById(R.id.dictionary_spinner);
      Dictionary dict = (Dictionary) dictionary_spinner.getSelectedItem();
      String word = editText.getText().toString();
      if (!(word.isEmpty()))
        {
          editText.selectAll();
          if (dict != null)
            {
              new JDictClientTask(
                  this,
                  JDictClientRequest.DEFINE(dict, word))
                .execute();
            } else {
              new JDictClientTask(
                  this,
                  JDictClientRequest.DEFINE(word))
                .execute();
            }
        }
    }

  public void getDictionaryInfo(View view)
    {
      Spinner dictionary_spinner =
        (Spinner) findViewById(R.id.dictionary_spinner);
      Dictionary dictionary =
        (Dictionary) dictionary_spinner.getSelectedItem();

      new JDictClientTask(
          this,
          JDictClientRequest.DICT_INFO(dictionary))
        .execute();
    }

  public void traverseHistory(DefinitionHistory.Direction direction)
    {
      HistoryEntry entry;

      if (DefinitionHistory.Direction.BACK == direction)
        entry = history.back();
      else
        entry = history.forward();

      if (entry != null)
        displayHistoryEntry(entry);

      supportInvalidateOptionsMenu();
    }

  public void refreshDictionaries()
    {
      new JDictClientTask(this, JDictClientRequest.DICT_LIST()).execute();
    }

  public void reset()
    {
      EditText search_text = (EditText) findViewById(R.id.search_text);
      search_text.setText("");
      history.clear();
      supportInvalidateOptionsMenu();
    }

  private void setDictionarySpinnerData(List<Dictionary> list)
    {
      ((Spinner) findViewById(R.id.dictionary_spinner)).setAdapter(
          new DictionarySpinnerAdapter(this, list));
    }

  private void displayHistoryEntry(HistoryEntry entry)
    {
      EditText search_text = (EditText) findViewById(R.id.search_text);
      TextView definition_view = (TextView) findViewById(R.id.definition_view);
      search_text.setText(entry.getWord());
      definition_view.setText(entry.getDefinitionText());
    }
}
