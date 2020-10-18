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
package org.exoplatform.services.database;

import java.util.HashMap;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Oct 22, 2004
 * @version $Id: XResources.java 5332 2006-04-29 18:32:44Z geaz $
 */
public class XResources extends HashMap<Class, Object>
{

   public Object getResource(Class<?> cl)
   {
      return get(cl);
   }

   public XResources addResource(Class<?> cl, Object resource)
   {
      put(cl, resource);
      return this;
   }

   public Object removeResource(Class<?> cl)
   {
      return remove(cl);
   }
}
