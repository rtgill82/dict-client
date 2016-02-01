package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import org.lonestar.sdf.locke.libs.dict.Dictionary;

public class JDictClientRequest {
    public enum JDictClientCommand {
        DEFINE,
        DICT_INFO,
        DICT_LIST
    }

    private final JDictClientCommand command;
    private final Dictionary dictionary;
    private final String word;

    private JDictClientRequest(JDictClientCommand command, Dictionary dictionary, String word) {
        this.command = command;
        this.dictionary = dictionary;
        this.word = word;
    }

    public static JDictClientRequest DEFINE(String word) {
        return new JDictClientRequest(JDictClientCommand.DEFINE, new Dictionary(null, "All Dictionaries"), word);
    }

    public static JDictClientRequest DEFINE(Dictionary dictionary, String word) {
        return new JDictClientRequest(JDictClientCommand.DEFINE, dictionary, word);
    }

    public static JDictClientRequest DICT_LIST() {
        return new JDictClientRequest(JDictClientCommand.DICT_LIST, null, null);
    }

    public static JDictClientRequest DICT_INFO(Dictionary dictionary) {
        return new JDictClientRequest(JDictClientCommand.DICT_INFO, dictionary, null);
    }

    public JDictClientCommand getCommand() {
        return command;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public String getWord() {
        return word;
    }
}
