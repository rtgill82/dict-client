package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@SuppressWarnings("unused")
@DatabaseTable(tableName = "dictionaries")
public class Dictionary {

    @DatabaseField(canBeNull = false, foreign = true)
    private DictionaryHost host;
    @DatabaseField(canBeNull = false)
    private String database;
    @DatabaseField(canBeNull = true)
    private String description;

    public Dictionary() {
    }

    public Dictionary(DictionaryHost host, String database, String description) {
        this.host = host;
        this.database = database;
        this.description = description;
    }

    public DictionaryHost getHost() {
        return host;
    }

    public void setHost(DictionaryHost host) {
        this.host = host;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
