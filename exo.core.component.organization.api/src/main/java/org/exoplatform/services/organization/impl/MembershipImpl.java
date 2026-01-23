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
package org.exoplatform.services.organization.impl;

import java.util.Objects;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.exoplatform.services.organization.ExtendedCloneable;
import org.exoplatform.services.organization.Membership;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

public class MembershipImpl implements Membership, ExtendedCloneable {
  private static final long serialVersionUID = 3393494689182081442L;

  private String id = null;

  private String membershipType = "member";

  private String userName = null;

  private String groupId = null;

  private boolean isInherited;

  public MembershipImpl() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMembershipType() {
    return membershipType;
  }

  public void setMembershipType(String type) {
    this.membershipType = type;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String user) {
    this.userName = user;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String group) {
    this.groupId = group;
  }

  public boolean isInherited() {
    return isInherited;
  }

  public void setInherited(boolean isInherited) {
    this.isInherited = isInherited;
  }

  // toString
  public String toString() {
    return "Membership[" + id + "]";
  }

  /**
   * {@inheritDoc}
   **/
  public MembershipImpl clone() {
    try {
      return (MembershipImpl) super.clone();
    } catch (CloneNotSupportedException e) {
      return this;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MembershipImpl that = (MembershipImpl) o;
    return Objects.equals(id, that.id) &&
            Objects.equals(membershipType, that.membershipType) &&
            Objects.equals(userName, that.userName) &&
            Objects.equals(groupId, that.groupId) &&
            Objects.equals(isInherited, that.isInherited);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, membershipType, userName, groupId);
  }
}
