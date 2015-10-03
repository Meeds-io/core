/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.services.organization.hibernate;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.database.ObjectQuery;
import org.exoplatform.services.organization.ExtendedUserHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserEventListenerHandler;
import org.exoplatform.services.organization.UserHandler;
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
public class UserDAOImpl implements UserHandler, UserEventListenerHandler, ExtendedUserHandler
{
   public static final String queryFindUserByName =
      "from org.exoplatform.services.organization.impl.UserImpl where userName = :id";

   private static final String USER_PROFILE_DATA_ENTITY_HSQL_PATH =
           "org.exoplatform.services.organization.impl.UserProfileDataHsql";

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

   final public List<UserEventListener> getUserEventListeners()
   {
      return listeners_;
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
      if ( service_.getSessionFactory().getClassMetadata(USER_PROFILE_DATA_ENTITY_HSQL_PATH) != null) {
         ((UserProfileDAOHsqlImpl) orgService.getUserProfileHandler()).removeUserProfileEntry(userName, session);
      }
      else
      {
         ((UserProfileDAOImpl) orgService.getUserProfileHandler()).removeUserProfileEntry(userName, session);
      }
      MembershipDAOImpl.removeMembershipEntriesOfUser(userName, session);

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
      User user = (User)cache_.get(userName);
      if (user != null)
         return user;
      Session session = service_.openSession();
      user = findUserByName(userName, session);
      if (user != null)
         cache_.put(userName, user);
      return user;
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
      String findQuery = "from o in class " + UserImpl.class.getName();
      String countQuery = "select count(o) from " + UserImpl.class.getName() + " o";

      return new HibernateListAccess<User>(service_, findQuery, countQuery);
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
      User user = findUserByName(username);
      if (user == null)
      {
         return false;
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

      return new HibernateListAccess<User>(service_, oq.getHibernateQueryWithBinding(),
         oq.getHibernateCountQueryWithBinding(), oq.getBindingFields());
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
      String queryFindUsersInGroup =
         "select u " + "from u in class org.exoplatform.services.organization.impl.UserImpl, "
            + "     m in class org.exoplatform.services.organization.impl.MembershipImpl "
            + "where m.userName = u.userName " + "     and m.groupId =  '" + groupId + "'";
      String countUsersInGroup =
         "select count(u) " + "from u in class org.exoplatform.services.organization.impl.UserImpl, "
            + "     m in class org.exoplatform.services.organization.impl.MembershipImpl "
            + "where m.userName = u.userName " + "  and m.groupId =  '" + groupId + "'";

      return new HibernateListAccess<User>(service_, queryFindUsersInGroup, countUsersInGroup);
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
}
