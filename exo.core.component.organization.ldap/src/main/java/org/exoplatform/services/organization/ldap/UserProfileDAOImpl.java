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

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.ldap.LDAPService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.CacheHandler;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.services.organization.UserProfileEventListenerHandler;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.organization.impl.UserProfileData;
import org.exoplatform.services.organization.impl.UserProfileImpl;
import org.exoplatform.services.security.PermissionConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapContext;


/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Oct 14, 2005. @version andrew00x $
 */
@Deprecated
public class UserProfileDAOImpl extends BaseDAO implements UserProfileHandler, UserProfileEventListenerHandler
{

   /**
    * User profile event listeners.
    * 
    * @see UserProfileEventListener
    */
   private List<UserProfileEventListener> listeners;

   /**
    * Logger. 
    */
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.organization.ldap.UserProfileDAOImpl");

   public UserProfileDAOImpl(LDAPAttributeMapping ldapAttrMapping, LDAPService ldapService, CacheHandler cacheHandler)
      throws Exception
   {
      super(ldapAttrMapping, ldapService, cacheHandler);
      this.listeners = new ArrayList<UserProfileEventListener>(3);
   }

   /**
    * {@inheritDoc}
    */
   public final UserProfile createUserProfileInstance()
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
      String profileDN =
         ldapAttrMapping.membershipTypeNameAttr + "=" + profile.getUserName() + "," + ldapAttrMapping.profileURL;
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               try
               {
                  ctx.lookup(profileDN);
               }
               catch (NameNotFoundException e)
               {
                  if (broadcast)
                     preSave(profile, true);

                  ctx.createSubcontext(profileDN, ldapAttrMapping.profileToAttributes(profile)).close();

                  if (broadcast)
                     postSave(profile, true);
                  
                  return;
               }
               UserProfileData upd = new UserProfileData();
               upd.setUserProfile(profile);

               if (broadcast)
                  preSave(profile, false);

               ModificationItem[] mods = new ModificationItem[1];
               mods[0] =
                  new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(
                     ldapAttrMapping.ldapDescriptionAttr, upd.getProfile()));
               ctx.modifyAttributes(profileDN, mods);

               if (broadcast)
                  postSave(profile, false);
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }
      }
      catch (InvalidAttributeValueException invalid)
      {
         LOG.error(invalid.getLocalizedMessage(), invalid);
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * {@inheritDoc}
    */
   public UserProfile removeUserProfile(String userName, boolean broadcast) throws Exception
   {
      String profileDN = ldapAttrMapping.membershipTypeNameAttr + "=" + userName + "," + ldapAttrMapping.profileURL;
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               // NameNotFoundException here if profile does not exists
               Attributes attrs = ctx.getAttributes(profileDN);
               UserProfile profile = ldapAttrMapping.attributesToProfile(attrs).getUserProfile();
               if (broadcast)
                  preDelete(profile);

               ctx.destroySubcontext(profileDN);
               
               if (broadcast)
                  postDelete(profile);

               return profile;
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }
      }
      catch (NameNotFoundException e)
      {
         if (LOG.isDebugEnabled())
            LOG.debug(e.getLocalizedMessage(), e);
         return null;
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * {@inheritDoc}
    */
   public UserProfile findUserProfileByName(String userName) throws Exception
   {
      String profileDN = ldapAttrMapping.membershipTypeNameAttr + "=" + userName + "," + ldapAttrMapping.profileURL;
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               // NameNotFoundException here if profile does not exists
               Attributes attrs = ctx.getAttributes(profileDN);
               return ldapAttrMapping.attributesToProfile(attrs).getUserProfile();
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }
      }
      catch (NameNotFoundException e)
      {
         if (LOG.isDebugEnabled())
            LOG.debug(e.getLocalizedMessage(), e);
         return null;
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * {@inheritDoc}
    */
   public Collection<UserProfile> findUserProfiles() throws Exception
   {
      // currently profile stored in database
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public void addUserProfileEventListener(UserProfileEventListener listener)
   {
      SecurityHelper.validateSecurityPermission(PermissionConstants.MANAGE_LISTENERS);
      listeners.add(listener);
   }

   /**
    * {@inheritDoc}
    */
   public void removeUserProfileEventListener(UserProfileEventListener listener)
   {
      SecurityHelper.validateSecurityPermission(PermissionConstants.MANAGE_LISTENERS);
      listeners.remove(listener);
   }

   private void preSave(UserProfile profile, boolean isNew) throws Exception
   {
      for (UserProfileEventListener listener : listeners)
         listener.preSave(profile, isNew);
   }

   private void postSave(UserProfile profile, boolean isNew) throws Exception
   {
      for (UserProfileEventListener listener : listeners)
         listener.postSave(profile, isNew);
   }

   private void preDelete(UserProfile profile) throws Exception
   {
      for (UserProfileEventListener listener : listeners)
         listener.preDelete(profile);
   }

   private void postDelete(UserProfile profile) throws Exception
   {
      for (UserProfileEventListener listener : listeners)
         listener.postDelete(profile);
   }
   /**
    * {@inheritDoc}
    */
   public List<UserProfileEventListener> getUserProfileListeners()
   {
      return Collections.unmodifiableList(listeners);
   }

}
