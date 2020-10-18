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

import org.exoplatform.services.listener.Event;

/**
 * Created by The eXo Platform SAS
 * Author : Nhu Dinh Thuan nhudinhthuan@exoplatform.com Apr 6, 2007
 */
public class DBObjectEvent<E extends DAO<?>, T extends DBObject> extends Event<E, T>
{

   public DBObjectEvent(String name, E e, T t)
   {
      super(name, e, t);
   }

}
