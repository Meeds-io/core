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

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.database.jdbc.CreateDBSchemaPlugin;
import org.exoplatform.services.database.jdbc.DBSchemaCreator;

import java.util.List;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: DBCreatorTest.java 5569 2006-05-17 12:48:47Z lautarul $
 */
public class DBCreatorTest extends TestCase
{

   //  private StandaloneContainer container;
   private DBSchemaCreator dbcreator;

   public void setUp() throws Exception
   {
      // >>>>> to avoid  two top-level container exception  
      //    StandaloneContainer.setConfigurationPath("src/main/java/conf/standalone/test-configuration.xml");
      //    container = StandaloneContainer.getInstance();
      PortalContainer container = PortalContainer.getInstance();
      dbcreator = (DBSchemaCreator)container.getComponentInstanceOfType(DBSchemaCreator.class);
   }

   public void testConf() throws Exception
   {
      //    DBSchemaCreator dbcreator = (DBSchemaCreator) container.getComponentInstanceOfType(DBSchemaCreator.class);
      List<?> plugins = (List<?>)dbcreator.getPlugins();
      assertFalse(plugins.isEmpty());

      assertTrue(plugins.get(0) instanceof CreateDBSchemaPlugin);
      CreateDBSchemaPlugin plugin = (CreateDBSchemaPlugin)plugins.get(0);

      assertNotNull(plugin.getDataSource());
      assertNotNull(plugin.getScript());
   }

   public void tearDown() throws Exception
   {
      //    container.stop();
   }
}
