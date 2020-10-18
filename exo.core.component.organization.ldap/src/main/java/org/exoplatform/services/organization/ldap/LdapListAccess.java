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

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.ldap.LDAPService;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Deprecated
public abstract class LdapListAccess<E> implements ListAccess<E>
{

   /**
    * LDAP service.
    */
   protected LDAPService ldapService;

   /**
    * @param ldapService LDAP service
    */
   public LdapListAccess(LDAPService ldapService)
   {
      this.ldapService = ldapService;
   }

   /**
    * {@inheritDoc}
    */
   public int getSize() throws Exception
   {
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               return getSize(ctx);
            }
            catch (NamingException e)
            {
               if (BaseDAO.isConnectionError(e) && err < BaseDAO.getMaxConnectionError())
               {
                  ldapService.release(ctx);
                  ctx = ldapService.getLdapContext(true);
               }
               else
               {
                  return 0;
               }
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
   public E[] load(int index, int length) throws Exception, IllegalArgumentException
   {
      LdapContext ctx = ldapService.getLdapContext();
      try
      {
         for (int err = 0;; err++)
         {
            try
            {
               return load(ctx, index, length);
            }
            catch (NamingException e)
            {
               if (BaseDAO.isConnectionError(e) && err < BaseDAO.getMaxConnectionError())
               {
                  ldapService.release(ctx);
                  ctx = ldapService.getLdapContext(true);
               }
               else
                  throw e;
            }
         }
      }
      finally
      {
         ldapService.release(ctx);
      }
   }

   /**
    * @param ctx LDAP context
    * @param index offset 
    * @param length number of users
    * @return result array of users 
    * @throws Exception if any error occurs
    */
   protected abstract E[] load(LdapContext ctx, int index, int length) throws Exception;

   /**
    * @param ctx LDAP context
    * @return list size
    * @throws Exception if any error occurs
    */
   protected abstract int getSize(LdapContext ctx) throws Exception;

}
