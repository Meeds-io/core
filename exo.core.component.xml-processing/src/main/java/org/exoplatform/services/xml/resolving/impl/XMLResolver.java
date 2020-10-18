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
package org.exoplatform.services.xml.resolving.impl;

import org.exoplatform.commons.utils.PrivilegedSystemHelper;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class XMLResolver implements EntityResolver
{

   private Map<String, String> publicIDs_;

   private Map<String, String> systemIDs_;

   /**
    * Is publicID prefer.
    */
   private boolean publicIDPrefer_ = false;

   public XMLResolver(Map<String, String> publicIDs, Map<String, String> systemIDs)
   {
      publicIDs_ = publicIDs;
      systemIDs_ = systemIDs;
   }

   /*
    * (non-Javadoc)
    * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
    * java.lang.String)
    */
   public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
   {
      String entity = null;
      // if publicId is prefer first check publicIDs table
      if (publicIDPrefer_ && publicId != null && publicId.length() != 0)
         entity = publicIDs_.get(publicId);
      // if publicId is not prefer
      if (entity == null && systemId != null && systemId.length() != 0)
         entity = systemIDs_.get(systemId);
      // if entity still null try get it from publicIDs table
      if (entity == null && publicId != null && publicId.length() != 0)
         entity = publicIDs_.get(publicId);
      if (entity != null)
      {
         if (PrivilegedSystemHelper.getResource(entity) != null)
         {
            InputSource src = new InputSource(PrivilegedSystemHelper.getResourceAsStream(entity));
            src.setSystemId(PrivilegedSystemHelper.getResource(entity).getPath());
            return src;
         }
      }
      return null;
   }

   public boolean isPublicIDPrefer()
   {
      return publicIDPrefer_;
   }

   public void setPublicIDPrefer(boolean publicPrefer)
   {
      publicIDPrefer_ = publicPrefer;
   }

}
