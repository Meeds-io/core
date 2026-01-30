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
package org.exoplatform.services.organization.impl;

import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserProfile;

public class NewUserEventListener extends UserEventListener {

  private NewUserConfig config;

  public NewUserEventListener(InitParams params) {
    this.config = params.getObjectParamValues(NewUserConfig.class).get(0);
  }

  @Override
  public void postSave(User user, boolean isNew) throws Exception {
    OrganizationService organizationService = ExoContainerContext.getService(OrganizationService.class);
    UserProfile up = organizationService.getUserProfileHandler().createUserProfileInstance(user.getUserName());
    organizationService.getUserProfileHandler().saveUserProfile(up, false);
    if (config == null)
      return;
    if (isNew && !config.isIgnoreUser(user.getUserName())) {
      createDefaultUserMemberships(organizationService, user);
    }
  }

  private void createDefaultUserMemberships(OrganizationService organizationService, User user) throws Exception {
    List<?> groups = config.getGroup();
    if (groups != null && groups.isEmpty()) {
      for (int i = 0; i < groups.size(); i++) {
        NewUserConfig.JoinGroup jgroup = (NewUserConfig.JoinGroup) groups.get(i);
        Group group = organizationService.getGroupHandler().findGroupById(jgroup.getGroupId());
        MembershipType mtype = organizationService.getMembershipTypeHandler().findMembershipType(jgroup.getMembership());
        organizationService.getMembershipHandler().linkMembership(user, group, mtype, true);
      }
    }
  }

}
