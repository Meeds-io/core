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

import org.exoplatform.container.spi.DefinitionByType;
import org.exoplatform.services.xml.transform.impl.EncodingMapImpl;

/**
 * Created by The eXo Platform SAS . Conversions between IANA encoding names and
 * Java encoding names,
 * 
 * @author <a href="mailto:alex.kravchuk@gmail.com">Alexander Kravchuk</a>
 * @version $Id:
 */
@DefinitionByType(type = EncodingMapImpl.class)
public interface EncodingMap
{
   public String convertIANA2Java(String iana);

   public String convertJava2IANA(String java);
}
