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
package org.exoplatform.services.organization.impl.mock;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.Membership;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author <a href="abazko@exoplatform.com">Anatoliy Bazko</a>
 * @version $Id: SimpleJDBCMembershipListAccess.java 34360 2009-07-22 23:58:59Z tolusha $
 */
public class SimpleMembershipListAccess implements ListAccess<Membership>
{

   private final Collection<Membership> memberships;
   
   public SimpleMembershipListAccess(Collection<Membership> memberships)
   {
      this.memberships = memberships;
   }

   /**
    * {@inheritDoc}
    */
   public Membership[] load(int index, int length) throws Exception, IllegalArgumentException
   {
      if (index < 0)
      {
         throw new IllegalArgumentException("Illegal index: can't be a negative number");
      }

      if (length < 0)
      {
         throw new IllegalArgumentException("Illegal length: can't be a negative number");
      }
      
      if (index + length > memberships.size())
      {
         throw new IllegalArgumentException("The sum of the index and the length cannot be greater than the list size");
      }

      Membership[] results = new Membership[length];
      Iterator<Membership> iter = memberships.iterator();
      
      for (int p = 0, counter = 0; counter < length; p++)
      {
         Membership membership = iter.next();

         if (p >= index)
         {
            results[counter++] = membership;
         }
      }

      return results;
   }

   /**
    * {@inheritDoc}
    */
   public int getSize() throws Exception
   {
      return memberships.size();
   }

}
