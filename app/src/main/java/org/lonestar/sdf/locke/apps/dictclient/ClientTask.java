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

import org.lonestar.sdf.locke.libs.jdictclient.Command;
import org.lonestar.sdf.locke.libs.jdictclient.Definition;
import org.lonestar.sdf.locke.libs.jdictclient.JDictClient;
import org.lonestar.sdf.locke.libs.jdictclient.Match;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.lonestar.sdf.locke.apps.dictclient.ClientTask.ClientCommand.*;

class ClientTask extends AsyncTask<Void,Void,ClientTask.Result> {
    public enum ClientCommand {
        DEFINE,
        MATCH,
        DICT_STRAT_LIST,
        DICT_INFO
    }

    final private WeakReference<MainActivity> mContext;
    final private Request mRequest;

    private Exception mException;
    private ProgressDialog mProgressDialog;

    private static final Map<ClientCommand,String>
      sMessages = new EnumMap<>(ClientCommand.class);

    public ClientTask(MainActivity context, Request request) {
        super();
        mContext = new WeakReference<>(context);
        mRequest = request;

        if (sMessages.isEmpty()) {
            sMessages.put(DEFINE, context.getString(R.string.task_define));
            sMessages.put(MATCH, context.getString(R.string.task_match));
            sMessages.put(DICT_STRAT_LIST, context.getString(R.string.task_dict_list));
            sMessages.put(DICT_INFO, context.getString(R.string.task_dict_info));
        }
    }

    public static Request DEFINE(Host host, String word) {
        return new Request(host, ClientCommand.DEFINE, word, new Dictionary());
    }

    public static Request DEFINE(Host host, String word,
                                 Dictionary dictionary) {
        return new Request(host, ClientCommand.DEFINE, word, dictionary);
    }

    public static Request MATCH(Host host, String word, Dictionary dictionary,
                                Strategy strategy) {
        return new Request(host, ClientCommand.MATCH, word,
                           dictionary, strategy);
    }

    public static Request DICT_LIST(Host host) {
        return new Request(host, ClientCommand.DICT_STRAT_LIST,
                           null, null, null);
    }

    public static Request DICT_INFO(Host host, Dictionary dictionary) {
        return new Request(host, ClientCommand.DICT_INFO, null, dictionary);
    }

    @Override
    protected void onPreExecute() {
        if (mRequest.displayWaitMessage()) {
            mProgressDialog = ProgressDialog.show(
              mContext.get(),
              "Waiting",
              sMessages.get(mRequest.getCommand()),
              true
            );
        }
    }

    protected Result doInBackground(Void... voids) {
        try {
            switch (mRequest.getCommand()) {
              case DEFINE:
                return new Result(
                  mRequest,
                  getDefinitions(mRequest.getWord(), mRequest.getDictionary())
                );
              case MATCH:
                return new Result(
                  mRequest,
                  getMatches(mRequest.getWord(), mRequest.getDictionary(),
                             mRequest.getStrategy())
                );
              case DICT_STRAT_LIST:
                Pair<List<Dictionary>, List<Strategy>> results =
                  getDictionariesAndStrategies();
                return new Result(
                  mRequest,
                  results.t,
                  results.u
                );
              case DICT_INFO:
                return new Result(
                  mRequest,
                  getDictionaryInfo(mRequest.getDictionary())
                );
              default:
                break;
            }
        } catch (IOException e) {
            mException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Result result) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mContext.get().onTaskFinished(result, mException);
    }

    private Pair<List<Dictionary>, List<Strategy>>
    getDictionariesAndStrategies() throws IOException {
        Host host = mRequest.getHost();
        JDictClient client =
          JDictClient.connect(host.getName(), host.getPort());
        Command.Builder builder = new Command.Builder(Command.Type.OTHER)
                                    .setCommandString("SHOW DB\nSHOW STRAT");
        DictStratListResponseHandler handler =
          new DictStratListResponseHandler(host);
        builder.setResponseHandler(handler);
        builder.build().execute(client.getConnection());
        client.close();
        return (Pair<List<Dictionary>, List<Strategy>>) handler.getResults();
    }

    @Override
    protected void onCancelled(Result result) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private List<Definition> getDefinitions(String word, Dictionary dictionary)
          throws IOException {
        Host host = mRequest.getHost();
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
          throws IOException {
        Host host = mRequest.getHost();
        JDictClient client =
          JDictClient.connect(host.getName(), host.getPort());
        List <Match> matches = client.match(word,
                                            strategy.getName(),
                                            dictionary.getName());
        client.close();
        return matches;
    }

    private String getDictionaryInfo(Dictionary dictionary)
          throws IOException {
        Host host = mRequest.getHost();
        JDictClient client =
          JDictClient.connect(host.getName(), host.getPort());
        String dictInfo = client.getDatabaseInfo(dictionary.getName());
        client.close();
        return dictInfo;
    }

    public static class Request {
        private final Host host;
        private final ClientCommand command;
        private final Dictionary dictionary;
        private final Strategy strategy;
        private final String word;

        boolean displayWaitMessage = true;

        private Request(Host host, ClientCommand command, String word,
                        Dictionary dictionary, Strategy strategy) {
            this.host = host;
            this.command = command;
            this.word = word;
            this.dictionary = dictionary;
            this.strategy = strategy;
        }

        private Request(Host host, ClientCommand command, String word,
                        Dictionary dictionary) {
            this(host, command, word, dictionary, null);
        }

        public boolean displayWaitMessage() {
            return this.displayWaitMessage;
        }

        public Host getHost() {
            return host;
        }

        public ClientCommand getCommand() {
            return command;
        }

        public Dictionary getDictionary() {
            return dictionary;
        }

        public Strategy getStrategy() {
            return strategy;
        }

        public String getWord() {
            return word;
        }
    }

    public class Result {
        private final Request request;
        private final List<Definition> definitions;
        private final List<Dictionary> dictionaries;
        private final List<Strategy> strategies;
        private final List<Match> matches;
        private final String dictionaryInfo;

        private Result(Request request, List<?> list1,
                       List<?> list2, String dictionaryInfo) {
            this.request = request;
            this.dictionaryInfo = dictionaryInfo;

            switch (this.request.getCommand()) {
              case DEFINE:
                definitions = (List<Definition>) list1;
                dictionaries = null;
                strategies = null;
                matches = null;
                break;
              case MATCH:
                definitions = null;
                dictionaries = null;
                strategies = null;
                matches = (List<Match>) list1;
                break;
              case DICT_STRAT_LIST:
                dictionaries = (List<Dictionary>) list1;
                definitions = null;
                strategies = (List<Strategy>) list2;
                matches = null;
                break;
              default:
                definitions = null;
                dictionaries = null;
                strategies = null;
                matches = null;
                break;
            }
        }

        public Result(Request request, List<?> list) {
            this(request, list, null, null);
        }

        public Result(Request request, String dictionaryInfo) {
            this(request, null, null, dictionaryInfo);
        }

        public Result(Request request, List<Dictionary> dictionaries,
                      List<Strategy> strategies) {
            this(request, dictionaries, strategies, null);
        }

        public Request getRequest() {
            return request;
        }

        public List<Dictionary> getDictionaries() {
            return dictionaries;
        }

        public List<Definition> getDefinitions() {
            return definitions;
        }

        public List<Match> getMatches() {
            return matches;
        }

        public List<Strategy> getStrategies() {
            return strategies;
        }

        public String getDictionaryInfo() {
            return dictionaryInfo;
        }
    }
}
