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

import org.exoplatform.services.ldap.LDAPService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;

import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.SortControl;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Deprecated
public class SimpleLdapUserListAccess extends LdapListAccess<User>
{

   /**
    * Base search DN.
    */
   protected final String searchBase;

   /**
    * Search filter.
    */
   protected final String filter;

   /**
    * LDAP attribute to organization service essences.
    */
   protected final LDAPAttributeMapping ldapAttrMapping;

   private int size = -1;

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.organization.ldap.SimpleLdapUserListAccess");

   /**
    * @param ldapAttrMapping LDAP attribute to organization service essences 
    * @param ldapService LDAP service
    * @param searchBase base search DN
    * @param filter search filter
    */
   public SimpleLdapUserListAccess(LDAPAttributeMapping ldapAttrMapping, LDAPService ldapService, String searchBase,
      String filter)
   {
      super(ldapService);
      this.ldapAttrMapping = ldapAttrMapping;
      this.searchBase = searchBase;
      this.filter = filter;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected User[] load(LdapContext ctx, int index, int length) throws Exception
   {
      User[] users = new User[length];
      if (length == 0)
         return users;
      NamingEnumeration<SearchResult> results = null;

      try
      {
         SortControl sctl = new SortControl(new String[]{ldapAttrMapping.userUsernameAttr}, Control.NONCRITICAL);
         ctx.setRequestControls(new Control[]{sctl});

         // returns only needed attributes for creation UserImpl in
         // LDAPAttributeMapping.attributesToUser() method 
         String[] returnedAtts;
         if (ldapAttrMapping.hasUserAccountControl())
         {
            String[] attrs = {ldapAttrMapping.userUsernameAttr, ldapAttrMapping.userFirstNameAttr, ldapAttrMapping.userLastNameAttr,
               ldapAttrMapping.userDisplayNameAttr, ldapAttrMapping.userMailAttr, ldapAttrMapping.userPassword, ldapAttrMapping.userAccountControlAttr};
            returnedAtts = attrs;
         }
         else
         {
            String[] attrs ={ldapAttrMapping.userUsernameAttr, ldapAttrMapping.userFirstNameAttr, ldapAttrMapping.userLastNameAttr,
               ldapAttrMapping.userDisplayNameAttr, ldapAttrMapping.userMailAttr, ldapAttrMapping.userPassword};
            returnedAtts = attrs;
         }

         SearchControls constraints = new SearchControls();
         constraints.setReturningAttributes(returnedAtts);
         constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

         results = ctx.search(searchBase, filter, constraints);
         for (int p = 0, counter = 0; counter < length; p++)
         {
            if (!results.hasMoreElements())
            {
               throw new IllegalArgumentException(
                  "Illegal index or length: sum of the index and the length cannot be greater than the list size");
            }

            SearchResult result = results.next();

            if (p >= index)
            { // start point for getting results
               User user = ldapAttrMapping.attributesToUser(result.getAttributes());
               users[counter++] = user;
            }
         }
      }
      finally
      {
         if (results != null)
            results.close();
      }

      if (LOG.isDebugEnabled())
         LOG.debug("range of users from " + index + " to " + (index + length));
      return users;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected int getSize(LdapContext ctx) throws Exception
   {
      if (size < 0)
      {
         NamingEnumeration<SearchResult> results = null;

         try
         {
            String[] returnedAtts = {ldapAttrMapping.userUsernameAttr};

            SearchControls constraints = new SearchControls();
            constraints.setReturningAttributes(returnedAtts);
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

            results = ctx.search(searchBase, filter, constraints);
            size = 0;
            while (results.hasMoreElements())
            {
               results.next();
               size++;
            }

         }
         finally
         {
            if (results != null)
               results.close();
         }
      }
      if (LOG.isDebugEnabled())
         LOG.debug("size : " + size);
      return size;
   }

}
