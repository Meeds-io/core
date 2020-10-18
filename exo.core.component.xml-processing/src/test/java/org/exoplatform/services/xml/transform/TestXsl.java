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
package org.exoplatform.services.xml.transform;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.xml.BaseTest;
import org.exoplatform.services.xml.transform.trax.TRAXTemplates;
import org.exoplatform.services.xml.transform.trax.TRAXTransformer;
import org.exoplatform.services.xml.transform.trax.TRAXTransformerService;

import java.io.*;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Created by the Exo Development team.
 */
public class TestXsl extends BaseTest
{
   private TRAXTransformerService traxService;

   public void setUp() throws Exception
   {
      StandaloneContainer.setConfigurationPath("src/test/resources/conf/standalone/test-configuration.xml");
      StandaloneContainer container = StandaloneContainer.getInstance();
      traxService = (TRAXTransformerService)container.getComponentInstanceOfType(TRAXTransformerService.class);
      assertNotNull("traxService", traxService);
   }

   public void testSimpleXslt() throws Exception
   {
      InputStream res = resourceStream("rss-in.xhtml");
      File outputFile = File.createTempFile("rss-out-", ".xml");
      outputFile.deleteOnExit();

      assertTrue("Empty input file", res.available() > 0);

      // output file
      OutputStream outputFileOutputStream = new FileOutputStream(outputFile);

      // get xsl
      InputStream xslInputStream = resourceStream("html-url-rewite.xsl");
      assertNotNull("empty xsl", xslInputStream);
      Source xslSource = new StreamSource(xslInputStream);
      assertNotNull("get xsl source", xslSource);

      // init transformer
      TRAXTransformer traxTransformer = traxService.getTransformer(xslSource);
      assertNotNull("get transformer", traxTransformer);

      traxTransformer.initResult(new StreamResult(outputFileOutputStream));
      traxTransformer.transform(new StreamSource(res));

      res.close();
      outputFileOutputStream.close();

      // read the output file
      FileInputStream outputFileInputStream = new FileInputStream(outputFile);

      assertTrue("Output is empty", outputFileInputStream.available() > 0);
      outputFileInputStream.close();

   }

   public void testXsltUseTemplates() throws Exception
   {
      InputStream res = resourceStream("rss-in.xhtml");

      assertTrue("Empty input file", res.available() > 0);

      // output
      ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

      // get xsl
      InputStream xslInputStream = resourceStream("html-url-rewite.xsl");
      assertNotNull("empty xsl", xslInputStream);
      Source xslSource = new StreamSource(xslInputStream);
      assertNotNull("get xsl source", xslSource);

      // init templates
      TRAXTemplates traxTemplates = traxService.getTemplates(xslSource);
      assertNotNull("get templates", traxTemplates);

      // get transformer
      TRAXTransformer traxTransformer = traxTemplates.newTransformer();
      assertNotNull("get transformer", traxTransformer);

      // transform
      traxTransformer.initResult(new StreamResult(byteOutputStream));
      traxTransformer.transform(new StreamSource(res));
      res.close();

      assertTrue("Output is empty", byteOutputStream.size() > 0);

      // other transformer from same templates

      TRAXTransformer traxOtherTransformer = traxTemplates.newTransformer();
      assertNotNull("get Other transformer", traxOtherTransformer);

      res = resourceStream("rss-in.xhtml");

      assertTrue("Empty input other file", res.available() > 0);

      ByteArrayOutputStream byteOtherOutputStream = new ByteArrayOutputStream();

      traxOtherTransformer.initResult(new StreamResult(byteOtherOutputStream));
      traxOtherTransformer.transform(new StreamSource(res));
      res.close();
      assertTrue("Output other is empty", byteOutputStream.size() > 0);

   }

}
