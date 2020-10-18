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
package org.exoplatform.services.database;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Nhu Dinh Thuan nhudinhthuan@exoplatform.com Mar 29, 2007
 */
public class ReflectionUtil
{

   private static final Log LOG = ExoLogger.getLogger("exo.core.component.database.ReflectionUtil");

   public final static void setValue(Object bean, Field field, Object value) throws Exception
   {
      Class<? extends Object> clazz = bean.getClass();
      Method method = getMethod("set", field, clazz);
      if (method != null)
         method.invoke(bean, new Object[]{value});
      method = getMethod("put", field, clazz);
      if (method != null)
         method.invoke(bean, new Object[]{value});
      field.setAccessible(true);
      field.set(bean, value);
   }

   public final static Object getValue(Object bean, Field field) throws Exception
   {
      Class<? extends Object> clazz = bean.getClass();
      Method method = getMethod("get", field, clazz);
      if (method != null)
         return method.invoke(bean, new Object[]{});
      method = getMethod("is", field, clazz);
      if (method != null)
         return method.invoke(bean, new Object[]{});
      field.setAccessible(true);
      return field.get(bean);
   }

   public final static Method getMethod(String prefix, Field field, Class<? extends Object> clazz) throws Exception
   {
      StringBuilder name = new StringBuilder(field.getName());
      name.setCharAt(0, Character.toUpperCase(name.charAt(0)));
      name.insert(0, prefix);
      try
      {
         Method method = clazz.getDeclaredMethod(name.toString(), new Class[]{});
         return method;
      }
      catch (Exception e)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("An exception occurred: " + e.getMessage());
         }
      }
      return null;
   }

   public final static List<Method> getMethod(Class<?> clazz, String name) throws Exception
   {
      Method[] methods = clazz.getDeclaredMethods();
      List<Method> list = new ArrayList<Method>();
      for (Method method : methods)
      {
         if (method.getName().equals(name))
            list.add(method);
      }
      return list;
   }
}
