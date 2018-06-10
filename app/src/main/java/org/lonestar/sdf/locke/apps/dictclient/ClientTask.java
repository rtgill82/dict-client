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

import org.lonestar.sdf.locke.libs.jdictclient.Definition;
import org.lonestar.sdf.locke.libs.jdictclient.JDictClient;
import org.lonestar.sdf.locke.libs.jdictclient.Match;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.lonestar.sdf.locke.apps.dictclient.ClientRequest.ClientCommand.*;

class ClientTask extends AsyncTask<Void,Void,ClientResult> {
    private MainActivity context;
    private ClientRequest request;
    private Exception exception;

    private ProgressDialog progressDialog;

    private static final Map<ClientRequest.ClientCommand,String>
      messages = new EnumMap<>(ClientRequest.ClientCommand.class);

    public ClientTask(MainActivity context, ClientRequest request) {
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

    protected ClientResult doInBackground(Void... voids) {
        try {
            switch (request.getCommand()) {
              case DEFINE:
                return new ClientResult(
                  request,
                  getDefinitions(request.getWord(), request.getDictionary())
                );
              case MATCH:
                return new ClientResult(
                  request,
                  getMatches(request.getWord(), request.getDictionary(),
                             request.getStrategy())
                );
              case DICT_LIST:
                return new ClientResult(
                  request,
                  getDictionaries(),
                  getStrategies()
                );
              case DICT_INFO:
                return new ClientResult(
                  request,
                  getDictionaryInfo(request.getDictionary())
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
    protected void onPostExecute(ClientResult result) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        context.onTaskFinished(result, exception);
    }

    private List<Dictionary> getDictionaries() throws Exception {
        Host host = request.getHost();
        JDictClient client =
          JDictClient.connect(host.getName(), host.getPort());

        List<Dictionary> dictionaries = new ArrayList<>();
        dictionaries.addAll(
          ClassConvert.convertDictionaryList(client.getDictionaries(), host)
        );
        client.close();
        return dictionaries;
    }

    private List<Strategy> getStrategies()
          throws Exception {
        Host host = request.getHost();
        JDictClient client =
          JDictClient.connect(host.getName(), host.getPort());

        List<Strategy> strategies = new ArrayList<>();
        strategies.addAll(
          ClassConvert.convertStrategyList(client.getStrategies(), host)
        );
        client.close();
        return strategies;
    }

    @Override
    protected void onCancelled(ClientResult result) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private List<Definition> getDefinitions(String word, Dictionary dictionary)
          throws Exception {
        Host host = request.getHost();
        JDictClient client =
          JDictClient.connect(host.getName(), host.getPort());

        List<Definition> definitions;
        if (dictionary != null && dictionary.getName() != null) {
            definitions = client.define(word, dictionary.getName());
        } else {
            definitions = client.define(word);
        }

        client.close();
        return definitions;
    }

    private List<Match> getMatches(String word, Dictionary dictionary,
                                   Strategy strategy)
          throws Exception {
        Host host = request.getHost();
        JDictClient client =
          JDictClient.connect(host.getName(), host.getPort());

        List <Match> matches = client.match(word,
                                            strategy.getName(),
                                            dictionary.getName());
        client.close();
        return matches;
    }

    private String getDictionaryInfo(Dictionary dictionary)
          throws Exception {
        Host host = request.getHost();
        JDictClient client =
          JDictClient.connect(host.getName(), host.getPort());

        String dictInfo = client.getDatabaseInfo(dictionary.getName());
        client.close();
        return dictInfo;
    }
}
