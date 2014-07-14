package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.lonestar.sdf.locke.apps.dict.dictclient.R;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
	private static SQLInitStatements stmts = null;

	public static DatabaseOpenHelper initialize(Context context)
			throws IOException {
		Resources resources = context.getResources();
		stmts = new SQLInitStatements(context);

		return new DatabaseOpenHelper(
				context,
				resources.getString(R.string.database_name),
				resources.getInteger(R.integer.database_version)
			);
	}

	private DatabaseOpenHelper(Context context, String name, int version) {
		super(context, name, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		List<String> create_stmts = stmts.getCreateStatements();
		Iterator<String> itr = create_stmts.iterator();
		while (itr.hasNext()) {
			String sql = itr.next();
			db.execSQL(sql);
		}
		
		stmts = null;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("DatabaseOpenHelper", "onUpgrade() called.");
		stmts = null;
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("DatabaseOpenHelper", "onDowngrade() called.");
		stmts = null;
	}
}
