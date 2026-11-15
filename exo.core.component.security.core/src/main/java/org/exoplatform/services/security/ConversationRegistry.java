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
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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

  private static final String                         USERS_NAME      = "portal.ConversationRegistryUsers";

  /**
   * The generation of the user's record a conversation was registered under
   */
  private static final String                         GENERATION      = "exo.core.security.ConversationRegistry.generation";

  /**
   * How long after a change of the user its record and identity are removed
   * once more: a conversation another node built from the user as it was
   * before the change committed, and registered after the removal reached
   * it, is built again then. Longer than building a conversation takes.
   */
  static final long                                   REINVALIDATION_DELAY_SECONDS = 10;

  private static final ScheduledExecutorService       REINVALIDATION  = Executors.newSingleThreadScheduledExecutor(task -> {
                                                                        Thread thread = new Thread(task,
                                                                                                   "ConversationRegistry-reinvalidation");
                                                                        thread.setDaemon(true);
                                                                        return thread;
                                                                      });

  private static final Log                            LOG             = ExoLogger.getLogger(ConversationRegistry.class);

  private final ExoCache<StateKey, ConversationState> statesCache;

  private final ExoCache<String, Set<StateKey>>       stateKeysCache;

  /**
   * The generation of each user whose conversations, or other state derived
   * from the user ({@link #getUserGeneration(String)}), this node holds: an
   * entry is written on this node only, and removed on every node when the
   * user's status or memberships change; a conversation registered under
   * another generation is then built again, and other state is read again.
   * An entry evicted for its size or its age has the same effect: the cache is
   * declared at least as large and as long-lived as
   * portal.ConversationRegistryStates
   */
  private final ExoCache<String, String>              usersCache;

  private final IdentityRegistry                      identityRegistry;

  private final ListenerService                       listenerService;

  private final Consumer<Runnable>                    delayedRun;

  public ConversationRegistry(IdentityRegistry identityRegistry,
                              ListenerService listenerService,
                              CacheService cacheService) {
    this(identityRegistry, listenerService, cacheService, delayedOn(REINVALIDATION));
  }

  /**
   * @param delayedRun runs a task {@link #REINVALIDATION_DELAY_SECONDS} later
   */
  ConversationRegistry(IdentityRegistry identityRegistry,
                       ListenerService listenerService,
                       CacheService cacheService,
                       Consumer<Runnable> delayedRun) {
    this.delayedRun = delayedRun;
    this.statesCache = cacheService.getCacheInstance(CACHE_NAME);
    this.stateKeysCache = cacheService.getCacheInstance(STATE_KEYS_NAME);
    this.usersCache = cacheService.getCacheInstance(USERS_NAME);
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
    ConversationState state = statesCache.get(key);
    if (state != null && !isCurrent(state)) {
      // The user changed on another node, or on this one: the conversation is
      // no longer served, so that the next request builds it again from the
      // user as stored and registers it in its place. Its session goes on: it
      // stays registered until then, so that neither a logout nor a login is
      // broadcast, and the end of the session still unregisters it
      return null;
    }
    return state;
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
    if (StringUtils.isNotBlank(userId)) {
      state.setAttribute(GENERATION, currentGeneration(userId));
    }
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
        // The states of a node are its own: the other nodes keep the identity
        // of the conversations they hold
        identityRegistry.unregisterLocal(userId);
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
   * Makes every node build the conversations of a user whose status or
   * memberships changed again: the identity is unregistered and each node
   * stops serving the user's conversations at its next read of each one, so
   * the next request builds both again from the user as stored. Both are
   * removed once more {@link #REINVALIDATION_DELAY_SECONDS} later, for a node
   * that read the user before the change committed. The sessions go on:
   * neither a logout nor a login is broadcast for them.
   *
   * @param userId user Id
   */
  public void invalidateUser(String userId) {
    removeUser(userId);
    delayedRun.accept(() -> {
      try {
        removeUser(userId);
      } catch (Throwable e) { // NOSONAR a failure of this run must not be lost unseen
        LOG.warn("Cannot invalidate once more the conversations of user {}", userId, e);
      }
    });
  }

  /**
   * A holder of state derived from a user, such as an open real-time session,
   * keeps the value this returns with that state, and reads the user again
   * once it no longer matches: the value changes each time
   * {@link #invalidateUser(String)} runs for the user, on any node.
   *
   * @param userId the user's identifier
   * @return the user's current generation on this node
   */
  public String getUserGeneration(String userId) {
    return currentGeneration(userId);
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
    usersCache.clearCache();
  }

  private boolean isCurrent(ConversationState state) {
    String userId = state.getIdentity() == null ? null : state.getIdentity().getUserId();
    if (StringUtils.isBlank(userId)) {
      return true;
    }
    Object generation = state.getAttribute(GENERATION);
    return generation != null && generation.equals(usersCache.get(userId));
  }

  /**
   * @return the user's generation on this node, a new one when the record was
   *         removed or never written
   */
  private String currentGeneration(String userId) {
    String generation = usersCache.get(userId);
    if (generation == null) {
      generation = UUID.randomUUID().toString();
      usersCache.putLocal(userId, generation);
    }
    return generation;
  }

  /**
   * @param executor the scheduler the delayed removals run on
   * @return what runs a task {@link #REINVALIDATION_DELAY_SECONDS} later on it
   */
  static Consumer<Runnable> delayedOn(ScheduledExecutorService executor) {
    return task -> executor.schedule(task, REINVALIDATION_DELAY_SECONDS, TimeUnit.SECONDS);
  }

  private void removeUser(String userId) {
    usersCache.remove(userId);
    identityRegistry.unregister(userId);
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
