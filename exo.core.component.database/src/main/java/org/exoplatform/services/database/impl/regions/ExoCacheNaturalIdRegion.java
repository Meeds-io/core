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
import org.exoplatform.services.database.impl.strategies.ExoCacheNaturalIdRegionReadWriteAccessStrategy;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.NaturalIdRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;

import java.io.Serializable;

/**
 * @author <a href="dvishinskiy@exoplatform.com">Dmitriy Vishinskiy</a>
 * @version $Id:$
 */
public class ExoCacheNaturalIdRegion extends ExoCacheTransactionalDataRegion implements NaturalIdRegion
{

   public ExoCacheNaturalIdRegion(ExoCache<Serializable, Object> cache, CacheDataDescription metadata)
   {
      super(cache, metadata);
   }

   /**
    * {@inheritDoc}
    */
   public NaturalIdRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException
   {
      switch (accessType)
      {
         case READ_WRITE :
            return new ExoCacheNaturalIdRegionReadWriteAccessStrategy(this);
         default :
            throw new IllegalArgumentException("Unrecognized access strategy type [" + accessType + "]");
      }
   }

}
