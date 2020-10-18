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
package org.exoplatform.services.organization.ldap;

import org.exoplatform.services.ldap.ObjectClassAttribute;
import org.exoplatform.services.organization.DisabledUserException;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.services.organization.impl.MembershipTypeImpl;
import org.exoplatform.services.organization.impl.UserProfileData;

import java.util.Calendar;
import java.util.Date;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Oct 13, 2005
 */
@Deprecated
public class LDAPAttributeMapping
{

   public String userLDAPClasses;

   public String profileLDAPClasses;

   public String groupLDAPClasses;

   public String membershipTypeLDAPClasses;

   public String membershipLDAPClasses;

   static String[] USER_LDAP_CLASSES;

   static String[] PROFILE_LDAP_CLASSES;

   static String[] GROUP_LDAP_CLASSES;

   static String[] MEMBERSHIPTYPE_LDAP_CLASSES;

   static String[] MEMBERSHIP_LDAP_CLASSES;

   public String baseURL, groupsURL, membershipTypeURL, userURL, profileURL;

   // for AD.
   String userDNKey = "cn";

   // configuration.
   String groupDNKey = "ou";

   String userUsernameAttr;

   String userPassword;

   String userFirstNameAttr;

   String userLastNameAttr;

   String userDisplayNameAttr;

   String userMailAttr;

   String userAccountControlAttr;

   String userObjectClassFilter;

   String userAccountControlFilter;

   String membershipTypeMemberValue;

   String membershipTypeRoleNameAttr;

   String membershipTypeNameAttr;

   String membershipTypeObjectClassFilter;

   String membershiptypeObjectClass;

   String groupObjectClass, groupObjectClassFilter;

   String membershipObjectClass, membershipObjectClassFilter;

   String ldapCreatedTimeStampAttr, ldapModifiedTimeStampAttr, ldapDescriptionAttr;

   // configuration.
   String groupNameAttr = "ou";

   String groupLabelAttr = "l";

   /**
    * Create LDAP attributes that represents user in LDAP context.
    * 
    * @param user User
    * @return LDAP Attributes
    */
   public final Attributes userToAttributes(User user)
   {
      BasicAttributes attrs = new BasicAttributes();
      if (USER_LDAP_CLASSES == null)
         USER_LDAP_CLASSES = userLDAPClasses.split(",");
      attrs.put(new ObjectClassAttribute(USER_LDAP_CLASSES));
      attrs.put(userDNKey, user.getUserName());
      attrs.put(userDisplayNameAttr, user.getDisplayName());
      attrs.put(userUsernameAttr, user.getUserName());
      attrs.put(userPassword, user.getPassword());
      attrs.put(userLastNameAttr, user.getLastName());
      attrs.put(userFirstNameAttr, user.getFirstName());
      attrs.put(userMailAttr, user.getEmail());
      attrs.put(ldapDescriptionAttr, "Account for " + user.getDisplayName());
      if (hasUserAccountControl())
         attrs.put(userAccountControlAttr, Integer.toString(user.isEnabled() ? 0 : UserDAOImpl.UF_ACCOUNTDISABLE));
      return attrs;
   }

   /**
    * Create User from LDAP attributes.
    * 
    * @param attrs {@link Attributes}
    * @return User
    * @throws DisabledUserException in case the attribute <code>userAccountControlAttr</code>
    *         could not be parsed properly.
    */
   public final User attributesToUser(Attributes attrs) throws DisabledUserException
   {
      if (attrs == null || attrs.size() == 0)
         return null;
      LDAPUserImpl user = new LDAPUserImpl();
      user.setUserName(getAttributeValueAsString(attrs, userUsernameAttr));
      user.setLastName(getAttributeValueAsString(attrs, userLastNameAttr));
      user.setFirstName(getAttributeValueAsString(attrs, userFirstNameAttr));
      user.setDisplayName(getAttributeValueAsString(attrs, userDisplayNameAttr));
      user.setEmail(getAttributeValueAsString(attrs, userMailAttr));
      user.setPassword(getAttributeValueAsString(attrs, userPassword));
      user.setCreatedDate(Calendar.getInstance().getTime());
      user.setLastLoginTime(Calendar.getInstance().getTime());
      int iUserAccountControl = getUserAccountControl(user.getUserName(), attrs);
      user.setEnabled((iUserAccountControl & 2) == 0);
      user.setUserAccountControl(iUserAccountControl);
      return user;
   }

   /**
    * Indicates whether the user account is enabled or not according to the provided
    * attributes
    * @param username the user name corresponding to the account to check
    * @param attrs the attributes related to the user account
    * @return <code>true</code> if the user account is enabled, <code>false</code>
    *         otherwise
    * @throws DisabledUserException in case the attribute <code>userAccountControlAttr</code>
    *         could not be parsed properly.
    */
   public final boolean isEnabled(String username, Attributes attrs) throws DisabledUserException
   {
      int iUserAccountControl = getUserAccountControl(username, attrs);
      return (iUserAccountControl & 2) == 0;
   }

   /**
    * Gives the value of the attribute <code>userAccountControlAttr</code>
    * @param username the user name corresponding to the provided attributes
    * @param attrs the attributes related to the user account
    * @return the value of the attribute if it could be found, <code>0</code> by default
    * @throws DisabledUserException in case the attribute <code>userAccountControlAttr</code>
    *         could not be parsed properly.
    */
   private final int getUserAccountControl(String username, Attributes attrs) throws DisabledUserException
   {
      if (!hasUserAccountControl() || attrs == null || attrs.size() == 0)
         return 0;
      String userAccountControl = getAttributeValueAsString(attrs, userAccountControlAttr);
      if (userAccountControl != null && !userAccountControl.isEmpty())
      {
         try
         {
            return Integer.parseInt(userAccountControl);
         }
         catch (NumberFormatException e)
         {
            throw new DisabledUserException(username, e);
         }
      }
      return 0;
   }

   /**
    * Create LDAP attributes that represents group in LDAP context.
    * 
    * @param group Group
    * @return LDAP attributes
    */
   public final Attributes groupToAttributes(Group group)
   {
      BasicAttributes attrs = new BasicAttributes();
      if (GROUP_LDAP_CLASSES == null)
         GROUP_LDAP_CLASSES = groupLDAPClasses.split(",");
      attrs.put(new ObjectClassAttribute(GROUP_LDAP_CLASSES));
      attrs.put(groupNameAttr, group.getGroupName());
      String desc = group.getDescription();
      if (desc != null && desc.length() > 0)
         attrs.put(ldapDescriptionAttr, desc);
      String lbl = group.getLabel();
      if (lbl != null && lbl.length() > 0)
         attrs.put(groupLabelAttr, lbl);
      return attrs;
   }

   /**
    * Create group from LDAP attributes.
    * 
    * @param attrs {@link Attributes}
    * @return Group
    */
   public final Group attributesToGroup(Attributes attrs)
   {
      if (attrs == null || attrs.size() == 0)
         return null;
      Group group = new GroupImpl();
      group.setGroupName(getAttributeValueAsString(attrs, groupNameAttr));
      group.setDescription(getAttributeValueAsString(attrs, ldapDescriptionAttr));
      group.setLabel(getAttributeValueAsString(attrs, groupLabelAttr));
      return group;
   }

   /**
    * Create LDAP attributes that represents {@link MembershipType} in LDAP
    * context.
    * 
    * @param mt MemebrshipType
    * @return LDAP attributes
    */
   public final Attributes membershipTypeToAttributes(MembershipType mt)
   {
      BasicAttributes attrs = new BasicAttributes();
      if (MEMBERSHIPTYPE_LDAP_CLASSES == null)
         MEMBERSHIPTYPE_LDAP_CLASSES = membershipTypeLDAPClasses.split(",");
      attrs.put(new ObjectClassAttribute(MEMBERSHIPTYPE_LDAP_CLASSES));
      attrs.put(membershipTypeNameAttr, mt.getName());
      String desc = mt.getDescription();
      if (desc != null && desc.length() > 0)
         attrs.put(ldapDescriptionAttr, desc);
      return attrs;
   }

   /**
    * Create MembershipType from LDAP attributes.
    * 
    * @param attrs {@link Attributes}
    * @return MemebrshipType
    */
   public final MembershipType attributesToMembershipType(Attributes attrs)
   {
      if (attrs == null || attrs.size() == 0)
         return null;
      MembershipType m = new MembershipTypeImpl();
      m.setName(getAttributeValueAsString(attrs, membershipTypeNameAttr));
      m.setDescription(getAttributeValueAsString(attrs, ldapDescriptionAttr));
      m.setCreatedDate(new Date());
      m.setModifiedDate(new Date());
      return m;
   }

   /**
    * Create LDAP attributes that represents user Membership in LDAP context.
    * 
    * @param m Membership
    * @param userDN user Distinguished Name
    * @return DAP attributes
    */
   public final Attributes membershipToAttributes(Membership m, String userDN)
   {
      BasicAttributes attrs = new BasicAttributes();
      if (MEMBERSHIP_LDAP_CLASSES == null)
         MEMBERSHIP_LDAP_CLASSES = membershipLDAPClasses.split(",");
      attrs.put(new ObjectClassAttribute(MEMBERSHIP_LDAP_CLASSES));
      attrs.put(membershipTypeRoleNameAttr, m.getMembershipType());
      attrs.put(membershipTypeMemberValue, userDN);
      return attrs;
   }

   /**
    * Create LDAP attributes that represents UserProfile in LDAP context.
    * 
    * @param profile UserProfile
    * @return LDAP attributes.
    */
   public final Attributes profileToAttributes(UserProfile profile)
   {
      BasicAttributes attrs = new BasicAttributes();
      if (PROFILE_LDAP_CLASSES == null)
         PROFILE_LDAP_CLASSES = profileLDAPClasses.split(",");
      attrs.put(new ObjectClassAttribute(PROFILE_LDAP_CLASSES));

      attrs.put("sn", profile.getUserName());
      UserProfileData upd = new UserProfileData();
      upd.setUserProfile(profile);
      attrs.put(ldapDescriptionAttr, upd.getProfile());
      return attrs;
   }

   /**
    * Create UserProfileData from LDAP attributes.
    * 
    * @param attrs {@link Attributes}
    * @return {@link UserProfileData}
    */
   public final UserProfileData attributesToProfile(Attributes attrs)
   {
      if (attrs == null || attrs.size() == 0)
         return null;
      UserProfileData upd = new UserProfileData();
      upd.setProfile(getAttributeValueAsString(attrs, ldapDescriptionAttr));
      return upd;
   }

   /**
    * Get LDAP attribute as String with specified name from {@link Attributes}.
    * 
    * @param attributes {@link Attributes}
    * @param name attribute name
    * @return attribute as string
    */
   public final String getAttributeValueAsString(Attributes attributes, String name)
   {
      if (attributes == null)
         return "";
      Attribute attr = attributes.get(name);
      if (attr == null)
         return "";
      try
      {
         Object obj = attr.get();
         if (obj instanceof byte[])
            return new String((byte[])obj);
         return (String)obj;
      }
      catch (Exception e)
      {
         return "";
      }
   }

   /**
    * @return <code>true</code> if the user account attribute is configured false otherwise
    */
   public boolean hasUserAccountControl()
   {
      return userAccountControlAttr != null;
   }
}
