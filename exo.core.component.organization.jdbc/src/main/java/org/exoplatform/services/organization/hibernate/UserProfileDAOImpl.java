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

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.services.organization.UserProfileEventListenerHandler;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.organization.impl.UserProfileData;
import org.exoplatform.services.organization.impl.UserProfileImpl;
import org.exoplatform.services.security.PermissionConstants;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.naming.InvalidNameException;

/**
 * Created by The eXo Platform SAS
 * Author : Mestrallet Benjamin benjmestrallet@users.sourceforge.net
 * Author : Tuan Nguyen tuan08@users.sourceforge.net
 * Date: Aug 22, 2003 Time: 4:51:21 PM
 */
@Deprecated
public class UserProfileDAOImpl implements UserProfileHandler, UserProfileEventListenerHandler
{
   static private UserProfile NOT_FOUND = new UserProfileImpl();

   private static final String queryFindUserProfileByName =
      "from u in class org.exoplatform.services.organization.impl.UserProfileData where u.userName = :id";

   private static final String queryFindUserProfiles =
      "from u in class org.exoplatform.services.organization.impl.UserProfileData";

   private HibernateService service_;

   private ExoCache<Serializable, Object> cache_;

   private List<UserProfileEventListener> listeners_;

   private UserHandler userDAO;

   public UserProfileDAOImpl(HibernateService service, CacheService cservice, UserHandler userDAO) throws Exception
   {
      service_ = service;
      cache_ = cservice.getCacheInstance(getClass().getName());
      listeners_ = new ArrayList<UserProfileEventListener>(3);
      this.userDAO = userDAO;
   }

   /**
    * {@inheritDoc}
    */
   public void addUserProfileEventListener(UserProfileEventListener listener)
   {
      SecurityHelper.validateSecurityPermission(PermissionConstants.MANAGE_LISTENERS);
      listeners_.add(listener);
   }

   /**
    * {@inheritDoc}
    */
   public void removeUserProfileEventListener(UserProfileEventListener listener)
   {
      SecurityHelper.validateSecurityPermission(PermissionConstants.MANAGE_LISTENERS);
      listeners_.remove(listener);
   }

   /**
    * {@inheritDoc}
    */
   final public UserProfile createUserProfileInstance()
   {
      return new UserProfileImpl();
   }

   /**
    * {@inheritDoc}
    */
   public UserProfile createUserProfileInstance(String userName)
   {
      return new UserProfileImpl(userName);
   }

   /**
    * {@inheritDoc}
    */
   public void saveUserProfile(UserProfile profile, boolean broadcast) throws Exception
   {
      String userName = profile.getUserName();
      Session session = service_.openSession();
      UserProfileData upd = (UserProfileData)service_.findOne(session, queryFindUserProfileByName, userName);

      User user = userDAO.findUserByName(userName);
      if (user == null)
      {
         throw new InvalidNameException("User " + userName + " not exists");
      }

      if (upd == null)
      {
         upd = new UserProfileData();
         upd.setUserProfile(profile);
         if (broadcast)
            preSave(profile, true);

         session.save(userName, upd);
         session.flush();
         cache_.put(userName, profile);

         if (broadcast)
            postSave(profile, true);
      }
      else
      {
         upd.setUserProfile(profile);
         if (broadcast)
            preSave(profile, false);

         session.update(upd);
         session.flush();
         cache_.put(userName, profile);

         if (broadcast)
            postSave(profile, false);
      }
   }

   /**
    * {@inheritDoc}
    */
   public UserProfile removeUserProfile(String userName, boolean broadcast) throws Exception
   {
      Session session = service_.openSession();
      try
      {
         UserProfileData upd = (UserProfileData)service_.findExactOne(session, queryFindUserProfileByName, userName);
         UserProfile profile = upd.getUserProfile();
         if (broadcast)
            preDelete(profile);

         session.delete(upd);
         session.flush();
         cache_.remove(userName);

         if (broadcast)
            postDelete(profile);

         return profile;
      }
      catch (Exception exp)
      {
         return null;
      }
   }

   /**
    * {@inheritDoc}
    */
   public UserProfile findUserProfileByName(String userName) throws Exception
   {
      UserProfile up = (UserProfile)cache_.get(userName);
      if (up != null)
      {
         if (NOT_FOUND == up) //NOSONAR
         {
            return null;
         }
         return up;
      }
      Session session = service_.openSession();
      up = findUserProfileByName(userName, session);
      if (up != null)
         cache_.put(userName, up);
      else
         cache_.put(userName, NOT_FOUND);
      return up;
   }

   /**
    * {@inheritDoc}
    */
   public UserProfile findUserProfileByName(String userName, Session session) throws Exception
   {
      UserProfileData upd = (UserProfileData)service_.findOne(session, queryFindUserProfileByName, userName);
      if (upd != null)
      {
         return upd.getUserProfile();
      }
      return null;
   }

   void removeUserProfileEntry(String userName, Session session) throws Exception
   {
      UserProfileData upd = (UserProfileData)session.createQuery(queryFindUserProfileByName).setString("id", userName).uniqueResult();
      if (upd != null)
      {
         session.delete(upd);
         cache_.remove(userName);
      }
   }

   /**
    * {@inheritDoc}
    */
   public Collection<UserProfile> findUserProfiles() throws Exception
   {
      Session session = service_.openSession();
      @SuppressWarnings("unchecked")
      Collection<UserProfileData> result =
         (Collection<UserProfileData>)service_.findAll(session, queryFindUserProfiles);
      if (result == null)
         return null;
      Collection<UserProfile> profiles = new ArrayList<UserProfile>(result.size());
      for (UserProfileData data : result)
      {
         if (data != null)
            profiles.add(data.getUserProfile());
      }
      return profiles;
   }

   private void preSave(UserProfile profile, boolean isNew) throws Exception
   {
      for (UserProfileEventListener listener : listeners_)
         listener.preSave(profile, isNew);
   }

   private void postSave(UserProfile profile, boolean isNew) throws Exception
   {
      for (UserProfileEventListener listener : listeners_)
         listener.postSave(profile, isNew);
   }

   private void preDelete(UserProfile profile) throws Exception
   {
      for (UserProfileEventListener listener : listeners_)
         listener.preDelete(profile);
   }

   private void postDelete(UserProfile profile) throws Exception
   {
      for (UserProfileEventListener listener : listeners_)
         listener.postDelete(profile);
   }

   /**
    * {@inheritDoc}
    */
   public List<UserProfileEventListener> getUserProfileListeners()
   {
      return Collections.unmodifiableList(listeners_);
   }

}
