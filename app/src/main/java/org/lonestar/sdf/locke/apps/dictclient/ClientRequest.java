/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

class ClientRequest {
    public enum ClientCommand {
        DEFINE,
        MATCH,
        DICT_STRAT_LIST,
        DICT_INFO
    }

    private final Host host;
    private final ClientCommand command;
    private final Dictionary dictionary;
    private final Strategy strategy;
    private final String word;

    boolean displayWaitMessage = true;

    private ClientRequest(Host host, ClientCommand command, String word,
                          Dictionary dictionary, Strategy strategy) {
        this.host = host;
        this.command = command;
        this.word = word;
        this.dictionary = dictionary;
        this.strategy = strategy;
    }

    private ClientRequest(Host host, ClientCommand command, String word,
                          Dictionary dictionary) {
        this(host, command, word, dictionary, null);
    }

    public static ClientRequest DEFINE(Host host, String word) {
        return new ClientRequest(host, ClientCommand.DEFINE,
                                 word, new Dictionary());
    }

    public static ClientRequest DEFINE(Host host, String word,
                                       Dictionary dictionary) {
        return new ClientRequest(host, ClientCommand.DEFINE,
                                 word, dictionary);
    }

    public static ClientRequest MATCH(Host host, String word,
                                      Dictionary dictionary,
                                      Strategy strategy) {
        return new ClientRequest(host, ClientCommand.MATCH,
                                 word, dictionary, strategy);
    }

    public static ClientRequest DICT_LIST(Host host) {
        return new ClientRequest(host, ClientCommand.DICT_STRAT_LIST,
                                 null, null, null);
    }

    public static ClientRequest DICT_INFO(Host host, Dictionary dictionary) {
        return new ClientRequest(host, ClientCommand.DICT_INFO,
                                 null, dictionary);
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
