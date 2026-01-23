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
package org.exoplatform.services.organization.cache;

import org.exoplatform.services.organization.Membership;

import java.io.Serializable;
import java.util.Objects;

/**
 * Will be used as key for cacheMembership.
 */
public class MembershipCacheKey implements Serializable {
  /**
   * serialVersionUID.
   */
  private static final long serialVersionUID = -188435911917156440L;

  private final String      userName;

  private final String      groupId;

  private final String      type;

  private final int         hashcode;

  private final boolean     includesInherited;

  public MembershipCacheKey(String userName, String groupId, String type) {
    this.userName = userName;
    this.groupId = groupId;
    this.type = type;
    this.includesInherited = false;
    hashcode = Objects.hash(this.userName, this.groupId, this.type, this.includesInherited);
  }

  public MembershipCacheKey(String userName, String groupId, String type, boolean includesInherited) {
    this.userName = userName;
    this.groupId = groupId;
    this.type = type;
    this.includesInherited = includesInherited;
    hashcode = Objects.hash(this.userName, this.groupId, this.type, this.includesInherited);

  }

  public MembershipCacheKey(Membership m) {
    this.userName = m.getUserName();
    this.groupId = m.getGroupId();
    this.type = m.getMembershipType();
    this.includesInherited = false;
    hashcode = Objects.hash(this.userName, this.groupId, this.type, this.includesInherited);
  }

  public String getGroupId() {
    return groupId;
  }

  public String getType() {
    return type;
  }

  public String getUserName() {
    return userName;
  }

  public boolean isIncludesInherited() {
    return includesInherited;
  }

  @Override
  public int hashCode() {
    return hashcode;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MembershipCacheKey other = (MembershipCacheKey) obj;
    if (groupId == null) {
      if (other.groupId != null)
        return false;
    } else if (!groupId.equals(other.groupId))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    if (userName == null) {
      if (other.userName != null)
        return false;
    } else if (!userName.equals(other.userName))
    if (includesInherited != other.isIncludesInherited()) {
        return false;
    }
    return true;
  }

}
