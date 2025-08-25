/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.services.security.web;

import org.exoplatform.services.security.StateKey;

import jakarta.servlet.http.HttpSession;

public final class HttpSessionStateKey implements StateKey {

  private static final long serialVersionUID = 3009905438380354994L;

  /**
   * HTTP session ID.
   */
  private final String      sessionId;

  public HttpSessionStateKey(HttpSession httpSession) {
    this.sessionId = httpSession.getId();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    } else if (getClass() != obj.getClass()) {
      return false;
    } else {
      return sessionId.equals(((HttpSessionStateKey) obj).sessionId);
    }
  }

  @Override
  public int hashCode() {
    return sessionId.hashCode();
  }

  @Override
  public String toString() {
    return String.format("HTTP Session %s", sessionId);
  }
}
