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
package org.exoplatform.services.organization.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.MockedStatic;

import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.IdentityRegistry;

public class TestMembershipUpdateListener {

  /**
   * A membership change invalidates the user's conversations before and after
   * the commit: a node that rebuilt the conversation from the memberships read
   * before the commit is invalidated once more. No session is ended.
   */
  @Test
  public void testAMembershipChangeInvalidatesTheUserBeforeAndAfterTheCommit() throws Exception {
    ConversationRegistry conversationRegistry = mock(ConversationRegistry.class);
    Membership membership = mock(Membership.class);
    when(membership.getUserName()).thenReturn("john");
    MembershipUpdateListener listener = new MembershipUpdateListener(conversationRegistry, mock(IdentityRegistry.class));

    List<String> calls = new ArrayList<>();
    doAnswer(invocation -> calls.add("invalidate")).when(conversationRegistry).invalidateUser("john");

    try (MockedStatic<RequestLifeCycle> requestLifeCycle = mockStatic(RequestLifeCycle.class)) {
      requestLifeCycle.when(RequestLifeCycle::restartTransaction).thenAnswer(invocation -> calls.add("commit"));
      listener.postSave(membership, true);
      assertEquals(List.of("invalidate", "commit", "invalidate"), calls);

      calls.clear();
      listener.postDelete(membership);
      assertEquals(List.of("invalidate", "commit", "invalidate"), calls);
    }
    verify(conversationRegistry, never()).unregisterByUserId("john");
  }

}
