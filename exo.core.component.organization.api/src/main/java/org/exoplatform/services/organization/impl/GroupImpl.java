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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.exoplatform.services.organization.ExtendedCloneable;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.NestedMembership;
import org.exoplatform.services.organization.OrganizationService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class GroupImpl implements Group, ExtendedCloneable {

  private static final long     serialVersionUID = -5909516396351606340L;

  private String                id;

  private String                parentId;

  private String                groupName;

  private String                label;

  private String                desc;

  private String                originatingStore;

  private Set<NestedMembership> enclosingMemberships;                    // NOSONAR

  public GroupImpl(String name) {
    groupName = name;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getParentId() {
    return parentId;
  }

  @Override
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  @Override
  public String getGroupName() {
    return groupName;
  }

  @Override
  public void setGroupName(String name) {
    this.groupName = name;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public void setLabel(String s) {
    label = s;
  }

  @Override
  public String getDescription() {
    return desc;
  }

  @Override
  public void setDescription(String s) {
    desc = s;
  }

  @Override
  public Set<NestedMembership> getEnclosingMemberships() {
    return this.enclosingMemberships;
  }

  @Override
  public void setEnclosingMemberships(Set<NestedMembership> inheritedMemberships) {
    this.enclosingMemberships = inheritedMemberships;
  }

  @Override
  public String toString() {
    return "Group[" + id + "|" + groupName + "]";
  }

  /**
   * Set originating store name (internal or external)
   *
   * @param originatingStore
   */
  @Override
  public void setOriginatingStore(String originatingStore) {
    this.originatingStore = originatingStore;
  }

  /**
   * @return originating store name (internal or external)
   */
  @Override
  public String getOriginatingStore() {
    return originatingStore;
  }

  /**
   * @return true if the group was initially added to internal store
   */
  @Override
  public boolean isInternalStore() {
    return originatingStore == null || OrganizationService.INTERNAL_STORE.equals(originatingStore);
  }

  /**
   * {@inheritDoc}
   **/
  public GroupImpl clone() { // NOSONAR
    return new GroupImpl(id,
                         parentId,
                         groupName,
                         label,
                         desc,
                         originatingStore,
                         enclosingMemberships == null ? null : new HashSet<>(enclosingMemberships));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    GroupImpl group = (GroupImpl) o;
    return Objects.equals(id, group.id)
           && Objects.equals(parentId, group.parentId)
           && Objects.equals(groupName, group.groupName)
           && Objects.equals(label, group.label)
           && Objects.equals(desc, group.desc)
           && Objects.equals(enclosingMemberships, group.enclosingMemberships);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, parentId, groupName, label, desc, enclosingMemberships);
  }
}
