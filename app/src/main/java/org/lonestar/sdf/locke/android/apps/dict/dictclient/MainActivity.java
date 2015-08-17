package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

public class MainActivity extends FragmentActivity {
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner dictionary_spinner = (Spinner) findViewById(R.id.dictionary_spinner);
        dictionary_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedItemView, int position, long id)
            {
                Button dictinfo_button = (Button) findViewById(R.id.dictinfo_button);
                TextView definition_view = (TextView) findViewById(R.id.definition_view);
                Dictionary currentDictionary = (Dictionary) parent.getSelectedItem();
                if (currentDictionary.getDatabase() != null) {
                    dictinfo_button.setEnabled(true);
                } else {
                    dictinfo_button.setEnabled(false);
                }
                definition_view.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                Button dictinfo_button = (Button) findViewById(R.id.dictinfo_button);
                TextView definition_view = (TextView) findViewById(R.id.definition_view);
                dictinfo_button.setEnabled(false);
                definition_view.setText("");
            }
        });

        new ListDictionariesTask(this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.menu_settings:
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            break;

        case R.id.menu_about:
            AboutDialog about = new AboutDialog(this);
            about.setTitle("About");
            about.show();
            break;

        default:
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void lookupWord(View view) {
        EditText editText = (EditText) findViewById(R.id.search_text);
        Spinner dictionary_spinner = (Spinner) findViewById(R.id.dictionary_spinner);
        String dict = ((Dictionary) dictionary_spinner.getSelectedItem()).getDatabase();
        String word = editText.getText().toString();
        if (dict != null) {
            new DefineTask(this).execute(dict, word);
        } else {
            new DefineTask(this).execute(word);
        }
    }

    public void getDictionaryInfo(View view) {
        Spinner dictionary_spinner = (Spinner) findViewById(R.id.dictionary_spinner);
        Dictionary dictionary = (Dictionary) dictionary_spinner.getSelectedItem();
        new DictionaryInfoTask(this).execute(dictionary);
    }
}
