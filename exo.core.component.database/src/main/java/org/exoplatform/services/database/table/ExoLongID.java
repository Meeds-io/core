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
package org.exoplatform.services.database.table;

import org.exoplatform.services.database.DBObject;
import org.exoplatform.services.database.annotation.Table;
import org.exoplatform.services.database.annotation.TableField;

/**
 * Created by The eXo Platform SAS Mar 16, 2007
 */
@Table(name = "EXO_LONG_ID", field = {
   @TableField(name = "EXO_NAME", type = "string", length = 500, unique = true, nullable = false),
   @TableField(name = "EXO_START", type = "long")})
public class ExoLongID extends DBObject
{

   final static public long BLOCK_SIZE = 3;

   private String name;

   private long currentBlockId;

   public ExoLongID()
   {
   }

   public ExoLongID(String name, long start)
   {
      this.name = name;
      this.currentBlockId = start;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public long getCurrentBlockId()
   {
      return currentBlockId;
   }

   public void setCurrentBlockId(long start)
   {
      this.currentBlockId = start;
   }

   public void setNextBlock()
   {
      this.currentBlockId = this.currentBlockId + BLOCK_SIZE;
   }

}
