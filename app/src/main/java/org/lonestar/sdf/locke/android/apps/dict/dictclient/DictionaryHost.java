package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.lonestar.sdf.locke.libs.dict.Dictionary;

import java.util.Date;
import java.util.List;

@SuppressWarnings("unused")
@DatabaseTable(tableName = "dict_servers")
public class DictionaryHost
{
  final private static int PORT = 2628;

  @DatabaseField(generatedId = true, columnName = "_id")
    private Integer id;
  @DatabaseField(canBeNull = false, uniqueIndexName = "host_port_idx")
    private String host_name;
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

  private List<Dictionary> dictionaries = null;

  public DictionaryHost()
    {
      this.port = PORT;
    }

  public DictionaryHost(String host_name)
    {
      this.host_name = host_name;
      this.port = PORT;
    }

  public DictionaryHost(String host_name, int port)
    {
      this.host_name = host_name;
      this.port = port;
    }

  public Integer getId()
    {
      return id;
    }

  public void setId(Integer id)
    {
      this.id = id;
    }

  public String getHostName()
    {
      return host_name;
    }

  public void setHostName(String host_name)
    {
      this.host_name = host_name;
    }

  public Integer getPort()
    {
      return port;
    }

  public void setPort(Integer port)
    {
      this.port = port;
    }

  public String getDescription()
    {
      return description;
    }

  public void setDescription(String description)
    {
      this.description = description;
    }

  public boolean isReadonly()
    {
      return readonly;
    }

  public void setReadonly(boolean readonly)
    {
      this.readonly = readonly;
    }

  public boolean isUserDefined()
    {
      return user_defined;
    }

  public void setUserDefined(boolean userDefined)
    {
      this.user_defined = userDefined;
    }

  public Date getLastDBRefresh()
    {
      return last_db_refresh;
    }

  public void setLastDBRefresh(Date date)
    {
      this.last_db_refresh = date;
    }

  public List<Dictionary> getDictionaries()
    {
      return dictionaries;
    }

  public void setDictionaries(List<Dictionary> list)
    {
      dictionaries = list;
    }
}
