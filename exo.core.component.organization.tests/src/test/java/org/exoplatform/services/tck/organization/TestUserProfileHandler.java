/**
 * 
 */
/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.tck.organization;

import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.services.organization.UserProfileEventListenerHandler;

import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestUserProfileHandlerImpl.java 111 2008-11-11 11:11:11Z $
 */
public class TestUserProfileHandler extends AbstractOrganizationServiceTest
{
   /**
    * Find user profile by name.
    */
   public void testFindUserProfileByName() throws Exception
   {
      createUser(userName);
      createUserProfile(userName);

      UserProfile up = upHandler.findUserProfileByName(userName);
      assertNotNull(up);
      assertEquals(userName, up.getUserName());
      assertEquals("value1", up.getAttribute("key1"));
      assertEquals("value2", up.getAttribute("key2"));

      // try to find profile for not existed user. We are supposed to get "null" instead of Exception
      try
      {
         assertNull(upHandler.findUserProfileByName(newUserName));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }

      // try to find not existed profile. We are supposed to get "null" instead of Exception
      createUser(newUserName);
      try
      {
         assertNull(upHandler.findUserProfileByName(newUserName));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }
   }

   /**
    * Find user profile by name with Empty Cache
    */
   public void testFindUserProfileByNameEmptyCache() throws Exception
   {
      createUser(cachedUserName);
      createUserProfile(cachedUserName);
      cacheService.getCacheInstance(upHandler.getClass().getName()).clearCache();
      try
      {
         assertNotNull(upHandler.findUserProfileByName(cachedUserName));
      }
      catch (Exception e)
      {
         e.printStackTrace();
         fail("Exception should not be thrown");
      }
   }

   /**
    * Find user profiles.
    */
   public void testFindUserProfiles() throws Exception
   {
      createUser(userName);
      createUserProfile(userName);

      createUser(newUserName);
      createUserProfile(newUserName);

      assertEquals(upHandler.findUserProfiles().size(), 2);
   }

   /**
    * Remove user profile.
    */
   public void testRemoveUserProfile() throws Exception
   {
      createUser(userName);
      createUserProfile(userName);
      
      UserProfile up = upHandler.removeUserProfile(userName, true);
      assertNotNull(up);
      assertEquals(up.getAttribute("key1"), "value1");
      assertEquals(up.getAttribute("key2"), "value2");
      assertNull(upHandler.findUserProfileByName("userP1"));

      // remove not existed profile. We are supposed to get "null" instead of Exception
      try
      {
         assertNull(upHandler.removeUserProfile(newUserName, true));
      }
      catch (Exception e)
      {
         fail("Exception should not be thrown");
      }
   }

   /**
    * Save user profile.
    */
   public void testSaveUserProfile() throws Exception
   {
      createUser(userName);
      createUserProfile(userName);

      UserProfile up = upHandler.findUserProfileByName(userName);
      up.setAttribute("key1", "value11");
      up.setAttribute("key2", null);
      upHandler.saveUserProfile(up, true);

      up = upHandler.findUserProfileByName(userName);
      assertEquals(up.getAttribute("key1"), "value11");
      assertNull(up.getAttribute("key2"));

      // try to save user profile for not existed user
      try
      {
         up = upHandler.createUserProfileInstance(newUserName);
         upHandler.saveUserProfile(up, true);
         fail("Exception should be thrown");
      }
      catch (Exception e)
      {
      }
   }

   /**
    * Test get listeners.
    */
   public void testGetListeners() throws Exception
   {
      if (upHandler instanceof UserProfileEventListenerHandler)
      {
         List<UserProfileEventListener> list = ((UserProfileEventListenerHandler) upHandler).getUserProfileListeners();
         try
         {
            list.clear();
            fail("We should not able to modife list of listeners");
         }
         catch (Exception e)
         {
         }
      }
   }
}
