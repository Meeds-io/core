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
package org.exoplatform.services.ldap;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Oct 16, 2005
 */
public class ObjectClassAttributes extends BasicAttributes
{

   private static final long serialVersionUID = 188564309594273556L;

   public ObjectClassAttributes()
   {

   }

   public ObjectClassAttributes(String[] classes)
   {
      setClasses(classes);
   }

   public void setClasses(String[] classes)
   {
      BasicAttribute attr = new BasicAttribute("objectClass");
      for (String clazz : classes)
         attr.add(clazz);
      put(attr);
   }

   public void setClasses(String classes)
   {
      String[] clazz = classes.split(",");
      BasicAttribute attr = new BasicAttribute("objectClass");
      for (String c : clazz)
         attr.add(c);
      put(attr);
   }

   public void addAttribute(String key, Object value)
   {
      put(key, value);
   }
}
