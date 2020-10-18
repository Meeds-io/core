/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.services.organization.hibernate;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.organization.BaseOrganizationService;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : Mestrallet Benjamin benjmestrallet@users.sourceforge.net
 * Author : Tuan Nguyen tuan08@users.sourceforge.net
 * Date: Aug 22, 2003 Time: 4:51:21 PM
 */
@Deprecated
public class OrganizationServiceImpl extends BaseOrganizationService implements Startable
{

   public OrganizationServiceImpl(HibernateService hservice, CacheService cservice) throws Exception
   {

      userDAO_ = new UserDAOImpl(hservice, cservice, this);
      userProfileDAO_ = new UserProfileDAOImpl(hservice, cservice, userDAO_);
      groupDAO_ = new GroupDAOImpl(hservice, this);
      membershipTypeDAO_ = new MembershipTypeDAOImpl(hservice, this);
      membershipDAO_ = new MembershipDAOImpl(hservice, this);
   }
}
