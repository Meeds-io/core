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
package org.exoplatform.services.security.web;

import java.io.IOException;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.StateKey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SetCurrentIdentityFilter extends AbstractFilter {

  private static final Log     LOG = ExoLogger.getLogger("exo.core.component.security.core.SetCurrentIdentityFilter");

  private ConversationRegistry conversationRegistry;

  private IdentityRegistry     identityRegistry;

  /**
   * Set current {@link ConversationState}, if it is not registered yet then
   * create new one and register in {@link ConversationRegistry}. {@inheritDoc}
   */
  public void doFilter(ServletRequest request,
                       ServletResponse response,
                       FilterChain chain) throws IOException,
                                          ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    ExoContainer container = getContainer();

    try {
      ExoContainerContext.setCurrentContainer(container);
      ConversationState state = getCurrentState(container, httpRequest);
      ConversationState.setCurrent(state);
      chain.doFilter(request, response);
    } finally {
      try {
        ConversationState.setCurrent(null);
      } catch (Exception e) {
        LOG.warn("An error occured while cleaning the ThreadLocal", e);
      }
      try {
        ExoContainerContext.setCurrentContainer(null);
      } catch (Exception e) {
        LOG.warn("An error occured while cleaning the ThreadLocal", e);
      }
    }
  }

  /**
   * Gives the current state
   */
  private ConversationState getCurrentState(ExoContainer container, HttpServletRequest httpRequest) {
    ConversationState state = null;
    String userId = httpRequest.getRemoteUser();

    // only if user authenticated, otherwise there is no reason to do anythings
    if (userId != null) {
      HttpSession httpSession = httpRequest.getSession();
      StateKey stateKey = new HttpSessionStateKey(httpSession);

      if (LOG.isDebugEnabled()) {
        LOG.debug("Looking for Conversation State " + httpSession.getId());
      }

      state = getConversationRegistry(container).getState(stateKey);

      if (state != null && !userId.equals(state.getIdentity().getUserId())) {
        state = null;
        getConversationRegistry(container).unregister(stateKey, false);
        LOG.debug("The current conversation state with the session ID " + httpSession.getId() + " does not belong to the user " +
            userId + ". The conversation state registry will be updated.");
      }

      if (state == null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Conversation State not found, try create new one.");
        }

        Identity identity = getIdentityRegistry(container).getIdentity(userId);
        if (identity != null) {
          state = new ConversationState(identity);
          // Keep subject as attribute in ConversationState.
          state.setAttribute(ConversationState.SUBJECT, identity.getSubject());
        } else {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Not found identity for " + userId + " try to restore it. ");
          }

          Authenticator authenticator = container.getComponentInstanceOfType(Authenticator.class);
          try {
            identity = authenticator.createIdentity(userId);
            getIdentityRegistry(container).register(identity);
          } catch (Exception e) {
            LOG.error("Unable restore identity. " + e.getMessage(), e);
          }

          if (identity != null) {
            state = new ConversationState(identity);
          }
        }

        if (state != null) {
          getConversationRegistry(container).register(stateKey, state);
          if (LOG.isDebugEnabled()) {
            LOG.debug("Register Conversation state " + httpSession.getId());
          }
        }
      }
    } else {
      state = new ConversationState(new Identity(IdentityConstants.ANONIM));
    }
    return state;
  }

  public ConversationRegistry getConversationRegistry(ExoContainer container) {
    if (conversationRegistry == null) {
      conversationRegistry = container.getComponentInstanceOfType(ConversationRegistry.class);
    }
    return conversationRegistry;
  }

  public IdentityRegistry getIdentityRegistry(ExoContainer container) {
    if (identityRegistry == null) {
      identityRegistry = container.getComponentInstanceOfType(IdentityRegistry.class);
    }
    return identityRegistry;
  }
}
