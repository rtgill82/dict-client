package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.lonestar.sdf.locke.apps.dict.dictclient.R;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

public class DatabaseManager extends OrmLiteSqliteOpenHelper {
    final private static String DATABASE_NAME    = "dictclient.db";
    final private static int    DATABASE_VERSION = 1;

    private Context context;

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(SQLiteDatabase db, ConnectionSource cs) {
        Resources resources = this.context.getResources();
        try {
            TableUtils.createTable(cs, DictionaryServer.class);
            TableUtils.createTable(cs, Dictionary.class);
            Dao<DictionaryServer, Integer> dao = DaoManager.createDao(cs, DictionaryServer.class);
            Yaml yaml = new Yaml();
            InputStream stream = resources.openRawResource(R.raw.dictservers);
            ArrayList<DictionaryServer> list = (ArrayList<DictionaryServer>) yaml.load(stream);
            Iterator<DictionaryServer> iterator = list.iterator();

            while (iterator.hasNext()) {
                DictionaryServer server = iterator.next();
                dao.create(server);
            }
        } catch (SQLException e) {
            Log.d("DatabaseManager", "SQLException caught: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource cs, int oldVersion, int newVersion) {
        Log.d("DatabaseOpenHelper", "onUpgrade() called.");
    }

    public DictionaryServer getCurrentServer(Context context)
            throws SQLException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int default_host = Integer.parseInt(prefs.getString("default_host", context.getResources().getString(R.string.pref_value_default_host)));

        Dao<DictionaryServer, Integer> dao = DictClientApplication.getDatabaseManager().getDao(DictionaryServer.class);
        return dao.queryForId(default_host);
    }
}
