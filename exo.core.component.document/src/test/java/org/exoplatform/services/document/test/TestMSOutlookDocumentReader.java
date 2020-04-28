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
package org.exoplatform.services.document.test;

import org.exoplatform.services.document.impl.DocumentReaderServiceImpl;
import org.exoplatform.services.document.impl.MSOutlookDocumentReader;
import org.exoplatform.test.BasicTestCase;

import java.io.InputStream;

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua>
 * 
 * @version $Id: $
 */

public class TestMSOutlookDocumentReader extends BaseStandaloneTest
{
   DocumentReaderServiceImpl service;

   public void setUp() throws Exception
   {
      super.setUp();
      service = new DocumentReaderServiceImpl(null);
      service.addDocumentReader(new MSOutlookDocumentReader());
   }

   public void testGetContentAsString() throws Exception
   {
      String name = "/test.msg";
      InputStream is = BasicTestCase.class.getResourceAsStream(name);
      assertNotNull(is);
      try
      {
         String text = service.getDocumentReader("application/vnd.ms-outlook").getContentAsText(is);
         String etalon = "Goooogle\n" + "theme\n" + "Hello, this is the test outlook message.\r\n";
         assertEquals("Wrong string returned", etalon, text);
      }
      finally
      {
         is.close();
      }
   }
}
