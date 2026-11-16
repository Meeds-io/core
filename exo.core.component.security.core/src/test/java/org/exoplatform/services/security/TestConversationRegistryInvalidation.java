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

import static org.junit.Assert.assertNotEquals;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfigPlugin;
import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;

/**
 * A change of a user's status or memberships made through one node makes every
 * node build the conversations it holds for that user again, from the user as
 * stored.
 */
public class TestConversationRegistryInvalidation extends TestCase {

  private static final String                                    REGISTER_EVENT   = "exo.core.security.ConversationRegistry.register";

  private static final String                                    UNREGISTER_EVENT = "exo.core.security.ConversationRegistry.unregister";

  private final List<String>                                     events           = new CopyOnWriteArrayList<>();

  private final List<Runnable>                                   delayed          = new CopyOnWriteArrayList<>();

  private final List<InvalidatingExoCache<Serializable, Object>> users      = InvalidatingExoCache.newCluster();

  private final List<InvalidatingExoCache<Serializable, Object>> identities = InvalidatingExoCache.newCluster();

  public void testAUserInvalidatedThroughOneNodeLosesTheConversationsOfEveryNode() {
    ConversationRegistry nodeA = newNode();
    ConversationRegistry nodeB = newNode();
    StateKey onA = new SimpleStateKey("onA");
    StateKey onB = new SimpleStateKey("onB");
    StateKey maryOnB = new SimpleStateKey("maryOnB");
    nodeA.register(onA, new ConversationState(new Identity("john")));
    nodeB.register(onB, new ConversationState(new Identity("john")));
    nodeB.register(maryOnB, new ConversationState(new Identity("mary")));

    nodeA.invalidateUser("john");

    assertNull(nodeA.getState(onA));
    assertNull("Another node drops the conversation at its next read", nodeB.getState(onB));
    assertNotNull("Other users keep their conversations", nodeB.getState(maryOnB));
  }

  public void testAConversationBuiltAgainAfterTheInvalidationStays() {
    ConversationRegistry nodeA = newNode();
    ConversationRegistry nodeB = newNode();
    StateKey onB = new SimpleStateKey("onB");
    nodeB.register(onB, new ConversationState(new Identity("john")));
    nodeA.invalidateUser("john");
    assertNull(nodeB.getState(onB));

    ConversationState rebuilt = new ConversationState(new Identity("john"));
    nodeB.register(onB, rebuilt);

    assertSame(rebuilt, nodeB.getState(onB));
  }

  public void testARegistrationOnOneNodeKeepsTheConversationsOfTheOthers() {
    ConversationRegistry nodeA = newNode();
    ConversationRegistry nodeB = newNode();
    StateKey onA = new SimpleStateKey("onA");
    nodeA.register(onA, new ConversationState(new Identity("john")));

    nodeB.register(new SimpleStateKey("onB"), new ConversationState(new Identity("john")));

    assertNotNull(nodeA.getState(onA));
  }

  /**
   * A holder of state derived from the user, on another node, learns of the
   * change by comparing the generation it kept
   */
  public void testTheUserGenerationChangesOnEveryNodeWhenTheUserIsInvalidated() {
    ConversationRegistry nodeA = newNode();
    ConversationRegistry nodeB = newNode();
    String kept = nodeB.getUserGeneration("john");
    assertEquals(kept, nodeB.getUserGeneration("john"));
    String maryKept = nodeB.getUserGeneration("mary");

    nodeA.invalidateUser("john");

    assertNotEquals(kept, nodeB.getUserGeneration("john"));
    assertEquals("Other users keep theirs", maryKept, nodeB.getUserGeneration("mary"));
  }

  public void testTheInvalidationUnregistersTheIdentityOnEveryNode() {
    ConversationRegistry nodeA = newNode();
    newNode();
    InvalidatingExoCache<Serializable, Object> identitiesOfB = identities.get(1);
    identitiesOfB.putLocal("john", new Identity("john"));

    nodeA.invalidateUser("john");

    assertNull(identitiesOfB.get("john"));
  }

  /**
   * The session goes on across the invalidation: the rebuild broadcasts no
   * logout and no login, and the end of the session still broadcasts its
   * logout once
   */
  public void testTheRebuildBroadcastsNeitherLogoutNorLogin() {
    ConversationRegistry nodeA = newNode();
    ConversationRegistry nodeB = newNode();
    StateKey onB = new SimpleStateKey("onB");
    nodeB.register(onB, new ConversationState(new Identity("john")));
    events.clear();

    nodeA.invalidateUser("john");
    assertNull(nodeB.getState(onB));
    nodeB.register(onB, new ConversationState(new Identity("john")));
    assertNotNull(nodeB.getState(onB));
    assertEquals(List.of(), events);

    nodeB.unregister(onB);
    assertEquals(List.of(UNREGISTER_EVENT), events);
    assertTrue(nodeB.getStateKeys("john").isEmpty());
  }

  /**
   * A conversation no longer current is not listed, so that each key listed
   * has a state, until its session's next request registers it again
   */
  public void testTheKeysOfConversationsNoLongerCurrentAreNotListed() {
    ConversationRegistry nodeA = newNode();
    ConversationRegistry nodeB = newNode();
    StateKey first = new SimpleStateKey("first");
    StateKey second = new SimpleStateKey("second");
    nodeB.register(first, new ConversationState(new Identity("john")));
    nodeB.register(second, new ConversationState(new Identity("john")));

    nodeA.invalidateUser("john");
    assertEquals(List.of(), nodeB.getStateKeys("john"));

    nodeB.register(first, new ConversationState(new Identity("john")));
    assertEquals(List.of(first), nodeB.getStateKeys("john"));
    assertNotNull(nodeB.getState(first));
  }

  /**
   * A new session of a user whose other sessions are no longer current is not
   * the user's login: its registration broadcasts no login
   */
  public void testANewSessionBesideConversationsNoLongerCurrentBroadcastsNoLogin() {
    ConversationRegistry nodeA = newNode();
    ConversationRegistry nodeB = newNode();
    nodeB.register(new SimpleStateKey("first"), new ConversationState(new Identity("john")));
    nodeA.invalidateUser("john");
    events.clear();

    nodeB.register(new SimpleStateKey("second"), new ConversationState(new Identity("john")));
    assertEquals(List.of(), events);
  }

  /**
   * Each session of the user is built again: the rebuild of one does not make
   * the other sessions' conversations current again
   */
  public void testEverySessionOfTheUserIsBuiltAgain() {
    ConversationRegistry nodeA = newNode();
    ConversationRegistry nodeB = newNode();
    StateKey first = new SimpleStateKey("first");
    StateKey second = new SimpleStateKey("second");
    nodeB.register(first, new ConversationState(new Identity("john")));
    nodeB.register(second, new ConversationState(new Identity("john")));

    nodeA.invalidateUser("john");
    assertNull(nodeB.getState(first));
    nodeB.register(first, new ConversationState(new Identity("john")));

    assertNotNull(nodeB.getState(first));
    assertNull("The other session still reads the user again", nodeB.getState(second));
    ConversationState rebuilt = new ConversationState(new Identity("john"));
    nodeB.register(second, rebuilt);
    assertSame(rebuilt, nodeB.getState(second));
    assertNotNull(nodeB.getState(first));
  }

  /**
   * A node that read the user before the change committed, and registered the
   * conversation and the identity after the removal reached it, builds both
   * again once the delayed removal runs
   */
  public void testARebuildThatReadTheUserBeforeTheCommitIsBuiltOnceMore() {
    ConversationRegistry nodeA = newNode();
    ConversationRegistry nodeB = newNode();
    InvalidatingExoCache<Serializable, Object> identitiesOfB = identities.get(1);
    StateKey onB = new SimpleStateKey("onB");
    nodeB.register(onB, new ConversationState(new Identity("john")));

    nodeA.invalidateUser("john");
    assertNull(nodeB.getState(onB));
    nodeA.invalidateUser("john");
    identitiesOfB.putLocal("john", new Identity("john"));
    nodeB.register(onB, new ConversationState(new Identity("john")));
    assertNotNull(nodeB.getState(onB));

    delayed.forEach(Runnable::run);

    assertNull(nodeB.getState(onB));
    assertNull(identitiesOfB.get("john"));
    ConversationState rebuilt = new ConversationState(new Identity("john"));
    nodeB.register(onB, rebuilt);
    assertSame(rebuilt, nodeB.getState(onB));
  }

  /**
   * A failure of the delayed removal stays in its task: the scheduler's thread is
   * shared by every later invalidation
   */
  public void testAFailingDelayedRemovalDoesNotEscapeItsTask() {
    ConversationRegistry node = newNode();
    node.invalidateUser("john");
    users.get(0).failNextRemove();

    delayed.forEach(Runnable::run);
  }

  /**
   * The delayed removal runs ten seconds later on the registry's scheduler
   */
  public void testTheDelayedRemovalRunsTenSecondsLater() {
    List<Long> delays = new CopyOnWriteArrayList<>();
    try (ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1) {
      @Override
      public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        delays.add(unit.toSeconds(delay));
        return null;
      }
    }) {
      ConversationRegistry.delayedOn(executor).accept(() -> {
      });
    }

    assertEquals(List.of(10L), delays);
  }

  /**
   * Another session of the user registering while the user's keys are listed
   * does not break the listing, which reads a copy of the keys
   */
  public void testASessionRegisteredWhileTheKeysAreListedDoesNotBreakTheListing() {
    ConversationRegistry[] node = new ConversationRegistry[1];
    StateKey first = new SimpleStateKey("first");
    StateKey second = new SimpleStateKey("second");
    StateKey late = new SimpleStateKey("late");
    AtomicBoolean listing = new AtomicBoolean();
    ExoCache<Serializable, Object> states = new ConcurrentFIFOExoCache<>("portal.ConversationRegistry", 100) {
      @Override
      public Object get(Serializable name) {
        if (listing.getAndSet(false)) {
          node[0].register(late, new ConversationState(new Identity("john")));
        }
        return super.get(name);
      }
    };
    node[0] = newNode(Map.of("portal.ConversationRegistry", states));
    node[0].register(first, new ConversationState(new Identity("john")));
    node[0].register(second, new ConversationState(new Identity("john")));

    listing.set(true);
    List<StateKey> keys = node[0].getStateKeys("john");

    assertEquals(Set.of(first, second), new HashSet<>(keys));
    assertEquals(Set.of(first, second, late), new HashSet<>(node[0].getStateKeys("john")));
  }

  private ConversationRegistry newNode() {
    return newNode(Map.of());
  }

  private ConversationRegistry newNode(Map<String, ExoCache<? extends Serializable, ?>> ownCaches) {
    Map<String, ExoCache<? extends Serializable, ?>> caches = new HashMap<>(ownCaches);
    caches.put("portal.ConversationRegistryUsers", new InvalidatingExoCache<>("portal.ConversationRegistryUsers", users));
    caches.put("portal.IdentityRegistry", new InvalidatingExoCache<>("portal.IdentityRegistry", identities));
    CacheService cacheService = new CacheService() {
      @Override
      public void addExoCacheConfig(ExoCacheConfigPlugin plugin) {
        // Not used
      }

      @Override
      @SuppressWarnings("unchecked")
      public <K extends Serializable, V> ExoCache<K, V> getCacheInstance(String region) {
        return (ExoCache<K, V>) caches.computeIfAbsent(region, name -> new ConcurrentFIFOExoCache<>(name, 100));
      }

      @Override
      public Collection<ExoCache<? extends Serializable, ?>> getAllCacheInstances() {
        return caches.values();
      }
    };
    ListenerService listenerService = new ListenerService(new ExoContainerContext(new ExoContainer()));
    for (String eventName : List.of(REGISTER_EVENT, UNREGISTER_EVENT)) {
      listenerService.addListener(eventName, new Listener<Object, Object>() {
        @Override
        public void onEvent(Event<Object, Object> event) {
          events.add(event.getEventName());
        }
      });
    }
    return new ConversationRegistry(new IdentityRegistry(cacheService), listenerService, cacheService, delayed::add);
  }

}
