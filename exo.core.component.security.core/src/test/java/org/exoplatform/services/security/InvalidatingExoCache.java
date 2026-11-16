/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.services.security;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;

/**
 * One node's copy of a cache in invalidation mode: a local put stays on its
 * node, a put or a remove drops the key on every other node of the cluster
 */
public class InvalidatingExoCache<K extends Serializable, V> extends ConcurrentFIFOExoCache<K, V> {

  private final List<InvalidatingExoCache<K, V>> cluster;

  private volatile boolean                       failNextRemove;

  public InvalidatingExoCache(String name, List<InvalidatingExoCache<K, V>> cluster) {
    super(name, 100);
    setLiveTime(-1);
    this.cluster = cluster;
    cluster.add(this);
  }

  public static <K extends Serializable, V> List<InvalidatingExoCache<K, V>> newCluster() {
    return new CopyOnWriteArrayList<>();
  }

  @Override
  public void put(K key, V value) {
    invalidateOthers(key);
    super.put(key, value);
  }

  /**
   * Makes the next remove throw, as a cache whose cluster cannot be reached
   */
  public void failNextRemove() {
    failNextRemove = true;
  }

  @Override
  public V remove(Serializable key) {
    if (failNextRemove) {
      failNextRemove = false;
      throw new IllegalStateException("The cluster cannot be reached");
    }
    invalidateOthers(key);
    return super.remove(key);
  }

  @Override
  public void removeLocal(Serializable key) {
    super.remove(key);
  }

  private void invalidateOthers(Serializable key) {
    cluster.stream().filter(node -> node != this).forEach(node -> node.removeLocal(key));
  }

}
