/**
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2022 Meeds Association
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
package org.exoplatform.services.organization.auth;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.security.Credential;

/**
 * A component Plugin that could be injected in
 * #OrganizationAuthenticatorImpl to extend the way to authenticate users
 */
public abstract class AuthenticatorPlugin extends BaseComponentPlugin {

  /**
   * Authenticate user and return userId which can be different to username.
   * 
   * @param credentials - list of users credentials (such as name/password, X509
   *          certificate etc)
   * @return userId the user's identifier.
   */
  public abstract String validateUser(Credential[] credentials);

}
