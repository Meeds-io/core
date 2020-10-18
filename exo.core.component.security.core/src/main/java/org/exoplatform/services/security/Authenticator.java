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

import javax.security.auth.login.LoginException;

/**
 * Created by The eXo Platform SAS<br>
 * Component responsible for user authentication (session creation)
 * In JAAS LoginModule typically called in login() method
 * 
 * @author Gennady Azarenkov
 * @LevelAPI Platform
 */

public interface Authenticator
{
   /**
    * Authenticate user and return userId which can be different to username.
    * 
    * @param credentials - list of users credentials (such as name/password, X509
    *          certificate etc)
    * @return userId the user's identifier.
    * @throws LoginException in case the authentication fails
    * @throws Exception if any exception occurs
    */
   String validateUser(Credential[] credentials) throws LoginException, Exception;

   /**
    * @param userId the user's identifier
    * @return returns the Identity representing the user
    * @throws Exception if any exception occurs
    */
   Identity createIdentity(String userId) throws Exception;

   /**
    * Gives the last exception that occurs while calling {@link #validateUser(Credential[])}. This
    * allows applications outside JAAS like UI to be able to know which exception occurs
    * while calling {@link #validateUser(Credential[])}.
    * @return the original Exception that occurs while calling {@link #validateUser(Credential[])} 
    * for the very last time if an exception occurred, <code>null</code> otherwise.
    * <p>
    * <b>WARNING: to prevent potential memory leaks, the second call to this method may return <code>null</code>
    * so if you need to access several times to this value, you should store the result into a local variable.</b>
    * </p>
    */
   Exception getLastExceptionOnValidateUser();
}
