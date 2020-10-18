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
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeEventListener;
import org.exoplatform.services.organization.MembershipTypeEventListenerHandler;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.MembershipTypeImpl;
import org.exoplatform.services.security.PermissionConstants;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.naming.InvalidNameException;

/**
 * Created by The eXo Platform SAS
 * Author : Mestrallet Benjamin benjmestrallet@users.sourceforge.net
 * Author : Tuan Nguyen tuan08@users.sourceforge.net
 * Date: Aug 22, 2003 Time: 4:51:21 PM
 */
@Deprecated
public class MembershipTypeDAOImpl implements MembershipTypeHandler, MembershipTypeEventListenerHandler
{
   private static final String queryFindMembershipType =
      "from m in class org.exoplatform.services.organization.impl.MembershipTypeImpl " + "where m.name = :id ";

   private static final String queryFindAllMembershipType =
      "from m in class org.exoplatform.services.organization.impl.MembershipTypeImpl";

   /**
    * The list of listeners to broadcast the events.
    */
   protected final List<MembershipTypeEventListener> listeners = new ArrayList<MembershipTypeEventListener>();

   private final HibernateService service_;

   protected final OrganizationService orgService;

   public MembershipTypeDAOImpl(HibernateService service, OrganizationService orgService)
   {
      this.service_ = service;
      this.orgService = orgService;
   }

   final public MembershipType createMembershipTypeInstance()
   {
      return new MembershipTypeImpl();
   }

   public MembershipType createMembershipType(MembershipType mt, boolean broadcast) throws Exception
   {
      Session session = service_.openSession();
      Date now = new Date();
      mt.setCreatedDate(now);
      mt.setModifiedDate(now);

      if (broadcast)
      {
         preSave(mt, true);
      }

      session.save(mt);
      session.flush();

      if (broadcast)
      {
         postSave(mt, true);
      }

      return mt;
   }

   public MembershipType saveMembershipType(MembershipType mt, boolean broadcast) throws Exception
   {
      Session session = service_.openSession();
      Date now = new Date();
      mt.setModifiedDate(now);

      if (broadcast)
      {
         preSave(mt, false);
      }

      session.update(mt);
      session.flush();

      if (broadcast)
      {
         postSave(mt, false);
      }

      return mt;
   }

   public MembershipType findMembershipType(String name) throws Exception
   {
      Session session = service_.openSession();
      MembershipType m = (MembershipType)service_.findOne(session, queryFindMembershipType, name);
      return m;
   }

   public MembershipType removeMembershipType(String name, boolean broadcast) throws Exception
   {
      Session session = service_.openSession();
      MembershipTypeImpl mt = (MembershipTypeImpl)session.get(MembershipTypeImpl.class, name);

      if (mt == null)
      {
         throw new InvalidNameException("Can not remove membership type" + name
            + "record, because membership type does not exist.");
      }

      if (broadcast)
      {
         preDelete(mt);
      }

      session.delete(mt);
      MembershipDAOImpl membershipHanler = (MembershipDAOImpl)orgService.getMembershipHandler();
      membershipHanler.removeMembershipEntriesOfMembershipType(mt, session);
      session.flush();

      if (broadcast)
      {
         postDelete(mt);
      }

      return mt;
   }

   @SuppressWarnings("unchecked")
   public Collection<MembershipType> findMembershipTypes() throws Exception
   {
      Session session = service_.openSession();
      Collection<MembershipType> result = session.createQuery(queryFindAllMembershipType).list();
      List<MembershipType> l = new ArrayList<MembershipType>(result);
      Collections.sort(l, MembershipTypeHandler.COMPARATOR);
      return l;
   }

   /**
    * {@inheritDoc}
    */
   public void addMembershipTypeEventListener(MembershipTypeEventListener listener)
   {
      SecurityHelper.validateSecurityPermission(PermissionConstants.MANAGE_LISTENERS);
      listeners.add(listener);
   }

   /**
    * {@inheritDoc}
    */
   public void removeMembershipTypeEventListener(MembershipTypeEventListener listener)
   {
      SecurityHelper.validateSecurityPermission(PermissionConstants.MANAGE_LISTENERS);
      listeners.remove(listener);
   }

   /**
    * PreSave event.
    */
   private void preSave(MembershipType type, boolean isNew) throws Exception
   {
      for (MembershipTypeEventListener listener : listeners)
      {
         listener.preSave(type, isNew);
      }
   }

   /**
    * PostSave event.
    */
   private void postSave(MembershipType type, boolean isNew) throws Exception
   {
      for (MembershipTypeEventListener listener : listeners)
      {
         listener.postSave(type, isNew);
      }
   }

   /**
    * PreDelete event.
    */
   private void preDelete(MembershipType type) throws Exception
   {
      for (MembershipTypeEventListener listener : listeners)
      {
         listener.preDelete(type);
      }
   }

   /**
    * PostDelete event.
    */
   private void postDelete(MembershipType type) throws Exception
   {
      for (MembershipTypeEventListener listener : listeners)
      {
         listener.postDelete(type);
      }
   }

   /**
    * {@inheritDoc}
    */
   public List<MembershipTypeEventListener> getMembershipTypeListeners()
   {
      return Collections.unmodifiableList(listeners);
   }
}
