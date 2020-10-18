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
import org.exoplatform.services.organization.CacheHandler.CacheType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

/**
 * Created by The eXo Platform SAS .
 * Author : James Chamberlain james.chamberlain@gmail.com
 */
@Deprecated
public class ADUserDAOImpl extends UserDAOImpl
{

   /**
    * AD user's account controls attribute.
    */
   static final int UF_PASSWD_NOTREQD = 0x0020;

   /**
    * AD user's account controls attribute.
    */
   static final int UF_NORMAL_ACCOUNT = 0x0200;

   /**
    * AD user's account controls attribute.
    */
   static final int UF_PASSWORD_EXPIRED = 0x800000;

   /**
    * @param ldapAttrMapping {@link LDAPAttributeMapping}
    * @param ldapService {@link LDAPService}
    * @param cacheHandler
    *          The Cache Handler
    * @throws Exception if any errors occurs
    */
   public ADUserDAOImpl(LDAPAttributeMapping ldapAttrMapping, LDAPService ldapService, CacheHandler cacheHandler,
                        OrganizationService os)
      throws Exception
   {
      super(ldapAttrMapping, ldapService, cacheHandler, os);
      LDAPUserPageList.SEARCH_CONTROL = Control.CRITICAL;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void createUser(User user, boolean broadcast) throws Exception
   {
      String userDN = ldapAttrMapping.userDNKey + "=" + user.getUserName() + "," + ldapAttrMapping.userURL;
      Attributes attrs = ldapAttrMapping.userToAttributes(user);
      attrs.put(ldapAttrMapping.userAccountControlAttr,
         Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWD_NOTREQD + UF_PASSWORD_EXPIRED + UF_ACCOUNTDISABLE));
      attrs.remove(ldapAttrMapping.userPassword);
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               if (broadcast)
                  preSave(user, true);
               // see comments about saving password below
               ctx.createSubcontext(userDN, attrs).close();
               if (broadcast)
                  postSave(user, true);

               cacheHandler.put(user.getUserName(), user, CacheType.USER);
               break;
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
      // Really need do it separately ?
      // Do it in method with new LdapContext to avoid NameAlreadyBoundException,
      // if got connection error occurs when try to save password.
      saveUserPassword(user, userDN);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void saveUserPassword(User user, String userDN) throws Exception
   {
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         Object v = ctx.getEnvironment().get(Context.SECURITY_PROTOCOL);
         if (v == null)
            return;
         String security = String.valueOf(v);
         if (!security.equalsIgnoreCase("ssl"))
            return;
         String newQuotedPassword = "\"" + user.getPassword() + "\"";
         byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
         ModificationItem[] mods = new ModificationItem[2];
         mods[0] =
            new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(ldapAttrMapping.userPassword,
               newUnicodePassword));
         mods[1] =
            new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(ldapAttrMapping.userAccountControlAttr,
               Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWORD_EXPIRED)));
         for (int err = 0;; err++)
         {
            try
            {
               ctx.modifyAttributes(userDN, mods);
               break;
            }
            catch (NamingException e)
            {
               ctx = reloadCtx(ctx, err, e);
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected ModificationItem[] createSetEnabledModification(int userAccountControl, boolean enabled)
   {
      int value;
      if (enabled)
      {
         value = (userAccountControl | UF_NORMAL_ACCOUNT) & ~UF_ACCOUNTDISABLE;
      }
      else
      {
         value = userAccountControl | UF_PASSWD_NOTREQD | UF_ACCOUNTDISABLE;
      }
      ModificationItem[] mods = new ModificationItem[1];
      mods[0] =
         new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(ldapAttrMapping.userAccountControlAttr,
            Integer.toString(value)));
      return mods;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void setDefaultUserAccountControlAttr(LDAPAttributeMapping ldapAttrMapping)
   {
      ldapAttrMapping.userAccountControlAttr = "userAccountControl";
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void setDefaultUserAccountControlFilter(LDAPAttributeMapping ldapAttrMapping)
   {
      ldapAttrMapping.userAccountControlFilter =
         "!(" + ldapAttrMapping.userAccountControlAttr + ":1.2.840.113556.1.4.803:=" + UF_ACCOUNTDISABLE + ")";
   }
}
