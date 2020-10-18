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
package org.exoplatform.services.security;

import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.Permissions;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:nikolazius@gmail.com">Nikolay Zamosenchuk</a>
 * @version $Id: BaseSecurityTest.java 34360 2009-07-22 23:58:59Z nzamosenchuk $
 *
 */
public abstract class BaseSecurityTest extends TestCase
{
   /**
    * 
    */
   public BaseSecurityTest()
   {
      super();
   }

   /**
    * @param name
    */
   public BaseSecurityTest(String name)
   {
      super(name);
   }

   /**
    * Run privileged action with given privileges.
    */
   public <T> T doActionWithPermissions(PrivilegedExceptionAction<T> action, Permission... permissions) throws Exception
   {
      Permissions allPermissions = new Permissions();
      for (Permission permission : permissions)
      {
         if (permission != null)
         {
            allPermissions.add(permission);
         }
      }
      ProtectionDomain[] protectionDomains =
         new ProtectionDomain[]{new ProtectionDomain(new CodeSource(getCodeSource(),
            (java.security.cert.Certificate[])null), allPermissions)};
      return AccessController.doPrivileged(action, new AccessControlContext(protectionDomains));
   }

   protected URL getCodeSource()
   {
      return getClass().getProtectionDomain().getCodeSource().getLocation();
   }

}