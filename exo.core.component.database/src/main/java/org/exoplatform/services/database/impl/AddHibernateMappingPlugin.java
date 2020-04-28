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
package org.exoplatform.services.database.impl;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Jul 26, 2005
 */
public class AddHibernateMappingPlugin extends BaseComponentPlugin
{

   List mapping_;

   List<String> annotations;

   public AddHibernateMappingPlugin(InitParams params)
   {
      if (params.containsKey("hibernate.mapping"))
      {
         mapping_ = params.getValuesParam("hibernate.mapping").getValues();
      }
      if (params.containsKey("hibernate.annotations"))
      {
         annotations = params.getValuesParam("hibernate.annotations").getValues();
      }
   }

   public List getMapping()
   {
      return mapping_;
   }

   public List<String> getAnnotations()
   {
      return annotations;
   }

}
