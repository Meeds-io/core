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
import org.exoplatform.services.xml.transform.html.HTMLTransformer;
import org.exoplatform.services.xml.transform.html.HTMLTransformerService;
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
public class TestPipe extends BaseTest
{

   private HTMLTransformer htmlTransformer;

   private TRAXTemplates traxTemplates;

   public void setUp() throws Exception
   {

      // html transformer
      StandaloneContainer.setConfigurationPath("src/test/resources/conf/standalone/test-configuration.xml");
      StandaloneContainer container = StandaloneContainer.getInstance();

      TRAXTransformerService traxService =
         (TRAXTransformerService)container.getComponentInstanceOfType(TRAXTransformerService.class);
      assertNotNull("traxService", traxService);
      HTMLTransformerService htmlService =
         (HTMLTransformerService)container.getComponentInstanceOfType(HTMLTransformerService.class);
      assertNotNull("htmlService", htmlService);

      htmlTransformer = htmlService.getTransformer();
      assertNotNull("get html transformer", htmlTransformer);

      // get xsl
      InputStream xslInputStream = resourceStream("html-url-rewite.xsl");
      assertNotNull("empty xsl", xslInputStream);
      Source xslSource = new StreamSource(xslInputStream);
      assertNotNull("get xsl source", xslSource);
      // init transformer
      traxTemplates = traxService.getTemplates(xslSource);
      assertNotNull("get trax Templates", traxTemplates);
   }

   public void testTidyAndXsl() throws Exception
   {
      // input
      InputStream res = resourceStream("rss-in.xhtml");

      assertTrue("Empty input file", res.available() > 0);

      // output
      File outputFile = File.createTempFile("rss-out-", ".xhtml");
      OutputStream outputFileOutputStream = new FileOutputStream(outputFile);
      TRAXTransformer traxTransformer = traxTemplates.newTransformer();

      // construct pipe
      htmlTransformer.initResult(traxTransformer.getTransformerAsResult());
      traxTransformer.initResult(new StreamResult(outputFileOutputStream));
      htmlTransformer.transform(new StreamSource(res));

      res.close();

      // read the output file
      FileInputStream outputFileInputStream = new FileInputStream(outputFile);

      assertTrue("Output is empty", outputFileInputStream.available() > 0);
      outputFileInputStream.close();
   }

   public void testXslAndXsl() throws Exception
   {
      // input
      InputStream res = resourceStream("rss-in.xhtml");

      assertTrue("Empty input file", res.available() > 0);

      // output
      File outputFile = File.createTempFile("rss-out-", ".xml");
      outputFile.deleteOnExit();
      OutputStream outputFileOutputStream = new FileOutputStream(outputFile);

      TRAXTransformer traxTransformer1 = traxTemplates.newTransformer();
      TRAXTransformer traxTransformer2 = traxTemplates.newTransformer();

      assertNotNull("pipe supported ", traxTransformer2.getTransformerAsResult());

      // construct pipe
      traxTransformer1.initResult(traxTransformer2.getTransformerAsResult());

      traxTransformer2.initResult(new StreamResult(outputFileOutputStream));

      traxTransformer1.transform(new StreamSource(res));

      res.close();
      outputFileOutputStream.flush();
      outputFileOutputStream.close();

      // read the output file
      FileInputStream outputFileInputStream = new FileInputStream(outputFile);

      assertTrue("Output is empty", outputFileInputStream.available() > 0);
      // validate output
      validateXML(outputFileInputStream);
      outputFileInputStream.close();

   }

   public void testTidyAndXslWithEmptySource() throws Exception
   {

      java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
      InputStream input = new java.io.ByteArrayInputStream(new byte[]{});

      TRAXTransformer traxTransformer = traxTemplates.newTransformer();

      // construct pipe
      htmlTransformer.initResult(traxTransformer.getTransformerAsResult());
      traxTransformer.initResult(new StreamResult(output));
      htmlTransformer.transform(new StreamSource(input));

      assertTrue("Output is not empty", output.toByteArray().length == 0);
   }

}
