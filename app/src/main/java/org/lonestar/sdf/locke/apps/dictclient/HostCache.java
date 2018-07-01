/*
 * Copyright (C) 2017 Robert Gill <locke@sdf.lonestar.org>
 * All rights reserved.
 *
 * This file is a part of DICT Client.
 *
 */

package org.lonestar.sdf.locke.apps.dictclient;

import java.util.Collection;
import java.util.LinkedList;

class HostCache extends LinkedList<Host> {
    public Host getHostById(Integer id) {
        for (Host item : this) {
            if (item.getId().equals(id))
              return item;
        }
        return null;
    }

    @Override
    public boolean add(Host host) {
        if (host == null)
          return false;

        if (getHostById(host.getId()) == null) {
            super.add(host);
            return true;
        }
        return false;
    }

    @Override
    public void add(int index, Host host) {
        if (host == null)
          return;

        if (getHostById(host.getId()) == null)
          super.add(index, host);
    }

    @Override
    public boolean addAll(Collection<? extends Host> c) {
        boolean rv = false;

        for (Host host : c)
          rv |= add(host);

        return rv;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Host> c) {
        boolean rv = false;

        for (Host host : c) {
            if (getHostById(host.getId()) == null) {
                super.add(index, host);
                rv = true;
            }
        }
        return rv;
    }

    @Override
    public void addFirst(Host host) {
        if (getHostById(host.getId()) == null)
          super.addFirst(host);
    }

    @Override
    public void addLast(Host host) {
        if (getHostById(host.getId()) == null)
          super.addLast(host);
    }

    @Override
    public void push(Host host) {
        if (getHostById(host.getId()) == null)
          super.push(host);
    }

    @Override
    public Host set(int index, Host host) {
        Host oldHost = getHostById(host.getId());

        if (oldHost != null) {
            int oldIndex = indexOf(oldHost);
            if (oldIndex < index)
              index -= 1;

            if (oldIndex != index)
              remove(oldHost);
        }
        return super.set(index, host);
    }
}
