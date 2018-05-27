/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.lonestar.sdf.locke.libs.dict.Definition;
import org.lonestar.sdf.locke.libs.dict.JDictClient;
import org.lonestar.sdf.locke.libs.dict.Match;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.lonestar.sdf.locke.apps.dictclient.JDictClientRequest.JDictClientCommand.*;

class JDictClientTask extends AsyncTask<Void,Void,JDictClientResult> {
    private MainActivity context;
    private JDictClientRequest request;
    private Exception exception;

    private ProgressDialog progressDialog;

    private static final Map<JDictClientRequest.JDictClientCommand,String> messages =
      new EnumMap<>(JDictClientRequest.JDictClientCommand.class);

    public JDictClientTask(MainActivity context, JDictClientRequest request) {
        super();
        this.context = context;
        this.request = request;

        if (messages.isEmpty()) {
            messages.put(DEFINE, context.getString(R.string.task_define));
            messages.put(MATCH, context.getString(R.string.task_match));
            messages.put(DICT_INFO, context.getString(R.string.task_dict_info));
            messages.put(DICT_LIST, context.getString(R.string.task_dict_list));
        }
    }

    @Override
    protected void onPreExecute() {
        if (request.displayWaitMessage()) {
            progressDialog = ProgressDialog.show(
                context,
                "Waiting",
                messages.get(request.getCommand()),
                true
            );
        }
    }

    protected JDictClientResult doInBackground(Void... voids) {
        try {
            switch (request.getCommand()) {
              case DEFINE:
                return new JDictClientResult(
                    request,
                    getDefinitions(request.getWord(), request.getDictionary())
                );
              case MATCH:
                return new JDictClientResult(
                    request,
                    getMatches(request.getWord(), request.getDictionary(),
                               request.getStrategy())
                );
              case DICT_INFO:
                return new JDictClientResult(
                    request,
                    getDictionaryInfo(request.getDictionary())
                );
              case DICT_LIST:
                return new JDictClientResult(
                    request,
                    getDictionaries(),
                    getStrategies()
                );
              default:
                break;
            }
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                exception = e;
            } else {
                throw(NullPointerException) e;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(JDictClientResult result) {
        if (progressDialog != null)
          progressDialog.dismiss();
        context.onTaskFinished(result, exception);
    }

    private List<Dictionary> getDictionaries()
          throws Exception {
        Host host = request.getHost();
        JDictClient dictClient =
          JDictClient.connect(host.getHostName(), host.getPort());

        List<Dictionary> dictionaries = new ArrayList<Dictionary>();
        dictionaries.add(Dictionary.ALL_DICTIONARIES);
        dictionaries.addAll(
            ClassConvert.convertDictionaryList(dictClient.getDictionaries(),
                                               host)
        );
        dictClient.close();
        return dictionaries;
    }

    private List<Strategy> getStrategies()
          throws Exception {
        Host host = request.getHost();
        JDictClient dictClient =
          JDictClient.connect(host.getHostName(), host.getPort());

        List<Strategy> strategies = new ArrayList<Strategy>();
        strategies.add(Strategy.DEFINE);
        strategies.addAll(
            ClassConvert.convertStrategyList(dictClient.getStrategies(),
                                             host)
        );
        dictClient.close();
        return strategies;
    }

    @Override
    protected void onCancelled(JDictClientResult result) {
        if (progressDialog != null)
          progressDialog.dismiss();
    }

    private List<Definition> getDefinitions(String word,
                                            Dictionary dictionary)
          throws Exception {
        Host host = request.getHost();
        JDictClient dictClient =
          JDictClient.connect(host.getHostName(), host.getPort());

        List<Definition> definitions;
        if (dictionary != null && dictionary.getDatabase() != null)
          definitions = dictClient.define(dictionary.getDatabase(), word);
        else
          definitions = dictClient.define(word);

        dictClient.close();
        return definitions;
    }

    private List<Match> getMatches(String word, Dictionary dictionary,
                                   Strategy strategy)
          throws Exception {
        Host host = request.getHost();
        JDictClient dictClient =
          JDictClient.connect(host.getHostName(), host.getPort());

        List <Match> matches = dictClient.match(strategy.getStrategy(),
                                                word,
                                                dictionary.getDatabase());
        dictClient.close();
        return matches;
    }

    private String getDictionaryInfo(Dictionary dictionary)
          throws Exception {
        Host host = request.getHost();
        JDictClient dictClient = JDictClient.connect(host.getHostName(),
                                                     host.getPort());
        String dictInfo =
          dictClient.getDictionaryInfo(dictionary.getDatabase());
        dictClient.close();
        return dictInfo;
    }
}
