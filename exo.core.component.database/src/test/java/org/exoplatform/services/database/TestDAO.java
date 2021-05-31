/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.services.database;

import junit.framework.TestCase;

import org.exoplatform.services.listener.ListenerService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan.nguyen@exoplatform.com Mar 27, 2007
 */
public class TestDAO extends TestCase
{

   public void testDummy()
   {
      // empty, to doesn't fail during the tests
   }

   private String printQueryResult(DatabaseService service) throws Exception
   {
      Connection conn = service.getConnection();
      Statement statement = conn.createStatement();
      String output = "\nQuery result: \n";
      ResultSet rs = statement.executeQuery("SELECT * FROM ExoLongId");
      while (rs.next())
      {
         output += rs.getString(1) + "\n" + rs.getString(2) + "\n" + rs.getString(3) + "====\n";
      }

      return output;
   }

   private void queries(DatabaseService service) throws Exception
   {
   }
}
