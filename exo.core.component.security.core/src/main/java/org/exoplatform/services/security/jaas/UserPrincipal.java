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
package org.exoplatform.services.security.jaas;

import java.io.Serializable;
import java.security.Principal;

/**
 * Created y the eXo platform team User: Tuan Nguyen Date: May 6th, 2007
 */
public class UserPrincipal implements Principal, Serializable
{

   private static final long serialVersionUID = -8915147646892501932L;

   /**
    * user principal name.
    */
   private String name;

   public UserPrincipal(String name)
   {
      this.name = name;
   }

   /**
    * {@inheritDoc}
    */
   public String getName()
   {
      return name;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      return getName();
   }
}
