/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

import java.util.List;

public class IdentityRegistry {

  private static final String              CACHE_NAME = "portal.IdentityRegistry";

  private final ExoCache<String, Identity> identitiesCache;

  public IdentityRegistry(CacheService cacheService) {
    identitiesCache = cacheService.getCacheInstance(CACHE_NAME);
  }

  /**
   * Get identity for supplied user ID.
   * 
   * @param userId user ID
   * @return identity or null if not found
   */
  public Identity getIdentity(String userId) {
    return identitiesCache.get(userId);
  }

  /**
   * Register new identity in registry.
   * 
   * @param identity {@link Identity}
   */
  public void register(Identity identity) {
    this.identitiesCache.put(identity.getUserId(), identity);
  }

  /**
   * Remove identity with supplied user ID.
   * 
   * @param userId user ID
   */
  public void unregister(String userId) {
    this.identitiesCache.remove(userId);
  }

  /**
   * Remove all identities.
   */
  void clear() {
    identitiesCache.clearCache();
  }

  /**
   * Retrieves all identities
   */
  public List<Identity> getIdentities() throws Exception {
    return (List<Identity>) identitiesCache.getCachedObjects();
  }

}
