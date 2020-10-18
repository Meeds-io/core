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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.database.ObjectQuery;
import org.exoplatform.services.organization.DisabledUserException;
import org.exoplatform.services.organization.ExtendedUserHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserEventListenerHandler;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.impl.UserImpl;
import org.exoplatform.services.security.PasswordEncrypter;
import org.exoplatform.services.security.PermissionConstants;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Mestrallet Benjamin benjmestrallet@users.sourceforge.net
 * Author : Tuan Nguyen tuan08@users.sourceforge.net
 * Date: Aug 22, 2003 Time: 4:51:21 PM
 */
@Deprecated
public class UserDAOImpl implements UserHandler, UserEventListenerHandler, ExtendedUserHandler
{
   public static final String queryFindUserByName =
      "from org.exoplatform.services.organization.impl.UserImpl where userName = :id";

   private HibernateService service_;

   private ExoCache<String, User> cache_;

   private List<UserEventListener> listeners_ = new ArrayList<UserEventListener>(3);

   private OrganizationService orgService;

   public UserDAOImpl(HibernateService service, CacheService cservice, OrganizationService orgService) throws Exception
   {
      service_ = service;
      cache_ = cservice.getCacheInstance(UserImpl.class.getName());
      this.orgService = orgService;
   }

   /**
    * {@inheritDoc}
    */
   public void addUserEventListener(UserEventListener listener)
   {
      SecurityHelper.validateSecurityPermission(PermissionConstants.MANAGE_LISTENERS);
      listeners_.add(listener);
   }

   /**
    * {@inheritDoc}
    */
   public void removeUserEventListener(UserEventListener listener)
   {
      SecurityHelper.validateSecurityPermission(PermissionConstants.MANAGE_LISTENERS);
      listeners_.remove(listener);
   }

   /**
    * {@inheritDoc}
    */
   public User createUserInstance()
   {
      return new UserImpl();
   }

   /**
    * {@inheritDoc}
    */
   public User createUserInstance(String username)
   {
      return new UserImpl(username);
   }

   /**
    * {@inheritDoc}
    */
   public void createUser(User user, boolean broadcast) throws Exception
   {
      if (broadcast)
         preSave(user, true);

      final Session session = service_.openSession();

      UserImpl userImpl = (UserImpl)user;
      userImpl.setId(user.getUserName());
      session.save(user);
      session.flush();

      if (broadcast)
         postSave(user, true);
   }

   /**
    * {@inheritDoc}
    */
   public void saveUser(User user, boolean broadcast) throws Exception
   {
      if (user != null && !user.isEnabled())
         throw new DisabledUserException(user.getUserName());
      if (broadcast)
         preSave(user, false);

      Session session = service_.openSession();

      session.merge(user);
      session.flush();
      cache_.put(user.getUserName(), user);

      if (broadcast)
         postSave(user, false);
   }

   /**
    * {@inheritDoc}
    */
   public User removeUser(String userName, boolean broadcast) throws Exception
   {
      Session session = service_.openSession();
      User foundUser = findUserByName(userName, session);

      if (foundUser == null)
         return null;

      if (broadcast)
         preDelete(foundUser);

      session.delete(foundUser);
      ((UserProfileDAOImpl)orgService.getUserProfileHandler()).removeUserProfileEntry(userName, session);
      ((MembershipDAOImpl)orgService.getMembershipHandler()).removeMembershipEntriesOfUser(userName, session);

      session.flush();
      cache_.remove(userName);

      if (broadcast)
         postDelete(foundUser);

      return foundUser;
   }

   /**
    * {@inheritDoc}
    */
   public User findUserByName(String userName) throws Exception
   {
      return findUserByName(userName, UserStatus.ENABLED);
   }

   public User findUserByName(String userName, Session session) throws Exception
   {
      User user = (User)service_.findOne(session, queryFindUserByName, userName);
      return user;
   }

   /**
    * {@inheritDoc}
    */
   public LazyPageList<User> getUserPageList(int pageSize) throws Exception
   {
      return new LazyPageList<User>(findAllUsers(), 20);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<User> findAllUsers() throws Exception
   {
      return findAllUsers(UserStatus.ENABLED);
   }

   /**
    * {@inheritDoc}
    */
   public boolean authenticate(String username, String password) throws Exception
   {
      return authenticate(username, password, null);
   }

   /**
    * {@inheritDoc}
    */
   public boolean authenticate(String username, String password, PasswordEncrypter pe) throws Exception
   {
      User user = findUserByName(username, UserStatus.ANY);
      if (user == null)
      {
         return false;
      }

      if (!user.isEnabled())
      {
         throw new DisabledUserException(username);
      }
      boolean authenticated;
      if (pe == null)
      {
         authenticated = user.getPassword().equals(password);
      }
      else
      {
         String encryptedPassword = new String(pe.encrypt(user.getPassword().getBytes()));
         authenticated = encryptedPassword.equals(password);
      }
      if (authenticated)
      {
         UserImpl userImpl = (UserImpl)user;
         userImpl.setLastLoginTime(Calendar.getInstance().getTime());
         saveUser(userImpl, false);
      }
      return authenticated;
   }

   /**
    * {@inheritDoc}
    */
   public LazyPageList<User> findUsers(Query q) throws Exception
   {
      return new LazyPageList<User>(findUsersByQuery(q), 20);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<User> findUsersByQuery(Query q) throws Exception
   {
      return findUsersByQuery(q, UserStatus.ENABLED);
   }

   /**
    * {@inheritDoc}
    */
   public LazyPageList<User> findUsersByGroup(String groupId) throws Exception
   {
      return new LazyPageList<User>(findUsersByGroupId(groupId), 20);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<User> findUsersByGroupId(String groupId) throws Exception
   {
      return findUsersByGroupId(groupId, UserStatus.ENABLED);
   }

   /**
    * {@inheritDoc}
    */
   public Collection<?> findUsersByGroupAndRole(String groupName, String role) throws Exception
   {
      String queryFindUsersByGroupAndRole =
         "select u " + "from u in class org.exoplatform.services.organization.impl.UserImpl, "
            + "     m in class org.exoplatform.services.organization.impl.MembershipImpl, "
            + "     g in class org.exoplatform.services.organization.impl.GroupImpl " + "where m.user = u "
            + "  and m.group = g " + "  and g.groupName = :groupname " + "  and m.role = :role ";
      Session session = service_.openSession();
      org.hibernate.Query query = session.createQuery(queryFindUsersByGroupAndRole);
      query.setParameter("groupname", groupName);
      query.setParameter("role", role);
      List<?> users = query.list();
      return users;
   }

   private void preSave(User user, boolean isNew) throws Exception
   {
      for (UserEventListener listener : listeners_)
         listener.preSave(user, isNew);
   }

   private void postSave(User user, boolean isNew) throws Exception
   {
      for (UserEventListener listener : listeners_)
         listener.postSave(user, isNew);
   }

   private void preDelete(User user) throws Exception
   {
      for (UserEventListener listener : listeners_)
         listener.preDelete(user);
   }

   private void postDelete(User user) throws Exception
   {
      for (UserEventListener listener : listeners_)
         listener.postDelete(user);
   }

   private void preSetEnabled(User user) throws Exception
   {
      for (UserEventListener listener : listeners_)
         listener.preSetEnabled(user);
   }

   private void postSetEnabled(User user) throws Exception
   {
      for (UserEventListener listener : listeners_)
         listener.postSetEnabled(user);
   }

   private String addAsterisk(String s)
   {
      StringBuffer sb = new StringBuffer(s);
      if (!s.startsWith("*"))
      {
         sb.insert(0, "*");
      }
      if (!s.endsWith("*"))
      {
         sb.append("*");
      }

      return sb.toString();

   }

   /**
    * {@inheritDoc}
    */
   public List<UserEventListener> getUserListeners()
   {
      return Collections.unmodifiableList(listeners_);
   }

   /**
    * {@inheritDoc}
    */
   public User setEnabled(String userName, boolean enabled, boolean broadcast) throws Exception
   {
      Session session = service_.openSession();
      User foundUser = findUserByName(userName, session);

      if (foundUser == null || foundUser.isEnabled() == enabled)
      {
         return foundUser;
      }
      ((UserImpl)foundUser).setEnabled(enabled);
      if (broadcast)
         preSetEnabled(foundUser);

      session.merge(foundUser);
      session.flush();

      if (broadcast)
         postSetEnabled(foundUser);

      cache_.put(foundUser.getUserName(), foundUser);
      return foundUser;
  }

   /**
    * {@inheritDoc}
    */
   public User findUserByName(String userName, UserStatus status) throws Exception
   {
      User user = cache_.get(userName);
      if (user != null)
         return status.matches(user.isEnabled()) ? user : null;
      Session session = service_.openSession();
      user = findUserByName(userName, session);
      if (user != null)
      {
         cache_.put(userName, user);
         return status.matches(user.isEnabled()) ? user : null;
      }
      return user;
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<User> findUsersByGroupId(String groupId, UserStatus status) throws Exception
   {
      String queryFindUsersInGroup =
         "select u " + "from u in class org.exoplatform.services.organization.impl.UserImpl, "
            + "     m in class org.exoplatform.services.organization.impl.MembershipImpl "
            + "where m.userName = u.userName" +
            (status != UserStatus.ANY ? " and u.enabled = " + status.acceptsEnabled() : "") +
            " and m.groupId =  '" + groupId + "'";
      String countUsersInGroup =
         "select count(u) " + "from u in class org.exoplatform.services.organization.impl.UserImpl, "
            + "     m in class org.exoplatform.services.organization.impl.MembershipImpl "
            + "where m.userName = u.userName" +
            (status != UserStatus.ANY ? " and u.enabled = " + status.acceptsEnabled() : "") +
            " and m.groupId =  '" + groupId + "'";

      return new HibernateListAccess<User>(service_, queryFindUsersInGroup, countUsersInGroup);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<User> findAllUsers(UserStatus status) throws Exception
   {
      String findQuery = "from o in class " + UserImpl.class.getName() +
      (status != UserStatus.ANY ? " where o.enabled = " + status.acceptsEnabled() : "");
      String countQuery = "select count(o) from " + UserImpl.class.getName() + " o" +
      (status != UserStatus.ANY ? " where o.enabled = " + status.acceptsEnabled() : "");

      return new HibernateListAccess<User>(service_, findQuery, countQuery);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<User> findUsersByQuery(Query q, UserStatus status) throws Exception
   {
      ObjectQuery oq = new ObjectQuery(UserImpl.class);
      if (q.getUserName() != null)
      {
         oq.addLIKE("UPPER(userName)", addAsterisk(q.getUserName().toUpperCase()));
      }
      if (q.getFirstName() != null)
      {
         oq.addLIKE("UPPER(firstName)", q.getFirstName().toUpperCase());
      }
      if (q.getLastName() != null)
      {
         oq.addLIKE("UPPER(lastName)", q.getLastName().toUpperCase());
      }
      oq.addLIKE("email", q.getEmail());
      oq.addGT("lastLoginTime", q.getFromLoginDate());
      oq.addLT("lastLoginTime", q.getToLoginDate());
      if (status != UserStatus.ANY)
      {
         oq.addEQ("enabled", status.acceptsEnabled());
      }
      return new HibernateListAccess<User>(service_, oq.getHibernateQueryWithBinding(),
         oq.getHibernateCountQueryWithBinding(), oq.getBindingFields());
   }
}
