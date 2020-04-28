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
package org.exoplatform.services.organization.externalstore.job;

import org.quartz.*;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.externalstore.IDMExternalStoreImportService;

/**
 * This is a Job to check deleted entries on external store and that are
 * existing on internal store. When deleted entries are detected, a queue entry
 * will be populated by detected deleted entry.
 */
@DisallowConcurrentExecution
public class IDMEntitiesDeleteJob implements InterruptableJob {

  private IDMExternalStoreImportService externalStoreImportService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      getExternalStoreImportService().checkAllEntitiesToDeleteIntoQueue();
    } catch (Exception e) {
      throw new JobExecutionException("An error occurred while executing job IDMEntitiesDeleteJob", e);
    }
  }

  public IDMExternalStoreImportService getExternalStoreImportService() {
    if (externalStoreImportService == null) {
      externalStoreImportService = ExoContainerContext.getCurrentContainer()
                                                      .getComponentInstanceOfType(IDMExternalStoreImportService.class);
    }
    return externalStoreImportService;
  }

  @Override
  public void interrupt() throws UnableToInterruptJobException {
    getExternalStoreImportService().interrupt();
  }
}
