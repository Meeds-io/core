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
package org.exoplatform.services.organization;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.picocontainer.Startable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Oct 13, 2005
 */
abstract public class BaseOrganizationService implements OrganizationService, Startable, ComponentRequestLifecycle
{
   protected UserHandler userDAO_;

   protected UserProfileHandler userProfileDAO_;

   protected GroupHandler groupDAO_;

   protected MembershipHandler membershipDAO_;

   protected MembershipTypeHandler membershipTypeDAO_;

   protected List<OrganizationServiceInitializer> listeners_ = new ArrayList<OrganizationServiceInitializer>(3);

   public UserHandler getUserHandler()
   {
      return userDAO_;
   }

   public UserProfileHandler getUserProfileHandler()
   {
      return userProfileDAO_;
   }

   public GroupHandler getGroupHandler()
   {
      return groupDAO_;
   }

   public MembershipTypeHandler getMembershipTypeHandler()
   {
      return membershipTypeDAO_;
   }

   public MembershipHandler getMembershipHandler()
   {
      return membershipDAO_;
   }

   public void start()
   {
      try
      {
         RequestLifeCycle.begin(this);

         for (OrganizationServiceInitializer listener : listeners_)
         {
            try
            {
               listener.init(this);
            }
            catch (Exception ex)
            {
               String msg =
                  "Failed start Organization Service " + getClass().getName()
                     + ", probably because of configuration error. Error occurs when initialize "
                     + listener.getClass().getName();
               throw new RuntimeException(msg, ex);
            }
         }
      }
      finally
      {
         RequestLifeCycle.end();
      }
   }

   public void stop()
   {
   }

   synchronized public void addListenerPlugin(ComponentPlugin listener) throws Exception
   {
      if (listener instanceof UserEventListener)
      {
         userDAO_.addUserEventListener((UserEventListener)listener);
      }
      else if (listener instanceof GroupEventListener)
      {
         groupDAO_.addGroupEventListener((GroupEventListener)listener);
      }
      else if (listener instanceof MembershipTypeEventListener)
      {
         membershipTypeDAO_.addMembershipTypeEventListener((MembershipTypeEventListener)listener);
      }
      else if (listener instanceof MembershipEventListener)
      {
         membershipDAO_.addMembershipEventListener((MembershipEventListener)listener);
      }
      else if (listener instanceof UserProfileEventListener)
      {
         userProfileDAO_.addUserProfileEventListener((UserProfileEventListener)listener);
      }
      else if (listener instanceof OrganizationServiceInitializer)
      {
         listeners_.add((OrganizationServiceInitializer)listener);
      }
      else
      {
         throw new RuntimeException(listener.getClass().getName() + " is an unknown listener type");
      }
   }

   /**
    * {@inheritDoc}
    */
   public void startRequest(ExoContainer container)
   {
   }

   /**
    * {@inheritDoc}
    */
   public void endRequest(ExoContainer container)
   {
   }
}
