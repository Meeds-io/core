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
package org.exoplatform.services.database.impl.strategies;

import org.exoplatform.services.database.impl.regions.ExoCacheTransactionalDataRegion;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.NaturalIdRegion;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;

/**
 * @author <a href="dvishinskiy@exoplatform.com">Dmitriy Vishinskiy</a>
 * @version $Id:$
 */
public class ExoCacheNaturalIdRegionReadWriteAccessStrategy extends ExoCacheAccessStrategy implements
   NaturalIdRegionAccessStrategy
{

   public ExoCacheNaturalIdRegionReadWriteAccessStrategy(ExoCacheTransactionalDataRegion region)
   {
      super(region);
   }

   /**
    * {@inheritDoc}
    */
   public NaturalIdRegion getRegion()
   {
      return (NaturalIdRegion)region;
   }

   /**
    * {@inheritDoc}
    */
   public boolean insert(Object key, Object value) throws CacheException
   {
      region.put(key, value);
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public boolean update(Object key, Object value) throws CacheException
   {
      region.put(key, value);
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public boolean afterInsert(Object key, Object value) throws CacheException
   {
      return false;
   }

   /**
    * {@inheritDoc}
    */
   public boolean afterUpdate(Object key, Object value, SoftLock lock) throws CacheException
   {
      return false;
   }

}