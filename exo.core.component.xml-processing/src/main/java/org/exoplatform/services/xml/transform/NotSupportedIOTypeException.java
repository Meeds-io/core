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
package org.exoplatform.services.xml.transform;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: NotSupportedIOTypeException.java 5799 2006-05-28 17:55:42Z geaz
 *          $
 */

public class NotSupportedIOTypeException extends Exception
{

   /** Constructs an Exception without a message. */
   public NotSupportedIOTypeException()
   {
      super();
   }

   /** Constructs an Exception with a message. */
   public NotSupportedIOTypeException(Result result)
   {
      super("Result type " + result.getClass().getName() + " is not supported by this transformer.");
   }

   /** Constructs an Exception with a message. */
   public NotSupportedIOTypeException(Source source)
   {
      super("Source type " + source.getClass().getName() + " is not supported by this transformer.");
   }

   /**
    * Constructs an Exception with a detailed message.
    * 
    * @param message The message associated with the exception.
    */
   public NotSupportedIOTypeException(String message)
   {
      super(message);
   }
}
