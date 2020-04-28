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
package org.exoplatform.services.organization.externalstore.model;

import java.util.Calendar;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

/**
 * IDM Queue entry DTO
 */
public class IDMQueueEntry {

  private long             id;

  private String           entityId;

  private IDMEntityType<?> entityType;

  private IDMOperationType operationType;

  private boolean          processed  = false;

  private int              retryCount = 0;

  private Calendar         creationDate;

  public IDMQueueEntry() {
  }

  public IDMQueueEntry(IDMEntityType<?> entityType, String entityId, IDMOperationType operationType) {
    if (entityType == null) {
      throw new IllegalArgumentException("'entityType' parameter shouldn't be null");
    }
    if (StringUtils.isBlank(entityId)) {
      throw new IllegalArgumentException("'entityId' parameter is mandatory");
    }
    if (operationType == null) {
      throw new IllegalArgumentException("'operationType' parameter shouldn't be null");
    }
    this.operationType = operationType;
    this.entityType = entityType;
    this.entityId = entityId;
  }

  public IDMEntityType<?> getEntityType() {
    return entityType;
  }

  public IDMQueueEntry setEntityType(IDMEntityType<?> entityType) {
    this.entityType = entityType;
    return this;
  }

  public String getEntityId() {
    return entityId;
  }

  public IDMQueueEntry setEntityId(String entityId) {
    this.entityId = entityId;
    return this;
  }

  public IDMOperationType getOperationType() {
    return operationType;
  }

  public IDMQueueEntry setOperationType(IDMOperationType operationType) {
    this.operationType = operationType;
    return this;
  }

  public boolean isProcessed() {
    return processed;
  }

  public IDMQueueEntry setProcessed(boolean processed) {
    this.processed = processed;
    return this;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public IDMQueueEntry setRetryCount(int retryCount) {
    this.retryCount = retryCount;
    return this;
  }

  public long getId() {
    return id;
  }

  public IDMQueueEntry setId(long id) {
    this.id = id;
    return this;
  }

  public Calendar getCreationDate() {
    return creationDate;
  }

  public IDMQueueEntry setCreationDate(Calendar creationDate) {
    this.creationDate = creationDate;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof IDMQueueEntry)) {
      return false;
    }
    IDMQueueEntry entry = (IDMQueueEntry) obj;
    return StringUtils.equals(entry.getEntityId(), getEntityId()) && entry.getOperationType() == getOperationType()
        && Objects.equals(entry.getEntityType(), getEntityType());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getEntityId(), getOperationType(), getEntityType());
  }

  @Override
  public String toString() {
    return "[entityType=" + (entityType == null ? null : entityType.getClassType().getName()) + ", entityId=" + entityId
        + ",operationType=" + operationType + ",processed=" + processed + ",retryCount=" + retryCount + "]";
  }
}
