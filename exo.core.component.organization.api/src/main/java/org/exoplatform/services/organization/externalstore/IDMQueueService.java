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
package org.exoplatform.services.organization.externalstore;

import java.time.*;
import java.util.List;

import org.exoplatform.services.organization.externalstore.model.IDMEntityType;
import org.exoplatform.services.organization.externalstore.model.IDMQueueEntry;

/**
 * This API exposes the IDM FIFO Queue operations to be able to manage IDM
 * entities management on internal store from external store (creation, deletion
 * and modification)
 */
public interface IDMQueueService {

  /**
   * Get date and time of last check made on modified and added entities on
   * external store for a chosen entity type
   * 
   * @param entityType
   * @return
   */
  LocalDateTime getLastCheckedTime(IDMEntityType<?> entityType) throws Exception;

  /**
   * Stores date and time of last check on modified entities on external store
   * for a chosen entity type
   * 
   * @param entityType
   * @param dateTime
   */
  void setLastCheckedTime(IDMEntityType<?> entityType, LocalDateTime dateTime) throws Exception;

  /**
   * Count queue entries which retryCount equals to nbRetries and which are not
   * processed yet
   * 
   * @param nbRetries
   * @return
   * @throws Exception
   */
  int count(int nbRetries) throws Exception;

  /**
   * Count not processed queue entries and nbRetries less than maxRetries
   * 
   * @return
   * @throws Exception
   */
  int countAll() throws Exception;

  /**
   * Push a new IDM Queue entry at the beginning of the queue
   * 
   * @param queueEntry
   * @throws Exception
   */
  void push(IDMQueueEntry queueEntry) throws Exception;

  /**
   * Retrieve a list of queue entries that has retryCount equals to nbRetries.
   * The maximum list size is at most equals to limit. If keepInQueue = true,
   * the elements will not be deleted from queue.
   * 
   * @param limit
   * @param nbRetries
   * @param keepInQueue
   * @return
   * @throws Exception
   */
  List<IDMQueueEntry> pop(int limit, int nbRetries, boolean keepInQueue) throws Exception;

  /**
   * Set queue entries as processed
   * 
   * @param queueEntries
   * @throws Exception
   */
  void storeAsProcessed(List<IDMQueueEntry> queueEntries) throws Exception;

  /**
   * Purge processed queue entries
   * 
   * @throws Exception
   */
  void deleteProcessedEntries() throws Exception;

  /**
   * Purge entries which have nbRetries greater or equals to maxRetries
   */
  void deleteExceededRetriesEntries();

  /**
   * Increment retryCount for each passed queue entry in the list
   * 
   * @param queueEntries
   * @throws Exception
   */
  void incrementRetry(List<IDMQueueEntry> queueEntries) throws Exception;

  /**
   * Get maximum of retries for queue entries
   *
   * @return
   */
  int getMaxRetries();

}
