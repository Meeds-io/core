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

import java.util.HashSet;
import java.util.Set;

import javax.security.auth.login.LoginException;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id:$
 */

public class DummyAuthenticatorImpl implements Authenticator
{

   private String[] acceptableUIDs = {"exo"};

   private RolesExtractor rolesExtractor;

   public DummyAuthenticatorImpl(RolesExtractor rolesExtractor)
   {
      this.rolesExtractor = rolesExtractor;

   }

   /*
    * (non-Javadoc)
    * @see
    * org.exoplatform.services.security.Authenticator#createIdentity(java.lang
    * .String)
    */
   public Identity createIdentity(String userId)
   {
      Set<MembershipEntry> entries = new HashSet<MembershipEntry>();
      entries.add(new MembershipEntry(userId));
      return new Identity(userId, entries, rolesExtractor.extractRoles(userId, entries));
   }

   /*
    * (non-Javadoc)
    * @see
    * org.exoplatform.services.security.Authenticator#validateUser(org.exoplatform
    * .services.security.Credential[])
    */
   public String validateUser(Credential[] credentials) throws LoginException, Exception
   {
      String myID = ((UsernameCredential)credentials[0]).getUsername();
      for (String id : this.acceptableUIDs)
      {
         if (id.equals(myID))
            return id;
      }
      throw new LoginException();
   }

   /**
    * @see org.exoplatform.services.security.Authenticator#getLastExceptionOnValidateUser()
    */
   public Exception getLastExceptionOnValidateUser()
   {
      return null;
   }

}
