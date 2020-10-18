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

import org.exoplatform.services.tck.organization.TestUserHandler;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestUserHandlerWithOldData extends TestUserHandler
{
   /**
    * Create new user for test purpose only with old structure.
    */
   protected void createUser(String userName) throws Exception
   {
      String[] beforeUserLdapClasses = LDAPAttributeMapping.USER_LDAP_CLASSES;
      String beforeUserAccountControlAttr = ((UserDAOImpl)uHandler).ldapAttrMapping.userAccountControlAttr;
      try
      {
         LDAPAttributeMapping.USER_LDAP_CLASSES = "top,person,organizationalPerson,inetOrgPerson".split(",");
         ((UserDAOImpl)uHandler).ldapAttrMapping.userAccountControlAttr = null;
         super.createUser(userName);
      }
      finally
      {
         LDAPAttributeMapping.USER_LDAP_CLASSES = beforeUserLdapClasses;
         ((UserDAOImpl)uHandler).ldapAttrMapping.userAccountControlAttr = beforeUserAccountControlAttr;
      }
   }
}
