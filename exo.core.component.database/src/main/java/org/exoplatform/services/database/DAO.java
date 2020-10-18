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

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Apr 4, 2006
 */
public abstract class DAO<T extends DBObject>
{

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.database.DAO");

   protected ExoDatasource eXoDS_;

   protected DBObjectMapper<T> mapper_;

   static int totalQueryTime = 0;

   static int totalBathTime = 0;

   static int totalCloseConnect = 0;

   public DAO(ExoDatasource datasource)
   {
      eXoDS_ = datasource;
      mapper_ = new ReflectionMapper<T>();
   }

   public DAO(ExoDatasource datasource, DBObjectMapper<T> mapper)
   {
      eXoDS_ = datasource;
      mapper_ = mapper;
   }

   public ExoDatasource getExoDatasource()
   {
      return eXoDS_;
   }

   abstract public T load(long id) throws Exception;

   abstract public PageList loadAll() throws Exception;

   abstract public void update(T bean) throws Exception;

   abstract public void update(List<T> beans) throws Exception;

   abstract public void save(T bean) throws Exception;

   abstract public void save(List<T> beans) throws Exception;

   abstract public void remove(T bean) throws Exception;

   abstract public T remove(long id) throws Exception;

   abstract public T createInstance() throws Exception;

   protected T loadUnique(String query) throws Exception
   {
      Connection connection = eXoDS_.getConnection();
      try
      {
         return loadUnique(connection, query);
      }
      finally
      {
         eXoDS_.closeConnection(connection);
      }
   }

   protected T loadUnique(Connection connection, String query) throws Exception
   {
      Statement statement = null;
      try
      {
         statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery(query);
         if (!resultSet.next())
         {
            return null;
         }
         T bean = createInstance();
         mapper_.mapResultSet(resultSet, bean);
         resultSet.close();
         return bean;
      }
      finally
      {
         if (statement != null)
            statement.close();
      }
   }

   protected void loadInstances(String loadQuery, List<T> list) throws Exception
   {
      Connection connection = eXoDS_.getConnection();
      try
      {
         loadInstances(connection, loadQuery, list);
      }
      finally
      {
         eXoDS_.closeConnection(connection);
      }
   }

   protected void loadInstances(Connection connection, String loadQuery, List<T> list) throws Exception
   {
      Statement statement = connection.createStatement();
      ResultSet resultSet = null;
      try
      {
         resultSet = statement.executeQuery(loadQuery);
         while (resultSet.next())
         {
            T bean = createInstance();
            mapper_.mapResultSet(resultSet, bean);
            list.add(bean);
         }
      }
      finally
      {
         if (resultSet != null)
         {
            try
            {
               resultSet.close();
            }
            catch (Exception e)
            {
               LOG.debug("Could not close the result set");
            }
         }
         try
         {
            statement.close();
         }
         catch (Exception e)
         {
            LOG.debug("Could not close the statement");
         }
      }
   }

   protected void execute(String query, T bean) throws Exception
   {
      Connection connection = eXoDS_.getConnection();
      try
      {
         execute(connection, query, bean);
      }
      finally
      {
         eXoDS_.closeConnection(connection);
      }
   }

   protected void execute(Connection connection, String query, T bean) throws Exception
   {
      PreparedStatement statement = connection.prepareStatement(query);
      if (bean != null)
         mapper_.mapUpdate(bean, statement);
      statement.executeUpdate();
      eXoDS_.commit(connection);
      statement.close();
   }

   public <E> E loadDBField(String query) throws Exception
   {
      Connection connection = eXoDS_.getConnection();
      try
      {
         return this.<E> loadDBField(connection, query);
      }
      finally
      {
         eXoDS_.closeConnection(connection);
      }
   }

   @SuppressWarnings("unchecked")
   protected <E> E loadDBField(Connection connection, String query) throws Exception
   {
      Statement statement = connection.createStatement();
      long startGet = System.currentTimeMillis();
      ResultSet resultSet = statement.executeQuery(query);
      totalQueryTime += System.currentTimeMillis() - startGet;
      if (!resultSet.next())
         return null;
      E value = (E)resultSet.getObject(1);
      resultSet.close();
      statement.close();
      return value;
   }

   protected void execute(String template, List<T> beans) throws Exception
   {
      Connection connection = eXoDS_.getConnection();
      try
      {
         execute(connection, template, beans);
      }
      finally
      {
         eXoDS_.closeConnection(connection);
      }
   }

   protected void execute(Connection connection, String template, List<T> beans) throws Exception
   {
      PreparedStatement statement = connection.prepareStatement(template);
      QueryBuilder builder = eXoDS_.getQueryBuilder();
      for (T bean : beans)
      {
         String query = builder.mapDataToSql(template, mapper_.toParameters(bean));
         statement.addBatch(query);
         LOG.info(" addBatch " + query);
      }
      statement.executeBatch();
      statement.close();
      eXoDS_.commit(connection);
   }

   public DBObjectMapper<T> getDBObjectMapper()
   {
      return mapper_;
   }

}
