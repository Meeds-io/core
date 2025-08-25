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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * In-memory registry of user's sessions
 */
public final class ConversationRegistry {

  private static final String                         CACHE_NAME      = "portal.ConversationRegistry";

  private static final String                         STATE_KEYS_NAME = "portal.ConversationRegistryStates";

  private static final Log                            LOG             = ExoLogger.getLogger(ConversationRegistry.class);

  private final ExoCache<StateKey, ConversationState> statesCache;

  private final ExoCache<String, Set<StateKey>>       stateKeysCache;

  private final IdentityRegistry                      identityRegistry;

  private final ListenerService                       listenerService;

  public ConversationRegistry(IdentityRegistry identityRegistry,
                              ListenerService listenerService,
                              CacheService cacheService) {
    this.statesCache = cacheService.getCacheInstance(CACHE_NAME);
    this.stateKeysCache = cacheService.getCacheInstance(STATE_KEYS_NAME);
    this.identityRegistry = identityRegistry;
    this.listenerService = listenerService;
  }

  /**
   * Get ConversationState with specified key.
   * 
   * @param key the key.
   * @return ConversationState.
   */
  public ConversationState getState(StateKey key) {
    return statesCache.get(key);
  }

  /**
   * Sets the user's session to the registry and broadcasts ADD_SESSION_EVENT
   * message to interested listeners.
   * 
   * @param key the session identifier.
   * @param state the conversation state.
   */
  public void register(StateKey key, ConversationState state) {
    String userId = state.getIdentity().getUserId();
    // We will broadcast login event if :
    // 1- session is not already registered -> session ID is not in the states
    // map keys
    // 2- user is not already logged-in (there is no registered state with the
    // same userID)
    boolean broadcast = statesCache.get(key) == null && StringUtils.isNotBlank(userId) && getStateKeys(userId).isEmpty();
    statesCache.put(key, state);
    addStateKey(userId, key);
    if (broadcast) { // NOSONAR
      try {
        listenerService.broadcast("exo.core.security.ConversationRegistry.register", this, state);
      } catch (Exception e) {
        LOG.error("Broadcast message filed ", e);
      }
    }
  }

  /**
   * Remove ConversationStae with specified key. If there is no more
   * ConversationState for user then remove Identity from IdentityRegistry.
   * 
   * @param key the key.
   * @return removed ConversationState or null.
   */
  public ConversationState unregister(StateKey key) {
    return unregister(key, true);
  }

  /**
   * Remove ConversationState with specified key. If there is no more
   * ConversationState for user and <code>unregisterIdentity</code> is true then
   * remove Identity from IdentityRegistry.
   * 
   * @param key the key.
   * @param unregisterIdentity if true and no more ConversationStates for user
   *          then unregister Identity
   * @return removed ConversationState or null.
   */
  public ConversationState unregister(StateKey key, boolean unregisterIdentity) {
    ConversationState state = statesCache.remove(key);
    if (state != null) {
      String userId = state.getIdentity().getUserId();
      removeStateKey(userId, key);
      if (unregisterIdentity && CollectionUtils.isEmpty(stateKeysCache.get(userId))) {
        identityRegistry.unregister(userId);
      }
      try {
        listenerService.broadcast("exo.core.security.ConversationRegistry.unregister", this, state);
      } catch (Exception e) {
        LOG.error("Broadcast message filed ", e);
      }
    }
    return state;
  }

  /**
   * Unregister all conversation states for user with specified Id.
   * 
   * @param userId user Id
   * @return set of unregistered conversation states
   */
  public List<ConversationState> unregisterByUserId(String userId) {
    List<ConversationState> states = new ArrayList<>();
    Set<StateKey> stateKeys = stateKeysCache.get(userId);
    if (stateKeys != null) {
      stateKeys = new HashSet<>(stateKeys); // Avoid ConcurrentModificationException
      for (StateKey key : stateKeys) {
        ConversationState state = unregister(key, false);
        if (state != null) {
          states.add(state);
        }
      }
    }
    return states;
  }

  /**
   * @param userId the user's identifier.
   * @return list of users ConversationState.
   */
  public List<StateKey> getStateKeys(String userId) {
    return stateKeysCache.get(userId) == null ? Collections.emptyList(): new ArrayList<>(stateKeysCache.get(userId));
  }

  /**
   * Remove all ConversationStates.
   */
  void clear() {
    statesCache.clearCache();
    stateKeysCache.clearCache();
  }

  private void removeStateKey(String userId, StateKey key) {
    Set<StateKey> stateKeys = stateKeysCache.get(userId);
    if (stateKeys != null) {
      stateKeys.remove(key);
      if (stateKeys.isEmpty()) {
        stateKeysCache.remove(userId);
      }
    }
  }

  private void addStateKey(String userId, StateKey key) {
    Set<StateKey> states = stateKeysCache.get(userId);
    if (states == null) {
      states = Collections.synchronizedSet(new HashSet<>());
    }
    states.add(key);
    stateKeysCache.put(userId, states);
  }

}
