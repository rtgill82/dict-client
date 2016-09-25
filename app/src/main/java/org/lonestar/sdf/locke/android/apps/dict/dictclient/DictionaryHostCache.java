package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import java.util.LinkedList;

/**
 * Created by locke on 9/24/16.
 */

public class DictionaryHostCache extends LinkedList<DictionaryHost> {
    @Override
    public boolean add(DictionaryHost host)
    {
        boolean listHasHost = false;

        if (host == null)
            return false;

        for (DictionaryHost item : this) {
            if (host.getId() == item.getId()) {
                listHasHost = true;
                break;
            }
        }

        if (!listHasHost) {
            super.add(host);
            return true;
        }

        return false;
    }

    public DictionaryHost findHostById(Integer id)
    {
        for (DictionaryHost item : this) {
            if (item.getId() == id)
                return item;
        }

        return null;
    }
}
