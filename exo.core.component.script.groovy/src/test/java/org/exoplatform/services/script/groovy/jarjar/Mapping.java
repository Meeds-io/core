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
package org.exoplatform.services.script.groovy.jarjar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Mapping
{

   /** . */
   private final Map<List<String>, List<String>> map;

   public Mapping()
   {
      map = new HashMap<List<String>, List<String>>();
   }

   public void addMapping(String source, String destination)
   {
      List<String> sourcePackage = Arrays.asList(source.split("\\."));
      List<String> destinationPackage = Arrays.asList(destination.split("\\."));
      map.put(sourcePackage, destinationPackage);
   }

   public void configure(JarJarClassLoader loader)
   {
      for (Map.Entry<List<String>, List<String>> entry : map.entrySet())
      {
         loader.addMapping(entry.getKey(), entry.getValue());
      }
   }
}
