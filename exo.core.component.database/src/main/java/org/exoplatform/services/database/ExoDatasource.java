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

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.database.table.IDGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Created by The eXo Platform SAS
 * Author : Tuan Nguyen tuan08@users.sourceforge.net Apr 4, 2006
 * This class is a wrapper class for the java.sql.Datasource class.
 * In additional to the java.sql.Datasourcemethod such getConnection().
 * The ExoDatasource provides 2 other methods:
 * DBTableManager getDBTableManager and IDGenerator getIDGenerator()
 */
public class ExoDatasource
{

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.core.component.organization.database.ExoDatasource");

   final public static int STANDARD_DB_TYPE = 0;

   final public static int HSQL_DB_TYPE = 1;

   final public static int MYSQL_DB_TYPE = 2;

   final public static int DB2_DB_TYPE = 3;

   final public static int DERBY_DB_TYPE = 4;

   final public static int ORACLE_DB_TYPE = 5;

   final public static int SQL_SERVER_TYPE = 6;

   static int totalGetConnect = 0;

   final public static int MSSQL_DB_TYPE = 6;

   final public static int SYSBASE_DB_TYPE = 7;

   final public static int POSTGRES_DB_TYPE = 8;

   private DataSource xaDatasource_;

   private DBTableManager tableManager_;

   private IDGenerator idGenerator_;

   private QueryBuilder queryManager_;

   private String databaseName_;

   private String databaseVersion_;

   private int dbType_ = STANDARD_DB_TYPE;

   Connection conn;

   /**
    * The constructor should:
    * 1. Keep track of the datasource object
    * 2. Create the DBTableManager object base on the datasource information such database type, version
    * 3. Create an IDGenerator for the datasource
    * 
    * @param ds
    * @throws Exception
    */
   public ExoDatasource(final DataSource ds) throws Exception
   {
      xaDatasource_ = ds;
      DatabaseMetaData metaData =
         SecurityHelper.doPrivilegedSQLExceptionAction(new PrivilegedExceptionAction<DatabaseMetaData>()
         {
            public DatabaseMetaData run() throws SQLException
            {
               return ds.getConnection().getMetaData();
            }
         });

      databaseName_ = metaData.getDatabaseProductName();
      databaseVersion_ = metaData.getDatabaseProductVersion();

      String dbname = databaseName_.toLowerCase();
      LOG.debug("DB Name: " + dbname);
      if (dbname.indexOf("oracle") >= 0)
      {
         dbType_ = ORACLE_DB_TYPE;
      }
      else if (dbname.indexOf("hsql") >= 0)
      {
         dbType_ = HSQL_DB_TYPE;
      }
      else if (dbname.indexOf("mysql") >= 0)
      {
         dbType_ = MYSQL_DB_TYPE;
      }
      else if (dbname.indexOf("derby") >= 0)
      {
         dbType_ = DERBY_DB_TYPE;
      }
      else if (dbname.indexOf("db2") >= 0)
      {
         dbType_ = DB2_DB_TYPE;
      }
      else if (dbname.indexOf("server") >= 0)
      {
         dbType_ = SQL_SERVER_TYPE;
      }
      else
      {
         dbType_ = STANDARD_DB_TYPE;
      }

      tableManager_ = DBTableManager.createDBTableManager(this);
      idGenerator_ = new IDGenerator(this);
      queryManager_ = new QueryBuilder(dbType_);
   }

   /**
    * This method should return the real Datasource object
    * 
    * @return
    */
   public DataSource getDatasource()
   {
      return xaDatasource_;
   }

   /**
    * This method should call the datasource getConnection method and return the
    * Connection object. The developer can add some debug code or broadcast an
    * event here.
    * 
    * @return
    * @throws Exception
    */
   public Connection getConnection() throws Exception
   {
      return xaDatasource_.getConnection();
   }

   /**
    * This method should delegate to the method close of the Connection object.
    * The developer can add debug or broadcast an event here.
    * 
    * @param conn
    * @throws Exception
    */
   public void closeConnection(Connection conn) throws Exception
   {
      conn.close();
   }

   /**
    * This method should delegate to the commit() method of the Connection
    * object. The developer can add the debug code here
    * 
    * @param conn
    * @throws Exception
    */
   public void commit(Connection conn) throws Exception
   {
      conn.setAutoCommit(false);
      conn.commit();
   }

   /**
    * This method should return the DBTableManager object. The DBTableManager
    * object should be initialized in the constructor according to the database
    * type and version
    * 
    * @return
    */
   public DBTableManager getDBTableManager()
   {
      return tableManager_;
   }

   /**
    * This method should return the IDGenerator object, the developer can use the
    * id generator to generate an unique long id for an db object
    * 
    * @return
    */
   public IDGenerator getIDGenerator()
   {
      return idGenerator_;
   }

   public int getDatabaseType()
   {
      return dbType_;
   }

   public String getDatabaseName()
   {
      return databaseName_;
   }

   public String getDatabaseVersion()
   {
      return databaseVersion_;
   }

   public QueryBuilder getQueryBuilder()
   {
      return queryManager_;
   }
}
