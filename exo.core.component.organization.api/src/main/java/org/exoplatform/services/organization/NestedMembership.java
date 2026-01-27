/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.services.organization;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NestedMembership {

  public static final String INHERIT_MEMBERSHIP_TYPE = "~";

  public static final String ALL_MEMBERSHIP_TYPE     = "*";

  private String             membershipType;

  private String             groupId;

  private String             nestedMembershipType;

  private String             nestedGroupId;

  public boolean isInheritMembershipType() {
    return StringUtils.isBlank(membershipType) || INHERIT_MEMBERSHIP_TYPE.equals(membershipType);
  }

  public boolean isIncludeAllMembershipTypes() {
    return StringUtils.isBlank(nestedMembershipType) || ALL_MEMBERSHIP_TYPE.equals(nestedMembershipType);
  }

  public String toNestedMembership() {
    return "%s:%s:%s".formatted(StringUtils.defaultIfBlank(membershipType, INHERIT_MEMBERSHIP_TYPE),
                                StringUtils.defaultIfBlank(nestedMembershipType, ALL_MEMBERSHIP_TYPE),
                                nestedGroupId);
  }

  public String toEnclosingMembership() {
    return "%s:%s:%s".formatted(StringUtils.defaultIfBlank(nestedMembershipType, ALL_MEMBERSHIP_TYPE),
                                StringUtils.defaultIfBlank(membershipType, INHERIT_MEMBERSHIP_TYPE),
                                groupId);
  }

  /**
   * @param expression parent Membership (with pattern ROLE:NESTED_MEMBERSHIP).
   *          - ROLE: is the parent Membership Type. This role will be applied
   *          on selected users coming from nested memberships. This value can
   *          be '~' to indicate that the same role is applied from nested
   *          group. By exemple, when the user has a membership
   *          'publisher:/group1' which is nested inside the parent group
   *          (/group2) as '~:publisher:/group1', the the user will inherit the
   *          same role in parent group and thus will be 'publisher:/group2'.
   *          Another example, when the parentMembership expression is
   *          'manager:publisher:/group1', then the users having the membership
   *          'publisher:/group1' will inherit the Membership 'manager:/group2'.
   *          - NESTED_MEMBERSHIP: nested Membership (with pattern
   *          ROLE:NESTED_GROUP_ID).
   * @param groupId Parent Group Id
   * @return corresponding {@link NestedMembership}
   */
  public static NestedMembership parseNestedMembership(String expression, String groupId) {
    String[] parts = expression.split(":");
    return NestedMembership.builder()
                           .groupId(groupId)
                           .membershipType(parts[0])
                           .nestedMembershipType(parts[1])
                           .nestedGroupId(parts[2])
                           .build();
  }

  /**
   * @param expression nested Membership (with pattern ROLE:PARENT_MEMBERSHIP).
   *          - ROLE: is the nested Membership Type. This role will allow to
   *          select a subset of a Group Members who will be inheriting parent
   *          group memberships. By example, when the expression is
   *          'publisher:/manager:/group2' and the nestedGroupId is '/group1'.
   *          This means that the users having the membership
   *          'publisher:/group1' will inherit automatically the membership
   *          'manager:/group2'. - PARENT_MEMBERSHIP: parent Membership (with
   *          pattern ROLE:PARENT_GROUP_ID).
   * @param nestedGroupId Parent Group Id
   * @return corresponding {@link NestedMembership}
   */
  public static NestedMembership parseEnclosingMembership(String expression, String nestedGroupId) {
    String[] parts = expression.split(":");
    return NestedMembership.builder()
                           .nestedGroupId(nestedGroupId)
                           .nestedMembershipType(parts[0])
                           .membershipType(parts[1])
                           .groupId(parts[2])
                           .build();
  }

}
