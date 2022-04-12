/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2022 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.exoplatform.services.organization;

import java.time.Instant;

/**
 * This exception is raised when trying to interact with a temporarily locked
 * account.
 */
public class AccountTemporaryLockedException extends Exception
{

   /**
    * The serial version id
    */
   private static final long serialVersionUID = 1751536769113302305L;

   /**
    * The user name of the locked account
    */
   private final String username;

   /**
    * Moment when the account will be unlocked
    */
   private final Instant unlockTime;

   /**
    * Constructs the exception with the user name and the account unlock time
    * 
    * @param username the user name of the disabled account
    * @param unlockTime the moment when the account will be unlocked
    */
   public AccountTemporaryLockedException(String username, Instant unlockTime)
   {
      this.username = username;
      this.unlockTime = unlockTime;
   }

   /**
    * @return the user name of the locked account account
    */
   public String getUsername()
   {
      return username;
   }

   /**
    * @return the instant when the account will be unlocked
    */
   public Instant getUnlockTime()
   {
      return unlockTime;
   }

}
