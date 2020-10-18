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
package org.exoplatform.services.organization.cache;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;

import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br>Date: 2009
 *
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a> 
 * @version $Id$
 */
public class CacheableUserHandlerImpl implements UserHandler
{

   private final ExoCache<String, User> userCache;

   private final ExoCache<?, ?> userProfileCache;

   private final ExoCache membershipCache;

   private final UserHandler userHandler;

   /**
    * CacheableUserHandler  constructor.
    *
    * @param userHandler
    *             - user handler
    */
   public CacheableUserHandlerImpl(OrganizationCacheHandler organizationCacheHandler, UserHandler userHandler)
   {
      this.userCache = organizationCacheHandler.getUserCache();
      this.userProfileCache = organizationCacheHandler.getUserProfileCache();
      this.membershipCache = organizationCacheHandler.getMembershipCache();
      this.userHandler = userHandler;
   }

   /**
    * {@inheritDoc}
    */
   public void addUserEventListener(UserEventListener listener)
   {
      userHandler.addUserEventListener(listener);
   }

   /**
    * {@inheritDoc}
    */
   public void removeUserEventListener(UserEventListener listener)
   {
      userHandler.removeUserEventListener(listener);
   }

   /**
    * {@inheritDoc}
    */
   public boolean authenticate(String username, String password) throws Exception
   {
      boolean authenticated = userHandler.authenticate(username, password);
      userCache.remove(username);

      return authenticated;
   }

   /**
    * {@inheritDoc}
    */
   public void createUser(User user, boolean broadcast) throws Exception
   {
      userHandler.createUser(user, broadcast);
   }

   /**
    * {@inheritDoc}
    */
   public User createUserInstance()
   {
      return userHandler.createUserInstance();
   }

   /**
    * {@inheritDoc}
    */
   public User createUserInstance(String username)
   {
      return userHandler.createUserInstance(username);
   }

   /**
    * {@inheritDoc}
    */
   public User findUserByName(String userName) throws Exception
   {
      return findUserByName(userName, UserStatus.ENABLED);
   }

   /**
    * {@inheritDoc}
    */
   public PageList<User> findUsers(Query query) throws Exception
   {
      return userHandler.findUsers(query);
   }

   /**
    * {@inheritDoc}
    */
   public PageList<User> findUsersByGroup(String groupId) throws Exception
   {
      return userHandler.findUsersByGroup(groupId);
   }

   /**
    * {@inheritDoc}
    */
   public PageList<User> getUserPageList(int pageSize) throws Exception
   {
      return userHandler.getUserPageList(pageSize);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<User> findUsersByGroupId(String groupId) throws Exception
   {
      return userHandler.findUsersByGroupId(groupId);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<User> findAllUsers() throws Exception
   {
      return userHandler.findAllUsers();
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<User> findUsersByQuery(Query query) throws Exception
   {
      return userHandler.findUsersByQuery(query);
   }

   /**
    * {@inheritDoc}
    */
   public User removeUser(String userName, boolean broadcast) throws Exception
   {
      User user = userHandler.removeUser(userName, broadcast);
      if (user != null)
      {
         userCache.remove(userName);
         userProfileCache.remove(userName);

         List<Membership> memberships = membershipCache.getCachedObjects();
         for (Membership membership : memberships)
         {
            if (membership.getUserName().equals(userName))
            {
               membershipCache.remove(membership.getId());
               membershipCache.remove(new MembershipCacheKey(membership));
            }
         }
      }

      return user;
   }

   /**
    * {@inheritDoc}
    */
   public void saveUser(User user, boolean broadcast) throws Exception
   {
      userHandler.saveUser(user, broadcast);
      userCache.put(user.getUserName(), user);
   }

   /**
    * {@inheritDoc}
    */
   public User setEnabled(String userName, boolean enabled, boolean broadcast) throws Exception
   {
      User result = userHandler.setEnabled(userName, enabled, broadcast);
      if (result != null)
      {
         userCache.put(result.getUserName(), result);
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public User findUserByName(String userName, UserStatus status) throws Exception
   {
      User user = userCache.get(userName);
      if (user != null)
         return status.matches(user.isEnabled()) ? user : null;

      user = userHandler.findUserByName(userName, status);
      if (user != null)
         userCache.put(userName, user);

      return user;
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<User> findUsersByGroupId(String groupId, UserStatus status) throws Exception
   {
      return userHandler.findUsersByGroupId(groupId, status);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<User> findAllUsers(UserStatus status) throws Exception
   {
      return userHandler.findAllUsers(status);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<User> findUsersByQuery(Query query, UserStatus status) throws Exception
   {
      return userHandler.findUsersByQuery(query, status);
   }
}
