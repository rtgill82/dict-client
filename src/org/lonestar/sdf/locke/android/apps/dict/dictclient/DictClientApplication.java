package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import android.app.Application;

public class DictClientApplication extends Application {
	private DatabaseManager databaseManager;
	
	@Override
	public void onCreate() {
		super.onCreate();
		databaseManager = new DatabaseManager(getApplicationContext());
	}
	
	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}
}
