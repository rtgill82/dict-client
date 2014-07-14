package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lonestar.sdf.locke.apps.dict.dictclient.R;

import android.content.Context;
import android.content.res.Resources;

class SQLInitStatements {

	private Resources resources = null;
	private List<String> createStatements = null;
	private List<List<String>> upgradeStatements = null;
	private List<List<String>> downgradeStatements = null;

	SQLInitStatements(Context context)
			throws IOException {
		resources = context.getResources();
		upgradeStatements = new ArrayList<List<String>>();
		downgradeStatements = new ArrayList<List<String>>();

		createStatements = readStatements(R.raw.create_database_0001);
		upgradeStatements.add(createStatements);

		int dbversion = resources.getInteger(R.integer.database_version);

		for (int i = 2; i <= dbversion; i++) {
			String filename = String.format(Locale.US, "upgrade_database_%04d", i);
			int id = resources.getIdentifier(filename, "raw", null);
			if (id != 0) {
				upgradeStatements.add(readStatements(id));
			}
		}

		for (int i = 1; i <= dbversion - 1; i++) {
			String filename = String.format(Locale.US, "downgrade_database_%04d", i);
			int id = resources.getIdentifier(filename, "raw", null);
			if (id != 0) {
				downgradeStatements.add(readStatements(id));
			}
		}
	}

	public List<String> getCreateStatements() {
		return createStatements;
	}

	public List<String> getUpgrateStatements(int version) {
		return upgradeStatements.get(version);
	}

	public List<String> getDowngradeStatements(int version) {
		return downgradeStatements.get(version);
	}

	private List<String> readStatements(int resource)
			throws IOException {
		ArrayList<String> stmts = new ArrayList<String>();
		InputStream stream = resources.openRawResource(resource);
		SimpleSQLParser parser = new SimpleSQLParser(stream);

		String sql;
		while ((sql = parser.getNextStatement()) != null) {
			stmts.add(sql);
		}

		return stmts;
	}
}
