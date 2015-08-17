package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "dictionaries")
public class Dictionary {

    @DatabaseField(canBeNull = false, foreign = true)
    private DictionaryServer server;
    @DatabaseField(canBeNull = false)
    private String database;
    @DatabaseField(canBeNull = true)
    private String description;

    public Dictionary() {
    }

    public Dictionary(DictionaryServer server, String database, String description) {
        this.server = server;
        this.database = database;
        this.description = description;
    }

    public DictionaryServer getServer() {
        return server;
    }

    public void setServer(DictionaryServer server) {
        this.server = server;
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
