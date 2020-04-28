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
package org.exoplatform.services.database.impl.regions;

import org.exoplatform.services.cache.ExoCache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.GeneralDataRegion;

import java.io.Serializable;

/**
 * @author <a href="dvishinskiy@exoplatform.com">Dmitriy Vishinskiy</a>
 * @version $Id:$
 */
public class ExoCacheGeneralDataRegion extends ExoCacheRegion implements GeneralDataRegion
{

   public ExoCacheGeneralDataRegion(ExoCache<Serializable, Object> cache)
   {
      super(cache);
   }

   /**
    * {@inheritDoc}
    */
   public Object get(Object key) throws CacheException
   {
      try
      {
         return cache.get((Serializable)key);
      }
      catch (Exception ex)
      {
         throw new CacheException(ex);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void put(Object key, Object value) throws CacheException
   {
      try
      {
         cache.put((Serializable)key, (Serializable)value);
      }
      catch (Exception ex)
      {
         throw new CacheException(ex);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void evict(Object key) throws CacheException
   {
      cache.remove((Serializable)key);
   }

   /**
    * {@inheritDoc}
    */
   public void evictAll() throws CacheException
   {
      cache.clearCache();
   }

}
