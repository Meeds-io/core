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
package org.exoplatform.services.xml.transform.impl.html;

import org.exoplatform.commons.utils.PrivilegedSystemHelper;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.xml.transform.EncodingMap;
import org.exoplatform.services.xml.transform.NotSupportedIOTypeException;
import org.exoplatform.services.xml.transform.html.HTMLTransformer;
import org.exoplatform.services.xml.transform.impl.EncodingMapImpl;
import org.exoplatform.services.xml.transform.impl.TransformerBase;
import org.w3c.tidy.Tidy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.PrivilegedAction;
import java.util.Properties;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by The eXo Platform SAS . Tidying incoming HTML to XHTML result
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @author <a href="mailto:alex.kravchuk@gmail.com">Alexander Kravchuk</a>
 * @version $Id: TidyTransformerImpl.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class TidyTransformerImpl extends TransformerBase implements HTMLTransformer
{
   protected Tidy tidy;

   protected Properties props;

   public TidyTransformerImpl()
   {
      super();
      tidy = SecurityHelper.doPrivilegedAction(new PrivilegedAction<Tidy>()
      {
         public Tidy run()
         {
            return new Tidy();
         }
      });
      initProps();
   }

   public Properties getOutputProperties()
   {
      return this.props;
   }

   /**
    * Sets properties for Tidy parser See Tidy properties
    */
   public void setOutputProperties(Properties props)
   {
      this.props = props;
   }

   private void initProps()
   {
      this.props = new Properties();

      props.setProperty("quiet", "true");
      props.setProperty("quote-ampersand", "true");
      props.setProperty("output-xhtml", "true");
      props.setProperty("show-warnings", "false");
      props.setProperty("clean", "true");

      props.setProperty("add-xml-decl", "true");
      props.setProperty("char-encoding", "raw");//
      props.setProperty("doctype", "omit");
      props.setProperty("tidy-mark", "no");
   }

   public void processNotNativeResult(ByteArrayInputStream byteInputStream) throws TransformerException
   {

      // ByteArrayInputStream byteInputStream =
      // new ByteArrayInputStream(output.toByteArray());
      //
      transformInputStream2Result(byteInputStream, getResult());
      LOG.debug("Transform from temp output to " + getResult().getClass().getName() + " complete");
   }

   @Override
   protected void internalTransform(Source source) throws NotSupportedIOTypeException, TransformerException,
      IllegalStateException
   {
      InputStream input = sourceAsInputStream(source);

      try
      {
         if (LOG.isDebugEnabled())
         {
            LOG.debug(" input available bytes " + input.available());
         }
         if (input.available() == 0)
            return;
      }
      catch (IOException ex)
      {
         LOG.error("Error on read Source", ex);
      }

      // OutputStream output = null;
      tidy.setConfigurationFromProps(props);

      if (getResult() instanceof StreamResult)
      {
         OutputStream output = ((StreamResult)getResult()).getOutputStream();
         LOG.debug("Prepare to write transform result direct to OutputStream");
         tidy.parse(input, output);
         LOG.debug("Tidy parse is complete");
      }
      else
      {
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         LOG.debug("Prepare to write transform result to temp output");
         tidy.parse(input, output);
         LOG.debug("Tidy parse is complete");
         // sex with coding
         String outputString = output.toString();
         try
         {
            outputString =
               outputString.replaceFirst("<\\?xml version=\"1.0\"\\?>", "<?xml version=\"1.0\" encoding=\""
                  + getCurrentIANAEncoding() + "\"?>");
            output.flush();
         }
         catch (IOException ex)
         {
            throw new TransformerException(ex);
         }
         processNotNativeResult(new ByteArrayInputStream(outputString.getBytes()));
      }

   }

   protected String getCurrentIANAEncoding() throws UnsupportedEncodingException
   {
      EncodingMap encodingMap = new EncodingMapImpl();
      String ianaEncoding = encodingMap.convertJava2IANA(PrivilegedSystemHelper.getProperty("file.encoding"));
      if (ianaEncoding == null)
      {
         throw new UnsupportedEncodingException("Can't find corresponding type of encoding for : "
            + System.getProperty("file.encoding"));
      }
      return encodingMap.convertJava2IANA(ianaEncoding);
   }
}
