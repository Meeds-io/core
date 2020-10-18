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
package org.exoplatform.services.xml.transform.trax;

import javax.xml.transform.Source;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public interface TRAXTemplatesService
{

   /**
    * Add new TRAXTemplates to the service.
    * 
    * @param key the key for this templates.
    * @param templates the TRAXTemplates.
    * @throws IllegalArgumentException
    */
   void addTRAXTemplates(String key, TRAXTemplates templates) throws IllegalArgumentException;

   /**
    * Add new TRAXTemplates to the service from javax.xml.transform.Source.
    * 
    * @param key the key for this templates.
    * @param source the TRAXTemplates.
    * @throws IllegalArgumentException
    */
   void addTRAXTemplates(String key, Source source) throws IllegalArgumentException;

   /**
    * Get templates by key.
    * 
    * @param key the key.
    * @return the TRAXTemplates or null.
    */
   TRAXTemplates getTemplates(String key);

}
