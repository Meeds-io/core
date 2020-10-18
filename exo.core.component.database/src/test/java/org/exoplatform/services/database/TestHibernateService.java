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

/*
 * Thu, May 15, 2003 @   
 * @author: Tuan Nguyen
 * @version: $Id: TestDatabaseService.java 5332 2006-04-29 18:32:44Z geaz $
 * @since: 0.0
 * @email: tuan08@yahoo.com
 */
public class TestHibernateService extends TestCase
{
   HibernateService hservice_;

   public TestHibernateService(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      PortalContainer pcontainer = PortalContainer.getInstance();
      hservice_ = (HibernateService)pcontainer.getComponentInstanceOfType(HibernateService.class);
   }

   public void testDabaseService() throws Exception
   {
      // assertTrue("Expect hibernate service instance" , hservice_ != null) ;
      assertTrue("Expect database service instance", hservice_ != null);
   }

   protected String getDescription()
   {
      return "Test Database Service";
   }
}
