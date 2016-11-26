package org.lonestar.sdf.locke.android.apps.dict.dictclient;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by locke on 9/24/16.
 */

public class DictionaryHostCache extends LinkedList<DictionaryHost>
{
  public DictionaryHost getHostById(Integer id)
    {
      for (DictionaryHost item : this)
        {
          if (item.getId() == id)
            return item;
        }

      return null;
    }

  @Override
  public boolean add(DictionaryHost host)
    {
      if (host == null)
        return false;

      if (getHostById(host.getId()) == null)
        {
          super.add(host);
          return true;
        }

      return false;
    }

  @Override
  public void add(int index, DictionaryHost host)
    {
      if (host == null)
        return;

      if (getHostById(host.getId()) == null)
        super.add(index, host);
    }

  @Override
  public boolean addAll(Collection<? extends DictionaryHost> c)
    {
      boolean rv = false;

      for (DictionaryHost host : c)
        {
          rv |= add(host);
        }

      return rv;
    }

  @Override
  public boolean addAll(int index, Collection<? extends DictionaryHost> c)
    {
      boolean rv = false;

      for (DictionaryHost host : c)
        {
          if (getHostById(host.getId()) == null)
            {
              super.add(index, host);
              rv = true;
            }
        }

      return rv;
    }

  @Override
  public void addFirst(DictionaryHost host)
    {
      if (getHostById(host.getId()) == null)
        {
          super.addFirst(host);
        }
    }

  @Override
  public void addLast(DictionaryHost host)
    {
      if (getHostById(host.getId()) == null)
        super.addLast(host);
    }

  @Override
  public void push(DictionaryHost host)
    {
      if (getHostById(host.getId()) == null)
        super.push(host);
    }

  @Override
  public DictionaryHost set(int index, DictionaryHost host)
    {
      DictionaryHost oldHost = getHostById(host.getId());

      if (oldHost != null)
        {
          int oldIndex = indexOf(oldHost);
          if (oldIndex < index)
            index -= 1;

          if (oldIndex != index)
            remove(oldHost);
        }

      return super.set(index, host);
    }
}
