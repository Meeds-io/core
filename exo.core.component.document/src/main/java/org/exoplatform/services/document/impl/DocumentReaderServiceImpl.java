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
package org.exoplatform.services.document.impl;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.document.DocumentReader;
import org.exoplatform.services.document.DocumentReaderService;
import org.exoplatform.services.document.HandlerNotFoundException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by The eXo Platform SAS Author : Phung Hai Nam
 * 
 * @author Gennady Azarenkov
 * @version $Id: DocumentReaderServiceImpl.java 11659 2007-01-05 15:35:06Z geaz
 *          $ Oct 19, 2005
 */
public class DocumentReaderServiceImpl implements DocumentReaderService
{
   protected volatile Map<String, DocumentReader> readers_;

   public DocumentReaderServiceImpl(InitParams params)
   {
      readers_ = new HashMap<String, DocumentReader>();
   }

   @Deprecated
   public String getContentAsText(String mimeType, InputStream is) throws Exception
   {
      DocumentReader reader = readers_.get(mimeType.toLowerCase());
      if (reader != null)
         return reader.getContentAsText(is);
      throw new Exception("Cannot handle the document type: " + mimeType);
   }

   /**
    * @param plugin
    */
   public void addDocumentReader(ComponentPlugin plugin)
   {
      BaseDocumentReader reader = (BaseDocumentReader)plugin;
      for (String mimeType : reader.getMimeTypes())
         readers_.put(mimeType.toLowerCase(), reader);
   }

   /*
    * (non-Javadoc)
    * @see
    * org.exoplatform.services.document.DocumentReaderService#getDocumentReader
    * (java.lang.String)
    */
   public DocumentReader getDocumentReader(String mimeType) throws HandlerNotFoundException
   {
      DocumentReader reader = readers_.get(mimeType.toLowerCase());
      if (reader != null)
         return reader;
      else
         throw new HandlerNotFoundException("No appropriate properties extractor for " + mimeType);
   }

}
