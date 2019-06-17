package org.lonestar.sdf.locke.apps.dictclient;

import android.content.Context;

class DictionaryInfoResults extends Results {
    public DictionaryInfoResults(String dictionaryInfo) {
        super(null, null, null,
              formatDictionaryInfo(dictionaryInfo));
    }

    private static CharSequence formatDictionaryInfo(String dictionaryInfo) {
        Context context = DictClient.getContext();
        if (dictionaryInfo == null) {
            return context.getString(R.string.result_dict_info);
        }
        return dictionaryInfo;
    }
}
