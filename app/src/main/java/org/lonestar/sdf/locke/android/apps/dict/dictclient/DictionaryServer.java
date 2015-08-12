package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "dict_servers")
public class DictionaryServer {
	final private static int PORT = 2628;

	@DatabaseField(generatedId = true)
	private Integer id;
	@DatabaseField(canBeNull = false, uniqueIndexName = "host_port_idx")
	private String host;
	@DatabaseField(canBeNull = false, uniqueIndexName = "host_port_idx", defaultValue = "2628")
	private Integer port;
	@DatabaseField(canBeNull = true)
	private String description;
	@DatabaseField(defaultValue = "false")
	private boolean readonly;
	@DatabaseField(defaultValue = "false")
	private boolean user_defined;
	@DatabaseField(canBeNull = true)
	private Date last_db_refresh;

	public DictionaryServer() {
		this.port = PORT;
	}

	public DictionaryServer(String host) {
		this.host = host;
		this.port = PORT;
	}

	public DictionaryServer(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public Integer getId() {
		return id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public boolean isUserDefined() {
		return user_defined;
	}

	public void setUserDefined(boolean userDefined) {
		this.user_defined = userDefined;
	}

	public Date getLastDBRefresh() {
		return last_db_refresh;
	}

	public void setLastDBRefresh(Date date) {
		this.last_db_refresh = date;
	}
}
