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
package org.exoplatform.services.web.css.model;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class StringLexicalUnitObject extends LexicalUnitObject
{

   private String stringValue;

   public StringLexicalUnitObject(short type, String stringValue)
   {
      super(type);
      this.stringValue = stringValue;
   }

   public String getStringValue()
   {
      return stringValue;
   }

   public void setStringValue(String s)
   {
      this.stringValue = s;
   }

   protected boolean safeEquals(LexicalUnitObject that)
   {
      if (super.safeEquals(that))
      {
         if (that instanceof StringLexicalUnitObject)
         {
            String thatStringValue = ((StringLexicalUnitObject)that).stringValue;
            return stringValue != null ? stringValue.equals(thatStringValue) : thatStringValue == null;
         }
      }
      return false;
   }
}
