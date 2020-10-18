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

import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.security.auth.Subject;

/**
 * Test used to check whether SecurityManager related features are working properly.
 * 
 * @author <a href="mailto:nikolazius@gmail.com">Nikolay Zamosenchuk</a>
 * @version $Id: TestPermissions.java 34360 2009-07-22 23:58:59Z nzamosenchuk $
 *
 */
public class TestIdentityPermissions extends BaseSecurityTest
{

   public void testSecurityManagerExists()
   {
      assertNotNull(System.getSecurityManager());
   }

   /**
    * Checks that modification is permitted if MODIFY_IDENTITY_PERMISSION given
    */
   public void testModifyRolesWithPermissions()
   {
      try
      {
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               getIdentity().getRoles().clear();
               return null;
            }
         }, PermissionConstants.MODIFY_IDENTITY_PERMISSION);
      }
      catch (Exception e)
      {
         fail("Modification should be successfull, since it is launched with required permissions.");
      }
   }

   /**
    * Checks that setRoles is permitted if MODIFY_IDENTITY_PERMISSION given
    */
   public void testSetRolesWithPermissions()
   {
      try
      {
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               getIdentity().setRoles(new HashSet<String>());
               return null;
            }
         }, PermissionConstants.MODIFY_IDENTITY_PERMISSION);
      }
      catch (Exception e)
      {
         fail("Modification should be successfull, since it is launched with required permissions.");
      }
   }

   /**
    * Checks that modification is denied if no permission given
    */
   public void testModifyRolesWithNoPermissions()
   {
      try
      {
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               getIdentity().getRoles().clear();
               return null;
            }
         });
         fail("Modification should be denied");
      }
      catch (Exception e)
      {
         // it's ok
      }
   }

   /**
    * Checks that setRoles is denied if no permission given
    */
   public void testSetWithRolesNoPermissions()
   {
      try
      {
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               getIdentity().setRoles(new HashSet<String>());
               return null;
            }
         });
         fail("Modification should be denied");
      }
      catch (Exception e)
      {
         // it's ok
      }
   }

   /**
    * Checks that modification is permitted if MODIFY_IDENTITY_PERMISSION given
    */
   public void testModifyMembershipsWithPermissions()
   {
      try
      {
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               getIdentity().getMemberships().clear();
               return null;
            }
         }, PermissionConstants.MODIFY_IDENTITY_PERMISSION);
      }
      catch (Exception e)
      {
         fail("Modification should be successfull, since it is launched with required permissions.");
      }
   }

   /**
    * Checks that setMemberships is permitted if MODIFY_IDENTITY_PERMISSION given
    */
   public void testSetMembershipsWithPermissions()
   {
      try
      {
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            @SuppressWarnings("deprecation")
            public Object run() throws Exception
            {
               getIdentity().setMemberships(new HashSet<MembershipEntry>());
               return null;
            }
         }, PermissionConstants.MODIFY_IDENTITY_PERMISSION);
      }
      catch (Exception e)
      {
         fail("Modification should be successfull, since it is launched with required permissions.");
      }
   }

   /**
    * Checks that modification is denied if no permission given
    */
   public void testModifyMembershipsWithNoPermissions()
   {
      try
      {
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               getIdentity().getMemberships().clear();
               return null;
            }
         });
         fail("Modification should be denied");
      }
      catch (Exception e)
      {
         // it's ok
      }
   }

   /**
    * Checks that setMemberships is denied if no permission given
    */
   public void testSetWithMembershipsNoPermissions()
   {
      try
      {
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            @SuppressWarnings("deprecation")
            public Object run() throws Exception
            {
               getIdentity().setMemberships(new HashSet<MembershipEntry>());
               return null;
            }
         });
         fail("Modification should be denied");
      }
      catch (Exception e)
      {
         // it's ok
      }
   }

   /**
    * Checks setSubject is permitted with "setSubject" permission
    */
   public void testSubjectWithSetSubjectPermissions()
   {
      try
      {
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               getIdentity().setSubject(new Subject());
               return null;
            }
         }, PermissionConstants.MODIFY_IDENTITY_PERMISSION);
      }
      catch (Exception e)
      {
         fail("Modification should be successfull, since it is launched with required permissions.");
      }
   }

   /**
    * Checks setSubject is denied without "setSubject" permission
    */
   public void testSubjectWithNoPermissions()
   {
      try
      {
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               getIdentity().setSubject(new Subject());
               return null;
            }
         });
         fail("Modification should be denied");
      }
      catch (Exception e)
      {
         // ok
      }
   }

   /**
    * Creates dummy Identity for testing purposes
    * 
    * @return
    */
   private Identity getIdentity()
   {
      Collection<MembershipEntry> memberships = null;

      memberships = new ArrayList<MembershipEntry>();
      memberships.add(new MembershipEntry("/group1", "*"));
      memberships.add(new MembershipEntry("/group2", "member"));

      final Identity identity = new Identity("user", memberships);
      return identity;
   }

}
