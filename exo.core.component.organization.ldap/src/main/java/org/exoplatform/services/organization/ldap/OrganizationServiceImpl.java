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
package org.exoplatform.services.organization.ldap;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.ldap.LDAPService;
import org.exoplatform.services.organization.BaseOrganizationService;
import org.exoplatform.services.organization.CacheHandler;
import org.exoplatform.services.organization.hibernate.UserProfileDAOImpl;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Oct 14, 2005. @version andrew00x $
 */
@Deprecated
public class OrganizationServiceImpl extends BaseOrganizationService
{

   /**
    * @param params see {@link InitParams}
    * @param ldapService see {@link LDAPService}
    * @param hservice see {@link HibernateService}
    * @param cservice see {@link CacheService}
    * @throws Exception if any errors occurs
    */
   public OrganizationServiceImpl(InitParams params, LDAPService ldapService, HibernateService hservice,
      CacheService cservice) throws Exception
   {

      LDAPAttributeMapping ldapAttrMapping =
         (LDAPAttributeMapping)params.getObjectParam("ldap.attribute.mapping").getObject();

      CacheHandler cacheHandler = new CacheHandler(cservice);

      if (ldapService.getServerType() == LDAPService.ACTIVE_DIRECTORY_SERVER)
      {
         userDAO_ = new ADUserDAOImpl(ldapAttrMapping, ldapService, cacheHandler, this);
         ADSearchBySID adSearch = new ADSearchBySID(ldapAttrMapping);
         groupDAO_ = new ADGroupDAOImpl(ldapAttrMapping, ldapService, adSearch, cacheHandler);
         membershipDAO_ = new ADMembershipDAOImpl(ldapAttrMapping, ldapService, adSearch, this, cacheHandler);
      }
      else
      {
         userDAO_ = new UserDAOImpl(ldapAttrMapping, ldapService, cacheHandler, this);
         groupDAO_ = new GroupDAOImpl(ldapAttrMapping, ldapService, cacheHandler);
         membershipDAO_ = new MembershipDAOImpl(ldapAttrMapping, ldapService, this, cacheHandler);
      }
      userProfileDAO_ = new UserProfileDAOImpl(hservice, cservice, userDAO_);
      membershipTypeDAO_ = new MembershipTypeDAOImpl(ldapAttrMapping, ldapService, cacheHandler);

      ValueParam param = params.getValueParam("ldap.userDN.key");
      if (param != null)
         ldapAttrMapping.userDNKey = param.getValue();

      param = params.getValueParam("ldap.groupDN.key");
      if (param != null)
         ldapAttrMapping.groupDNKey = param.getValue();
   }

}
