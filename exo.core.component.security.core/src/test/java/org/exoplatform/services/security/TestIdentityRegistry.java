/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfigPlugin;
import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;

/**
 * In a cluster, a node registers the identities of its own requests, and an
 * unregistration reaches every node.
 */
public class TestIdentityRegistry extends TestCase {

  private RecordingCache   cache;

  private IdentityRegistry registry;

  @Override
  protected void setUp() {
    cache = new RecordingCache();
    registry = new IdentityRegistry(new CacheService() {
      @Override
      public void addExoCacheConfig(ExoCacheConfigPlugin plugin) {
        // Not used
      }

      @Override
      @SuppressWarnings("unchecked")
      public <K extends Serializable, V> ExoCache<K, V> getCacheInstance(String region) {
        return (ExoCache<K, V>) cache;
      }

      @Override
      public Collection<ExoCache<? extends Serializable, ?>> getAllCacheInstances() {
        return List.of(cache);
      }
    });
  }

  public void testARegistrationStaysOnTheNodeThatMadeIt() {
    Identity identity = new Identity("john");

    registry.register(identity);

    assertEquals(List.of("putLocal john"), cache.calls);
    assertSame(identity, registry.getIdentity("john"));
  }

  public void testAnUnregistrationReachesEveryNode() {
    registry.register(new Identity("john"));

    registry.unregister("john");

    assertEquals(List.of("putLocal john", "remove john"), cache.calls);
    assertNull(registry.getIdentity("john"));
  }

  private static class RecordingCache extends ConcurrentFIFOExoCache<String, Identity> {

    private final List<String> calls = new ArrayList<>();

    RecordingCache() {
      setLiveTime(-1);
    }

    @Override
    public void put(String key, Identity value) {
      calls.add("put " + key);
      super.put(key, value);
    }

    @Override
    public void putLocal(String key, Identity value) {
      calls.add("putLocal " + key);
      super.putLocal(key, value);
    }

    @Override
    public Identity remove(Serializable key) {
      calls.add("remove " + key);
      return super.remove(key);
    }
  }
}
