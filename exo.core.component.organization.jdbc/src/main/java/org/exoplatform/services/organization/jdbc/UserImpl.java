/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.services.organization.jdbc;

import org.exoplatform.services.database.DBObject;
import org.exoplatform.services.database.annotation.Table;
import org.exoplatform.services.database.annotation.TableField;
import org.exoplatform.services.organization.User;

import java.util.Date;

@Table(name = "EXO_USER", field = {
   @TableField(name = "USER_NAME", type = "string", length = 200, unique = true, nullable = false),
   @TableField(name = "PASSWORD", type = "string", length = 100),
   @TableField(name = "FIRST_NAME", type = "string", length = 500),
   @TableField(name = "LAST_NAME", type = "string", length = 200),
   @TableField(name = "EMAIL", type = "string", length = 200),
   @TableField(name = "DISPLAY_NAME", type = "string", length = 200),
   @TableField(name = "CREATED_DATE", type = "date", length = 100),
   @TableField(name = "LAST_LOGIN_TIME", type = "date", length = 100),
   @TableField(name = "ORGANIZATION_ID", type = "string", length = 100)})
public class UserImpl extends DBObject implements User
{

   private String userName = null;

   private transient String password = null;

   private String firstName = null;

   private String lastName = null;

   private String email = null;

   private Date createdDate;

   private Date lastLoginTime;

   private String organizationId = null;

   private String displayName = null;

   public UserImpl()
   {
   }

   public UserImpl(String username)
   {
      this.userName = username.toLowerCase();
   }

   public String getDisplayName()
   {
      return displayName != null ? displayName : getFirstName() + " " + getLastName();
   }

   public void setDisplayName(String displayName)
   {
      this.displayName = displayName;
   }

   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String name)
   {
      this.userName = name.toLowerCase();
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   public String getFirstName()
   {
      return firstName;
   }

   public void setFirstName(String firstName)
   {
      this.firstName = firstName;
   }

   public String getLastName()
   {
      return lastName;
   }

   public void setLastName(String lastName)
   {
      this.lastName = lastName;
   }

   public String getEmail()
   {
      return email;
   }

   public void setEmail(String email)
   {
      this.email = email;
   }

   public String getFullName()
   {
      return getDisplayName();
   }

   public void setFullName(String s)
   {
      setDisplayName(s);
   }

   public Date getCreatedDate()
   {
      return createdDate;
   }

   public void setCreatedDate(Date t)
   {
      createdDate = t;
   }

   public Date getLastLoginTime()
   {
      return lastLoginTime;
   }

   public void setLastLoginTime(Date t)
   {
      lastLoginTime = t;
   }

   public String toString()
   {
      return "User[" + dbObjectId_ + "|" + userName + "]" + (organizationId == null ? "" : ("@" + organizationId));
   }

   public String getOrganizationId()
   {
      return organizationId;
   }

   public void setOrganizationId(String organizationId)
   {
      this.organizationId = organizationId;
   }

}
