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
import org.exoplatform.services.organization.CacheHandler;
import org.exoplatform.services.organization.Group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

/**
 * Created by The eXo Platform SAS Author : James Chamberlain.
 * james.chamberlain@gmail.com Feb 22, 2006
 * @version andrew00x $
 */
@Deprecated
public class ADGroupDAOImpl extends GroupDAOImpl
{

   private ADSearchBySID adSearch;

   /**
    * @param ldapAttrMapping mapping LDAP attributes to eXo organization service
    *          items
    * @param ldapService {@link LDAPService}
    * @param ad See {@link ADSearchBySID}
    * @param cacheHandler
    *          The Cache Handler
    * @throws Exception if any errors occurs
    */
   public ADGroupDAOImpl(LDAPAttributeMapping ldapAttrMapping, LDAPService ldapService, ADSearchBySID ad,
      CacheHandler cacheHandler) throws Exception
   {
      super(ldapAttrMapping, ldapService, cacheHandler);
      adSearch = ad;
   }

   /**
    * {@inheritDoc}
    */
   public Collection<Group> findGroupByMembership(String userName, String membershipType) throws Exception
   {
      return findGroups(userName, membershipType);
   }

   /**
    * {@inheritDoc}
    */
   public Collection<Group> findGroupsOfUser(String userName) throws Exception
   {
      return findGroups(userName, null);
   }

   /**
    * @param userName user name
    * @param type membership type
    * @return collection of groups
    * @throws Exception if any errors occurs
    */
   private Collection<Group> findGroups(String userName, String type) throws Exception
   {
      LdapContext ctx = ldapService.getLdapContext();
      List<Group> groups = new ArrayList<Group>();
      try
      {
         NamingEnumeration<SearchResult> results = null;
         for (int err = 0;; err++)
         {
            groups.clear();
            try
            {
               String userDN = getDNFromUsername(ctx, userName);
               if (userDN == null)
                  return groups;

               String filter = ldapAttrMapping.userObjectClassFilter;
               String[] retAttrs = {"tokenGroups"};
               SearchControls constraints = new SearchControls();
               constraints.setSearchScope(SearchControls.OBJECT_SCOPE);
               constraints.setReturningAttributes(retAttrs);

               results = ctx.search(userDN, filter, constraints);
               while (results.hasMore())
               {
                  SearchResult sr = results.next();
                  Attributes attrs = sr.getAttributes();
                  Attribute attr = attrs.get("tokenGroups");
                  for (int x = 0; x < attr.size(); x++)
                  {
                     byte[] SID = (byte[])attr.get(x);
                     String membershipDN = adSearch.findMembershipDNBySID(ctx, SID, ldapAttrMapping.groupsURL, type);
                     if (membershipDN != null)
                     {
                        Group group = getGroupFromMembershipDN(ctx, membershipDN);
                        if (group != null && !checkExist(group, groups))
                           groups.add(group);
                     }
                  }
               }
               return groups;
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
            finally
            {
               if (results != null)
                  results.close();
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * Check is supplied group presents in groups collection.
    * 
    * @param group {@link Group}
    * @param list collection on Groups
    * @return true if collection contains group false otherwise
    */
   private boolean checkExist(Group group, List<Group> list)
   {
      for (Group ele : list)
      {
         if (ele.getId().equals(group.getId()))
            return true;
      }
      return false;
   }

}
