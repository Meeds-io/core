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
package org.exoplatform.services.organization;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import org.picocontainer.Startable;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseOrganizationService implements OrganizationService, Startable, ComponentRequestLifecycle {
  public static final Log                        LOG        = ExoLogger.getLogger(BaseOrganizationService.class);

  protected UserHandler                          userDAO_;                                                       // NOSONAR

  protected UserProfileHandler                   userProfileDAO_;                                                // NOSONAR

  protected GroupHandler                         groupDAO_;                                                      // NOSONAR

  protected MembershipHandler                    membershipDAO_;                                                 // NOSONAR

  protected MembershipTypeHandler                membershipTypeDAO_;                                             // NOSONAR

  protected List<OrganizationServiceInitializer> listeners_ = new ArrayList<>();                                 // NOSONAR

  public UserHandler getUserHandler() {
    return userDAO_;
  }

  public UserProfileHandler getUserProfileHandler() {
    return userProfileDAO_;
  }

  public GroupHandler getGroupHandler() {
    return groupDAO_;
  }

  public MembershipTypeHandler getMembershipTypeHandler() {
    return membershipTypeDAO_;
  }

  public MembershipHandler getMembershipHandler() {
    return membershipDAO_;
  }

  @Override
  public void start() {
    RequestLifeCycle.begin(this);
    try {
      init(OrganizationServiceInitializer.GROUPS_ENTITY_TYPE);
      init(OrganizationServiceInitializer.ROLES_ENTITY_TYPE);
      init(OrganizationServiceInitializer.USERS_ENTITY_TYPE);
    } finally {
      RequestLifeCycle.end();
    }
  }

  public synchronized void addListenerPlugin(ComponentPlugin listener) throws Exception {
    if (listener instanceof UserEventListener eventListener) {
      userDAO_.addUserEventListener(eventListener);
    } else if (listener instanceof GroupEventListener eventListener) {
      groupDAO_.addGroupEventListener(eventListener);
    } else if (listener instanceof MembershipTypeEventListener eventListener) {
      membershipTypeDAO_.addMembershipTypeEventListener(eventListener);
    } else if (listener instanceof MembershipEventListener eventListener) {
      membershipDAO_.addMembershipEventListener(eventListener);
    } else if (listener instanceof UserProfileEventListener eventListener) {
      userProfileDAO_.addUserProfileEventListener(eventListener);
    } else if (listener instanceof OrganizationServiceInitializer initializer) {
      listeners_.add(initializer);
    }
  }

  public void startRequest(ExoContainer container) {
  }

  public void endRequest(ExoContainer container) {
  }

  private void init(String entityType) {
    for (OrganizationServiceInitializer listener : listeners_) {
      try {
        listener.init(this, entityType);
      } catch (Exception e) {
        LOG.warn("Failed start Organization Service {}, probably because of configuration error. Error occurs when initialize {}",
                 getClass().getName(),
                 listener.getClass().getName(),
                 e);
      }
    }
  }

}
